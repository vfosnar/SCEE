package de.westnordost.streetcomplete.util

import android.util.Log
import de.westnordost.streetcomplete.data.download.tiles.TilePos
import de.westnordost.streetcomplete.data.download.tiles.enclosingTilesRect
import de.westnordost.streetcomplete.data.download.tiles.minTileRect
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MutableMapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Relation
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.util.math.contains
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.intersect
import de.westnordost.streetcomplete.util.math.isCompletelyInside

class MdcCache(
    private val maxTiles: Int,
    private val tileZoom: Int,
    private val fetch: (BoundingBox) -> Pair<List<Element>, List<ElementGeometryEntry>>
) {
    private val map = LinkedHashMap<TilePos, HashMap<ElementKey, Pair<Element, GeoThing>>>(maxTiles, 0.75f, true)

    fun getElement(elementKey: ElementKey): Element? {
        // this is clearly slower than a single HashMap<Key, Element>
        //  how much depends on number of tiles and number of elements (numbers very approximate, tests done blocking and on desktop instead of live on phone)
        //  16 tiles, 1000 elements each: factor 2
        //  16 tiles, 10000 elements each: factor 4
        //  128 tiles, 1000 elements each: factor 20-30
        //  128 tiles, 10000 elements each: factor 60-70
        // anyway, even for 128 tile, 10000 elements each it's 20 vs 1200 ns
        //  this is both negligible compared to other stuff (measured in ms, nut µs)

        // possible optimization: it's likely that the needed element is in the most recently accesses tiles
        //  doing the loop in reversed order would likely finish faster, but how to do it?
        //  just calling values.reversed() takes 2x longer than searching through all the values (same for values.last())
        return getEntry(elementKey)?.first
    }

    // used for getting nodes for ways/relations -> usually close together (same/neighboring tiles)
    fun getElements(keys: Collection<ElementKey>): List<Element> = synchronized(this) {
        return keys.mapNotNull { elementKey ->
            map.values.forEach { it[elementKey]?.let { return@mapNotNull it.first } }
            null
        }
        // version below is some 10-20% faster than above on average, but in worst case it's much slower
        // still, even for 800 elements in a large cache on S4 mini its under 5 ms
/*        val elements = hashSetOf<Element>()
        map.values.forEach { tile -> // maybe reversed
            keys.forEach { tile[it]?.let { elements.add(it.first) } }
            if (elements.size == keys.size) return elements
        }*/
    }

    fun getGeometry(elementKey: ElementKey): ElementGeometry? {
        return getEntry(elementKey)?.second?.geo
    }

    private fun getEntry(elementKey: ElementKey): Pair<Element, GeoThing>? = synchronized(this) {
        map.values.forEach { it[elementKey]?.let { return it } }
        return null
    }

    // mainly used when getting quests, with many geometries (~100-1000)
    // fast enough, 2-20 ms for 800 geometries if all cached
    fun getGeometries(keys: Collection<ElementKey>): List<ElementGeometryEntry> = synchronized(this) {
        // don't use some common getEntries because geometry can be null even if entry is not null
        return keys.mapNotNull { elementKey ->
            map.values.forEach { it[elementKey]?.let { (_, geo) -> return@mapNotNull geo.geo?.let { ElementGeometryEntry(elementKey.type, elementKey.id, geo.geo) } } }
            null
        }
    }

    // returns null if node is not in cache
    // this is weird... sometimes it's quite slow, definitely slower than db
    //  but then sometimes it's really fast again, even for nodes in the same tile
    fun getWaysForNode(nodeId: Long): List<Way>? = synchronized(this) {
        val nodeKey = ElementKey(ElementType.NODE, nodeId)
        map.values.forEach { tile -> tile[nodeKey]?.let { entry ->
            // all ways containing the node must be in this tile (may also be in others, but doesn't matter)
            // get all ways, and filter by id
            //  filtering additionally by bbox (which must contain the node) sometimes is slower, sometimes clearly faster... weird
            return tile.values.mapNotNull {
                if (it.first is Way
                    && it.second.bbox.contains((entry.first as Node).position)
                    && nodeId in (it.first as Way).nodeIds
                )
                    it.first as Way
                else null
            }
        } }
        return null
    }

    // returns null if element is not in cache
    fun getRelationsForElement(elementKey: ElementKey): List<Relation>? = synchronized(this) {
        // similar to getWaysForNode
        map.values.forEach { tile -> tile[elementKey]?.let { entry ->
            return tile.values.mapNotNull {
                if (it.first is Relation
                    && it.second.bbox.intersect(entry.second.bbox)
                    && (it.first as Relation).members.any { it.ref == elementKey.id && it.type == elementKey.type }
                )
                    it.first as Relation
                else null
            }
        } }
        return null
    }

    fun get(bbox: BoundingBox): MutableMapDataWithGeometry = synchronized(this) {
        val requiredTiles = bbox.asListOfEnclosingTilePos()
        val mapData = MutableMapDataWithGeometry()
        mapData.boundingBox = bbox

        val tilesToFetch = requiredTiles.filterNot { map.containsKey(it) }

        // fill mapData with cached data before calling fetch
        //  because adding new tiles may lead to other tiles being dropped
        // putting ~10k elements is ~50-100 ms
        requiredTiles.filter { map.containsKey(it) }.forEach { tile -> mapData.putAll(tile, bbox) }

        if (tilesToFetch.isNotEmpty()) {
            // fetch and put tiles
            val fetchBBox = tilesToFetch.minTileRect()!!.asBoundingBox(tileZoom)
            val lists = fetch(fetchBBox)

            replaceAllInTiles(lists.first, lists.second, tilesToFetch)
            if (fetchBBox.isCompletelyInside(bbox)) // simply add add all data, duplicates are not a problem
                mapData.putAll(lists.first, lists.second)
            else if (tilesToFetch.size <= maxTiles)
                tilesToFetch.forEach { tile -> mapData.putAll(tile, bbox) } // get remaining data from cache
            else {
                // not all of the new tiles are in cache, this should actually not happen
                //  but we can still deal with it
                val geometriesByKey = lists.second.associateBy { ElementKey(it.elementType, it.elementId) }
                lists.first.forEach {
                    val geometry = geometriesByKey[ElementKey(it.type, it.id)]
                    if (geometry?.geometry?.getBounds()?.intersect(bbox) != false)
                        mapData.put(it, geometry?.geometry)
                }
            }
        }
        return mapData
    }

    fun remove(keys: Collection<ElementKey>) = synchronized(this) {
        // loop over all tiles, as an element may be contained in multiple tiles
        // maybe add shortcut for nodes if this is actually slow
        map.values.forEach { tile -> keys.forEach { tile.remove(it) } }
    }

    fun replaceAllInBBox(elements: Iterable<Element>, geometries: Collection<ElementGeometryEntry>, bbox: BoundingBox) = synchronized(this) {
        // remove all tiles not fully contained in bbox
        val tiles = bbox.asListOfEnclosingTilePos()
        val completelyContainedTiles = tiles.filter { it.asBoundingBox(tileZoom).isCompletelyInside(bbox) }
        val incompleteTiles = tiles.filter { !it.asBoundingBox(tileZoom).isCompletelyInside(bbox) }
        if (incompleteTiles.isNotEmpty()) {
            Log.w("cache", "bbox does not align with tile, clearing incomplete tiles from cache")
            incompleteTiles.forEach { removeTile(it) }
        }
        if (completelyContainedTiles.size > maxTiles)
            Log.w("cache", "bbox is larger than cache, not putting all elements")
        replaceAllInTiles(elements, geometries, completelyContainedTiles)
    }

    private fun replaceAllInTiles(elements: Iterable<Element>, geometries: Collection<ElementGeometryEntry>, tiles: Collection<TilePos>) {
        tiles.forEach { putEmptyTile(it) }
        putAll(elements, geometries)
    }

    fun putAll(elements: Iterable<Element>, geometries: Collection<ElementGeometryEntry>) = synchronized(this) {
        val geometriesByKey = geometries.associateBy { ElementKey(it.elementType, it.elementId) }
        val entries = ArrayList<Pair<Element, GeoThing>>(geometriesByKey.size)
        val nullGeos = mutableListOf<Element>()
        elements.forEach {
            val geometry = geometriesByKey[ElementKey(it.type, it.id)]
            if (geometry == null)
                nullGeos.add(it)
            else
                entries.add(it to GeoThing(geometry.geometry))
        }

        val bboxByTile = map.keys.associateWith { it.asBoundingBox(tileZoom) }

        fun putEntry(entry: Pair<Element, GeoThing>) {
            val (element, geo) = entry
            val key = ElementKey(element.type, element.id)

            // remove element if it's cached and bbox changed
            val oldEntry = getEntry(key) // this could be accelerated by getting all entries before
            if (oldEntry != null && oldEntry.second.bbox != geo.bbox)
                remove(listOf(key)) // optimize / mass remove if it's slow

            for ((tile, bounds) in bboxByTile) {
                if (element is Node) {
                    if (bounds.contains(element.position)) {
                        map[tile]!!.put(key, entry)
                        return
                    }
                }
                if (bounds.intersect(geo.bbox))
                    map[tile]!!.put(key, entry)
            }
        }

        entries.forEach { putEntry(it) } // this is by far slowest part in put, 400 ms for 13k elements

        // this must happen after putting normal entries
        createGeoThingForNullGeos(nullGeos).forEach { putEntry(it) }
    }

    // requires other elements for this bbox to exist!
    private fun createGeoThingForNullGeos(elements: Iterable<Element>): List<Pair<Element, GeoThing>> {
        return elements.mapNotNull { element ->
            val bbox: BoundingBox = when (element) {
                is Node -> element.position.enclosingBoundingBox(0.0) // should not happen
                is Way -> getElements(element.nodeIds.map { ElementKey(ElementType.NODE, it) })
                    .map { (it as Node).position }.enclosingBoundingBox()
                is Relation -> {
                    val memberGeometries = getGeometries(element.members.map { ElementKey(it.type, it.ref) })
                    if (memberGeometries.isEmpty()) return@mapNotNull null // means: all members have null geometries
                    val memberBounds = memberGeometries.map { it.geometry.getBounds() }
                    // use min and max instead of just positions.enclosingBoundingBox, not sure about 180th meridian stuff
                    val memberMin = memberBounds.map { it.min }
                    val memberMax = memberBounds.map { it.max }
                    val min = LatLon(memberMin.minOf { it.latitude }, memberMin.minOf { it.longitude })
                    val max = LatLon(memberMax.maxOf { it.latitude }, memberMax.maxOf { it.longitude })
                    BoundingBox(min, max)
                }
            }
            element to GeoThing(null, bbox)
        }
    }

    // adds empty tile, or replaces existing tile with empty one
    private fun putEmptyTile(tilePos: TilePos) {
        map[tilePos] = HashMap()
        trim()
    }

    fun clear() = synchronized(this) { map.clear() }

    // this might be more useful for reducing memory than simply reducing to fixed number of tiles
    // and it can easily avoid removing empty tiles
    fun trimToElementCount(target: Int) = synchronized(this) {
        while (map.values.sumOf { it.size } > target) {
            removeTile(map.entries.first { it.value.isNotEmpty() }.key)
        }
    }

    fun trim(target: Int = maxTiles) = synchronized(this) {
        while (map.size > target) {
            removeTile(map.keys.first())
        }
    }

    private fun removeTile(tile: TilePos) = map.keys.remove(tile)

    private fun BoundingBox.asListOfEnclosingTilePos() = enclosingTilesRect(tileZoom).asTilePosSequence().toList()

    // get cached data in tile, and put elements inside bbox to MutableMapDataWithGeometry
    // will crash if the tile is not cached instead of causing weird bugs
    private fun MutableMapDataWithGeometry.putAll(tile: TilePos, bbox: BoundingBox) {
        if (tile.asBoundingBox(tileZoom).isCompletelyInside(bbox))
            map[tile]!!.values.forEach { put(it.first, it.second.geo) }
        else
            map[tile]!!.values.forEach {
                if (it.first is Node) {
                    if (bbox.contains((it.first as Node).position))
                        put(it.first, it.second.geo)
                }
                else {
                    if (it.second.bbox.intersect(bbox))
                        put(it.first, it.second.geo)
                }
            }
    }

}

// use this instead of ElementGeometry
// this is some ugly thing, but how to avoid with this cache?
// ignoring elements without geometry might cause problems if a deleted node is part of such a way/relation
private class GeoThing(val geo: ElementGeometry?, private val _bbox: BoundingBox? = null) {
    init {
        require((geo != null && _bbox == null) || (geo == null && _bbox != null)) {
            "can't both be null or both be set"
        }
    }
    val bbox get() = geo?.getBounds() ?: _bbox!!

    override fun hashCode(): Int {
        return geo?.hashCode() ?: _bbox.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is GeoThing) return false
        if (geo != null) return geo == other.geo
        return _bbox == other._bbox
    }
}
