package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.quests.NoAnswerFragment

class ShowFixme : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways, relations with
        fixme
        or FIXME
    """
    override val commitMessage = "I hope this does not get committed"
    override val wikiLink = "nope"
    override val icon = R.drawable.ic_quest_create_note // replace later, but need own icon...
    override val dotColor = "red"

    override fun getTitle(tags: Map<String, String>) = R.string.quest_fixme_title

    override fun createForm() = NoAnswerFragment()

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val name = featureName.value ?: tags.entries
        val fixme = tags["fixme"] ?: tags["FIXME"]
        return arrayOf(fixme.toString(),name.toString())
    }

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
    }
}
