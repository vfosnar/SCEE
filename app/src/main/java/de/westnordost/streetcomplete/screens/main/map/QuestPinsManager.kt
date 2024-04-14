package de.westnordost.streetcomplete.screens.main.map

import android.content.res.Resources
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.geojson.FeatureCollection
import org.maplibre.android.maps.MapLibreMap
import de.westnordost.streetcomplete.data.download.tiles.TilesRect
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestTypeOrderSource
import de.westnordost.streetcomplete.screens.main.map.components.Pin
import de.westnordost.streetcomplete.screens.main.map.components.PinsMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.toFeature
import de.westnordost.streetcomplete.screens.main.map.maplibre.toBoundingBox
import de.westnordost.streetcomplete.screens.main.map.maplibre.toLatLon
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.style.sources.CustomGeometrySource
import org.maplibre.android.style.sources.CustomGeometrySourceOptions
import org.maplibre.android.style.sources.GeometryTileProvider

/** Manages the layer of quest pins in the map view:
 *  Gets told by the QuestsMapFragment when a new area is in view and independently pulls the quests
 *  for the bbox surrounding the area from database and holds it in memory. */
class QuestPinsManager(
    private val map: MapLibreMap,
    private val pinsMapComponent: PinsMapComponent,
    private val questTypeOrderSource: QuestTypeOrderSource,
    private val questTypeRegistry: QuestTypeRegistry,
    private val resources: Resources,
    private val visibleQuestsSource: VisibleQuestsSource
) : DefaultLifecycleObserver {

    // draw order in which the quest types should be rendered on the map
    private val questTypeOrders: MutableMap<QuestType, Int> = mutableMapOf()
    // last displayed rect of (zoom 16) tiles
    private var lastDisplayedRect: TilesRect? = null
    // quests in current view: key -> [pin, ...]
    private val questsInView: MutableMap<QuestKey, List<Pin>> = mutableMapOf()
    private val questsInViewMutex = Mutex()

    private val viewLifecycleScope: CoroutineScope = CoroutineScope(SupervisorJob())

    // todo
    //  how to organize?
    //   merge manager and component?
    //   edit pins have a separate component?
    //   try a QuestPinsSource instead of manager and component?
    private val pinsSource = CustomGeometrySource(
        id = "pins-source",
        options = CustomGeometrySourceOptions()
            .withMaxZoom(14) // always load quests in z14 tiles to solve issues with quests for long ways disappearing on zoom in
            .withMinZoom(14),
        provider = object : GeometryTileProvider {
            override fun getFeaturesForBounds(bounds: LatLngBounds, zoomLevel: Int): FeatureCollection {
                val bbox = bounds.toBoundingBox()
                val t1 = nowAsEpochMilliseconds()
                val quests = visibleQuestsSource.getAllVisible(bbox)
                val t2 = nowAsEpochMilliseconds()
                val pins = quests.map { createQuestPins(it) }.flatten()
                val t3 = nowAsEpochMilliseconds()
                val features = pins.sortedBy { it.order }.map { it.toFeature() }
                val t4 = nowAsEpochMilliseconds()
                // todo: performance check
                Log.i("test", "get pins for z $zoomLevel, took ${t2-t1}+${t3-t2}+${t4-t3} ms")
                return FeatureCollection.fromFeatures(features)
            }
        }
    ).apply {
        maxOverscaleFactorForParentTiles = 10 // use data at higher zoom levels
        isVolatile = true
    }

    init {
        map.style?.addSource(pinsSource)
    }

    /** Switch visibility of quest pins layer */
    var isVisible: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            if (value) show() else hide()
        }

    private var isStarted: Boolean = false

    private val visibleQuestsListener = object : VisibleQuestsSource.Listener {
        override fun onUpdatedVisibleQuests(added: Collection<Quest>, removed: Collection<QuestKey>) {
            Log.i("test", "update pins -> invalidate all and wait for reload")
            pinsSource.invalidateRegion(LatLngBounds.world())
        }

        override fun onVisibleQuestsInvalidated() {
            invalidate()
        }
    }

    private val questTypeOrderListener = object : QuestTypeOrderSource.Listener {
        override fun onQuestTypeOrderAdded(item: QuestType, toAfter: QuestType) {
            reinitializeQuestTypeOrders()
        }

        override fun onQuestTypeOrdersChanged() {
            reinitializeQuestTypeOrders()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        isStarted = true
        show()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        isStarted = false
        hide()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        viewLifecycleScope.cancel()
    }

    private fun show() {
        if (!isStarted || !isVisible) return
        initializeQuestTypeOrders()
        onNewScreenPosition()
        visibleQuestsSource.addListener(visibleQuestsListener)
        questTypeOrderSource.addListener(questTypeOrderListener)
    }

    private fun hide() {
        viewLifecycleScope.coroutineContext.cancelChildren()
        clear()
        visibleQuestsSource.removeListener(visibleQuestsListener)
        questTypeOrderSource.removeListener(questTypeOrderListener)
    }

    private fun invalidate() {
        clear()
    }

    private fun clear() {
        viewLifecycleScope.launch {
            questsInViewMutex.withLock {
                questsInView.clear()
                lastDisplayedRect = null
                withContext(Dispatchers.Main) {
                    pinsSource.invalidateRegion(LatLngBounds.world()) // todo: does it work?
                }
                Log.i("test", "clear pins source")
            }
        }
    }

    fun getQuestKey(properties: Map<String, String>): QuestKey? =
        properties.toQuestKey()

    fun onNewScreenPosition() {
        return
    }

    private fun initializeQuestTypeOrders() {
        // this needs to be reinitialized when the quest order changes
        val sortedQuestTypes = questTypeRegistry.toMutableList()
        questTypeOrderSource.sort(sortedQuestTypes)
        synchronized(questTypeOrders) {
            questTypeOrders.clear()
            sortedQuestTypes.forEachIndexed { index, questType ->
                questTypeOrders[questType] = index
            }
        }
    }

    private fun createQuestPins(quest: Quest): List<Pin> {
        val iconName = resources.getResourceEntryName(quest.type.icon)
        val props = quest.key.toProperties()
        val order = synchronized(questTypeOrders) { questTypeOrders[quest.type] ?: 0 }
        return quest.markerLocations.map { Pin(it, iconName, props, order) }
    }

    private fun reinitializeQuestTypeOrders() {
        initializeQuestTypeOrders()
        invalidate()
    }

    companion object {
        private const val TILES_ZOOM = 16
    }
}

private const val MARKER_QUEST_GROUP = "quest_group"

private const val MARKER_ELEMENT_TYPE = "element_type"
private const val MARKER_ELEMENT_ID = "element_id"
private const val MARKER_QUEST_TYPE = "quest_type"
private const val MARKER_NOTE_ID = "note_id"

private const val QUEST_GROUP_OSM = "osm"
private const val QUEST_GROUP_OSM_NOTE = "osm_note"

private fun QuestKey.toProperties(): List<Pair<String, String>> = when (this) {
    is OsmNoteQuestKey -> listOf(
        MARKER_QUEST_GROUP to QUEST_GROUP_OSM_NOTE,
        MARKER_NOTE_ID to noteId.toString()
    )
    is OsmQuestKey -> listOf(
        MARKER_QUEST_GROUP to QUEST_GROUP_OSM,
        MARKER_ELEMENT_TYPE to elementType.name,
        MARKER_ELEMENT_ID to elementId.toString(),
        MARKER_QUEST_TYPE to questTypeName
    )
}

private fun Map<String, String>.toQuestKey(): QuestKey? = when (get(MARKER_QUEST_GROUP)) {
    QUEST_GROUP_OSM_NOTE ->
        OsmNoteQuestKey(getValue(MARKER_NOTE_ID).toLong())
    QUEST_GROUP_OSM ->
        OsmQuestKey(
            ElementType.valueOf(getValue(MARKER_ELEMENT_TYPE)),
            getValue(MARKER_ELEMENT_ID).toLong(),
            getValue(MARKER_QUEST_TYPE)
        )
    else -> null
}
