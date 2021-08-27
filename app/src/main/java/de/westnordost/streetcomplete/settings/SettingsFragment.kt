package de.westnordost.streetcomplete.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import de.westnordost.streetcomplete.ApplicationConstants.DELETE_OLD_DATA_AFTER
import de.westnordost.streetcomplete.ApplicationConstants.REFRESH_DATA_AFTER
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.HasTitle
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.download.tiles.DownloadedTilesDao
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataController
import de.westnordost.streetcomplete.data.osmnotes.NoteController
import de.westnordost.streetcomplete.data.quest.QuestController
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.visiblequests.*
import de.westnordost.streetcomplete.ktx.format
import de.westnordost.streetcomplete.ktx.toast
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject

/** Shows the settings screen */
class SettingsFragment : PreferenceFragmentCompat(), HasTitle,
    SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject internal lateinit var prefs: SharedPreferences
    @Inject internal lateinit var downloadedTilesDao: DownloadedTilesDao
    @Inject internal lateinit var noteController: NoteController
    @Inject internal lateinit var mapDataController: MapDataController
    @Inject internal lateinit var questController: QuestController
    @Inject internal lateinit var resurveyIntervalsUpdater: ResurveyIntervalsUpdater
    @Inject internal lateinit var questTypeRegistry: QuestTypeRegistry
    @Inject internal lateinit var visibleQuestTypeSource: VisibleQuestTypeSource
    @Inject internal lateinit var levelFilter: LevelFilter
    @Inject internal lateinit var questPresetsSource: QuestPresetsSource
    @Inject internal lateinit var visibleQuestTypeController: VisibleQuestTypeController
    @Inject internal lateinit var dayNightQuestFilter: DayNightQuestFilter

    interface Listener {
        fun onClickedQuestSelection()
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    init {
        Injector.applicationComponent.inject(this)
    }

    override val title: String get() = getString(R.string.action_settings)

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        PreferenceManager.setDefaultValues(requireContext(), R.xml.preferences, false)
        addPreferencesFromResource(R.xml.preferences)

        findPreference<Preference>("quests")?.setOnPreferenceClickListener {
            listener?.onClickedQuestSelection()
            true
        }

        findPreference<Preference>("delete_cache")?.setOnPreferenceClickListener {
            context?.let { ctx ->
                val view = LayoutInflater.from(ctx).inflate(R.layout.dialog_delete_cache, null) as TextView
                view.text = resources.getString(R.string.delete_cache_dialog_message,
                    (1.0 * REFRESH_DATA_AFTER / (24 * 60 * 60 * 1000)).format(Locale.getDefault(), 1),
                    (1.0 * DELETE_OLD_DATA_AFTER / (24 * 60 * 60 * 1000)).format(Locale.getDefault(), 1)
                )
                AlertDialog.Builder(ctx)
                    .setView(view)
                    .setPositiveButton(R.string.delete_confirmation) { _, _ ->
                        lifecycleScope.launch { deleteCache() }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
            true
        }

        findPreference<Preference>("quests.restore.hidden")?.setOnPreferenceClickListener {
            lifecycleScope.launch {
                val hidden = questController.unhideAll()
                context?.toast(getString(R.string.restore_hidden_success, hidden), Toast.LENGTH_LONG)
            }
            true
        }

        findPreference<Preference>("debug")?.isVisible = BuildConfig.DEBUG

        findPreference<Preference>("debug.quests")?.setOnPreferenceClickListener {
            startActivity(Intent(context, ShowQuestFormsActivity::class.java))
            true
        }

        findPreference<Preference>("quests.levelFilter")?.setOnPreferenceClickListener {
            showLevelFilterDialog()
            true
        }

    }

    private fun showLevelFilterDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose tags to check")
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.VERTICAL
        val levelTags = prefs.getString(Prefs.ALLOWED_LEVEL_TAGS, "level,level:ref")!!.split(",")

        val level = EditText(context)
        level.inputType = InputType.TYPE_CLASS_TEXT
        level.hint = "leave empty to show not tagged"
        level.setText(prefs.getString(Prefs.ALLOWED_LEVEL, ""))

        val enable = SwitchCompat(requireContext())
        enable.text = "enable level filter"
        enable.isChecked = levelFilter.isEnabled

        val tagLevel = CheckBox(requireContext())
        tagLevel.text = "level"
        tagLevel.isChecked = levelTags.contains("level")

        val tagLevelRef = CheckBox(requireContext())
        tagLevelRef.text = "level:ref"
        tagLevelRef.isChecked = levelTags.contains("level:ref")

        val tagAddrFloor = CheckBox(requireContext())
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
            levelFilter.isEnabled = enable.isChecked
            levelFilter.reload()
            visibleQuestTypeController.clear()
        }
        builder.show()
    }


    override fun onStart() {
        super.onStart()
        findPreference<Preference>("quests")?.summary = getQuestPreferenceSummary()
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when(key) {
            Prefs.AUTOSYNC -> {
                if (Prefs.Autosync.valueOf(prefs.getString(Prefs.AUTOSYNC, "ON")!!) != Prefs.Autosync.ON) {
                    val view = LayoutInflater.from(activity).inflate(R.layout.dialog_tutorial_upload, null)
                    AlertDialog.Builder(requireContext())
                        .setView(view)
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                }
            }
            Prefs.THEME_SELECT -> {
                val theme = Prefs.Theme.valueOf(prefs.getString(Prefs.THEME_SELECT, "AUTO")!!)
                AppCompatDelegate.setDefaultNightMode(theme.appCompatNightMode)
                activity?.recreate()
            }
            Prefs.RESURVEY_INTERVALS -> {
                resurveyIntervalsUpdater.update()
            }
            Prefs.DAY_NIGHT_FILTER -> {
                dayNightQuestFilter.enabled = prefs.getBoolean(Prefs.DAY_NIGHT_FILTER, true)
                visibleQuestTypeController.clear()
            }
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (preference is DialogPreferenceCompat) {
            val fragment = preference.createDialog()
            fragment.arguments = bundleOf("key" to preference.key)
            fragment.setTargetFragment(this, 0)
            fragment.show(parentFragmentManager, "androidx.preference.PreferenceFragment.DIALOG")
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    private suspend fun deleteCache() = withContext(Dispatchers.IO) {
        downloadedTilesDao.removeAll()
        val now = System.currentTimeMillis()
        noteController.deleteAllOlderThan(now)
        mapDataController.deleteOlderThan(now)
    }

    private fun getQuestPreferenceSummary(): String {
        val presetName = questPresetsSource.selectedQuestPresetName
        val presetStr = if (presetName != null)
            getString(R.string.pref_subtitle_quests_preset_name, presetName) + "\n" else ""

        val enabledCount = questTypeRegistry.filter { visibleQuestTypeSource.isVisible(it) }.count()
        val totalCount = questTypeRegistry.size
        val enabledStr = getString(R.string.pref_subtitle_quests, enabledCount, totalCount)

        return presetStr + enabledStr
    }
}
