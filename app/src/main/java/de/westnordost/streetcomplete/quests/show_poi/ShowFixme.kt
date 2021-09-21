package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType

class ShowFixme : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways, relations with
        (fixme or FIXME)
        and fixme !~ "continue|continue?|yes|Baum oder Strauch"
        and FIXME !~ continue|continue?|yes
    """
    override val commitMessage = "Remove fixme"
    override val wikiLink = "key:fixme"
    override val icon = R.drawable.ic_quest_create_note
    override val dotColor = "red"

    override fun getTitle(tags: Map<String, String>) = R.string.quest_fixme_title

    override fun createForm() = ShowFixmeAnswerForm()

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val name = tags.entries.mapNotNull {
            when (it.key) {
                "fixme", "FIXME" -> null
                else -> it
            }
        }
        val fixme = tags["fixme"] ?: tags["FIXME"]
        return arrayOf(fixme.toString(),name.toString())
    }

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        if (!answer) {
            changes.deleteIfExists("fixme")
            changes.deleteIfExists("FIXME")
        }
    }
}
