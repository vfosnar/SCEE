package de.westnordost.streetcomplete.data.quest

import android.app.AlertDialog
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement
import android.content.Context
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment

interface QuestType<T> {

    /** the icon resource id used to display this quest type on the map */
    val icon: Int

    /** the title resource id used to display the quest's question */
    val title: Int

    fun getQuestSettingsDialog(context: Context): AlertDialog? = null
    val hasQuestSettings: Boolean get() = false // how to avoid? checking getQuestSettings requires context (in questSelectionAdapter)

    /** returns the string resource id that explains why this quest is disabled by default or zero
     * if it is not disabled by default */
    val defaultDisabledMessage: Int get() = 0

    /** returns the dialog in which the user can add the data */
    fun createForm(): AbstractQuestAnswerFragment<T>

    /** The quest type can clean it's metadata that is older than the given timestamp here, if any  */
    fun deleteMetadataOlderThan(timestamp: Long) {}

    val dotColor: String get() = "no"

    /** towards which achievements the quest should count */
    val questTypeAchievements: List<QuestTypeAchievement> get() = listOf()
}
