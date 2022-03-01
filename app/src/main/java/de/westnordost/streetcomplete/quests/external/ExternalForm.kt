package de.westnordost.streetcomplete.quests.external

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.databinding.QuestExternalBinding
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem

class ExternalForm(private val externalList: ExternalList) : AbstractQuestAnswerFragment<Boolean>() {

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_external_ok) {
            deleteFromList()
            applyAnswer(true)
        }
    )

    override val contentLayoutResId = R.layout.quest_external
    private val binding by contentViewBinding(QuestExternalBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val element = osmElement ?: return
        val key = ElementKey(element.type, element.id)
        val text = externalList.thatMap[key]
        binding.description.text =
            if (text == null) "entry for this element has been removed from external file since creation of this quest. please remove from list"
            else "message for this element: $text"
    }

    private fun deleteFromList() {
        val element = osmElement ?: return
        val key = ElementKey(element.type, element.id)
        externalList.remove(key)
    }
}
