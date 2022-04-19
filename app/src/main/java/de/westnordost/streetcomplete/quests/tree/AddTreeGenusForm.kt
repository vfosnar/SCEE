package de.westnordost.streetcomplete.quests.tree

import android.os.Bundle
import android.view.View
import de.westnordost.osmfeatures.StringUtils
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.quests.shop_type.SearchAdapter
import de.westnordost.streetcomplete.databinding.QuestNameSuggestionBinding
import de.westnordost.streetcomplete.screens.main.map.getTreeGenus
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import org.koin.android.ext.android.inject
import androidx.core.widget.doAfterTextChanged

class AddTreeGenusForm : AbstractQuestFormAnswerFragment<Tree>() {

    override val contentLayoutResId = R.layout.quest_name_suggestion
    private val binding by contentViewBinding(QuestNameSuggestionBinding::bind)
    private val name: String? get() = binding.nameInput.text?.toString().orEmpty().trim()
    private val mapDataSource: MapDataWithEditsSource by inject()

    override fun onClickOk() {
        val tree = getSelectedTree()
        if (tree == null) {
            binding.nameInput.error = context?.resources?.getText(R.string.quest_tree_error)
        } else {
//            lastTree = tree
            applyAnswer(tree)
        }
    }

    override fun isFormComplete(): Boolean {
        return name?.isNotEmpty() ?: false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nameInput.setAdapter(SearchAdapter(requireContext(), { term -> getTrees(term) }, { it.toDisplayString() }))
        binding.nameInput.doAfterTextChanged { checkIsFormComplete() }

        // pre-filling with last tree is not nice for entering a different tree
//        lastTree?.let { binding.nameInput.setText(it.toDisplayString()) }
    }

    override fun onClickMapAt(position: LatLon, clickAreaSizeInMeters: Double): Boolean {
        val maxDist = clickAreaSizeInMeters + 5
        val bbox = position.enclosingBoundingBox(maxDist)
        val mapData = mapDataSource.getMapDataWithGeometry(bbox)
        var bestTree: Pair<String, Double>? = null

        mapData.forEach { element ->
            if (element is Node && element.tags["natural"] == "tree") {
                val name = getTreeGenus(element.tags) ?: return@forEach
                val distance = element.position.distanceTo(position)
                if (distance < bestTree?.second ?: maxDist)
                    bestTree = Pair(name, distance)
            }
        }
        bestTree?.let { binding.nameInput.setText(getTrees(it.first).first().toDisplayString()) }

        return true
    }

    private fun getSelectedTree(): Tree? {
        val input = binding.nameInput.text.toString()
        return getTrees(input).firstOrNull { StringUtils.canonicalize(it.toDisplayString()) == StringUtils.canonicalize(input) }
    }

    private fun getTrees(startsWith: String): List<Tree> {
        return trees.filter {
            it.toDisplayString() == startsWith
            || it.name.startsWith(startsWith, true)
            || it.localName.startsWith(startsWith, true)
        }
    }

    companion object {
        // TODO: fill...
        //  and translate? would need to have a separate resource string for each tree
        private val trees = setOf(
            Tree("Quercus", false, "Oak"),
            Tree("Acer", false, "Maple"),
            Tree("Aesculus", false, "Chestnut"),
            Tree("Aesculus hippocastanum", true, "Horse chestnut"),
        )

//        private var lastTree: Tree? = null
    }

    private fun Tree.toDisplayString() =
        "${this.name} (${this.localName})"
}

data class Tree(val name: String, val isSpecies: Boolean, val localName: String)
