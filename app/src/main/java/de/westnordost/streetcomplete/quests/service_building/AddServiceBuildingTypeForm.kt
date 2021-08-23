package de.westnordost.streetcomplete.quests.service_building

import de.westnordost.streetcomplete.quests.AYesNoQuestAnswerFragment

class AddServiceBuildingTypeForm : AYesNoQuestAnswerFragment<Unit>() {
    override fun onClick(answer: Boolean) {
        if (answer) applyAnswer(Unit)
        else skipQuest()
    }
}
