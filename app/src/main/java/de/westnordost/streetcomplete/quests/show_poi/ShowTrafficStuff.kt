package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType

class ShowTrafficStuff : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways, relations with
         barrier and barrier !~ wall|fence|retaining_wall|hedge
         or traffic_calming
         or crossing
         or entrance
         or public_transport
         or highway ~ crossing|stop|give_way|elevator
         or amenity ~ taxi|parking|motorcycle_parking
         or type = restriction
         """

    override val commitMessage = "Add raised crossing"
    override val wikiLink = "key:traffic_calming"
    override val icon = R.drawable.ic_quest_railway // replace later, but need own icon...
    override val dotColor = "deepskyblue"
    override val defaultDisabledMessage = R.string.default_disabled_msg_poi_traffic

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_thisIsOther_title

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val name = if ((!tags["crossing"].isNullOrBlank() && !tags["traffic_calming"].isNullOrBlank())
                        || tags["type"] == "restriction"
                        || tags["highway"] == "elevator")
            tags.entries
        else
            featureName.value ?: tags.entries
        return arrayOf(name.toString())
    }

    override fun createForm() = ShowTrafficStuffAnswerForm()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        if (answer)
            changes.add("traffic_calming", "table")
    }
}
