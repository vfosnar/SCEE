package de.westnordost.streetcomplete.quests.contact

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType

class AddContactPhone : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways, relations with
        (
         tourism = information and information = office
         or """.trimIndent() +
         PLACES_FOR_CONTACT_QUESTS +
        "\n) and !phone and !contact:phone and !contact:mobile and !brand and name"

    override val commitMessage = "Add phone number"
    override val wikiLink = "Key:phone"
    override val icon = R.drawable.ic_quest_phone

    override fun getTitle(tags: Map<String, String>) = R.string.quest_contact_phone

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val name = tags["name"]
        return if (name != null) arrayOf(name) else arrayOf()
    }

    override fun createForm() = AddContactPhoneForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("phone", answer)
    }

}

val PLACES_FOR_CONTACT_QUESTS = mapOf(
"amenity" to arrayOf(
"restaurant", "cafe", "internet_cafe",
"cinema", "townhall", "courthouse", "embassy", "community_centre", "youth_centre", "library",
"dentist", "doctors", "clinic", "veterinary", "animal_shelter",
"arts_centre", "ferry_terminal"
),
"tourism" to arrayOf(
"zoo", "aquarium", "gallery", "museum",
),
"shop" to arrayOf(
"beauty", "massage", "hairdresser","boutique","wool","tattoo","electrical","florist","glaziery",
"computer","electronics","hifi","mobile_phone","bicycle","outdoor","sports","art","craft","model",
"musical_instrument","camera","books","travel_agency","cheese","chocolate","coffee","health_food"
),
"leisure" to arrayOf(
"fitness_centre", "bowling_alley", "sports_centre"
),
"office" to arrayOf(
"insurance", "government", "travel_agent", "tax_advisor", "religion", "employment_agency",
"lawyer", "estate_agent", "therapist", "notary"
),
"craft" to arrayOf(
"carpenter", "shoemaker", "tailor", "photographer", "dressmaker",
"electronics_repair", "stonemason", "winery"
)
).map { it.key + " ~ " + it.value.joinToString("|") }.joinToString("\n or ")

