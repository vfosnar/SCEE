package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.quests.NoAnswerFragment

class ShowBikeParking : OsmFilterQuestType<Boolean>() {
    override val elementFilter = "nodes, ways with amenity = bicycle_parking"
    override val commitMessage = "I hope this does not get committed"
    override val wikiLink = "nope"
    override val icon = R.drawable.ic_quest_bicycle_parking_cover // replace later, but need own icon...
    override val dotColor = "violet"

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_thisIsBikeParking_title

    override fun createForm() = NoAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
    }
}
