package taichi.walking.seniors.beginners.ui.paywall.dialog

import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.databinding.DialogFragmentPurchaseStatusBinding
import taichi.walking.seniors.beginners.ui.paywall.PaywallEvents
import taichi.walking.seniors.beginners.ui.paywall.PaywallViewModel
import taichi.walking.seniors.beginners.util.viewBinding
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PurchaseStatusDialog : DialogFragment(R.layout.dialog_fragment_purchase_status) {

    private val paywallViewModel: PaywallViewModel by viewModels({ requireParentFragment() })

    private val binding by viewBinding(DialogFragmentPurchaseStatusBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogFragment)
        this.isCancelable = false
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel = paywallViewModel
            lifecycleOwner = viewLifecycleOwner
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    paywallViewModel.eventsFlow.collect {
                        when (it) {
                            PaywallEvents.DismissPurchaseStatusDialog -> navigateHome()
                            is PaywallEvents.PurchaseIsAcknowledged -> {
                                showSuccess()
                            }

                            PaywallEvents.PurchaseNotValid -> showValidationError()
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun navigateHome() {
        dismiss()
    }

    private fun showSuccess() = with(binding) {
        tvPurchaseStatusTitle.text = resources.getString(R.string.purchase_status_title_success)
        tvPurchaseStatusSubTitle.text =
            resources.getString(R.string.purchase_status_subtitle_success)
        progressBar.isVisible = false
        tvPurchaseStatusClose.isVisible = true
    }

    private fun showValidationError() = with(binding) {
        tvPurchaseStatusTitle.text = resources.getString(R.string.purchase_status_sub_not_valid)
        tvPurchaseStatusClose.isVisible = true
        tvPurchaseStatusSubTitle.isVisible = false
        progressBar.isVisible = false
    }

    companion object {
        const val TAG = "PurchaseStatusDialog"
    }
}