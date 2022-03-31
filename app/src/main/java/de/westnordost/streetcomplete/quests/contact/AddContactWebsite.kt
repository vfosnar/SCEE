package de.westnordost.streetcomplete.quests.contact

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags

class AddContactWebsite : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways, relations with
        (
         tourism = information and information = office
         or """.trimIndent() +
         PLACES_FOR_CONTACT_QUESTS +
        "\n) and !website and !contact:website and !contact:facebook and !contact:instagram and !brand and name"

    override val changesetComment = "Add website"
    override val wikiLink = "Key:website"
    override val icon = R.drawable.ic_quest_wifi

    override fun getTitle(tags: Map<String, String>) = R.string.quest_contact_website

    override fun createForm() = AddContactWebsiteForm()

    override fun applyAnswerTo(answer: String, tags: Tags, timestampEdited: Long) {
        tags["website"] = answer
    }

}
