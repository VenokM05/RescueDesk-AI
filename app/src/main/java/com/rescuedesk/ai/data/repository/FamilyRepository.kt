package com.rescuedesk.ai.data.repository

import com.rescuedesk.ai.data.local.ContactDao
import com.rescuedesk.ai.data.local.EmergencyContactEntity
import com.rescuedesk.ai.data.local.FamilyPlanDao
import com.rescuedesk.ai.data.local.FamilyPlanEntity
import com.rescuedesk.ai.data.local.GoBagDao
import com.rescuedesk.ai.data.local.GoBagItemEntity
import com.rescuedesk.ai.domain.model.EmergencyContact
import com.rescuedesk.ai.domain.model.GoBagCategories
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
            list.map {
                GoBagItem(it.id, it.category, it.label, it.labelFil, it.checked, it.isCustom, it.updatedAt)
            }
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
                // User-entered text is language-neutral: shown as typed in either UI language.
                labelFil = label.trim(),
                isCustom = true,
                updatedAt = java.time.LocalDate.now().toString()
            )
        )

    override suspend fun deleteGoBagItem(id: Long) = goBagDao.deleteById(id)

    /** Seeds the general starter checklist once; users add/edit from there. */
    override suspend fun ensureGoBagSeeded() {
        seedMutex.withLock {
            if (goBagDao.count() > 0) return
            GoBagDefaults.items.forEach { (category, labelEn, labelFil) ->
                goBagDao.insert(
                    GoBagItemEntity(
                        category = category,
                        label = labelEn,
                        labelFil = labelFil,
                        isCustom = false
                    )
                )
            }
        }
    }
}

/**
 * Starter list from PRD §5.10 step 4 + §5.11 categories; general, not a
 * substitute for household-specific planning. Seeded in both app languages
 * (category key, English, Filipino).
 */
private object GoBagDefaults {
    val items: List<Triple<String, String, String>> = listOf(
        Triple(
            GoBagCategories.WATER,
            "Drinking water (about 3 liters per person per day, 3 days)",
            "Inuumang tubig (mga 3 litro bawat tao bawat araw, 3 araw)"
        ),
        Triple(
            GoBagCategories.WATER,
            "Ready-to-eat food for 3 days",
            "Pagkaing hindi na kailangang lutuin para sa 3 araw"
        ),
        Triple(GoBagCategories.WATER, "Manual can opener", "Pangbukás ng lata na hindi kailangan ng kuryente"),
        Triple(GoBagCategories.FIRST_AID, "First aid kit", "First aid kit"),
        Triple(GoBagCategories.FIRST_AID, "7-day supply of maintenance medicines", "7 araw na sapat na gamot na regular na inumin"),
        Triple(GoBagCategories.FIRST_AID, "Face masks", "Mga mask sa mukha"),
        Triple(GoBagCategories.LIGHTING, "Flashlight or headlamp", "Flashlight o headlamp"),
        Triple(GoBagCategories.LIGHTING, "Batteries or charged power bank", "Baterya o charged na power bank"),
        Triple(
            GoBagCategories.LIGHTING,
            "Battery- or hand-crank-powered radio",
            "Radyo na baterya o kamikamang pang-charge"
        ),
        Triple(
            GoBagCategories.DOCUMENTS,
            "Copies of IDs and important documents in a waterproof bag",
            "Kopya ng ID at mahahalagang dokumento sa hindi tinatablan ng tubig na bag"
        ),
        Triple(GoBagCategories.DOCUMENTS, "Cash in small bills", "Pera sa maliliit na denomination"),
        Triple(
            GoBagCategories.CLOTHING,
            "Change of clothes for each person",
            "Isang palitang damit bawat tao"
        ),
        Triple(GoBagCategories.CLOTHING, "Towel, soap, and hygiene supplies", "Tuwalya, sabon, at mga kailangan sa kalinisan"),
        Triple(
            GoBagCategories.CLOTHING,
            "Plastic bags for wet items and waste",
            "Plastic na bag para sa basáng gamit at basura"
        ),
        Triple(GoBagCategories.SPECIAL, "Baby supplies, if applicable", "Mga pang-baby, kung may sanggol"),
        Triple(
            GoBagCategories.SPECIAL,
            "Needs for seniors or persons with disabilities (medicine, glasses, mobility aids)",
            "Pangangailangan ng matatanda o PWD (gamot, salamin, gamit sa paggalaw)"
        ),
        Triple(GoBagCategories.SPECIAL, "Pet food and supplies, if applicable", "Pagkain at gamit ng alagang hayop, kung mayroon")
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
