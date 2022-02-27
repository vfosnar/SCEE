package de.westnordost.streetcomplete.quests.external

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags

class ExternalCheck(private val externalList: ExternalList) : OsmElementQuestType<Boolean> {

    override val changesetComment = "this should never be uploaded"
    override val wikiLink = "nope"
    override val icon = R.drawable.ic_quest_external

    val thatMap = externalList.thatMap

    override fun isApplicableTo(element: Element): Boolean =
        thatMap.containsKey(ElementKey(element.type, element.id))

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val list = mutableListOf<Element>()
        mapData.forEach {
            if (thatMap.contains(ElementKey(it.type, it.id)))
                list.add(it)
        }
        return list
    }

    override fun getTitle(tags: Map<String, String>): Int = R.string.quest_external_title

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> =
        arrayOf(tags.toString())

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {
    }

    override fun createForm() = ExternalForm(externalList)
}
