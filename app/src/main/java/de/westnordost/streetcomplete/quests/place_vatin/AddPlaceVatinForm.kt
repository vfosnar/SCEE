package de.westnordost.streetcomplete.quests.place_vatin

import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestContactBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class AddPlaceVatinForm : AbstractOsmQuestForm<PlaceVatinAnswer>() {

    override val contentLayoutResId = R.layout.quest_contact
    private val binding by contentViewBinding(QuestContactBinding::bind)

    private val vatin get() = binding.nameInput.text?.toString().orEmpty().trim()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.nameInput.inputType = InputType.TYPE_CLASS_TEXT

        binding.nameInput.doAfterTextChanged {
            val text = binding.nameInput.text.toString()
            val countryPrefix = text.substring(0, text.length.coerceAtMost(2))
            if (countryPrefix.isNotEmpty() && countryPrefix.any { it.isLowerCase() }) {
                val textAfterCountryPrefix = if (text.length > 2) { text.substring(2) } else { "" }
                val newText = countryPrefix.uppercase() + textAfterCountryPrefix
                val selection = binding.nameInput.selectionStart
                binding.nameInput.setText(newText)
                binding.nameInput.setSelection(selection)
            }
            checkIsFormComplete()
        }
    }

    override val otherAnswers get() = listOfNotNull(
        AnswerItem(R.string.quest_placeVatin_no_vatin_answer) { confirmNoSign() }
    )

    override fun onClickOk() {
        applyAnswer(PlaceVatin(vatin))
    }

    override fun isFormComplete(): Boolean {
        if (vatin.length < 2)
            return false
        val countryPrefix = vatin.substring(0, 2);
        if (countryPrefix.any { !it.isUpperCase() })
            return false
        // TODO other countries https://en.wikipedia.org/wiki/VAT_identification_number
        return when (countryPrefix) {
            "CZ" -> vatin.length in 2 + 8..2 + 10 && vatin.substring(2).all { it.isDigit() }
            "SK" -> vatin.length == 2 + 10 && vatin.substring(2).all { it.isDigit() }
            else -> vatin.length >= 2 + 2 // Romania
        }
    }

    private fun confirmNoSign() {
        val ctx = context ?: return
        AlertDialog.Builder(ctx)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(
                NoPlaceVatinSign
            ) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }
}
