package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.quests.NoAnswerFragment

class ShowRecycling : OsmFilterQuestType<Boolean>() {
    override val elementFilter = "nodes, ways with amenity = recycling or amenity = waste_basket"
    override val commitMessage = "I hope this does not get committed"
    override val wikiLink = "nope"
    override val icon = R.drawable.ic_quest_recycling
    override val dotColor = "green"

    override fun getTitle(tags: Map<String, String>) =
        if (tags["amenity"].equals("recycling"))
            R.string.quest_thisIsRecycling_title
        else
            R.string.quest_thisIsWasteBasket_title

    override fun createForm() = NoAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
    }
}
