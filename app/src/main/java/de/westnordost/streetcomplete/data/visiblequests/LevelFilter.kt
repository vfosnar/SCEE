package de.westnordost.streetcomplete.data.visiblequests

import android.content.Context
import android.content.SharedPreferences
import android.text.InputType
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.edit
import androidx.preference.Preference
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.quest.Quest
import javax.inject.Inject
import javax.inject.Singleton

/** Controller for filtering all quests that are hidden because they are on the wrong level */
@Singleton
class LevelFilter @Inject internal constructor(
    private val prefs: SharedPreferences,
    private val mapDataController: MapDataController,
    private val visibleQuestTypeController: VisibleQuestTypeController
) {

    private var isEnabled = false
    private var allowedLevel: String? = null
    private lateinit var allowedLevelTags: List<String>

    init {
        reload()
    }

    private fun reload() {
        allowedLevel = prefs.getString(Prefs.ALLOWED_LEVEL, "").let { if (it.isNullOrBlank()) null else it }
        allowedLevelTags = prefs.getString(Prefs.ALLOWED_LEVEL_TAGS, "level,level:ref")!!.split(",")
    }

    fun isVisible(quest: Quest): Boolean =
        !isEnabled ||
            (quest is OsmQuest && quest.levelAllowed())

    private fun OsmQuest.levelAllowed(): Boolean {
        val tags = mapDataController.get(this.elementType, this.elementId)?.tags ?: return true
        val levelTags = tags.filterKeys { allowedLevelTags.contains(it) }
        if (levelTags.isEmpty()) return allowedLevel == null
        return levelTags.containsValue(allowedLevel)
    }

    fun showLevelFilterDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose tags to check")
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.VERTICAL
        val levelTags = prefs.getString(Prefs.ALLOWED_LEVEL_TAGS, "level,level:ref")!!.split(",")

        val level = EditText(context)
        level.inputType = InputType.TYPE_CLASS_TEXT
        level.hint = "leave empty to show not tagged"
        level.setText(prefs.getString(Prefs.ALLOWED_LEVEL, ""))

        val enable = SwitchCompat(context)
        enable.text = "enable level filter"
        enable.isChecked = isEnabled

        val tagLevel = CheckBox(context)
        tagLevel.text = "level"
        tagLevel.isChecked = levelTags.contains("level")

        val tagLevelRef = CheckBox(context)
        tagLevelRef.text = "level:ref"
        tagLevelRef.isChecked = levelTags.contains("level:ref")

        val tagAddrFloor = CheckBox(context)
        tagAddrFloor.text = "addr:floor"
        tagAddrFloor.isChecked = levelTags.contains("addr:floor")

        linearLayout.addView(tagLevel)
        linearLayout.addView(tagLevelRef)
        linearLayout.addView(tagAddrFloor)
        linearLayout.addView(level)
        linearLayout.addView(enable)
        linearLayout.setPadding(30,10,30,10)
        builder.setView(linearLayout)
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            val levelTagList = mutableListOf<String>()
            if (tagLevel.isChecked) levelTagList.add("level")
            if (tagLevelRef.isChecked) levelTagList.add("level:ref")
            if (tagAddrFloor.isChecked) levelTagList.add("addr:floor")
            prefs.edit {
                putString(Prefs.ALLOWED_LEVEL_TAGS, levelTagList.joinToString(","))
                putString(Prefs.ALLOWED_LEVEL, level.text.toString())
            }
            isEnabled = enable.isChecked
            reload()
            visibleQuestTypeController.clear()
        }
        builder.show()
    }

}
