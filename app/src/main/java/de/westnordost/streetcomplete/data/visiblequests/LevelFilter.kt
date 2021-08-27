package de.westnordost.streetcomplete.data.visiblequests

import android.content.SharedPreferences
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
    private val mapDataController: MapDataController
) {

    var isEnabled = false
    private var allowedLevel: String? = null
    private lateinit var allowedLevelTags: List<String>

    init {
        reload()
    }

    fun reload() {
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

}
