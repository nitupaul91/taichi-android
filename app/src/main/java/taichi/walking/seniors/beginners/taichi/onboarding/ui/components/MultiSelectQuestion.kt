package taichi.walking.seniors.beginners.taichi.onboarding.ui.components

import taichi.walking.seniors.beginners.taichi.onboarding.model.OnboardingOption
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import taichi.walking.seniors.beginners.taichi.onboarding.ui.theme.Dimens

@Composable
fun MultiSelectQuestion(
    options: List<OnboardingOption>,
    selectedIds: Set<String>,
    onToggle: (OnboardingOption) -> Unit
) {
    Column {
        options.forEach { option ->
            OptionCard(
                title = option.title,
                subtitle = option.subtitle,
                icon = option.icon,
                selected = selectedIds.contains(option.id),
                onClick = { onToggle(option) }
            )
            Spacer(modifier = Modifier.height(Dimens.itemSpacing))
        }
    }
}
