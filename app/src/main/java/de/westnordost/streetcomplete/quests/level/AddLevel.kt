package de.westnordost.streetcomplete.quests.level

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.isKindOfShopExpression
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.ktx.containsAny
import de.westnordost.streetcomplete.util.contains
import de.westnordost.streetcomplete.util.isInMultipolygon
import java.util.concurrent.FutureTask

class AddLevel(
    private val featureDictionaryFuture: FutureTask<FeatureDictionary>,
    private val prefs: SharedPreferences
    ) : OsmElementQuestType<String> {

    /* including any kind of public transport station because even really large bus stations feel
     * like small airport terminals, like Mo Chit 2 in Bangkok*/
    private val mallFilter by lazy { """
        ways, relations with
         shop = mall
         or aeroway = terminal
         or railway = station
         or amenity = bus_station
         or public_transport = station
         or (building and building:levels != 1)
    """.toElementFilterExpression() }

    private val thingsWithLevelFilter by lazy { """
        nodes, ways, relations with level
    """.toElementFilterExpression()}

    /* only nodes because ways/relations are not likely to be floating around freely in a mall
    *  outline */
    private val filter get() = if (prefs.getBoolean(PREF_LEVELS_FOR_EVERYTHING, false))
        everythingFilter
    else
        shopFilter

    private val everythingFilter by lazy { """
        nodes with
         (
           (shop and shop !~ no|vacant|mall)
           or craft
           or amenity
           or leisure
           or office
           or tourism
         )
         and !level
    """.toElementFilterExpression()}

    private val shopFilter by lazy { """
        nodes with
         (${isKindOfShopExpression()})
         and !level and (name or brand)
    """.toElementFilterExpression()}

    override val commitMessage = "Add level to places"
    override val wikiLink = "Key:level"
    override val icon = R.drawable.ic_quest_level
    /* disabled because in a mall with multiple levels, if there are nodes with no level defined,
    *  it really makes no sense to tag something as vacant if the level is not known. Instead, if
    *  the user cannot find the place on any level in the mall, delete the element completely. */
    override val isReplaceShopEnabled = false
    override val isDeleteElementEnabled = true

    override val questTypeAchievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = when {
        !hasProperName(tags)  -> R.string.quest_place_level_no_name_title
        !hasFeatureName(tags) -> R.string.quest_level_title
        else                  -> R.string.quest_place_level_name_type_title
    }

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val name = tags["name"] ?: tags["brand"]
        val hasProperName = name != null
        val hasFeatureName = hasFeatureName(tags)
        return when {
            !hasProperName  -> arrayOf(featureName.value.toString())
            !hasFeatureName -> arrayOf(name!!)
            else            -> arrayOf(name!!, featureName.value.toString())
        }
    }

    private fun hasName(tags: Map<String, String>) = hasProperName(tags) || hasFeatureName(tags)

    private fun hasProperName(tags: Map<String, String>): Boolean =
        tags.keys.containsAny(listOf("name", "brand"))

    private fun hasFeatureName(tags: Map<String, String>): Boolean =
        featureDictionaryFuture.get().byTags(tags).isSuggestion(false).find().isNotEmpty()

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        // get geometry of all malls in the area
        val mallGeometries = mapData
            .filter { mallFilter.matches(it) }
            .mapNotNull { mapData.getGeometry(it.type, it.id) as? ElementPolygonsGeometry }
        if (mallGeometries.isEmpty()) return emptyList()

        // get all shops that have level tagged
        val thingsWithLevel = mapData.filter { thingsWithLevelFilter.matches(it) }
        if (thingsWithLevel.isEmpty()) return emptyList()

        // with this, find malls that contain shops that have different levels tagged
        val multiLevelMallGeometries = mallGeometries.filter { mallGeometry ->
            var level: String? = null
            for (shop in thingsWithLevel) {
                val pos = mapData.getGeometry(shop.type, shop.id)?.center ?: continue
                if (!mallGeometry.getBounds().contains(pos)) continue
                if (!pos.isInMultipolygon(mallGeometry.polygons)) continue

                if (shop.tags.containsKey("level")) {
                    if (level != null) {
                        if (level != shop.tags["level"]) return@filter true
                    } else {
                        level = shop.tags["level"]
                    }
                }
            }
            return@filter false
        }
        if (multiLevelMallGeometries.isEmpty()) return emptyList()

        // now, return all shops that have no level tagged and are inside those multi-level malls
        val shopsWithoutLevel = mapData
            .filter { filter.matches(it) && hasName(it.tags) }
            .toMutableList()
        if (shopsWithoutLevel.isEmpty()) return emptyList()

        val result = mutableListOf<Element>()

        for (mallGeometry in multiLevelMallGeometries) {
            val it = shopsWithoutLevel.iterator()
            while (it.hasNext()) {
                val shop = it.next()
                val pos = mapData.getGeometry(shop.type, shop.id)?.center ?: continue
                if (!mallGeometry.getBounds().contains(pos)) continue
                if (!pos.isInMultipolygon(mallGeometry.polygons)) continue

                result.add(shop)
                it.remove() // shop can only be in one mall
            }
        }
        return result
    }

    override fun isApplicableTo(element: Element): Boolean? {
        if (!filter.matches(element)) return false
        // for shops with no level, we actually need to look at geometry in order to find if it is
        // contained within any multi-level mall
        return null
    }

    override fun createForm() = AddLevelForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("level", answer)
    }

    override val hasQuestSettings = true

    override fun getQuestSettingsDialog(context: Context): AlertDialog? {
        return AlertDialog.Builder(context)
            .setTitle("show quest for")
            .setNegativeButton(android.R.string.cancel, null)
            .setItems(arrayOf("a lot of nodes", "shops & similar (default)")) { _, i ->
                prefs.edit()
                    .putBoolean(PREF_LEVELS_FOR_EVERYTHING, i == 0)
                    .apply()
            }
            .create()
    }
}

private const val PREF_LEVELS_FOR_EVERYTHING = "levelsForEverything"
