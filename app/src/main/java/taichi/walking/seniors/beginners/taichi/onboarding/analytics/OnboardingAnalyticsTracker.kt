package taichi.walking.seniors.beginners.taichi.onboarding.analytics

import android.os.Bundle
import android.os.SystemClock
import com.mobteq.analytics.FirebaseAnalytics
import com.mobteq.analytics.TikTokAnalyticsTracker
import dagger.hilt.android.scopes.ActivityRetainedScoped
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes
import javax.inject.Inject

/**
 * The onboarding funnel events, with iOS `OnboardingRootView`'s names and parameters so one
 * dashboard reads both platforms. Retained across configuration changes, so a rotation neither
 * re-reports the current screen nor restarts the timers.
 *
 * Firebase on Android takes no booleans, so flags go out as 1 / 0, which is what the iOS SDK stores
 * for a Bool.
 */
@ActivityRetainedScoped
class OnboardingAnalyticsTracker @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val tikTokAnalyticsTracker: TikTokAnalyticsTracker,
) {
    // Analytics-only bookkeeping; none of this drives the flow.
    var lastTrackedStep: OnboardingRoutes? = null
        private set
    private var stepEnteredAt = 0L
    private var onboardingStartedAt: Long? = null
    private var answeredStepCount = 0
    private var hasTrackedOnboardingCompleted = false

    /**
     * `onboarding_view_<n>`, n being the 1-based position in this user's step list. Reported once
     * per entry into a step.
     */
    fun trackScreenView(step: OnboardingRoutes, navigationSteps: List<OnboardingRoutes>, direction: String) {
        if (lastTrackedStep == step) return
        lastTrackedStep = step

        val now = SystemClock.elapsedRealtime()
        val secondsOnPreviousStep = if (onboardingStartedAt == null) 0 else ((now - stepEnteredAt) / 1000).toInt()
        if (onboardingStartedAt == null) onboardingStartedAt = now
        stepEnteredAt = now

        val stepIndex = navigationSteps.indexOf(step).coerceAtLeast(0) + 1
        log(
            "$EVENT_VIEW_PREFIX$stepIndex",
            "step_name" to step.analyticsStepName,
            "step_index" to stepIndex,
            "total_steps" to navigationSteps.size,
            "direction" to direction,
            "seconds_on_previous_step" to secondsOnPreviousStep
        )
    }

    /** `onboarding_answer_submitted`, when the user moves forward from a question. */
    fun trackAnswerSubmitted(
        step: OnboardingRoutes,
        navigationSteps: List<OnboardingRoutes>,
        summary: OnboardingAnswerSummary,
        autoContinued: Boolean,
        practiceKind: String,
        answerPosition: Int?
    ) {
        answeredStepCount += 1
        val params = mutableListOf<Pair<String, Any>>(
            "step_name" to step.analyticsStepName,
            "step_index" to navigationSteps.indexOf(step).coerceAtLeast(0) + 1,
            "answer" to summary.answer,
            "answer_count" to summary.count,
            "is_default" to summary.isDefault,
            "auto_continued" to autoContinued,
            "practice_kind" to practiceKind
        )
        // The discovery-source rows are shuffled per user; the row the answer sat on measures position bias.
        answerPosition?.let { params += "answer_position" to it }
        log(EVENT_ANSWER_SUBMITTED, *params.toTypedArray())
    }

    /**
     * `onboarding_back_tap`. [source] is Android's addition: "button" for the on-screen arrow (the
     * only way back on iOS), "system" for the system back gesture.
     */
    fun trackBackTap(
        from: OnboardingRoutes,
        to: OnboardingRoutes,
        navigationSteps: List<OnboardingRoutes>,
        source: String
    ) {
        log(
            EVENT_BACK_TAP,
            "from_step" to from.analyticsStepName,
            "to_step" to to.analyticsStepName,
            "from_index" to navigationSteps.indexOf(from).coerceAtLeast(0) + 1,
            "source" to source
        )
    }

    /** `onboarding_completed`, once, on reaching the paywall step (as iOS reports it). */
    fun trackOnboardingCompleted(practiceKind: String) {
        if (hasTrackedOnboardingCompleted) return
        hasTrackedOnboardingCompleted = true
        val totalSeconds = onboardingStartedAt?.let { ((SystemClock.elapsedRealtime() - it) / 1000).toInt() } ?: 0
        log(
            EVENT_COMPLETED,
            "steps_answered" to answeredStepCount,
            "total_seconds" to totalSeconds,
            "practice_kind" to practiceKind
        )
    }

    /**
     * Leaving the onboarding paywall (closed or purchased): TikTok's `onboarding_complete`
     * conversion. Firebase's `onboarding_completed` is reported on reaching the paywall instead,
     * by [trackOnboardingCompleted], as on iOS.
     */
    fun trackOnboardingComplete() {
        tikTokAnalyticsTracker.trackCustomEvent("onboarding_complete")
    }

    /** `onboarding_plan_loading_done`, when the plan-loading screen hands over. */
    fun trackPlanLoadingDone(cta: String) {
        val secondsOnScreen = if (lastTrackedStep == OnboardingRoutes.Loading) {
            ((SystemClock.elapsedRealtime() - stepEnteredAt) / 1000).toInt()
        } else {
            0
        }
        log(EVENT_PLAN_LOADING_DONE, "seconds_on_screen" to secondsOnScreen, "cta" to cta)
    }

    /** `notification_permission_result` from the onboarding notification screen. */
    fun trackNotificationPermissionResult(
        action: String,
        promptShown: Boolean,
        authorized: Boolean,
        previousStatus: String
    ) {
        log(
            EVENT_NOTIFICATION_PERMISSION_RESULT,
            "context" to "onboarding",
            "action" to action,
            "prompt_shown" to promptShown,
            "authorized" to authorized,
            "previous_status" to previousStatus
        )
    }

    private fun log(name: String, vararg params: Pair<String, Any>) {
        val bundle = Bundle()
        params.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putLong(key, value.toLong())
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Boolean -> bundle.putLong(key, if (value) 1L else 0L)
                else -> bundle.putString(key, value.toString())
            }
        }
        firebaseAnalytics.logEvent(name, bundle)
    }

    private companion object {
        const val EVENT_VIEW_PREFIX = "onboarding_view_"
        const val EVENT_ANSWER_SUBMITTED = "onboarding_answer_submitted"
        const val EVENT_BACK_TAP = "onboarding_back_tap"
        const val EVENT_COMPLETED = "onboarding_completed"
        const val EVENT_PLAN_LOADING_DONE = "onboarding_plan_loading_done"
        const val EVENT_NOTIFICATION_PERMISSION_RESULT = "notification_permission_result"
    }
}
