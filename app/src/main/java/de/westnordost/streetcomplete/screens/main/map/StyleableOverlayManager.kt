package de.westnordost.streetcomplete.screens.main.map

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.geojson.FeatureCollection
import org.maplibre.android.maps.MapLibreMap
import de.westnordost.streetcomplete.data.download.tiles.TilesRect
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.key
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.screens.main.map.components.StyleableOverlayMapComponent
import de.westnordost.streetcomplete.screens.main.map.components.StyledElement
import de.westnordost.streetcomplete.screens.main.map.components.toFeatures
import de.westnordost.streetcomplete.screens.main.map.maplibre.screenAreaToBoundingBox
import de.westnordost.streetcomplete.screens.main.map.maplibre.toBoundingBox
import de.westnordost.streetcomplete.screens.main.map.maplibre.toLatLngBounds
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.logs.Log
import de.westnordost.streetcomplete.util.math.intersect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.style.sources.CustomGeometrySource
import org.maplibre.android.style.sources.CustomGeometrySourceOptions
import org.maplibre.android.style.sources.GeometryTileProvider
import kotlin.coroutines.coroutineContext

/** Manages the layer of styled map data in the map view:
 *  Gets told by the MainMapFragment when a new area is in view and independently pulls the map
 *  data for the bbox surrounding the area from database and holds it in memory. */
class StyleableOverlayManager(
    private val map: MapLibreMap,
    private val mapComponent: StyleableOverlayMapComponent,
    private val mapDataSource: MapDataWithEditsSource,
    private val selectedOverlaySource: SelectedOverlaySource
) : DefaultLifecycleObserver {

    // last displayed rect of (zoom 16) tiles
    private var lastDisplayedRect: TilesRect? = null
    // map data in current view: key -> [pin, ...]
    private val mapDataInView: MutableMap<ElementKey, StyledElement> = mutableMapOf()
    private val mapDataInViewMutex = Mutex()

    private val viewLifecycleScope: CoroutineScope = CoroutineScope(SupervisorJob())

    private var updateJob: Job? = null

    private var overlay: Overlay? = null
        set(value) {
            if (field == value) return
            val wasNull = field == null
            val isNullNow = value == null
            field = value
            when {
                isNullNow -> hide()
                wasNull ->   show()
                else ->      switchOverlay()
            }
        }

    private val overlayListener = object : SelectedOverlaySource.Listener {
        override fun onSelectedOverlayChanged() {
            overlay = selectedOverlaySource.selectedOverlay
        }
    }

    private val overlaySource = CustomGeometrySource(
        id = "overlay-source",
        options = CustomGeometrySourceOptions()
            .withMaxZoom(TILES_ZOOM)
            .withMinZoom(TILES_ZOOM),
        provider = object : GeometryTileProvider {
            override fun getFeaturesForBounds(bounds: LatLngBounds, zoomLevel: Int): FeatureCollection {
                val overlay = overlay ?: return FeatureCollection.fromFeatures(emptyList()) // todo: no need to create a new collection
                val bbox = bounds.toBoundingBox()
                val t1 = nowAsEpochMilliseconds()
                val mapData = synchronized(mapDataSource) { mapDataSource.getMapDataWithGeometry(bbox) }
                val t2 = nowAsEpochMilliseconds()
                val styledElements = createStyledElementsByKey(overlay, mapData)
                val t3 = nowAsEpochMilliseconds()
                // todo: not really great, especially it doesn't use the cache and creates features over and over again
                val features = styledElements.flatMap { it.second.toFeatures() }.toList()
                val t4 = nowAsEpochMilliseconds()
                Log.i("test", "get overlay elements took ${t2-t1}+${t3-t2}+${t4-t3} ms")
                return FeatureCollection.fromFeatures(features)
            }
        }
    ).apply {
        maxOverscaleFactorForParentTiles = 10
        isVolatile = true
    }

    init {
        map.style?.addSource(overlaySource)
    }

    private val mapDataListener = object : MapDataWithEditsSource.Listener {
        override fun onUpdated(updated: MapDataWithGeometry, deleted: Collection<ElementKey>) {
            Log.i("test", "updated map data -> invalidate all and wait for reload")
            overlaySource.invalidateRegion(LatLngBounds.world())
        }

        override fun onReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry) {
            Log.i("test", "replaced map data in bbox -> invalidate bbox and wait for reload")
            overlaySource.invalidateRegion(bbox.toLatLngBounds())
        }

        override fun onCleared() {
            clear()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        overlay = selectedOverlaySource.selectedOverlay
        selectedOverlaySource.addListener(overlayListener)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        overlay = null
        selectedOverlaySource.removeListener(overlayListener)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        viewLifecycleScope.cancel()
    }

    private fun show() {
        clear()
        onNewScreenPosition()
        mapDataSource.addListener(mapDataListener)
    }

    private fun switchOverlay() {
        clear()
        onNewScreenPosition()
    }

    private fun hide() {
        viewLifecycleScope.coroutineContext.cancelChildren()
        clear()
        mapDataSource.removeListener(mapDataListener)
    }

    fun onNewScreenPosition() {}

    private fun clear() {
        viewLifecycleScope.launch {
            mapDataInViewMutex.withLock {
                mapDataInView.clear()
                lastDisplayedRect = null
                withContext(Dispatchers.Main) { overlaySource.invalidateRegion(LatLngBounds.world()) }
                Log.i("test", "clear overlay source")
            }
        }
    }

    private suspend fun setStyledElements(mapData: MapDataWithGeometry) {
        val overlay = overlay ?: return
        mapDataInViewMutex.withLock {
            mapDataInView.clear()
            createStyledElementsByKey(overlay, mapData).forEach { (key, styledElement) ->
                mapDataInView[key] = styledElement
            }
            if (coroutineContext.isActive) {
                withContext(Dispatchers.Main) { mapComponent.set(mapDataInView.values) }
            }
        }
    }

    private suspend fun updateStyledElements(updated: MapDataWithGeometry, deleted: Collection<ElementKey>) {
        val overlay = overlay ?: return
        val displayedBBox = lastDisplayedRect?.asBoundingBox(TILES_ZOOM)
        var changedAnything = false
        mapDataInViewMutex.withLock {
            deleted.forEach {
                if (mapDataInView.remove(it) != null) {
                    changedAnything = true
                }
            }
            val styledElementsByKey = createStyledElementsByKey(overlay, updated).toMap()
            // for elements that used to be displayed in the overlay but now not anymore
            updated.forEach {
                if (!styledElementsByKey.containsKey(it.key)) {
                    mapDataInView.remove(it.key)
                    changedAnything = true
                }
            }
            styledElementsByKey.forEach { (key, styledElement) ->
                mapDataInView[key] = styledElement
                if (!changedAnything && displayedBBox?.intersect(styledElement.geometry.getBounds()) != false) {
                    changedAnything = true
                }
            }

            if (changedAnything && coroutineContext.isActive) {
                withContext(Dispatchers.Main) { mapComponent.set(mapDataInView.values) }
            }
        }
    }

    private fun createStyledElementsByKey(
        overlay: Overlay,
        mapData: MapDataWithGeometry
    ): Sequence<Pair<ElementKey, StyledElement>> =
        overlay.getStyledElements(mapData).mapNotNull { (element, style) ->
            val key = element.key
            val geometry = mapData.getGeometry(element.type, element.id) ?: return@mapNotNull null
            key to StyledElement(element, geometry, style)
        }

    companion object {
        private const val TILES_ZOOM = 16
    }
}
