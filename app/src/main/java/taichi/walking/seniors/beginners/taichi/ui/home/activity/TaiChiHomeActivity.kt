package taichi.walking.seniors.beginners.taichi.ui.home.activity

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import taichi.walking.seniors.beginners.taichi.onboarding.ui.theme.TaiChiTheme
import taichi.walking.seniors.beginners.taichi.ui.home.screen.TaiChiHomeRoot
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaiChiHomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)
        setContent {
            TaiChiTheme(useDarkTheme = false) {
                TaiChiHomeRoot()
            }
        }
    }
}
