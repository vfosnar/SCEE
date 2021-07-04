package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.quests.NoAnswerFragment

class ShowOther : OsmFilterQuestType<Boolean>() {
    override val elementFilter = """
        nodes, ways, relations with
        (
         playground
         or historic
         or club
         or information and information !~ office
         or tourism = viewpoint
         or tourism = artwork
         or tourism = wilderness_hut
         or pipeline = marker
         or """.trimIndent() +

        mapOf(
            "amenity" to arrayOf(
                // common
                "place_of_worship",                    // religious
                "toilets",
                "prison", "fire_station", "police", "ranger_station",                                             // civic
                "townhall", "courthouse", "embassy", "community_centre", "youth_centre", "library",  // civic
                "monastery",                                                             // religious
                "kindergarten", "school", "college", "university", "research_institute", // education
                "drinking_water","shower","post_box","bbq","grit_bin","clock","hunting_stand"
            ),
            "leisure" to arrayOf(
                "sports_centre", "stadium", "marina",
                "horse_riding", "dance", "nature_reserve","pitch","playground"
            ),
            "landuse" to arrayOf(
                "cemetery", "allotments"
            ),
            "military" to arrayOf(
                "airfield", "barracks", "training_area"
            ),
            "emergency" to arrayOf(
                "fire_hydrant", "defibrillator", "phone", "life_ring",
                "fire_extinguisher", "water_tank", "suction_point"
            )
        ).map { it.key + " ~ " + it.value.joinToString("|") }.joinToString("\n or ") +
        "\n)"

    override val commitMessage = "I hope this does not get committed"
    override val wikiLink = "nope"
    override val icon = R.drawable.ic_quest_fire_hydrant
    override val dotColor = "gold"

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_thisIsOther_title

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        if (tags["pipeline"].equals("marker"))
            return arrayOf(tags.entries.toString())
        val name = featureName.value ?: tags.entries
        return arrayOf(name.toString())
    }

    override fun createForm() = NoAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
    }
}
