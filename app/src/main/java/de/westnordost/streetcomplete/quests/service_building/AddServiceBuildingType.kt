package de.westnordost.streetcomplete.quests.service_building

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType

class AddServiceBuildingType : OsmFilterQuestType<String>() {

    override val elementFilter = """nodes, ways, relations with building = service
         and !power and !service and !man_made"""
    override val commitMessage = "Add service building type"
    override val wikiLink = "Tag:building=service"
    override val icon = R.drawable.ic_quest_power

    override fun getTitle(tags: Map<String, String>) = R.string.quest_service_building_type_title

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val title = tags["operator"]?.let { " ($it)" } ?: ""
        return arrayOf(title)
    }

    override fun createForm() = AddServiceBuildingTypeForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        when (answer) {
            "substation" -> {
                changes.add("power", "substation")
                changes.add("substation", "minor_distribution")
            }
            "gas" -> {
                changes.add("pipeline", "substation")
                changes.add("substation", "distribution")
                changes.add("substance", "gas")
            }
            "well", "reservoir_covered", "pumping_station" -> {
                changes.add("man_made", answer)
            }
        }
    }

}
