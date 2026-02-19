package taichi.walking.seniors.beginners.ui.rateapp

import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.databinding.DialogRateAppBinding
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.mobteq.analytics.MainTracker
import com.mobteq.billing.datastore.DataStorePrefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RateAppDialog : DialogFragment() {

    private lateinit var binding: DialogRateAppBinding

    @Inject
    lateinit var mainTracker: MainTracker

    @Inject
    lateinit var dataStorePrefs: DataStorePrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogRateAppBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivCloseDialog.setOnClickListener {
            mainTracker.trackAppEnjoyEvent(CLICK_DISMISS_RATE_APP)
            dismiss()
        }

        binding.buttonRateUs.setOnClickListener {
            mainTracker.trackAppEnjoyEvent(CLICK_RATE_APP)
            lifecycleScope.launch {
                dataStorePrefs.changeEnjoyAppDialogStatus(false)
            }
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data =
                Uri.parse("https://play.google.com/store/apps/details?id=taichi.walking.seniors.beginners")
            startActivity(intent)
            dismiss()
        }
    }

    companion object {
        const val TAG = "rateApp"
        const val CLICK_RATE_APP = "click_rate_app"
        const val CLICK_DISMISS_RATE_APP = "click_dismiss_rate_app"
    }
}