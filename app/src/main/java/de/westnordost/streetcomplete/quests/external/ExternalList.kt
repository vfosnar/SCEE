package de.westnordost.streetcomplete.quests.external

import android.content.Context
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import java.io.File
import java.lang.Exception

class ExternalList(context: Context) {
    private val path = context.getExternalFilesDir(null)
    private val file = File(path, "external.csv")
    val thatMap = if (file.exists())
        readExternal()
    else
        emptyMap()

    private fun readExternal(): Map<ElementKey, String> {
        return file.readLines().mapNotNull {
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
        }.toMap()
    }

    fun remove(key: ElementKey) {
        val lines = file.readLines().toMutableList()
        lines.removeAll {
            val line = it.split(",").map { it.trim() }
            line[1].toLong() == key.id && line[0] == key.type.name
        }
        file.writeText(lines.joinToString("\n"))
    }
}
