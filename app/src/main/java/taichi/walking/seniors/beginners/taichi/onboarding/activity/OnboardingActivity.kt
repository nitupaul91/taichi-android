package taichi.walking.seniors.beginners.taichi.onboarding.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import taichi.walking.seniors.beginners.taichi.onboarding.analytics.OnboardingAnalyticsTracker
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingNavGraph
import taichi.walking.seniors.beginners.taichi.onboarding.ui.theme.TaiChiTheme
import taichi.walking.seniors.beginners.taichi.ui.home.activity.TaiChiHomeActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingActivity : ComponentActivity() {

    @Inject
    lateinit var analyticsTracker: OnboardingAnalyticsTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        setContent {
            TaiChiTheme(useDarkTheme = false) {
                OnboardingNavGraph(
                    analyticsTracker = analyticsTracker,
                    onFinished = {
                        startActivity(Intent(this@OnboardingActivity, TaiChiHomeActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}
