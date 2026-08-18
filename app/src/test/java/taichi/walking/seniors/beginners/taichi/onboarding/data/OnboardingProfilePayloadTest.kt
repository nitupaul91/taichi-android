package taichi.walking.seniors.beginners.taichi.onboarding.data

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import java.io.File

/**
 * The JSON this produces is a wire contract with two independent consumers: the remote paywall's
 * JavaScript and the backend's `profile/android/upload`. Neither fails loudly on a renamed field —
 * the paywall silently loses personalization and the server silently stores a null — so the field
 * names are pinned here.
 */
class OnboardingProfilePayloadTest {

    private val gson = Gson()

    private val state = OnboardingState(
        discoverySourceId = "facebook",
        actualAge = 67,
        genderId = "male",
        goals = setOf("balance", "mobility"),
        taiChiFamiliarityId = "heard_of_it",
        currentBodyTypeId = "heavyset",
        targetBodyTypeId = "fit",
        targetZones = setOf("legs", "back"),
        activityLevelId = "sedentary",
        walkDailyId = "under_30",
        stairsFeelingId = "winded",
        squatAbilityId = "with_support",
        rotateHeadId = "yes",
        armsForwardId = "no",
        taiChiLevelId = "complete",
        betweenMealsId = "hungry",
        sleepAmountId = "6_7",
        waterIntakeId = "2_glasses",
        dietTypes = setOf("none"),
        heightCm = 178,
        weightKg = 92,
        targetWeightKg = 82,
        useMetric = true,
        shapeUpEventId = "holiday",
        shapeUpEventDateMillis = 1_792_243_800_000L,
        shapeUpEventDateSkipped = false,
        motivationLevelId = "very",
        exerciseBlockers = setOf("too_tired")
    )

    @Test
    fun `payload carries every field the backend reads, under the expected names`() {
        val json = gson.toJsonTree(
            state.toOnboardingProfilePayload(userId = "user-1", completedStep = "gender")
        ).asJsonObject

        val expected = listOf(
            "userId", "completedStep", "discoverySource", "age", "gender", "goals",
            "taiChiFamiliarity", "currentBodyType", "targetBodyType", "targetZones", "activityLevel",
            "dailyWalking", "stairsFeeling", "squatsAbility", "canRotateHead", "canHoldArms",
            "taiChiLevel", "betweenMealsFeeling", "sleepAmount", "waterIntake", "dietTypes",
            "height", "weight", "targetWeight", "shapeUpEvent", "shapeUpEventDate",
            "motivationLevel", "exerciseBlockers", "useMetric"
        )
        expected.forEach { field ->
            assertTrue(json.has(field), "missing wire field: $field")
        }

        assertEquals("user-1", json.get("userId").asString)
        assertEquals("gender", json.get("completedStep").asString)
        assertEquals(67, json.get("age").asInt)
        // These four are what the backend normalises; it needs the raw option ids, not indices.
        assertEquals("heavyset", json.get("currentBodyType").asString)
        assertEquals("fit", json.get("targetBodyType").asString)
        assertEquals("yes", json.get("canRotateHead").asString)
        assertEquals("no", json.get("canHoldArms").asString)
        // weight/targetWeight are Int on the client but Double on the wire.
        assertEquals(92.0, json.get("weight").asDouble)
        assertEquals(82.0, json.get("targetWeight").asDouble)
    }

    @Test
    fun `the shape up date is ISO-8601 UTC and works below API 26`() {
        // java.time would throw NoClassDefFoundError here (minSdk 23, no core library desugaring),
        // so the formatting deliberately uses SimpleDateFormat.
        val payload = state.toOnboardingProfilePayload(userId = "u", completedStep = "shapeUpEventDate")
        assertEquals("2026-10-17T13:30:00Z", payload.shapeUpEventDate)
    }

    @Test
    fun `a skipped shape up date is omitted rather than sent as an epoch default`() {
        val skipped = state.copy(shapeUpEventDateSkipped = true)
        assertNull(skipped.toOnboardingProfilePayload(userId = "u", completedStep = "x").shapeUpEventDate)
    }

    @Test
    fun `emit the payload json for the backend contract test`() {
        val json = gson.toJson(
            state.toOnboardingProfilePayload(userId = "8f14e45f-ceea-467a-9c1e-2b3d4e5f6a7b", completedStep = "gender")
        )
        File(System.getProperty("java.io.tmpdir"), "android-onboarding-payload.json").writeText(json)
        println("ANDROID_PAYLOAD_JSON=$json")
        assertTrue(json.startsWith("{") && json.endsWith("}"))
    }

    private fun JsonObject.has(name: String) = this.keySet().contains(name)
}
