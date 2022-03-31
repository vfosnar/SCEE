package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.quests.NoAnswerFragment

class ShowBench : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways, relations with
        amenity ~ bench|lounger|table
        or leisure ~ picnic_table|bleachers
        or tourism = picnic_site
    """
    override val changesetComment = "this should never appear in a changeset comment"
    override val wikiLink = "nope"
    override val icon = R.drawable.ic_quest_bench_poi // replace later, but need own icon...
    override val dotColor = "chocolate"
    override val defaultDisabledMessage = R.string.default_disabled_msg_poi_bench

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_poi_seating_title

    override fun createForm() = NoAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
    }
}
