package taichi.walking.seniors.beginners.taichi.ui.settings.cancellation

import com.mobteq.billing.domain.Subscription
import com.mobteq.billing.domain.repository.PurchasesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import taichi.walking.seniors.beginners.taichi.account.PlaySubscriptionManagement
import taichi.walking.seniors.beginners.taichi.ui.home.data.WorkoutPlanService
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeKind
import taichi.walking.seniors.beginners.taichi.ui.progress.data.PracticeRepository

@OptIn(ExperimentalCoroutinesApi::class)
class CancellationFlowViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val reporter = mock<CancellationFlowReporter>()
    private val practiceRepository = mock<PracticeRepository>()
    private val workoutPlanService = mock<WorkoutPlanService>()
    private val purchases = MutableStateFlow<List<Subscription>>(emptyList())
    private val purchasesRepository = mock<PurchasesRepository> { on { purchaseFlow } doReturn purchases }

    @BeforeEach
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterEach
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(playOpens: Boolean = true): CancellationFlowViewModel {
        val playSubscriptionManagement = mock<PlaySubscriptionManagement> { on { open() } doReturn playOpens }
        return CancellationFlowViewModel(
            reporter,
            practiceRepository,
            workoutPlanService,
            playSubscriptionManagement,
            purchasesRepository
        ).also { it.start() }
    }

    @Test
    fun `opening reports flow_opened and starts at the reason step`() {
        val viewModel = viewModel()

        verify(reporter).report(CancellationFlowEvent.FLOW_OPENED)
        assertEquals(CancellationStep.Reason, viewModel.state.value.step)
    }

    @Test
    fun `continue does nothing until a reason is picked`() {
        val viewModel = viewModel()

        viewModel.continueFromReason()

        verify(reporter, never()).report(CancellationFlowEvent.REASON_SELECTED, null)
        assertEquals(CancellationStep.Reason, viewModel.state.value.step)
    }

    @Test
    fun `too expensive offers the discount paywall`() {
        val viewModel = viewModel()

        viewModel.selectReason(CancellationReason.TOO_EXPENSIVE)
        viewModel.continueFromReason()

        verify(reporter).report(CancellationFlowEvent.REASON_SELECTED, CancellationReason.TOO_EXPENSIVE)
        verify(reporter).report(
            CancellationFlowEvent.RETENTION_OFFER_SHOWN,
            CancellationReason.TOO_EXPENSIVE,
            action = "discount_paywall"
        )
        assertTrue(viewModel.state.value.showDiscountPaywall)
    }

    @Test
    fun `closing the discount paywall leads to the last stop before Google Play`() {
        val viewModel = viewModel()
        viewModel.selectReason(CancellationReason.TOO_EXPENSIVE)
        viewModel.continueFromReason()

        viewModel.onDiscountPaywallFinished(purchased = false)

        val state = viewModel.state.value
        assertFalse(state.showDiscountPaywall)
        assertEquals(CancellationStep.Retention(CancellationReason.TOO_EXPENSIVE), state.step)
        assertFalse(state.isFinished)
    }

    @Test
    fun `buying the discount is reported once with the purchase and ends the flow`() {
        val viewModel = viewModel()
        viewModel.selectReason(CancellationReason.TOO_EXPENSIVE)
        viewModel.continueFromReason()
        purchases.value = listOf(
            Subscription("old-token", "GPA.1", purchaseTime = 1_000, productId = "taichi_yearly_v1"),
            Subscription("new-token", "GPA.2", purchaseTime = 2_000, productId = "taichi_monthly_v1")
        )

        viewModel.onDiscountPaywallFinished(purchased = true)
        viewModel.onDiscountPaywallFinished(purchased = true)

        verify(reporter, times(1)).report(
            CancellationFlowEvent.RETENTION_ACTION_ACCEPTED,
            CancellationReason.TOO_EXPENSIVE,
            action = "discount_purchase",
            analyticsParameters = mapOf("product_id" to "taichi_monthly_v1", "transaction_id" to "GPA.2")
        )
        assertTrue(viewModel.state.value.isFinished)
    }

    @Test
    fun `accepting gentler exercises saves intensity 1 and regenerates both visible programs`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.selectReason(CancellationReason.EXERCISES_TOO_HARD)
        viewModel.continueFromReason()

        verify(reporter).report(
            CancellationFlowEvent.RETENTION_OFFER_SHOWN,
            CancellationReason.EXERCISES_TOO_HARD,
            action = "easier_exercises"
        )
        assertEquals(CancellationStep.Retention(CancellationReason.EXERCISES_TOO_HARD), viewModel.state.value.step)

        viewModel.acceptAdjustment(CancellationReason.EXERCISES_TOO_HARD)
        assertEquals(CancellationStep.Applying, viewModel.state.value.step)
        advanceUntilIdle()

        verifyBlocking(practiceRepository) { setWorkoutIntensity("1") }
        verifyBlocking(workoutPlanService) { fetchWorkoutPlan(PracticeKind.TAI_CHI, "1") }
        verifyBlocking(workoutPlanService) { fetchWorkoutPlan(PracticeKind.WALKING, "1") }
        verify(reporter).report(
            CancellationFlowEvent.RETENTION_ACTION_ACCEPTED,
            CancellationReason.EXERCISES_TOO_HARD,
            action = "easier_exercises",
            details = "intensity_1"
        )
        assertEquals(CancellationStep.Applied(CancellationReason.EXERCISES_TOO_HARD), viewModel.state.value.step)
        assertTrue(viewModel.state.value.plansNeedRefresh)
    }

    @Test
    fun `harder exercises use intensity 5`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.selectReason(CancellationReason.EXERCISES_TOO_EASY)
        viewModel.continueFromReason()

        viewModel.acceptAdjustment(CancellationReason.EXERCISES_TOO_EASY)
        advanceUntilIdle()

        verifyBlocking(practiceRepository) { setWorkoutIntensity("5") }
        verify(reporter).report(
            CancellationFlowEvent.RETENTION_ACTION_ACCEPTED,
            CancellationReason.EXERCISES_TOO_EASY,
            action = "harder_exercises",
            details = "intensity_5"
        )
    }

    @Test
    fun `feedback is trimmed, reported only when present, and continues to Google Play`() {
        val viewModel = viewModel()
        viewModel.selectReason(CancellationReason.OTHER)
        viewModel.continueFromReason()
        assertEquals(CancellationStep.Feedback, viewModel.state.value.step)

        viewModel.updateFeedback("  Not enough time  ")
        viewModel.submitFeedback()

        verify(reporter).report(
            CancellationFlowEvent.FEEDBACK_SUBMITTED,
            CancellationReason.OTHER,
            feedback = "Not enough time"
        )
        verify(reporter).report(
            CancellationFlowEvent.CONTINUED_CANCELLATION,
            CancellationReason.OTHER,
            action = "play_management_opened"
        )
        assertTrue(viewModel.state.value.isFinished)
    }

    @Test
    fun `blank feedback is not reported`() {
        val viewModel = viewModel()
        viewModel.selectReason(CancellationReason.OTHER)
        viewModel.continueFromReason()

        viewModel.updateFeedback("   ")
        viewModel.submitFeedback()

        verify(reporter, never()).report(
            any(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            feedback = any(),
            analyticsParameters = any()
        )
    }

    @Test
    fun `when Google Play cannot be opened the support fallback is shown`() {
        val viewModel = viewModel(playOpens = false)
        viewModel.selectReason(CancellationReason.EXERCISES_TOO_HARD)
        viewModel.continueFromReason()

        viewModel.continueCancellation(CancellationReason.EXERCISES_TOO_HARD)

        verify(reporter).report(
            CancellationFlowEvent.CONTINUED_CANCELLATION,
            CancellationReason.EXERCISES_TOO_HARD,
            action = "play_management_failed"
        )
        assertTrue(viewModel.state.value.showPlayFallback)
        assertFalse(viewModel.state.value.isFinished)
    }

    @Test
    fun `leaving a decision step is reported as abandoned`() {
        val viewModel = viewModel()
        viewModel.selectReason(CancellationReason.EXERCISES_TOO_EASY)
        viewModel.continueFromReason()

        viewModel.leave(trigger = "keep_subscription")

        verify(reporter).reportAbandoned("retention", CancellationReason.EXERCISES_TOO_EASY, "keep_subscription")
        assertTrue(viewModel.state.value.isFinished)
    }

    @Test
    fun `closing after a change was applied is not an abandon`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.selectReason(CancellationReason.EXERCISES_TOO_HARD)
        viewModel.continueFromReason()
        viewModel.acceptAdjustment(CancellationReason.EXERCISES_TOO_HARD)
        advanceUntilIdle()

        viewModel.leave(trigger = "close_button")

        verify(reporter, never()).reportAbandoned(any(), anyOrNull(), any())
        assertTrue(viewModel.state.value.isFinished)
    }

    @Test
    fun `reopening resets the flow but keeps a plan refresh that is still owed`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.selectReason(CancellationReason.EXERCISES_TOO_HARD)
        viewModel.continueFromReason()
        viewModel.acceptAdjustment(CancellationReason.EXERCISES_TOO_HARD)
        advanceUntilIdle()
        viewModel.finish()

        viewModel.start()

        val state = viewModel.state.value
        assertEquals(CancellationStep.Reason, state.step)
        assertEquals(null, state.selectedReason)
        assertFalse(state.isFinished)
        assertTrue(state.plansNeedRefresh)
        verify(reporter, times(2)).report(CancellationFlowEvent.FLOW_OPENED)
    }
}
