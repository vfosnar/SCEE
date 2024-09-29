package de.westnordost.streetcomplete.quests.place_ico

import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.text.isDigitsOnly
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.databinding.QuestContactBinding
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class AddPlaceIcoForm : AbstractOsmQuestForm<PlaceIcoAnswer>() {

    override val contentLayoutResId = R.layout.quest_contact
    private val binding by contentViewBinding(QuestContactBinding::bind)

    private val ico get() = binding.nameInput.text?.toString().orEmpty().trim()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.nameInput.inputType = InputType.TYPE_CLASS_NUMBER

        binding.nameInput.doAfterTextChanged {
            checkIsFormComplete()
        }
    }

    override val otherAnswers get() = listOfNotNull(
        AnswerItem(R.string.quest_placeIco_no_ico_answer) { confirmNoSign() }
    )

    override fun onClickOk() {
        applyAnswer(PlaceIco(ico))
    }

    override fun isFormComplete() = ico.length == 8 && ico.isDigitsOnly()

    private fun confirmNoSign() {
        val ctx = context ?: return
        AlertDialog.Builder(ctx)
            .setTitle(R.string.quest_generic_confirmation_title)
            .setPositiveButton(R.string.quest_generic_confirmation_yes) { _, _ -> applyAnswer(
                NoPlaceIcoSign
            ) }
            .setNegativeButton(R.string.quest_generic_confirmation_no, null)
            .show()
    }
}
