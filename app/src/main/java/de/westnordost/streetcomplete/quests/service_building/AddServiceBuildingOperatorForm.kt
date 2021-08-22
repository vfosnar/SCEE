package de.westnordost.streetcomplete.quests.service_building

import de.westnordost.streetcomplete.quests.ANameWithSuggestionsForm

class AddServiceBuildingOperatorForm : ANameWithSuggestionsForm<String>() {

    // TODO: make proper list like atm operators
    override val suggestions: List<String>? get() = listOf(
        "Wiener Netze", "EVN", "Netz OÖ", "Salzburg AG", "KNG-Kärnten Netz GmbH", "Energie Steiermark", // power austria
        "ÖBB", "GKB", // rail austria
        "e.on", // power cz
        "DPMB", // brno public transport, maybe also for power?
    )

    override fun onClickOk() {
        applyAnswer(name)
    }
}
