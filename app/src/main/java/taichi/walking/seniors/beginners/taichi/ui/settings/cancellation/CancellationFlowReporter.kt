package taichi.walking.seniors.beginners.taichi.ui.settings.cancellation

import com.google.gson.annotations.SerializedName
import com.sageai.id.IDService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import taichi.walking.seniors.beginners.taichi.ui.settings.screen.SettingsAnalyticsTracker
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Every step of the in-app cancellation flow, reported to the backend so retention offers can be
 * evaluated. Raw values are the wire format shared with iOS — keep them stable.
 */
enum class CancellationFlowEvent(val rawValue: String) {
    FLOW_OPENED("flow_opened"),
    REASON_SELECTED("reason_selected"),
    RETENTION_OFFER_SHOWN("retention_offer_shown"),
    RETENTION_ACTION_ACCEPTED("retention_action_accepted"),
    CONTINUED_CANCELLATION("continued_cancellation"),
    FEEDBACK_SUBMITTED("feedback_submitted")
}

interface CancellationFlowApi {

    /** Shared with the iOS app; the `platform` field tells the two apart. */
    @POST("cancellation/iOS/event")
    suspend fun recordEvent(@Body body: CancellationFlowEventBody): Response<Unit>
}

// @SerializedName on every field so R8 cannot rename the keys Gson sends.
data class CancellationFlowEventBody(
    @SerializedName("userId") val userId: String,
    @SerializedName("event") val event: String,
    @SerializedName("reason") val reason: String?,
    @SerializedName("action") val action: String?,
    @SerializedName("details") val details: String?,
    @SerializedName("feedback") val feedback: String?,
    @SerializedName("platform") val platform: String,
    @SerializedName("clientTimestamp") val clientTimestamp: String
)

/**
 * Fire-and-forget reporting for the cancellation flow, mirroring the iOS `CancellationFlowService`.
 * Nothing in the UI waits on this; a failed report must never block a user from cancelling.
 */
@Singleton
class CancellationFlowReporter @Inject constructor(
    private val api: CancellationFlowApi,
    private val idService: IDService,
    private val analytics: SettingsAnalyticsTracker
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun report(
        event: CancellationFlowEvent,
        reason: CancellationReason? = null,
        action: String? = null,
        details: String? = null,
        feedback: String? = null,
        analyticsParameters: Map<String, String> = emptyMap()
    ) {
        val trimmedFeedback = feedback?.trim().orEmpty()
        val clientTimestamp = iso8601Utc()

        scope.launch {
            runCatching {
                api.recordEvent(
                    CancellationFlowEventBody(
                        userId = idService.getUserID(),
                        event = event.rawValue,
                        reason = reason?.rawValue,
                        action = action,
                        details = details,
                        feedback = trimmedFeedback.ifEmpty { null },
                        platform = PLATFORM,
                        clientTimestamp = clientTimestamp
                    )
                )
            }.onFailure { Timber.w(it, "Cancellation flow event %s not reported", event.rawValue) }
        }

        // Mirrored locally so the funnel is visible in the analytics dashboards too.
        val parameters = linkedMapOf<String, Any>(PARAM_EVENT to event.rawValue)
        reason?.let { parameters[PARAM_REASON] = it.rawValue }
        action?.let { parameters[PARAM_ACTION] = it }
        details?.let { parameters[PARAM_DETAILS] = it }
        // The feedback text itself stays off the analytics pipeline; only its presence and size go.
        parameters[PARAM_HAS_FEEDBACK] = trimmedFeedback.isNotEmpty()
        parameters[PARAM_FEEDBACK_LENGTH] = trimmedFeedback.length
        analyticsParameters.forEach { (key, value) -> if (key !in parameters) parameters[key] = value }
        analytics.trackCancellationFlow(parameters)
    }

    /** Analytics only (not reported to the backend): the user left the flow without cancelling. */
    fun reportAbandoned(step: String, reason: CancellationReason?, trigger: String) {
        analytics.trackCancellationFlow(
            linkedMapOf(
                PARAM_EVENT to EVENT_FLOW_ABANDONED,
                PARAM_STEP to step,
                PARAM_REASON to (reason?.rawValue ?: "none"),
                PARAM_TRIGGER to trigger
            )
        )
    }

    /** Same shape as iOS `ISO8601DateFormatter` output; java.time would need API 26. */
    private fun iso8601Utc(): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date())

    private companion object {
        const val PLATFORM = "android"

        const val EVENT_FLOW_ABANDONED = "flow_abandoned"

        const val PARAM_EVENT = "event"
        const val PARAM_REASON = "reason"
        const val PARAM_ACTION = "action"
        const val PARAM_DETAILS = "details"
        const val PARAM_HAS_FEEDBACK = "has_feedback"
        const val PARAM_FEEDBACK_LENGTH = "feedback_length"
        const val PARAM_STEP = "step"
        const val PARAM_TRIGGER = "trigger"
    }
}
