package de.westnordost.streetcomplete.quests.service_building

import de.westnordost.streetcomplete.quests.ANameWithSuggestionsForm

class AddServiceBuildingOperatorForm : ANameWithSuggestionsForm<String>() {

    // TODO: make proper list like atm operators
    override val suggestions: List<String>? get() = POWER + TRANSPORT

    override fun onClickOk() {
        applyAnswer(name)
    }
}

// separate power and others for the current implementation of service building type quest
// actually public transport could also have power, but this would probably not be a minor distribution substation
val POWER = listOf(
    "Wiener Netze", "Wien Energie", "Wienstrom", "EVN", "Netz Niederösterreich GmbH", "Netz OÖ", "Salzburg AG", "KNG-Kärnten Netz GmbH", "Energie Steiermark" // austria
    "e.on", // cz
)

val TRANSPORT = listOf(
    "ÖBB", "GKB", "Wiener Linien", // austria
    "DPMB", // cz
)
