package taichi.walking.seniors.beginners.taichi.onboarding.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import taichi.walking.seniors.beginners.taichi.onboarding.nav.OnboardingNavGraph
import taichi.walking.seniors.beginners.taichi.onboarding.ui.theme.TaiChiTheme
import taichi.walking.seniors.beginners.taichi.ui.home.activity.TaiChiHomeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TaiChiTheme(useDarkTheme = false) {
                OnboardingNavGraph(
                    onFinished = {
                        startActivity(Intent(this@OnboardingActivity, TaiChiHomeActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}
