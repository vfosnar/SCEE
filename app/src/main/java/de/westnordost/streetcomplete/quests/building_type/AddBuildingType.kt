package de.westnordost.streetcomplete.quests.building_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolygonsGeometry
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.BUILDING
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.quests.address.containsAnyNode
import de.westnordost.streetcomplete.util.LatLonRaster
import de.westnordost.streetcomplete.util.isInMultipolygon

class AddBuildingType : OsmElementQuestType<BuildingType> {

    override fun isApplicableTo(element: Element): Boolean? =
        if (!buildingFilter.matches(element)) false else null

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val buildings = mapData.filter { buildingFilter.matches(it) }

        return getBuildingsWithoutAddress(buildings, mapData)
    }

    override val commitMessage = "Add building types"
    override val wikiLink = "Key:building"
    override val icon = R.drawable.ic_quest_building

    override val questTypeAchievements = listOf(BUILDING)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_buildingType_title

    override fun createForm() = AddBuildingTypeForm()

    override fun applyAnswerTo(answer: BuildingType, changes: StringMapChangesBuilder) {
        applyBuildingAnswer(answer, changes)
    }
}

    // in the case of man_made, historic, military, aeroway and power, these tags already contain
    // information about the purpose of the building, so no need to force asking it
    // or question would be confusing as there is no matching reply in available answers
    // same goes (more or less) for tourism, amenity, leisure. See #1854, #1891, #3233
val buildingFilter = """
        ways, relations with (building = yes or building = unclassified)
         and !man_made
         and !historic
         and !military
         and !power
         and !tourism
         and !attraction
         and !amenity
         and !leisure
         and !aeroway
         and !description
         and location != underground
         and abandoned != yes
         and abandoned != building
         and abandoned:building != yes
         and ruins != yes and ruined != yes
    """.toElementFilterExpression()

private val nodesWithAddressFilter by lazy { """
   nodes with ~"addr:(housenumber|housename|conscriptionnumber|streetnumber|street)"
    """.toElementFilterExpression()}

private val addressFilter by lazy { """
   ways, relations with ~"addr:(housenumber|housename|conscriptionnumber|streetnumber|street)"
    """.toElementFilterExpression()}

fun getBuildingsWithoutAddress(buildings: List<Element>, mapData: MapDataWithGeometry): List<Element> {
    val bbox = mapData.boundingBox ?: return listOf()
    val addressNodesById = mapData.nodes.filter { nodesWithAddressFilter.matches(it) }.associateBy { it.id }
    val addressNodeIds = addressNodesById.keys

    val buildingsWithoutAddress = buildings.filter {
        !it.containsAnyNode(addressNodeIds, mapData) && !addressFilter.matches((it))
    }.toMutableList()

    /** exclude buildings that contain an address node somewhere within their area */

    val addressPositions = LatLonRaster(bbox, 0.0005)
    for (node in addressNodesById.values) {
        addressPositions.insert(node.position)
    }

    val buildingGeometriesById = buildingsWithoutAddress.associate {
        it.id to mapData.getGeometry(it.type, it.id) as? ElementPolygonsGeometry
    }

    buildingsWithoutAddress.removeAll { building ->
        val buildingGeometry = buildingGeometriesById[building.id]
        if (buildingGeometry != null) {
            val nearbyAddresses = addressPositions.getAll(buildingGeometry.getBounds())
            nearbyAddresses.any { it.isInMultipolygon(buildingGeometry.polygons) }
        } else true
    }

    return buildingsWithoutAddress

}

fun applyBuildingAnswer(answer: BuildingType, changes: StringMapChangesBuilder) {
    if (answer.osmKey == "man_made") {
        changes.delete("building")
        changes.add("man_made", answer.osmValue)
    } else if (answer.osmKey == "demolished:building") {
        changes.delete("building")
        changes.addOrModify(answer.osmKey, answer.osmValue)
    } else if (answer.osmValue == "transformer_tower") {
        changes.modify("building", answer.osmValue)
        changes.addOrModify("power", "substation")
        changes.addOrModify("substation", "minor_distribution")
    } else if (answer.osmKey != "building") {
        changes.addOrModify(answer.osmKey, answer.osmValue)
            if(answer == BuildingType.ABANDONED) {
                changes.deleteIfExists("disused")
            }
            if(answer == BuildingType.RUINS && changes.getPreviousValue("disused") == "no") {
                changes.deleteIfExists("disused")
            }
            if(answer == BuildingType.RUINS && changes.getPreviousValue("abandoned") == "no") {
                changes.deleteIfExists("abandoned")
            }
    } else {
        changes.modify("building", answer.osmValue)
    }

}
