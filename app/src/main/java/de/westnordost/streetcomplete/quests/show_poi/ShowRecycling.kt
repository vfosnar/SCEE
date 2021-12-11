package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.quests.NoAnswerFragment

class ShowRecycling : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways, relations with
          amenity ~ recycling|waste_basket|waste_disposal|waste_transfer_station|sanitary_dump_station
    """
    override val commitMessage = "Add excrement bag dispenser"
    override val wikiLink = "nope"
    override val icon = R.drawable.ic_quest_recycling
    override val dotColor = "green"
    override val defaultDisabledMessage = R.string.default_disabled_msg_poi_recycling

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
