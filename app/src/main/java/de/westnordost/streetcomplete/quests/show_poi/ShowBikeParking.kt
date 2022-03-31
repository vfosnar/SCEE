package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.quests.NoAnswerFragment

class ShowBikeParking : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways, relations with
          amenity ~ bicycle_parking|bicycle_rental|bicycle_repair_station|compressed_air
    """
    override val changesetComment = "this should never appear in a changeset comment"
    override val wikiLink = "nope"
    override val icon = R.drawable.ic_quest_bicycle_parking_cover // replace later, but need own icon...
    override val dotColor = "violet"
    override val defaultDisabledMessage = R.string.default_disabled_msg_poi_bike

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_poi_cycling_title

    override fun createForm() = NoAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
    }
}
