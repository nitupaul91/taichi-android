package taichi.walking.seniors.beginners.taichi.ui.onboarding.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobteq.billing.domain.Product
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.taichi.onboarding.model.OnboardingSocialProof
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingRoutes
import taichi.walking.seniors.beginners.taichi.onboarding.state.OnboardingState
import taichi.walking.seniors.beginners.taichi.onboarding.ui.components.PrimaryButton
import taichi.walking.seniors.beginners.ui.paywall.CachedRemotePaywall
import taichi.walking.seniors.beginners.ui.paywall.PaywallSource
import taichi.walking.seniors.beginners.ui.paywall.PaywallWebAction
import taichi.walking.seniors.beginners.ui.paywall.PaywallWebIntroOffer
import taichi.walking.seniors.beginners.taichi.onboarding.data.toOnboardingProfilePayload
import taichi.walking.seniors.beginners.ui.paywall.PaywallWebStorePayload
import taichi.walking.seniors.beginners.ui.paywall.PaywallWebStoreProduct
import taichi.walking.seniors.beginners.ui.paywall.PaywallEvents
import taichi.walking.seniors.beginners.ui.paywall.RemotePaywallVariant
import taichi.walking.seniors.beginners.ui.paywall.RemotePaywallWebView
import taichi.walking.seniors.beginners.ui.paywall.PaywallViewModel
import taichi.walking.seniors.beginners.ui.paywall.TrialTimelineDetails
import taichi.walking.seniors.beginners.ui.paywall.TrialTimelineMode
import taichi.walking.seniors.beginners.ui.paywall.TrialTimelineScreen
import taichi.walking.seniors.beginners.ui.paywall.UIProduct
import java.util.Locale

private enum class PurchaseDialogState {
    Hidden, Loading, Success, Failed
}

@Composable
fun PaywallScreen(
    onClose: () -> Unit,
    onComplete: () -> Unit,
    paywallVariant: RemotePaywallVariant = RemotePaywallVariant.PRIMARY,
    /** Which surface opened the paywall, sent as `source` with every paywall event. */
    source: String = PaywallSource.UNKNOWN,
    /** Refinement of [source], such as the notification campaign, sent as `source_detail`. */
    sourceDetail: String? = null,
    /** Called before [onComplete] when a purchase here started a free trial (iOS onTrialPurchased). */
    onTrialPurchased: ((TrialTimelineDetails) -> Unit)? = null,
    viewModel: PaywallViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val products by viewModel.products.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val onboardingState by viewModel.onboardingState.collectAsState()
    val userId by viewModel.userId.collectAsState()
    val isPurchaseInProgress by viewModel.isPurchaseInProgress.collectAsState()
    val isRestoring by viewModel.isRestoring.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val cachedVariantPaywall by remember(paywallVariant) {
        viewModel.observeCachedPaywall(paywallVariant)
    }.collectAsState()
    val cachedPrimaryPaywall by remember {
        viewModel.observeCachedPaywall(RemotePaywallVariant.PRIMARY)
    }.collectAsState()
    // As on iOS, a variant that is not cached yet falls back to the main web paywall, then native.
    val cachedRemotePaywall = cachedVariantPaywall ?: cachedPrimaryPaywall
    val purchaseRecovery by viewModel.purchaseRecovery.collectAsState()

    var purchaseDialogState by remember { mutableStateOf(PurchaseDialogState.Hidden) }
    val currentOnTrialPurchased by rememberUpdatedState(onTrialPurchased)

    val genderId = onboardingState?.genderId
    val useRemotePaywall = cachedRemotePaywall != null

    LaunchedEffect(Unit) {
        viewModel.onPaywallShown(
            source = source,
            sourceDetail = sourceDetail,
            renderer = if (useRemotePaywall) "web" else "native",
            paywallVariant = cachedRemotePaywall?.analyticsVariantName() ?: "native"
        )
    }
    val storePayload = remember(
        products,
        selectedProduct,
        onboardingState,
        userId,
        errorMessage,
        isPurchaseInProgress,
        isRestoring
    ) {
        buildRemotePaywallStorePayload(
            products = products,
            selectedProduct = selectedProduct,
            onboardingState = onboardingState,
            userId = userId,
            errorMessage = errorMessage,
            isPurchaseInProgress = isPurchaseInProgress,
            isRestoring = isRestoring
        )
    }

    LaunchedEffect(useRemotePaywall) {
        viewModel.eventsFlow.collect { event ->
            when (event) {
                PaywallEvents.PurchaseInProgress -> {
                    if (!useRemotePaywall) {
                        purchaseDialogState = PurchaseDialogState.Loading
                    }
                }
                PaywallEvents.PurchaseIsAcknowledged -> {
                    if (!useRemotePaywall) {
                        purchaseDialogState = PurchaseDialogState.Success
                    }
                }
                PaywallEvents.PurchaseNotValid -> {
                    if (!useRemotePaywall) {
                        purchaseDialogState = PurchaseDialogState.Failed
                    }
                }
                is PaywallEvents.TrialPurchased -> currentOnTrialPurchased?.invoke(event.details)
                PaywallEvents.CloseScreen -> onComplete()
                else -> Unit
            }
        }
    }

    if (!useRemotePaywall && purchaseDialogState != PurchaseDialogState.Hidden) {
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

    Box(modifier = Modifier.fillMaxSize()) {
        val remotePaywall = cachedRemotePaywall
        if (remotePaywall != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(top = statusBarPadding, bottom = navBarPadding)
            ) {
                RemotePaywallWebView(
                    paywall = remotePaywall,
                    storePayload = storePayload,
                    onAction = { action ->
                        when (action) {
                            PaywallWebAction.Close -> {
                                viewModel.onCloseRequested()
                                onClose()
                            }
                            PaywallWebAction.Restore -> viewModel.restorePurchases()
                            is PaywallWebAction.Purchase -> {
                                if (activity != null) {
                                    viewModel.makePurchase(activity, action.productId)
                                }
                            }
                            is PaywallWebAction.SelectProduct -> viewModel.selectProductById(action.productId)
                            is PaywallWebAction.OpenUrl -> {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(action.url))
                                context.startActivity(intent)
                            }
                            is PaywallWebAction.TrackPersonalization -> {
                                viewModel.trackPersonalization(action.variants)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            NativePaywall(
                products = products,
                selectedProduct = selectedProduct,
                genderId = genderId,
                statusBarPadding = statusBarPadding,
                navBarPadding = navBarPadding,
                onPurchase = { if (activity != null) viewModel.makePurchase(activity) },
                onSelectProduct = viewModel::selectProduct,
                onClose = {
                    viewModel.onCloseRequested()
                    onClose()
                }
            )
        }

        // Drawn over the paywall, which stays composed, so the web paywall is not reloaded.
        purchaseRecovery?.let { recovery ->
            LaunchedEffect(recovery) { viewModel.onPurchaseRecoveryViewed(recovery) }
            BackHandler { viewModel.onPurchaseRecoveryDismissed() }
            TrialTimelineScreen(
                details = recovery.details,
                mode = TrialTimelineMode.CANCELLATION_RECOVERY,
                reminderMessaging = recovery.reminderMessaging,
                onPrimaryAction = { if (activity != null) viewModel.onPurchaseRecoveryRetry(activity) },
                onSecondaryAction = viewModel::onPurchaseRecoveryDismissed
            )
        }
    }
}

/**
 * The native fallback, laid out like the HTML paywall (iOS f2cba97): title, store rating badge,
 * plan cards with the billed amount and the per-month figure, benefits, real reviews, then the
 * button with the reassurance line above the subscription terms.
 */
@Composable
private fun NativePaywall(
    products: List<UIProduct>,
    selectedProduct: Product?,
    genderId: String?,
    statusBarPadding: Dp,
    navBarPadding: Dp,
    onPurchase: () -> Unit,
    onSelectProduct: (Product) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val trialDays = selectedProduct?.freeTrial?.takeIf { it.isNotBlank() }
    val ctaText = if (trialDays != null) {
        stringResource(R.string.onboarding_paywall_cta_trial, trialDays)
    } else {
        stringResource(R.string.continue_button)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 24.dp)
                    .padding(top = 10.dp, bottom = navBarPadding + 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PrimaryButton(
                    text = ctaText,
                    enabled = selectedProduct != null,
                    onClick = onPurchase
                )
                Spacer(modifier = Modifier.height(14.dp))
                ReassuranceLine(hasFreeTrial = trialDays != null)
                selectedProduct?.let { product ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subscriptionTermsText(product, trialDays),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = PaywallTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_paywall_terms_of_use),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = PaywallTextMuted,
                        modifier = Modifier
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.terms_of_use_link)))
                                context.startActivity(intent)
                            }
                            .padding(vertical = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.onboarding_paywall_privacy_policy),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = PaywallTextMuted,
                        modifier = Modifier
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.privacy_policy_link)))
                                context.startActivity(intent)
                            }
                            .padding(vertical = 8.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = statusBarPadding + 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                BeforeAfterHeader(
                    genderId = genderId,
                    onClose = onClose
                )
            }
            item {
                TitleSection()
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (products.isEmpty()) {
                        Text(
                            text = stringResource(R.string.onboarding_paywall_loading_offers),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                    products.forEach { product ->
                        PlanCard(
                            product = product,
                            selected = selectedProduct?.productId == product.product.productId,
                            onClick = { onSelectProduct(product.product) }
                        )
                    }
                }
            }
            item {
                BenefitsCard()
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Real App Store reviews (US/RO storefronts, pulled 30 Oct 2026), the same set as
                    // the iOS paywall; Android has no reviews of its own to quote yet.
                    ReviewCard(stringResource(R.string.paywall_review_quote_1), "rosynise")
                    ReviewCard(stringResource(R.string.paywall_review_quote_2), "Mermaidlady23")
                    ReviewCard(stringResource(R.string.paywall_review_quote_3), "Becks591")
                }
            }
        }
    }
}

// iOS SeniorDesign colours used by the native paywall.
private val PaywallTextPrimary = Color(0xFF2F3C42)
private val PaywallTextSecondary = Color(0xFF6A7B82)
private val PaywallTextMuted = Color(0xFF9AA8AD)
private val PaywallCardBorder = Color(0xFFE8EEF0)
private val PaywallRadioBorder = Color(0xFFD9E5DE)
private val PaywallBestValue = Color(0xFFF5A623)


/** The plan's billing period, from Play's subscription period (product ids as a fallback). */
private enum class PlanPeriod { YEAR, MONTH, WEEK }

private val Product.planPeriod: PlanPeriod
    get() = when (subscriptionPeriodUnit) {
        "year" -> PlanPeriod.YEAR
        "month" -> PlanPeriod.MONTH
        "week" -> PlanPeriod.WEEK
        else -> if (productId.contains("yearly", ignoreCase = true)) PlanPeriod.YEAR else PlanPeriod.MONTH
    }

/** Per-month equivalent of a yearly price (iOS PaywallProductCard.pricePerMonth). */
private fun Product.monthlyBreakdownPrice(): String {
    if (price <= 0L) return displayPrice
    return formatWithProductCurrency((price / 1_000_000.0) / 12.0, displayPrice)
}

/** "7-day free trial, then €49.99/year. Auto-renews." or "€9.99/month. Cancel anytime." */
@Composable
private fun subscriptionTermsText(product: Product, trialDays: String?): String {
    val price = product.displayPrice
    return if (trialDays != null) {
        when (product.planPeriod) {
            PlanPeriod.YEAR -> stringResource(R.string.onboarding_paywall_trial_then_yearly, trialDays, price)
            PlanPeriod.MONTH -> stringResource(R.string.onboarding_paywall_trial_then_monthly, trialDays, price)
            PlanPeriod.WEEK -> stringResource(R.string.paywall_trial_then_weekly, trialDays, price)
        }
    } else {
        when (product.planPeriod) {
            PlanPeriod.YEAR -> stringResource(R.string.paywall_recurring_yearly, price)
            PlanPeriod.MONTH -> stringResource(R.string.paywall_recurring_monthly, price)
            PlanPeriod.WEEK -> stringResource(R.string.paywall_recurring_weekly, price)
        }
    }
}

/** File name of the rendered HTML (identifier is "<sourceUrl>#<cachedAt>"), as iOS reports it. */
private fun CachedRemotePaywall.analyticsVariantName(): String {
    val withoutSuffix = identifier.split('#', '?').first()
    return withoutSuffix.substringAfterLast('/').take(100).ifEmpty { "unknown" }
}

private fun buildRemotePaywallStorePayload(
    products: List<UIProduct>,
    selectedProduct: Product?,
    onboardingState: OnboardingState?,
    userId: String?,
    errorMessage: String?,
    isPurchaseInProgress: Boolean,
    isRestoring: Boolean
): PaywallWebStorePayload {
    return PaywallWebStorePayload(
        products = products.map { it.toPaywallWebStoreProduct(selectedProduct?.productId) },
        heroGender = onboardingState?.genderId ?: "female",
        isTaiChiWalkingFlow = true,
        onboardingProfile = onboardingState?.toOnboardingProfilePayload(
            userId = userId,
            completedStep = OnboardingRoutes.Paywall.analyticsStepName
        ),
        selectedProductId = selectedProduct?.productId,
        errorMessage = errorMessage,
        isPurchaseInProgress = isPurchaseInProgress,
        isRestoring = isRestoring
    )
}

private fun UIProduct.toPaywallWebStoreProduct(selectedProductId: String?): PaywallWebStoreProduct {
    val trialDays = product.freeTrial?.takeIf { it.isNotBlank() }
    val introOfferPaymentMode = product.introOfferPaymentMode
    val introOfferPeriodUnit = product.introOfferPeriodUnit
    val introOfferPeriodValue = product.introOfferPeriodValue
    val introOffer = if (introOfferPaymentMode != null &&
        introOfferPeriodUnit != null &&
        introOfferPeriodValue != null
    ) {
        PaywallWebIntroOffer(
            paymentMode = introOfferPaymentMode,
            periodUnit = introOfferPeriodUnit,
            periodValue = introOfferPeriodValue
        )
    } else {
        null
    }

    return PaywallWebStoreProduct(
        id = product.productId,
        title = title.ifBlank { null },
        description = description.ifBlank { null },
        displayPrice = product.displayPrice,
        billingPeriodLabel = billingPeriod,
        freeTrialDays = trialDays,
        trialDisplayText = trialDays?.let { "$it-day free trial" },
        subscriptionPeriodUnit = product.subscriptionPeriodUnit,
        subscriptionPeriodValue = product.subscriptionPeriodValue,
        introOffer = introOffer,
        isIntroOfferEligible = introOffer != null,
        isSelected = product.productId == selectedProductId
    )
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
                contentDescription = stringResource(R.string.close),
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun TitleSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.onboarding_paywall_title),
            fontSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Bold,
            color = PaywallTextPrimary,
            textAlign = TextAlign.Center
        )
        RatingBadge()
    }
}

/** Store rating badge, as on the HTML paywall (iOS: "Rated 4.7 on the App Store"). */
@Composable
private fun RatingBadge() {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(Brush.linearGradient(listOf(Color(0xFFFBF0C8), Color(0xFFF2DC93))))
            .border(1.dp, Color(0xFFE3C66A), shape)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFFC99A2E),
            modifier = Modifier.size(24.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(
                    R.string.paywall_rating_badge_title,
                    OnboardingSocialProof.formattedRating()
                ).uppercase(),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.6.sp,
                color = Color(0xFF4A3A0E)
            )
            OnboardingSocialProof.formattedRatingsCount().let { count ->
                Text(
                    text = stringResource(R.string.paywall_rating_badge_count, count).uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = Color(0xFF7A6424)
                )
            }
        }
    }
}

/**
 * One plan (iOS PaywallProductCard): the billed amount on the left, at least as prominent as the
 * per-month figure on the right, and the trial length when the plan has one.
 */
@Composable
private fun PlanCard(
    product: UIProduct,
    selected: Boolean,
    onClick: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val storeProduct = product.product
    val period = storeProduct.planPeriod
    val price = storeProduct.displayPrice
    val shape = RoundedCornerShape(16.dp)
    val billedText = when (period) {
        PlanPeriod.YEAR -> stringResource(R.string.paywall_billed_annually, price)
        PlanPeriod.MONTH -> stringResource(R.string.paywall_billed_monthly, price)
        PlanPeriod.WEEK -> stringResource(R.string.paywall_billed_weekly, price)
    }
    val periodPrice = if (period == PlanPeriod.YEAR) storeProduct.monthlyBreakdownPrice() else price
    val periodCaption = if (period == PlanPeriod.WEEK) {
        stringResource(R.string.paywall_per_week)
    } else {
        stringResource(R.string.per_month)
    }
    val trialDays = storeProduct.freeTrial?.takeIf { it.isNotBlank() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (selected) 6.dp else 3.dp,
                shape = shape,
                ambientColor = if (selected) primary.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.04f),
                spotColor = if (selected) primary.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.04f)
            )
            .clip(shape)
            .background(Color.White)
            .border(if (selected) 2.dp else 1.dp, if (selected) primary else PaywallCardBorder, shape)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .border(2.dp, if (selected) primary else PaywallRadioBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(primary, CircleShape)
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = product.title.ifBlank { storeProduct.productId },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = PaywallTextPrimary
                )
                if (period == PlanPeriod.YEAR) {
                    Text(
                        text = stringResource(R.string.onboarding_paywall_best_value),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.3.sp,
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PaywallBestValue)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
            Text(
                text = billedText,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PaywallTextPrimary
            )
            if (trialDays != null) {
                Text(
                    text = stringResource(R.string.onboarding_paywall_free_trial_days, trialDays),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primary
                )
            }
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = periodPrice,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = PaywallTextPrimary,
                maxLines = 1
            )
            Text(
                text = periodCaption,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = PaywallTextSecondary
            )
        }
    }
}

@Composable
private fun BenefitsCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(18.dp), ambientColor = Color.Black.copy(alpha = 0.04f), spotColor = Color.Black.copy(alpha = 0.04f))
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BenefitLine(Icons.Default.SelfImprovement, stringResource(R.string.onboarding_paywall_benefit_1))
        BenefitLine(Icons.Default.GraphicEq, stringResource(R.string.onboarding_paywall_benefit_2))
        BenefitLine(Icons.Default.TrendingUp, stringResource(R.string.onboarding_paywall_benefit_3))
        BenefitLine(Icons.Default.Notifications, stringResource(R.string.onboarding_paywall_benefit_4))
    }
}

@Composable
private fun BenefitLine(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium,
            color = PaywallTextPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}

/** A quoted store review (iOS TestimonialCard), attributed to its reviewer and store. */
@Composable
private fun ReviewCard(quote: String, author: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFFFFBF5))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(5) {
                Icon(Icons.Default.Star, contentDescription = null, tint = PaywallBestValue, modifier = Modifier.size(14.dp))
            }
        }
        Text(
            text = "\"$quote\"",
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
            fontStyle = FontStyle.Italic,
            color = PaywallTextPrimary,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.paywall_review_attribution, author),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = PaywallTextSecondary
        )
    }
}

/** "No payment now" with a trial, otherwise where the payment goes (iOS: through the App Store). */
@Composable
private fun ReassuranceLine(hasFreeTrial: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = if (hasFreeTrial) Icons.Default.VerifiedUser else Icons.Default.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = if (hasFreeTrial) {
                stringResource(R.string.onboarding_paywall_no_payment_now)
            } else {
                stringResource(R.string.paywall_secure_payment_google_play)
            },
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = PaywallTextPrimary
        )
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
                            text = stringResource(R.string.purchase_dialog_validating_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.purchase_dialog_validating_message),
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
                            text = stringResource(R.string.purchase_dialog_success_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.purchase_dialog_success_message),
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
                                text = stringResource(R.string.okay),
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
                            text = stringResource(R.string.purchase_dialog_failed_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.purchase_dialog_failed_message),
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
                                text = stringResource(R.string.retry),
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
