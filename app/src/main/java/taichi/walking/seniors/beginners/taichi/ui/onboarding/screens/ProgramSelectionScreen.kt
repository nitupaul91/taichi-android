package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.onboarding.model.OnboardingProgramSelection
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.QuestionScaffold
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor

private data class ProgramOption(
    val selection: OnboardingProgramSelection,
    @StringRes val title: Int,
    @StringRes val subtitle: Int,
    @DrawableRes val image: Int?
)

/** iOS `programOptions`, in the same order. */
private val programOptions = listOf(
    ProgramOption(
        OnboardingProgramSelection.TAI_CHI,
        R.string.onboarding_program_tai_chi_title,
        R.string.onboarding_program_tai_chi_subtitle,
        R.drawable.program_tai_chi
    ),
    ProgramOption(
        OnboardingProgramSelection.WALKING,
        R.string.onboarding_program_walking_title,
        R.string.onboarding_program_walking_subtitle,
        R.drawable.program_walking
    ),
    ProgramOption(
        OnboardingProgramSelection.CHAIR,
        R.string.onboarding_program_chair_title,
        R.string.onboarding_program_chair_subtitle,
        R.drawable.program_chair
    ),
    ProgramOption(
        OnboardingProgramSelection.LET_US_DECIDE,
        R.string.onboarding_program_let_us_decide_title,
        R.string.onboarding_program_let_us_decide_subtitle,
        null
    )
)

private val CardBackground = Color(0xFFF0F3F5)
private val CardBorder = Color(0xFFE8EEF0)
private val TextSecondary = Color(0xFF6A7B82)

/**
 * iOS `ProgramSelectionScreen` (behind `onboarding_program_selection_enabled`): tai chi, walking,
 * chair or "Let Us Decide". The choice is held on the screen and applied on Continue, as on iOS.
 */
@Composable
fun ProgramSelectionScreen(
    state: OnboardingState,
    onApply: (OnboardingProgramSelection) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    var selectedId by rememberSaveable { mutableStateOf(state.programSelection) }
    val selected = OnboardingProgramSelection.fromId(selectedId)

    val continueWithSelection = {
        selected?.let(onApply)
        onContinue()
    }

    QuestionScaffold(
        progress = progressFor(OnboardingRoutes.ProgramSelection),
        title = stringResource(R.string.onboarding_program_title),
        subtitle = stringResource(R.string.onboarding_program_subtitle),
        onBack = onBack,
        continueEnabled = selected != null,
        onContinue = continueWithSelection
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            programOptions.forEach { option ->
                ProgramCard(
                    option = option,
                    isSelected = selected == option.selection,
                    onClick = { selectedId = option.selection.id }
                )
            }
        }
    }
}

@Composable
private fun ProgramCard(
    option: ProgramOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isSelected) 6.dp else 2.dp,
                shape = shape,
                ambientColor = if (isSelected) primary.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.03f),
                spotColor = if (isSelected) primary.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.03f)
            )
            .clip(shape)
            .background(if (isSelected) Color.White else CardBackground)
            .background(if (isSelected) primary.copy(alpha = 0.12f) else Color.Transparent)
            .border(BorderStroke(2.dp, if (isSelected) primary else CardBorder), shape)
            .clickable(onClick = onClick)
            .semantics { selected = isSelected }
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(option.title),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = stringResource(option.subtitle),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        val thumbnailShape = RoundedCornerShape(12.dp)
        if (option.image != null) {
            Image(
                painter = painterResource(id = option.image),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(thumbnailShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(thumbnailShape)
                    .background(primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = primary,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}
