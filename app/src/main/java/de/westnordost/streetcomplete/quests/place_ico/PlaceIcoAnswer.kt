package de.westnordost.streetcomplete.quests.place_ico

sealed interface PlaceIcoAnswer

data class PlaceIco(val ico: String) : PlaceIcoAnswer
data object NoPlaceIcoSign : PlaceIcoAnswer
