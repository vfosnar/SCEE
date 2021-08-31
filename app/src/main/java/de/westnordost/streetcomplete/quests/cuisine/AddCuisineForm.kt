package de.westnordost.streetcomplete.quests.cuisine

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.util.TextChangedWatcher
import kotlinx.android.synthetic.main.quest_cuisine_suggestion.*

class AddCuisineForm : AbstractQuestFormAnswerFragment<String>() {

    override val contentLayoutResId = R.layout.quest_cuisine_suggestion
    val cuisines = mutableListOf<String>()

    val cuisine get() = cuisineInput?.text?.toString().orEmpty().trim()

    val suggestions = listOf("vietnamese", "italian", "indian", "japanese", "chinese", "asian", "russian", "french", "american", "mexican",
        "regional", "georgian", "czech", "heuriger", "korean", "spanish", "thai", "greek", "turkish",
        "pizza", "sushi", "hot_dog", "burger", "kebab", "chicken", "barbecue", "escalope", "fish",
        "curry", "noodle", "steak_house", "sandwich", "donut", "dessert", "ice_cream", "chimney_cake")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        suggestions?.let {
            cuisineInput.setAdapter(
                ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line, it
            )
            )
        }

        cuisineInput.addTextChangedListener(TextChangedWatcher { checkIsFormComplete() })

        addCuisineButton.setOnClickListener {
            if (isFormComplete()) {
                cuisines.add(cuisine)
                currentCuisines.text = cuisines.joinToString(";")
                cuisineInput.text.clear()
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
