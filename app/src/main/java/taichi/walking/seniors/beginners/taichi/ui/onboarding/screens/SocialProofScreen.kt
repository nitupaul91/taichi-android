package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.onboarding.model.OnboardingSocialProof
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.QuestionScaffold
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.RatingStars
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor

private val Gold = Color(0xFFE3B12B)
private val TextSecondary = Color(0xFF6A7B82)

/**
 * iOS `SocialProofScreen1`, the last screen before the paywall: the store rating in laurels, the
 * reviewer faces, "Thousands of active members" and three testimonials. The rating is the real
 * App Store and Google Play average ([OnboardingSocialProof]) instead of the old 4.9 / 12,000+ placeholders.
 */
@Composable
fun SocialProofScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    QuestionScaffold(
        progress = progressFor(OnboardingRoutes.SocialProof),
        title = "",
        subtitle = null,
        onBack = onBack,
        onContinue = onContinue
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.onboarding_social_proof_title),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 31.sp, lineHeight = 37.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.size(14.dp))
            SocialProofRatingHeader()
            Spacer(modifier = Modifier.size(14.dp))
            Text(
                text = stringResource(R.string.onboarding_social_proof_subtitle),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.size(10.dp))
            AvatarCluster()
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = stringResource(R.string.onboarding_social_proof_active_members),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.size(22.dp))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                TestimonialCard(
                    imageRes = R.drawable.review_1,
                    name = stringResource(R.string.onboarding_social_proof_author_1),
                    quote = stringResource(R.string.onboarding_social_proof_testimonial_1)
                )
                TestimonialCard(
                    imageRes = R.drawable.review_2,
                    name = stringResource(R.string.onboarding_social_proof_author_2),
                    quote = stringResource(R.string.onboarding_social_proof_testimonial_2)
                )
                TestimonialCard(
                    imageRes = R.drawable.review_3,
                    name = stringResource(R.string.onboarding_social_proof_author_3),
                    quote = stringResource(R.string.onboarding_social_proof_testimonial_3)
                )
            }
        }
    }
}

@Composable
private fun SocialProofRatingHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_laurel_left),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Gold),
            modifier = Modifier.size(width = 34.dp, height = 68.dp)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = OnboardingSocialProof.formattedRating(),
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 40.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                RatingStars(
                    filled = OnboardingSocialProof.filledStars,
                    starSize = 16.dp,
                    color = Gold,
                    spacing = 2.dp
                )
            }
            Text(
                text = stringResource(R.string.onboarding_social_proof_rating_caption),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
        Image(
            painter = painterResource(id = R.drawable.ic_laurel_right),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Gold),
            modifier = Modifier.size(width = 34.dp, height = 68.dp)
        )
    }
}

@Composable
private fun AvatarCluster() {
    val avatars = listOf(R.drawable.review_1, R.drawable.review_2, R.drawable.review_3)
    // Overlapping faces, the first on top (iOS HStack spacing -10).
    Row(horizontalArrangement = Arrangement.spacedBy((-10).dp)) {
        avatars.forEachIndexed { index, avatar ->
            Image(
                painter = painterResource(id = avatar),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .zIndex((avatars.size - index).toFloat())
                    .size(58.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .border(3.dp, Color.White, CircleShape)
            )
        }
    }
}

@Composable
private fun TestimonialCard(
    imageRes: Int,
    name: String,
    quote: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE7ECEE), RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Box(modifier = Modifier.padding(start = 12.dp)) {
                    RatingStars(filled = 5.0, starSize = 13.dp, color = Gold, spacing = 3.dp)
                }
            }
            Text(
                text = quote,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 22.sp),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
