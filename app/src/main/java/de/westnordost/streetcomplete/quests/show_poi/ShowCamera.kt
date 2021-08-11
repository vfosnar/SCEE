package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.quests.NoAnswerFragment

class ShowCamera : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways, relations with man_made = surveillance
    """
    override val commitMessage = "I hope this does not get committed"
    override val wikiLink = "nope"
    override val icon = R.drawable.ic_quest_blind_traffic_lights
    override val dotColor = "mediumvioletred"

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_thisIsOther_title

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val name = tags.entries
        return arrayOf(name.toString())
    }

    override fun createForm() = NoAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
    }
}
