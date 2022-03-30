package de.westnordost.streetcomplete.quests.step_count

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.util.measuredLength

class AddStepCount : OsmElementQuestType<Int> {

    val elementFilter by lazy { """
        ways with highway = steps
         and (!indoor or indoor = no)
         and access !~ private|no
         and (!conveying or conveying = no)
         and !step_count
    """.toElementFilterExpression() }
    override val changesetComment = "Add step count"
    override val wikiLink = "Key:step_count"
    override val icon = R.drawable.ic_quest_steps_count
    // because the user needs to start counting at the start of the steps
    override val hasMarkersAtEnds = true
    override val isSplitWayEnabled = true
    override val questTypeAchievements = listOf(PEDESTRIAN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_step_count_title

    override fun isApplicableTo(element: Element): Boolean? {
        if (!elementFilter.matches(element)) return false
        return null
    }

    // remove steps with length over 15 meters (they will have too many steps)
    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        return mapData.filter { elementFilter.matches(it) }
            .filter { element ->
                val geometry = mapData.getWayGeometry(element.id) as? ElementPolylinesGeometry
                val totalLength = geometry?.polylines?.sumOf { it.measuredLength() } ?: return@filter true
                if (totalLength > MAX_STEP_LENGTH)
                    return@filter false
                true
            }
    }

    override fun createForm() = AddStepCountForm()

    override fun applyAnswerTo(answer: Int, tags: Tags, timestampEdited: Long) {
        tags["step_count"] = answer.toString()
    }
}

private const val MAX_STEP_LENGTH = 15.0
