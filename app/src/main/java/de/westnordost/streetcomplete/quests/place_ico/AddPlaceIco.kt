package de.westnordost.streetcomplete.quests.place_ico

import android.content.Context
import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.quests.fullElementSelectionDialog
import de.westnordost.streetcomplete.quests.questPrefix

class AddPlaceIco(
    private val getFeature: (Element) -> Feature?
) : OsmElementQuestType<PlaceIcoAnswer> {

    private val filter by lazy { ("""
        nodes, ways with
        (
          shop and shop !~ no|vacant
          or office and office !~ no|vacant
          or craft
          or amenity = recycling and recycling_type = centre
          or tourism = information and information = office
          or """ +

        // The common list is shared by the name quest, the opening hours quest and the wheelchair quest.
        // So when adding other tags to the common list keep in mind that they need to be appropriate for all those quests.
        // Independent tags can by added in the "name only" tab.

        prefs.getString(questPrefix(prefs) + PREF_ELEMENTS, NAME_PLACES)+ "\n" + """
        )
        and !brand and !ref:ico and ref:ico:signed != no
    """).toElementFilterExpression() }

    override val changesetComment = "Determine place IČO"
    override val wikiLink = "Key:ref:ico"
    override val icon = R.drawable.ic_quest_general_ref
    override val isReplacePlaceEnabled = true
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_placeIco_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element): Boolean =
        filter.matches(element) && getFeature(element) != null

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().asSequence().filter { it.isPlaceOrDisusedPlace() }

    override fun createForm() = AddPlaceIcoForm()

    override fun applyAnswerTo(answer: PlaceIcoAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is NoPlaceIcoSign -> {
                tags["ref:ico:signed"] = "no"
            }
            is PlaceIco -> {
                tags["ref:ico"] = answer.ico
            }
        }
    }

    override val hasQuestSettings = true

    override fun getQuestSettingsDialog(context: Context) =
        fullElementSelectionDialog(context, prefs, questPrefix(prefs) + PREF_ELEMENTS, R.string.quest_settings_element_selection, NAME_PLACES)
}

private val NAME_PLACES = mapOf(
    "amenity" to arrayOf(
        // common
        "restaurant", "cafe", "ice_cream", "fast_food", "bar", "pub", "biergarten",         // eat & drink
        "food_court", "nightclub",
        "cinema", "planetarium", "casino",                                                  // amenities
        "townhall", "courthouse", "embassy", "community_centre", "youth_centre", "library", // civic
                "driving_school", "music_school", "prep_school", "language_school", "dive_centre",  // learning
                "dancing_school", "ski_school", "flight_school", "surf_school", "sailing_school",
                "cooking_school",
        "bank", "bureau_de_change", "money_transfer", "post_office", "marketplace",         // commercial
        "internet_cafe", "payment_centre",
        "car_wash", "car_rental", "fuel",                                                   // car stuff
        "dentist", "doctors", "clinic", "pharmacy", "veterinary",                           // health
        "animal_boarding", "animal_shelter", "animal_breeding",                             // animals
        "coworking_space",                                                                  // work

        // name & opening hours
        "boat_rental",

        // name & wheelchair
        "theatre",                                        // culture
        "conference_centre", "arts_centre",               // events
        "police", "ranger_station",                       // civic
        "ferry_terminal",                                 // transport
        "place_of_worship",                               // religious
        "hospital",                                       // health care
        "brothel", "gambling", "love_hotel", "stripclub", // bad stuff

        // name only
        "studio",                                                                // culture
        "events_venue", "exhibition_centre", "music_venue",                      // events
        "prison", "fire_station",                                                // civic
        "social_facility", "nursing_home", "childcare", "retirement_home", "social_centre", // social
        "monastery",                                                             // religious
        "kindergarten", "school", "college", "university", "research_institute", // education
    ),
    "tourism" to arrayOf(
        // common
        "zoo", "aquarium", "theme_park", "gallery", "museum",

        // name & wheelchair
        "attraction",
        "hotel", "guest_house", "motel", "hostel", "alpine_hut", "apartment", "resort", "camp_site", "caravan_site", "chalet" // accommodations

        // and tourism = information, see above
    ),
    "leisure" to arrayOf(
        // common
        "fitness_centre", "golf_course", "water_park", "miniature_golf", "bowling_alley",
        "amusement_arcade", "adult_gaming_centre", "tanning_salon",

        // name & wheelchair
        "sports_centre", "stadium",

        // name only
        "dance", "nature_reserve", "marina", "horse_riding",
    ),
    "landuse" to arrayOf(
        "cemetery", "allotments"
    ),
    "military" to arrayOf(
        "airfield", "barracks", "training_area"
    ),
    "healthcare" to arrayOf(
        // common
        "pharmacy", "doctor", "clinic", "dentist", "centre", "physiotherapist",
        "laboratory", "alternative", "psychotherapist", "optometrist", "podiatrist",
        "nurse", "counselling", "speech_therapist", "blood_donation", "sample_collection",
        "occupational_therapist", "dialysis", "vaccination_centre", "audiologist",
        "blood_bank", "nutrition_counselling",

        // name & wheelchair
        "rehabilitation", "hospice", "midwife", "birthing_centre"
    ),
).map { it.key + " ~ " + it.value.joinToString("|") }.joinToString("\n  or ")
private const val PREF_ELEMENTS = "qs_AddPlaceName_element_selection"
