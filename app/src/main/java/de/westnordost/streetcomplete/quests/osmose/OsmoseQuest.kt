package de.westnordost.streetcomplete.quests.osmose

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.quests.singleTypeElementSelectionDialog

class OsmoseQuest(private val db: OsmoseDao, private val prefs: SharedPreferences) : OsmElementQuestType<String> {

    override fun getTitle(tags: Map<String, String>) = R.string.quest_osmose_title

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> =
        arrayOf(tags.toString())

    override val changesetComment = "should not appear in any changeset"
    override val wikiLink = "Osmose"
    override val icon = R.drawable.ic_quest_osmose
    override val defaultDisabledMessage = R.string.quest_osmose_message

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        // not working
        val elements = mutableListOf<Element>()
        // maybe better get a map with all items from db, that's a lot faster...
        val map = db.getAll()
        val hiddenItemTypes = prefs.getString(PREF_OSMOSE_ITEMS, "")!!.split(',')
        mapData.forEach {
            val problem = map[ElementKey(it.type, it.id)] ?: return@forEach
            if (!hiddenItemTypes.contains(problem.uuid))
                elements.add(it)
        }
        return elements
    }

    override fun isApplicableTo(element: Element): Boolean {
        val problem = db.get(ElementKey(element.type, element.id)) ?: return false
        return !prefs.getString(PREF_OSMOSE_ITEMS, "")!!.split(',').contains(problem.uuid)
    }

    override fun createForm() = OsmoseForm(db)

    override fun applyAnswerTo(answer: String, tags: Tags, timestampEdited: Long) {
        if (answer.isNotBlank())
            db.setAsFalsePositive(answer)
    }

    override val hasQuestSettings = true

    override fun getQuestSettingsDialog(context: Context): AlertDialog =
        singleTypeElementSelectionDialog(context, prefs, PREF_OSMOSE_ITEMS, "", "set osmose item types to hide, comma separated")

}

private const val PREF_OSMOSE_ITEMS = "quest_osmose_items"
