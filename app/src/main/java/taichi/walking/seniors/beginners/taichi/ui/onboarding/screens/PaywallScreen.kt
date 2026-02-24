package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.gson.Gson
import kotlinx.coroutines.flow.map
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.onboarding.data.OnboardingPrefs
import taichi.walking.seniors.beginners.taichi.onboarding.data.taichiOnboardingDataStore
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.PrimaryButton
import taichi.walking.seniors.beginners.ui.paywall.PaywallEvents
import taichi.walking.seniors.beginners.ui.paywall.PaywallViewModel
import taichi.walking.seniors.beginners.ui.paywall.UIProduct
import java.util.Locale

private enum class PurchaseDialogState {
    Hidden, Loading, Success, Failed
}

@Composable
fun PaywallScreen(
    onClose: () -> Unit,
    onComplete: () -> Unit,
    viewModel: PaywallViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val answersJson by context.taichiOnboardingDataStore.data
        .map { prefs -> prefs[OnboardingPrefs.ONBOARDING_ANSWERS_JSON] }
        .collectAsState(initial = null)
    val products by viewModel.products.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val hasFreeTrial = !selectedProduct?.freeTrial.isNullOrBlank()
    val trialDays = selectedProduct?.freeTrial.orEmpty()
    val isYearlyProduct = selectedProduct?.productId?.contains("yearly", ignoreCase = true) == true
    val ctaTrialText = stringResource(R.string.onboarding_paywall_cta_trial, trialDays)
    val ctaContinueText = stringResource(R.string.continue_button)
    val ctaText = if (hasFreeTrial) ctaTrialText else ctaContinueText
    val trialSubtitleYearly = stringResource(R.string.onboarding_paywall_trial_then_yearly, trialDays, selectedProduct?.displayPrice.orEmpty())
    val trialSubtitleMonthly = stringResource(R.string.onboarding_paywall_trial_then_monthly, trialDays, selectedProduct?.displayPrice.orEmpty())
    val ctaSubtitle = when {
        hasFreeTrial && isYearlyProduct -> trialSubtitleYearly
        hasFreeTrial -> trialSubtitleMonthly
        else -> stringResource(R.string.onboarding_paywall_cancel_anytime)
    }

    var purchaseDialogState by remember { mutableStateOf(PurchaseDialogState.Hidden) }

    val genderId = remember(answersJson) {
        runCatching { Gson().fromJson(answersJson, OnboardingState::class.java)?.genderId }
            .getOrNull()
    }

    LaunchedEffect(Unit) {
        viewModel.eventsFlow.collect { event ->
            when (event) {
                PaywallEvents.PurchaseInProgress -> purchaseDialogState = PurchaseDialogState.Loading
                PaywallEvents.PurchaseIsAcknowledged -> purchaseDialogState = PurchaseDialogState.Success
                PaywallEvents.PurchaseNotValid -> purchaseDialogState = PurchaseDialogState.Failed
                PaywallEvents.CloseScreen -> onComplete()
                else -> Unit
            }
        }
    }

    // Purchase Status Dialog
    if (purchaseDialogState != PurchaseDialogState.Hidden) {
        PurchaseStatusDialog(
            state = purchaseDialogState,
            onOkay = {
                purchaseDialogState = PurchaseDialogState.Hidden
                onComplete()
            },
            onRetry = {
                purchaseDialogState = PurchaseDialogState.Hidden
                if (activity != null) viewModel.makePurchase(activity)
            }
        )
    }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp)
                    .padding(top = 10.dp, bottom = navBarPadding + 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PrimaryButton(
                    text = ctaText,
                    enabled = selectedProduct != null,
                    onClick = {
                        if (activity != null) viewModel.makePurchase(activity)
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = ctaSubtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_paywall_terms_of_use),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.terms_of_use_link)))
                            context.startActivity(intent)
                        }
                    )
                    Text(
                        text = stringResource(R.string.onboarding_paywall_privacy_policy),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.privacy_policy_link)))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = statusBarPadding, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                BeforeAfterHeader(genderId = genderId, onClose = onClose)
            }
            item {
                CenteredTitle()
            }
            if (products.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.onboarding_paywall_loading_offers),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(products) { product ->
                    val isYearly = product.product.productId.contains("yearly", ignoreCase = true)
                    val billedYearlyText = stringResource(R.string.onboarding_paywall_billed_yearly, product.product.displayPrice)
                    val bestValueText = stringResource(R.string.onboarding_paywall_best_value)
                    val freeTrialText = product.product.freeTrial?.let { stringResource(R.string.onboarding_paywall_free_trial_days, it) }
                    val perMonthText = stringResource(R.string.per_month)
                    PlanOption(
                        title = product.title.ifBlank { product.product.productId },
                        subtitle = if (isYearly) billedYearlyText else product.subtitleForCard(),
                        highlight = if (isYearly) bestValueText else null,
                        trialText = if (isYearly) null else freeTrialText,
                        rightMain = if (isYearly) product.monthlyBreakdownPrice() else product.product.displayPrice,
                        rightSub = perMonthText,
                        selected = selectedProduct?.productId == product.product.productId,
                        onClick = { viewModel.selectProduct(product.product) }
                    )
                }
            }
            item {
                BenefitsCard()
            }
            items(
                listOf(
                    "I've tried many exercise apps, but this is the first one designed with people my age in mind. The pace is perfect." to "— Helen, 69",
                    "After just two weeks, I noticed improved balance and less stiffness in my joints. The gentle movements are exactly what I needed." to "— Robert, 72",
                    "My doctor recommended Tai Chi for stress relief. This app made it so easy to start. I practice every day now." to "— Patricia, 75"
                )
            ) { (quote, author) ->
                TestimonialCard(quote = quote, author = author)
            }
        }
    }
}

private fun UIProduct.subtitleForCard(): String? {
    if (!product.freeTrial.isNullOrBlank()) return null
    return description.takeIf { it.isNotBlank() }
}

private fun UIProduct.monthlyBreakdownPrice(): String {
    if (product.price <= 0L) return product.displayPrice
    val monthlyPrice = (product.price / 1_000_000.0) / 12.0
    return formatWithProductCurrency(monthlyPrice, product.displayPrice)
}

private fun formatWithProductCurrency(value: Double, samplePrice: String): String {
    val sample = samplePrice.trim()
    val numberRegex = Regex("[\\d.,\\s]+")
    val numberMatch = numberRegex.find(sample) ?: return String.format(Locale.US, "%.2f", value)

    val prefix = sample.substring(0, numberMatch.range.first).trim()
    val numberPart = numberMatch.value.trim()
    val suffix = sample.substring(numberMatch.range.last + 1).trim()
    val usesCommaDecimal = numberPart.lastIndexOf(',') > numberPart.lastIndexOf('.')

    val formattedNumber = if (usesCommaDecimal) {
        String.format(Locale.US, "%.2f", value).replace('.', ',')
    } else {
        String.format(Locale.US, "%.2f", value)
    }

    return when {
        prefix.isNotEmpty() -> "$prefix$formattedNumber"
        suffix.isNotEmpty() -> "$formattedNumber $suffix"
        else -> formattedNumber
    }
}

private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}

@Composable
private fun BeforeAfterHeader(genderId: String?, onClose: () -> Unit) {
    val imageRes = if (genderId == "male") R.drawable.beforeaftermale else R.drawable.beforeafterfemale

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            modifier = Modifier
                .padding(12.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.36f))
                .clickable { onClose() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun CenteredTitle() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.onboarding_paywall_title),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 21.sp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(5) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF6B12D), modifier = Modifier.size(28.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.onboarding_paywall_trust_message),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
        )
    }
}

@Composable
private fun PlanOption(
    title: String,
    subtitle: String?,
    highlight: String? = null,
    trialText: String? = null,
    rightMain: String,
    rightSub: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(BorderStroke(2.dp, borderColor), RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .border(BorderStroke(3.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)), CircleShape)
                .padding(5.dp)
                .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 22.sp),
                    fontWeight = FontWeight.Bold
                )
                if (highlight != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF5B028))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = highlight,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 22.sp),
                    fontWeight = FontWeight.Bold
                )
            }
            if (trialText != null) {
                Text(
                    text = trialText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = rightMain,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 22.sp),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = rightSub,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }
    }
}

@Composable
private fun BenefitsCard() {
    Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BenefitLine(Icons.Default.SelfImprovement, "Unlimited access to all Tai Chi sessions")
            BenefitLine(Icons.Default.GraphicEq, "Calm voice guidance through every movement")
            BenefitLine(Icons.Default.TrendingUp, "Track your progress and celebrate milestones")
            BenefitLine(Icons.Default.Notifications, "Gentle daily reminders to keep you on track")
        }
    }
}

@Composable
private fun BenefitLine(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 18.sp),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TestimonialCard(quote: String, author: String) {
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFFCF6))
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(5) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF6B12D), modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "\"$quote\"",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 18.sp),
                textAlign = TextAlign.Center,
                fontStyle = FontStyle.Italic
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = author,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PurchaseStatusDialog(
    state: PurchaseDialogState,
    onOkay: () -> Unit,
    onRetry: () -> Unit
) {
    Dialog(
        onDismissRequest = { /* Non-dismissible during loading */ },
        properties = DialogProperties(
            dismissOnBackPress = state != PurchaseDialogState.Loading,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (state) {
                    PurchaseDialogState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = Color(0xFF0BA56B)
                        )
                        Text(
                            text = "Validating your purchase...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Please wait and don't close this screen",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                    PurchaseDialogState.Success -> {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFF0BA56B),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Welcome to Premium!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Your purchase was successful. Enjoy all premium features!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF0BA56B))
                                .clickable { onOkay() }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Okay",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    PurchaseDialogState.Failed -> {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Purchase Failed",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Something went wrong. Please try again.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF0BA56B))
                                .clickable { onRetry() }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Retry",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    else -> Unit
                }
            }
        }
    }
}
