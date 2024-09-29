package de.westnordost.streetcomplete.quests.place_vatin

sealed interface PlaceVatinAnswer

data class PlaceVatin(val vatin: String) : PlaceVatinAnswer
data object NoPlaceVatinSign : PlaceVatinAnswer
