package de.westnordost.streetcomplete.quests.healthcare_speciality

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType

class AddHealthcareSpeciality : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways with
         amenity = doctors
         and name and !healthcare:speciality
    """
    override val commitMessage = "Add healthcare specialities"
    override val wikiLink = "Key:healthcare:speciality"
    override val icon = R.drawable.ic_quest_healthcare_speciality

    override fun getTitle(tags: Map<String, String>) = R.string.quest_healthcare_speciality_title

    override fun createForm() = AddHealthcareSpecialityForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("healthcare:speciality", answer)
    }
}
