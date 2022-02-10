package de.westnordost.streetcomplete.settings.questselection

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import de.westnordost.streetcomplete.HasTitle
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.visiblequests.QuestPreset
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsController
import de.westnordost.streetcomplete.databinding.DialogInputTextBinding
import de.westnordost.streetcomplete.databinding.FragmentQuestPresetsBinding
import de.westnordost.streetcomplete.ktx.viewBinding
import de.westnordost.streetcomplete.ktx.viewLifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class QuestPresetsFragment : Fragment(R.layout.fragment_quest_presets), HasTitle {

    private val questPresetsController: QuestPresetsController by inject()

    private val binding by viewBinding(FragmentQuestPresetsBinding::bind)

    override val title: String get() = getString(R.string.action_manage_presets)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = QuestPresetsAdapter(requireContext(), questPresetsController)
        binding.addPresetButton.setOnClickListener { showProfileSelector() }
        lifecycle.addObserver(adapter)
        binding.questPresetsList.adapter = adapter
    }

    private fun showProfileSelector() {
        val c = context ?: return
        val presets = mutableListOf<QuestPreset>()
        presets.add(QuestPreset(0, c.getString(R.string.quest_presets_default_name)))
        presets.addAll(questPresetsController.getAll())
        var dialog: AlertDialog? = null
        val array = presets.map { it.name }.toTypedArray()
        val builder = AlertDialog.Builder(c)
            .setTitle("Copy from another profile?")
            .setSingleChoiceItems(array, -1) { _, i ->
                dialog?.dismiss()
                onClickAddPreset(presets[i].id)
            }
            .setNegativeButton("empty profile") { _, _ ->
                dialog?.dismiss()
                onClickAddPreset(null)
            }
        dialog = builder.create()
        dialog.show()
    }

    private fun onClickAddPreset(copyFrom: Long?) {
        val ctx = context ?: return

        val dialogBinding = DialogInputTextBinding.inflate(layoutInflater)
        dialogBinding.editText.hint = ctx.getString(R.string.quest_presets_preset_name)

        AlertDialog.Builder(ctx)
            .setTitle(R.string.quest_presets_preset_add)
            .setView(dialogBinding.root)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val name = dialogBinding.editText.text.toString().trim()
                viewLifecycleScope.launch(Dispatchers.IO) {
                    if (copyFrom == null) questPresetsController.add(name)
                    else questPresetsController.add(name, copyFrom)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
