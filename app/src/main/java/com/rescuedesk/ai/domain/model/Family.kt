package com.rescuedesk.ai.domain.model

/** Household plan as the UI consumes it (FR-05). */
data class HouseholdPlan(
    val householdNickname: String = "",
    val memberCount: Int = 0,
    val householdNotes: String = "",
    val meetingNearby: String = "",
    val meetingAlternate: String = "",
    val outOfAreaName: String = "",
    val outOfAreaPhone: String = "",
    val reminders: String = "",
    val planCompleted: Boolean = false,
    val updatedAt: String = ""
) {
    /** Section completion for the Screen J progress line "N of 5 sections completed". */
    val householdDone: Boolean
        get() = householdNickname.isNotBlank() || memberCount > 0 || householdNotes.isNotBlank()
    val meetingDone: Boolean
        get() = meetingNearby.isNotBlank() || meetingAlternate.isNotBlank()
    val remindersDone: Boolean
        get() = reminders.isNotBlank()
}

/** Saved emergency contact (FR-06). */
data class EmergencyContact(
    val id: Long,
    val name: String,
    val relationship: String,
    val phone: String
)

/** Go-bag checklist item (PRD section 5.11). */
data class GoBagItem(
    val id: Long,
    val category: String,
    val label: String,
    val labelFil: String = "",
    val checked: Boolean,
    val isCustom: Boolean,
    val updatedAt: String
) {
    /** Checklist text in the reader's language; Filipino falls back to English. */
    fun labelFor(languageTag: String): String =
        if (languageTag == "fil" && labelFil.isNotBlank()) labelFil else label
}

/**
 * Checklist category keys in display order (PRD section 5.11). Keys are stable
 * identifiers stored in the database; the UI maps them to localized headers.
 */
object GoBagCategories {
    const val WATER = "water"
    const val FIRST_AID = "first_aid"
    const val LIGHTING = "lighting"
    const val DOCUMENTS = "documents"
    const val CLOTHING = "clothing"
    const val SPECIAL = "special"

    val ALL = listOf(WATER, FIRST_AID, LIGHTING, DOCUMENTS, CLOTHING, SPECIAL)
}
