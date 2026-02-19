package taichi.walking.seniors.beginners

import taichi.walking.seniors.beginners.databinding.ActivityMainBinding
import taichi.walking.seniors.beginners.taichi.onboarding.activity.OnboardingActivity
import taichi.walking.seniors.beginners.taichi.onboarding.data.OnboardingRepository
import taichi.walking.seniors.beginners.taichi.ui.home.activity.TaiChiHomeActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import android.content.Intent

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    @Inject lateinit var onboardingRepository: OnboardingRepository
    private lateinit var navController: NavController
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent()
        lifecycleScope.launch {
            val completed = onboardingRepository.observeCompletion().first()
            val target = if (completed) TaiChiHomeActivity::class.java else OnboardingActivity::class.java
            startActivity(Intent(this@MainActivity, target))
            finish()
        }
    }

    private fun setContent() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
    }

    private fun initializeNavController(skipLegacyOnboarding: Boolean) {
        lifecycleScope.launch {
            val navHostFragment =
                supportFragmentManager.findFragmentById(binding.navHostFragment.id) as NavHostFragment
            navController = navHostFragment.navController

            val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
            val destinationId = when {
                !skipLegacyOnboarding && mainViewModel.shouldShowOnboarding() -> R.id.onboardingFragment
                else -> R.id.homeFragment
            }

            navGraph.setStartDestination(destinationId)
            navController.graph = navGraph
        }
    }

    override fun onStart() {
        super.onStart()
        mainViewModel.queryPurchases()
    }

    override fun onDestroy() {
        mainViewModel.endBillingConnection()
        super.onDestroy()
    }
}
