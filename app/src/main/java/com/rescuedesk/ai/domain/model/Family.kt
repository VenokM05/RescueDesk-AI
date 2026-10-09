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

/** Go-bag checklist item (PRD §5.11). */
data class GoBagItem(
    val id: Long,
    val category: String,
    val label: String,
    val checked: Boolean,
    val isCustom: Boolean,
    val updatedAt: String
)

/** Checklist categories in display order (PRD §5.11). */
object GoBagCategories {
    val ALL = listOf(
        "Water and Food",
        "First Aid and Medicines",
        "Lighting and Communication",
        "Documents and Money",
        "Clothing and Hygiene",
        "Special Household Needs"
    )
}
