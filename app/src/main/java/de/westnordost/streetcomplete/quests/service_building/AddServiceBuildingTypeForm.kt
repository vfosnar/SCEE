package de.westnordost.streetcomplete.quests.service_building

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem

class AddServiceBuildingTypeForm : AbstractQuestAnswerFragment<String>() {

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_service_building_type_pressure) { "gas" },
        AnswerItem(R.string.quest_service_building_type_substation) { applyAnswer("substation") }
    )
}
