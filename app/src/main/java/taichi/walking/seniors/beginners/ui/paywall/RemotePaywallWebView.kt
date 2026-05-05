package taichi.walking.seniors.beginners.ui.paywall

import android.annotation.SuppressLint
import android.graphics.Color
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.viewinterop.AndroidView
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

sealed interface PaywallWebAction {
    data object Close : PaywallWebAction
    data object Restore : PaywallWebAction
    data class Purchase(val productId: String?) : PaywallWebAction
    data class SelectProduct(val productId: String) : PaywallWebAction
    data class OpenUrl(val url: String) : PaywallWebAction
    data class TrackPersonalization(val variants: Map<String, Any?>) : PaywallWebAction
}

data class PaywallWebStorePayload(
    val products: List<PaywallWebStoreProduct>,
    val heroGender: String,
    val isTaiChiWalkingFlow: Boolean,
    val onboardingProfile: PaywallWebOnboardingProfile?,
    val selectedProductId: String?,
    val errorMessage: String?,
    val isPurchaseInProgress: Boolean,
    val isRestoring: Boolean
)

data class PaywallWebStoreProduct(
    val id: String,
    val title: String?,
    val description: String?,
    val displayPrice: String,
    val billingPeriodLabel: String?,
    val freeTrialDays: String?,
    val trialDisplayText: String?,
    val subscriptionPeriodUnit: String?,
    val subscriptionPeriodValue: Int?,
    val introOffer: PaywallWebIntroOffer?,
    val isIntroOfferEligible: Boolean,
    val isSelected: Boolean = false
)

data class PaywallWebIntroOffer(
    val paymentMode: String,
    val periodUnit: String?,
    val periodValue: Int?
)

data class PaywallWebOnboardingProfile(
    val userId: String?,
    val completedStep: String,
    val discoverySource: String?,
    val age: Int,
    val gender: String?,
    val goals: Set<String>,
    val taiChiFamiliarity: String?,
    val currentBodyType: String?,
    val targetBodyType: String?,
    val targetZones: Set<String>,
    val activityLevel: String?,
    val dailyWalking: String?,
    val stairsFeeling: String?,
    val squatsAbility: String?,
    val canRotateHead: String?,
    val canHoldArms: String?,
    val taiChiLevel: String?,
    val betweenMealsFeeling: String?,
    val sleepAmount: String?,
    val waterIntake: String?,
    val dietTypes: Set<String>,
    val height: Int,
    val weight: Double,
    val targetWeight: Double,
    val shapeUpEvent: String?,
    val shapeUpEventDate: String?,
    val motivationLevel: String?,
    val exerciseBlockers: Set<String>,
    val useMetric: Boolean?
)

@Composable
fun RemotePaywallWebView(
    paywall: CachedRemotePaywall,
    storePayload: PaywallWebStorePayload,
    onAction: (PaywallWebAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val gson = remember { Gson() }
    val payloadJson = remember(storePayload, gson) { gson.toJson(storePayload) }
    var isPageVisible by remember(paywall.identifier) { mutableStateOf(false) }

    AndroidView(
        modifier = modifier.alpha(if (isPageVisible) 1f else 0f),
        factory = { context ->
            createPaywallWebView(
                paywall = paywall,
                onAction = onAction,
                gson = gson,
                context = context,
                initialPayloadJson = payloadJson,
                onPageVisible = { isPageVisible = true }
            )
        },
        update = { webView ->
            webView.setTag(TAG_PAYLOAD_JSON, payloadJson)
            val loadedIdentifier = webView.getTag(TAG_PAYWALL_IDENTIFIER) as? String
            if (loadedIdentifier != paywall.identifier) {
                isPageVisible = false
                webView.setTag(TAG_PAYWALL_IDENTIFIER, paywall.identifier)
                webView.setTag(TAG_WEBVIEW_READY, false)
                webView.loadDataWithBaseURL(paywall.baseUrl, paywall.html, "text/html", "utf-8", null)
            } else if (webView.getTag(TAG_WEBVIEW_READY) == true) {
                isPageVisible = true
                webView.evaluateJavascript(updateStoreScript(payloadJson), null)
            }
        }
    )
}

@SuppressLint("SetJavaScriptEnabled")
private fun createPaywallWebView(
    context: android.content.Context,
    paywall: CachedRemotePaywall,
    onAction: (PaywallWebAction) -> Unit,
    gson: Gson,
    initialPayloadJson: String,
    onPageVisible: () -> Unit
): WebView {
    return WebView(context).apply {
        setBackgroundColor(Color.TRANSPARENT)
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadsImagesAutomatically = true
        settings.mediaPlaybackRequiresUserGesture = false
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        webChromeClient = WebChromeClient()
        webViewClient = object : WebViewClient() {
            override fun onPageCommitVisible(view: WebView?, url: String?) {
                super.onPageCommitVisible(view, url)
                onPageVisible()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                setTag(TAG_WEBVIEW_READY, true)
                onPageVisible()
                evaluateJavascript(bridgeBootstrapScript(), null)
                val latestPayloadJson = getTag(TAG_PAYLOAD_JSON) as? String ?: initialPayloadJson
                evaluateJavascript(updateStoreScript(latestPayloadJson), null)
            }
        }
        addJavascriptInterface(
            PaywallJavascriptBridge(onAction = onAction, gson = gson),
            JS_NATIVE_BRIDGE_NAME
        )
        setTag(TAG_PAYWALL_IDENTIFIER, paywall.identifier)
        setTag(TAG_PAYLOAD_JSON, initialPayloadJson)
        setTag(TAG_WEBVIEW_READY, false)
        loadDataWithBaseURL(paywall.baseUrl, paywall.html, "text/html", "utf-8", null)
    }
}

private fun bridgeBootstrapScript(): String {
    return """
        (function() {
          var nativeBridge = window.$JS_NATIVE_BRIDGE_NAME;
          if (!nativeBridge) return;
          var bridge = {
            postMessage: function(message) {
              try {
                nativeBridge.postMessage(JSON.stringify(message || {}));
              } catch (error) {}
            }
          };
          window.paywallBridge = bridge;
          window.webkit = window.webkit || {};
          window.webkit.messageHandlers = window.webkit.messageHandlers || {};
          window.webkit.messageHandlers.paywallBridge = bridge;
        })();
    """.trimIndent()
}

private fun updateStoreScript(payloadJson: String): String {
    return "if (window.updateStorePayload) { window.updateStorePayload($payloadJson); }"
}

private class PaywallJavascriptBridge(
    private val onAction: (PaywallWebAction) -> Unit,
    private val gson: Gson
) {
    @JavascriptInterface
    fun postMessage(messageBodyJson: String?) {
        val parsedAction = parseAction(messageBodyJson) ?: return
        onAction(parsedAction)
    }

    private fun parseAction(messageBodyJson: String?): PaywallWebAction? {
        if (messageBodyJson.isNullOrBlank()) {
            return null
        }

        return try {
            val body = gson.fromJson(messageBodyJson, Map::class.java)
            when (body["action"] as? String) {
                "close" -> PaywallWebAction.Close
                "restore" -> PaywallWebAction.Restore
                "purchase" -> PaywallWebAction.Purchase(body["productId"] as? String)
                "selectProduct" -> {
                    val productId = body["productId"] as? String ?: return null
                    PaywallWebAction.SelectProduct(productId)
                }
                "openURL" -> {
                    val url = body["url"] as? String ?: return null
                    PaywallWebAction.OpenUrl(url)
                }
                "trackPersonalization" -> {
                    @Suppress("UNCHECKED_CAST")
                    val variants = body["variants"] as? Map<String, Any?> ?: emptyMap()
                    PaywallWebAction.TrackPersonalization(variants)
                }
                else -> null
            }
        } catch (_: JsonSyntaxException) {
            null
        }
    }
}

private const val JS_NATIVE_BRIDGE_NAME = "paywallBridgeNative"
private const val TAG_PAYWALL_IDENTIFIER = 0x70617977
private const val TAG_PAYLOAD_JSON = 0x70617978
private const val TAG_WEBVIEW_READY = 0x70617979
