package de.westnordost.streetcomplete.quests.building_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType

class AddBuildingWithAddressType : OsmElementQuestType<BuildingType> {

    override fun isApplicableTo(element: Element): Boolean? =
        if (!buildingFilter.matches(element)) false else null

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val buildings = mapData.filter { buildingFilter.matches(it) }.toMutableList()
        val buildingsWithoutAddress = getBuildingsWithoutAddress(buildings, mapData)
        buildings.removeAll(buildingsWithoutAddress)
        return buildings
    }

    override val commitMessage = "Add building types"
    override val wikiLink = "Key:building"
    override val icon = R.drawable.ic_quest_building_address
    override val dotColor = "powderblue"

    override fun getTitle(tags: Map<String, String>) = R.string.quest_buildingType_title

    override fun createForm() = AddBuildingTypeForm()

    override fun applyAnswerTo(answer: BuildingType, changes: StringMapChangesBuilder) {
        applyBuildingAnswer(answer, changes)
    }
}
