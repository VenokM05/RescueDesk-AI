package com.rescuedesk.ai.data.repository

import com.rescuedesk.ai.data.local.ContactDao
import com.rescuedesk.ai.data.local.EmergencyContactEntity
import com.rescuedesk.ai.data.local.FamilyPlanDao
import com.rescuedesk.ai.data.local.FamilyPlanEntity
import com.rescuedesk.ai.data.local.GoBagDao
import com.rescuedesk.ai.data.local.GoBagItemEntity
import com.rescuedesk.ai.domain.model.EmergencyContact
import com.rescuedesk.ai.domain.model.GoBagItem
import com.rescuedesk.ai.domain.model.HouseholdPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface FamilyRepository {
    fun observePlan(): Flow<HouseholdPlan>
    fun observeContacts(): Flow<List<EmergencyContact>>
    fun observeGoBag(): Flow<List<GoBagItem>>
    suspend fun savePlan(plan: HouseholdPlan)
    suspend fun addContact(name: String, relationship: String, phone: String)
    suspend fun deleteContact(id: Long)
    suspend fun setGoBagChecked(id: Long, checked: Boolean)
    suspend fun addGoBagItem(category: String, label: String)
    suspend fun deleteGoBagItem(id: Long)
    suspend fun ensureGoBagSeeded()
}

/**
 * Local-only household data (FR-05, FR-06). Nothing here leaves the device;
 * deletion is a plain row delete — no server calls to make (PRD §2.2).
 */
class RoomFamilyRepository(
    private val planDao: FamilyPlanDao,
    private val contactDao: ContactDao,
    private val goBagDao: GoBagDao
) : FamilyRepository {

    private val seedMutex = Mutex()

    override fun observePlan(): Flow<HouseholdPlan> =
        planDao.observePlan().map { it?.toDomain() ?: HouseholdPlan() }

    override fun observeContacts(): Flow<List<EmergencyContact>> =
        contactDao.observeAll().map { list ->
            list.map { EmergencyContact(it.id, it.name, it.relationship, it.phone) }
        }

    override fun observeGoBag(): Flow<List<GoBagItem>> =
        goBagDao.observeAll().map { list ->
            list.map { GoBagItem(it.id, it.category, it.label, it.checked, it.isCustom, it.updatedAt) }
        }

    override suspend fun savePlan(plan: HouseholdPlan) {
        planDao.upsert(
            FamilyPlanEntity(
                householdNickname = plan.householdNickname.trim(),
                memberCount = plan.memberCount,
                householdNotes = plan.householdNotes.trim(),
                meetingNearby = plan.meetingNearby.trim(),
                meetingAlternate = plan.meetingAlternate.trim(),
                outOfAreaName = plan.outOfAreaName.trim(),
                outOfAreaPhone = plan.outOfAreaPhone.trim(),
                reminders = plan.reminders.trim(),
                planCompleted = plan.planCompleted,
                updatedAt = java.time.LocalDate.now().toString()
            )
        )
    }

    override suspend fun addContact(name: String, relationship: String, phone: String) {
        contactDao.insert(
            EmergencyContactEntity(
                name = name.trim(),
                relationship = relationship.trim(),
                phone = phone.trim()
            )
        )
    }

    override suspend fun deleteContact(id: Long) = contactDao.deleteById(id)

    override suspend fun setGoBagChecked(id: Long, checked: Boolean) =
        goBagDao.setChecked(id, checked, java.time.LocalDate.now().toString())

    override suspend fun addGoBagItem(category: String, label: String) =
        goBagDao.insert(
            GoBagItemEntity(
                category = category,
                label = label.trim(),
                isCustom = true,
                updatedAt = java.time.LocalDate.now().toString()
            )
        )

    override suspend fun deleteGoBagItem(id: Long) = goBagDao.deleteById(id)

    /** Seeds the general starter checklist once; users add/edit from there. */
    override suspend fun ensureGoBagSeeded() {
        seedMutex.withLock {
            if (goBagDao.count() > 0) return
            GoBagDefaults.items.forEach { (category, label) ->
                goBagDao.insert(
                    GoBagItemEntity(category = category, label = label, isCustom = false)
                )
            }
        }
    }
}

/** Starter list from PRD §5.10 step 4 + §5.11 categories; general, not a substitute for household-specific planning. */
private object GoBagDefaults {
    val items: List<Pair<String, String>> = listOf(
        "Water and Food" to "Drinking water (about 3 liters per person per day, 3 days)",
        "Water and Food" to "Ready-to-eat food for 3 days",
        "Water and Food" to "Manual can opener",
        "First Aid and Medicines" to "First aid kit",
        "First Aid and Medicines" to "7-day supply of maintenance medicines",
        "First Aid and Medicines" to "Face masks",
        "Lighting and Communication" to "Flashlight or headlamp",
        "Lighting and Communication" to "Batteries or charged power bank",
        "Lighting and Communication" to "Battery- or hand-crank-powered radio",
        "Documents and Money" to "Copies of IDs and important documents in a waterproof bag",
        "Documents and Money" to "Cash in small bills",
        "Clothing and Hygiene" to "Change of clothes for each person",
        "Clothing and Hygiene" to "Towel, soap, and hygiene supplies",
        "Clothing and Hygiene" to "Plastic bags for wet items and waste",
        "Special Household Needs" to "Baby supplies, if applicable",
        "Special Household Needs" to "Needs for seniors or persons with disabilities (medicine, glasses, mobility aids)",
        "Special Household Needs" to "Pet food and supplies, if applicable"
    )
}

private fun FamilyPlanEntity.toDomain() = HouseholdPlan(
    householdNickname = householdNickname,
    memberCount = memberCount,
    householdNotes = householdNotes,
    meetingNearby = meetingNearby,
    meetingAlternate = meetingAlternate,
    outOfAreaName = outOfAreaName,
    outOfAreaPhone = outOfAreaPhone,
    reminders = reminders,
    planCompleted = planCompleted,
    updatedAt = updatedAt
)
