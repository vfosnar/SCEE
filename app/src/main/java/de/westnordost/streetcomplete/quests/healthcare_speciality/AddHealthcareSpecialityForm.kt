package de.westnordost.streetcomplete.quests.healthcare_speciality

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestFormAnswerFragment
import de.westnordost.streetcomplete.databinding.QuestCuisineSuggestionBinding

class AddHealthcareSpecialityForm : AbstractQuestFormAnswerFragment<String>() {

    override val contentLayoutResId = R.layout.quest_cuisine_suggestion
    private val binding by contentViewBinding(QuestCuisineSuggestionBinding::bind)

    val specialities = mutableListOf<String>()

    val speciality get() = binding.cuisineInput.text?.toString().orEmpty().trim()

    val suggestions = listOf(
        "allergology",
        "anaesthetics",
        "cardiology",
        "cardiothoracic_surgery",
        "child_psychiatry",
        "community",
        "dermatology",
        "dermatovenereology",
        "diagnostic_radiology",
        "emergency",
        "endocrinology",
        "gastroenterology",
        "general",
        "geriatrics",
        "gynaecology",
        "haematology",
        "hepatology",
        "infectious_diseases",
        "intensive",
        "internal",
        "maxillofacial_surgery",
        "nephrology",
        "neurology",
        "neuropsychiatry",
        "neurosurgery",
        "nuclear",
        "occupational",
        "oncology",
        "ophthalmology",
        "orthodontics",
        "orthopaedics",
        "otolaryngology",
        "paediatric_surgery",
        "paediatrics",
        "palliative",
        "pathology",
        "physiatry",
        "plastic_surgery",
        "podiatry",
        "proctology",
        "psychiatry",
        "pulmonology",
        "radiology",
        "radiotherapy",
        "rheumatology",
        "stomatology",
        "surgery",
        "transplant",
        "trauma",
        "tropical",
        "urology",
        "vascular_surgery",
    )

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
                specialities.add(speciality)
                binding.currentCuisines.text = specialities.joinToString(";")
                binding.cuisineInput.text.clear()
            }
        }
        binding.addCuisineButton.text = "add another speciality"
    }

    override fun onClickOk() {
        if (speciality.isBlank())
            applyAnswer(specialities.joinToString(";"))
        else
            applyAnswer((specialities + listOf(speciality)).joinToString(";"))
    }

    override fun isFormComplete() = (speciality.isNotEmpty() || specialities.isNotEmpty()) && !speciality.contains(";")
}
