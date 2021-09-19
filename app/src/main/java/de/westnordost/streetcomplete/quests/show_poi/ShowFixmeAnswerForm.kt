package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.NoAAnswerFragment

class ShowFixmeAnswerForm : NoAAnswerFragment<Boolean>() {
		override val otherAnswers = listOf(
		AnswerItem(R.string.quest_fixme_remove) { applyAnswer(false) }
		)

    override fun onClick(answer: Boolean) {
    }
}
