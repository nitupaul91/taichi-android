package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.QuestionScaffold
import taichi.walking.seniors.beginners.taichi.onboarding.ui.util.progressFor

@Composable
fun SocialProofScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    QuestionScaffold(
        progress = progressFor(OnboardingRoutes.SocialProof),
        title = stringResource(R.string.onboarding_social_proof_title),
        subtitle = stringResource(R.string.onboarding_social_proof_subtitle),
        onBack = onBack,
        onContinue = onContinue
    ) {
        SocialProofHeader()
        Spacer(modifier = Modifier.size(20.dp))
        TestimonialCard(
            imageRes = R.drawable.review_1,
            name = stringResource(R.string.onboarding_social_proof_author_1),
            quote = stringResource(R.string.onboarding_social_proof_testimonial_1)
        )
        Spacer(modifier = Modifier.size(12.dp))
        TestimonialCard(
            imageRes = R.drawable.review_2,
            name = stringResource(R.string.onboarding_social_proof_author_2),
            quote = stringResource(R.string.onboarding_social_proof_testimonial_2)
        )
        Spacer(modifier = Modifier.size(12.dp))
        TestimonialCard(
            imageRes = R.drawable.review_3,
            name = stringResource(R.string.onboarding_social_proof_author_3),
            quote = stringResource(R.string.onboarding_social_proof_testimonial_3)
        )
    }
}

@Composable
private fun SocialProofHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(5) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color(0xFFE3B12B)
                )
            }
        }
        Text(
            text = stringResource(R.string.onboarding_social_proof_members),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TestimonialCard(
    imageRes: Int,
    name: String,
    quote: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = quote,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
