package taichi.walking.seniors.beginners.taichi.ui.home.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import taichi.walking.seniors.beginners.taichi.onboarding.ui.theme.TaiChiTheme
import taichi.walking.seniors.beginners.taichi.ui.home.screen.TaiChiHomeRoot
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaiChiHomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaiChiTheme(useDarkTheme = false) {
                TaiChiHomeRoot()
            }
        }
    }
}
