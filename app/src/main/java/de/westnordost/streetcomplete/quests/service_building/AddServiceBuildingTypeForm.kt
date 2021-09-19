package de.westnordost.streetcomplete.quests.service_building

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem

class AddServiceBuildingTypeForm : AbstractQuestAnswerFragment<Boolean>() {

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(true) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { skipQuest() }
    )
}
