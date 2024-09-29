package de.westnordost.streetcomplete.quests.place_vatin

sealed interface PlaceVatinAnswer

data class PlaceVatin(val ico: String) : PlaceVatinAnswer
data object NoPlaceVatinSign : PlaceVatinAnswer
