package de.westnordost.streetcomplete.quests.osmose

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.databinding.QuestExternalBinding
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem

class OsmoseForm(private val db: OsmoseDao) : AbstractQuestAnswerFragment<String>() {

    var issue: OsmoseIssue? = null

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_osmose_false_positive) {
            applyAnswer(issue?.uuid ?: "")
        }
    )

    override val contentLayoutResId = R.layout.quest_external
    private val binding by contentViewBinding(QuestExternalBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val element = osmElement ?: return
        issue = db.get(ElementKey(element.type, element.id))
        val text = issue?.let {
            if (it.subtitle.isBlank())
                it.title
            else
                it.title + ": \n" + it.subtitle
        }
        binding.description.text =
            if (text == null) "entry for this element has been removed from database since creation of this quest"
            else "message for this element (item type ${issue?.item}): $text"
    }

}
