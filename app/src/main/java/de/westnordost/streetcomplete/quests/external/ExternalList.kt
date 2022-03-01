package de.westnordost.streetcomplete.quests.external

import android.content.Context
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import java.io.File
import java.lang.Exception

class ExternalList(private val context: Context) {
    val thatMap = mutableMapOf<ElementKey, String>()

    init { reload() }

    fun reload() {
        val path = context.getExternalFilesDir(null)
        val file = File(path, "external.csv")
        thatMap.clear()
        if (!file.exists()) return
        thatMap.putAll(file.readLines().mapNotNull {
            val elementType = it.substringBefore(',').trim()
            val rest = it.substringAfter(',').trim()
            val elementId = rest.substringBefore(',').trim()
            val text =
                if (rest.contains(','))
                    rest.substringAfter(',').trim()
                else ""

            try {
                ElementKey(ElementType.valueOf(elementType), elementId.toLong()) to text
            } catch(e: Exception) {
                null
            }
        }
        )
    }

    fun remove(key: ElementKey) {
        thatMap.remove(key)
        val path = context.getExternalFilesDir(null)
        val file = File(path, "external.csv")
        val lines = file.readLines().toMutableList()
        lines.removeAll {
            if (!it.contains(','))
                false
            else {
                val line = it.split(",").map { it.trim() }
                line[1].toLong() == key.id && line[0] == key.type.name
            }
        }
        file.writeText(lines.joinToString("\n"))
    }
}
