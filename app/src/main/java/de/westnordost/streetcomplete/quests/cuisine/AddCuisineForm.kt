package de.westnordost.streetcomplete.quests.cuisine

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.databinding.QuestCuisineSuggestionBinding

class AddCuisineForm : AbstractQuestFormAnswerFragment<String>() {

    override val contentLayoutResId = R.layout.quest_cuisine_suggestion
    private val binding by contentViewBinding(QuestCuisineSuggestionBinding::bind)

    val cuisines = mutableListOf<String>()

    val cuisine get() = binding.cuisineInput.text?.toString().orEmpty().trim()

    val suggestions = listOf("vietnamese", "italian", "indian", "japanese", "chinese", "asian", "russian", "french", "american", "mexican",
        "regional", "georgian", "czech", "heuriger", "korean", "spanish", "thai", "greek", "turkish",
        "pizza", "sushi", "hot_dog", "burger", "kebab", "chicken", "barbecue", "escalope", "fish",
        "curry", "noodle", "steak_house", "sandwich", "donut", "dessert", "ice_cream", "chimney_cake")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        suggestions.let {
            binding.cuisineInput.setAdapter(
                ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line, it
            )
            )
        }

        binding.cuisineInput.doAfterTextChanged { checkIsFormComplete() }

        binding.addCuisineButton.setOnClickListener {
            if (isFormComplete()) {
                cuisines.add(cuisine)
                binding.currentCuisines.text = cuisines.joinToString(";")
                binding.cuisineInput.text.clear()
            }
        }
    }

    override fun onClickOk() {
        if (cuisine.isBlank())
            applyAnswer(cuisines.joinToString(";"))
        else
            applyAnswer((cuisines + listOf(cuisine)).joinToString(";"))
    }

    override fun isFormComplete() = (cuisine.isNotEmpty() || cuisines.isNotEmpty()) && !cuisine.contains(";")
}
