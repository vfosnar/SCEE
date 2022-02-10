package de.westnordost.streetcomplete.quests.max_speed

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.ANYTHING_UNPAVED
import de.westnordost.streetcomplete.data.meta.MAXSPEED_TYPE_KEYS
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CAR
import de.westnordost.streetcomplete.ktx.toYesNo

class AddMaxSpeed(private val prefs: SharedPreferences) : OsmFilterQuestType<MaxSpeedAnswer>() {

    override val elementFilter = """
        ways with
         highway ~ motorway|trunk|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential
         and !maxspeed and !maxspeed:advisory and !maxspeed:forward and !maxspeed:backward
         and ${MAXSPEED_TYPE_KEYS.joinToString(" and ") { "!$it" }}
         and surface !~ ${ANYTHING_UNPAVED.joinToString("|")}
         and cyclestreet != yes and bicycle_road != yes
         and motor_vehicle !~ private|no
         and vehicle !~ private|no
         and area != yes
         and (access !~ private|no or (foot and foot !~ private|no))
    """
    override val changesetComment = "Add speed limits"
    override val wikiLink = "Key:maxspeed"
    override val icon = R.drawable.ic_quest_max_speed
    override val hasMarkersAtEnds = true
    override val isSplitWayEnabled = true

    // see #813: US has different rules for each different state which need to be respected
    override val enabledInCountries = AllCountriesExcept("US")
    override val defaultDisabledMessage = R.string.default_disabled_msg_maxspeed

    override val questTypeAchievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) =
        if (tags.containsKey("name"))
            R.string.quest_maxspeed_name_title2
        else
            R.string.quest_maxspeed_title_short2

            kdiff3 is so incredibly shitty, removing accidentally duplicated line can't be undone and is a merge conflict now...
    override val wikiLink = "Key:maxspeed"
    override val icon = R.drawable.ic_quest_max_speed
    override val hasMarkersAtEnds = true
    override val isSplitWayEnabled = true

    // see #813: US has different rules for each different state which need to be respected
    override val enabledInCountries = AllCountriesExcept("US")
    override val defaultDisabledMessage = R.string.default_disabled_msg_maxspeed

    override val questTypeAchievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) =
        if (tags.containsKey("name"))
            R.string.quest_maxspeed_name_title2
        else
            R.string.quest_maxspeed_title_short2

    override fun createForm() = AddMaxSpeedForm()

    override fun applyAnswerTo(answer: MaxSpeedAnswer, tags: Tags, timestampEdited: Long) {
        val maxspeedTag = prefs.getString(PREF_MAXSPEED_TAG, MAXSPEED_TYPE)!!
        when (answer) {
            is MaxSpeedSign -> {
                tags["maxspeed"] = answer.value.toString()
                tags[maxspeedTag] = "sign"
            }
            is MaxSpeedZone -> {
                tags["maxspeed"] = answer.value.toString()
                tags[maxspeedTag] = answer.countryCode + ":" + answer.roadType
            }
            is AdvisorySpeedSign -> {
                tags["maxspeed:advisory"] = answer.value.toString()
                tags[maxspeedTag + ":advisory"] = "sign"
            }
            is IsLivingStreet -> {
                tags["highway"] = "living_street"
            }
            is ImplicitMaxSpeed -> {
                tags[maxspeedTag] = answer.countryCode + ":" + answer.roadType
                // Lit is either already set or has been answered by the user, so this wouldn't change the value of the lit tag
                answer.lit?.let { tags["lit"] = it.toYesNo() }
            }
        }
    }

    override val hasQuestSettings = true

    override fun getQuestSettingsDialog(context: Context): AlertDialog? {
        return AlertDialog.Builder(context)
            .setTitle("select maxspeed tag")
            .setNegativeButton(android.R.string.cancel, null)
            .setItems(arrayOf(MAXSPEED_TYPE, SOURCE_MAXSPEED)) { dialogInterface, i ->
                prefs.edit()
                    .putString(PREF_MAXSPEED_TAG, if (i == 0) MAXSPEED_TYPE else SOURCE_MAXSPEED)
                    .apply()
            }
            .create()
    }
}

private const val MAXSPEED_TYPE = "maxspeed:type"
private const val SOURCE_MAXSPEED = "source:maxspeed"
private const val PREF_MAXSPEED_TAG = "source:maxspeed"
