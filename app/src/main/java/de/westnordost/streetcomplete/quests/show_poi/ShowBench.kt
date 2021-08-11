package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.quests.NoAnswerFragment

class ShowBench : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways, relations with amenity = bench
        or leisure = picnic_table
        or amenity = lounger
        or leisure = bleachers
        or amenity = picnic_site
        or amenity = table
    """
    override val commitMessage = "I hope this does not get committed"
    override val wikiLink = "nope"
    override val icon = R.drawable.ic_quest_bench_poi // replace later, but need own icon...
    override val dotColor = "chocolate"

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_thisIsOther_title

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val name = featureName.value ?: tags.entries
        return arrayOf(name.toString())
    }

    override fun createForm() = NoAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
    }
}
