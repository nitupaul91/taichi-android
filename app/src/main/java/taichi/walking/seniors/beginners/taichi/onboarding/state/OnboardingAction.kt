package taichi.walking.seniors.beginners.taichi.onboarding.state

sealed interface OnboardingAction {
    data class SingleSelect(val questionId: String, val optionId: String) : OnboardingAction
    data class MultiToggle(val questionId: String, val optionId: String) : OnboardingAction
    data class HeightChange(val value: Int) : OnboardingAction
    data class WeightChange(val value: Int) : OnboardingAction
    data class TargetWeightChange(val value: Int) : OnboardingAction
    data class EmailChange(val value: String) : OnboardingAction
    object Complete : OnboardingAction
}
