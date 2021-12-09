package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.quests.NoAnswerFragment

class ShowRecycling : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways, relations with
        amenity = recycling
        or amenity = waste_basket
        or amenity = waste_disposal
        or amenity = waste_transfer_station
        or amenity = sanitary_dump_station
    """
    override val commitMessage = "I hope this does not get committed"
    override val wikiLink = "nope"
    override val icon = R.drawable.ic_quest_recycling
    override val dotColor = "green"

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_thisIsOther_title

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val name = if (!tags["recycling_type"].isNullOrBlank())
            tags.entries
        else
            featureName.value ?: tags.entries
        return arrayOf(name.toString())
    }

    override fun createForm() = ShowRecyclingAnswerForm()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        if (answer) {
            changes.modify("amenity", "vending_machine")
            changes.add("vending", "excrement_bags")
            changes.add("bin", "yes")
        }
    }
}
