package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.quests.NoAnswerFragment

class ShowMachine : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways with
          amenity ~ vending_machine|atm|telephone|charging_station|device_charging_station
          or atm = yes and (amenity or shop)
    """
    override val changesetComment = "this should never appear in a changeset comment"
    override val wikiLink = "nope"
    override val icon = R.drawable.ic_quest_cash
    override val dotColor = "blue"
    override val defaultDisabledMessage = R.string.default_disabled_msg_poi_machine

    override fun getTitle(tags: Map<String, String>) =
        if (tags["amenity"].equals("atm") || !tags["atm"].isNullOrEmpty())
            R.string.quest_thisIsAtm_title
        else if (tags["amenity"].equals("vending_machine"))
            R.string.quest_thisIsVendingMachine_title
        else
            R.string.quest_thisIsOther_title

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        return if (tags["amenity"].equals("vending_machine"))
            arrayOf(tags["vending"] ?: tags.entries.toString())
        else
            arrayOf((featureName.value ?: tags.entries).toString())
    }

    override fun createForm() = NoAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
    }
}
