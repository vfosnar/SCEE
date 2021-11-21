package de.westnordost.streetcomplete.quests.healthcare_specialty

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType

class AddHealthcareSpecialty : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways with
         amenity = doctors
         and name and !healthcare:specialty
    """
    override val commitMessage = "Add healthcare specialties"
    override val wikiLink = "Key:healthcare:specialty"
    override val icon = R.drawable.ic_quest_restaurant_vegan

    override fun getTitle(tags: Map<String, String>) = R.string.quest_cuisine_title

    override fun createForm() = AddHealthcareSpecialtyForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("healthcare:specialty", answer)
    }
}
