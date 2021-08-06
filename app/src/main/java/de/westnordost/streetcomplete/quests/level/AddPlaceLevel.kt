package de.westnordost.streetcomplete.quests.level

import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.*
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.ktx.containsAny
import de.westnordost.streetcomplete.util.isInMultipolygon
import java.util.concurrent.FutureTask

class AddPlaceLevel (
    private val featureDictionaryFuture: FutureTask<FeatureDictionary>
    ) : OsmElementQuestType<Int> {

    override val commitMessage = "Add level"
    override val wikiLink = "Key:level"
    override val icon = R.drawable.ic_quest_steps_count

    override fun getTitle(tags: Map<String, String>) = when {
            !hasProperName(tags)  -> R.string.quest_place_level_no_name_title
            !hasFeatureName(tags) -> R.string.quest_place_level_name_title
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

    private val nameTags = listOf("name", "brand")

    private fun hasProperName(tags: Map<String, String>): Boolean =
        tags.keys.containsAny(nameTags)

    private fun hasFeatureName(tags: Map<String, String>): Boolean =
        featureDictionaryFuture.get().byTags(tags).isSuggestion(false).find().isNotEmpty()

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        mapData.boundingBox ?: return listOf()

        val placeNodes = mapData.nodes.filter { placesWithoutLevel.matches(it) && hasName(it.tags) }

        /** filter: only places within building=retail or shop=mall with building:levels != 1 */

        val buildings = mapData.filter {
            mallFilter.matches(it)
        }

        if (buildings.isEmpty() || placeNodes.isEmpty()) return listOf()

        val buildingGeometriesById = buildings.associate {
            it.id to mapData.getGeometry(it.type, it.id) as? ElementPolygonsGeometry
        }

        /** use only places contained within a pre-selected area */

        val placeNodes2 = mutableListOf<Node>()

        buildings.forEach { building ->
            val buildingGeometry = buildingGeometriesById[building.id] ?: return@forEach
            val maybePlaceNodes = mutableListOf<Node>()
            placeNodes.forEach { place ->
                if (place.position.isInMultipolygon(buildingGeometry.polygons))
                    maybePlaceNodes.add(place)
            }
            // assume if more than 5 places in one building, it's a shopping mall
            if (maybePlaceNodes.size > 5)
                placeNodes2 += maybePlaceNodes
        }

        return placeNodes2
    }

    override fun isApplicableTo(element: Element): Boolean? =
        if (!placesWithoutLevel.matches(element) || !hasName(element.tags)) false else null

    override fun createForm() = AddPlaceLevelForm()

    override fun applyAnswerTo(answer: Int, changes: StringMapChangesBuilder) {
        changes.add("level", answer.toString())
    }
}

private val mallFilter by lazy { """
    ways, relations with
      (building = retail or shop = mall)
      and building:levels != 1
    """.toElementFilterExpression()}

private val placesWithoutLevel by lazy { """
    nodes with
      (shop
       or craft
       or amenity
      )
      and !level and !level:ref
    """.toElementFilterExpression()}
