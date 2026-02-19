package taichi.walking.seniors.beginners.ui.paywall

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTimeFilled
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.mobteq.billing.domain.Product
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.ui.paywall.dialog.PurchaseStatusDialog
import taichi.walking.seniors.beginners.ui.styles.LargeBoldWideTextStyle
import taichi.walking.seniors.beginners.ui.styles.MediumBoldWideTextStyle
import taichi.walking.seniors.beginners.ui.styles.MediumTextStyle
import taichi.walking.seniors.beginners.ui.styles.SmallBoldTextStyle
import taichi.walking.seniors.beginners.ui.styles.SmallRegularTextStyle
import taichi.walking.seniors.beginners.ui.styles.VerySmallRegularTextStyle

@AndroidEntryPoint
class PaywallFragment : Fragment() {

    private val paywallViewModel: PaywallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                paywallViewModel.navigateBack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    val selectedProduct by paywallViewModel.selectedProduct.collectAsState()
                    val products by paywallViewModel.products.collectAsState()

                    PaywallScreen(
                        products = products,
                        selectedProduct = selectedProduct,
                        onSubscribeClicked = {
                            paywallViewModel.makePurchase(requireActivity())
                        },
                        onProductSelected = { product ->
                            paywallViewModel.selectProduct(product.product)
                        },
                        onCloseClicked = {
                            closeScreen()
                        },
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                paywallViewModel.eventsFlow.collect { event ->
                    when (event) {
                        PaywallEvents.PurchaseInProgress -> {
                            PurchaseStatusDialog().show(
                                childFragmentManager,
                                PurchaseStatusDialog.TAG
                            )
                        }

                        PaywallEvents.CloseScreen -> {
                            closeScreen()
                        }

                        PaywallEvents.DismissPurchaseStatusDialog -> {
                            closeScreen()
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun closeScreen() {
        val action = PaywallFragmentDirections.navigateHome()
        findNavController().navigate(action)
    }
}

@Composable
fun PaywallScreen(
    products: List<UIProduct>,
    selectedProduct: Product?,
    onProductSelected: (UIProduct) -> Unit,
    onSubscribeClicked: () -> Unit,
    onCloseClicked: () -> Unit,
) {
    val selectedIndex = products.indexOfFirst { it.product.productId == selectedProduct?.productId }
        .takeIf { it != -1 } ?: 0

    val selectedUIProduct =
        products.firstOrNull { it.product.productId == selectedProduct?.productId }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(modifier = Modifier.padding(top = 16.dp)) {
            Box {
                Image(
                    painter = painterResource(id = R.drawable.lambo),
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .height(350.dp)
                        .fillMaxWidth()
                        .drawWithCache {
                            onDrawWithContent {
                                drawContent()
                                drawRect(
                                    Brush.verticalGradient(
                                        0f to Color.Black,
                                        0.5f to Color.Black.copy(alpha = 0f)
                                    )
                                )
                            }
                        },
                    contentDescription = ""
                )
                Text(
                    text = stringResource(id = R.string.onboarding_one_title),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = LargeBoldWideTextStyle,
                    modifier = Modifier
                        .padding(horizontal = 60.dp)
                        .padding(top = 12.dp)
                )
            }
            Icon(
                imageVector = Icons.Rounded.Close,
                tint = Color.Gray,
                contentDescription = "",
                modifier = Modifier
                    .padding(start = 16.dp)
                    .clickable {
                        onCloseClicked()
                    }
                    .clip(CircleShape)
                    .size(24.dp)
            )
        }

        TabRow(
            selectedTabIndex = selectedIndex,
            containerColor = colorResource(id = R.color.blackTwo),
            contentColor = Color.Black,
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .clip(RoundedCornerShape(16)),
            indicator = { tabPositions: List<TabPosition> ->
                Box {}
            },
            divider = {}
        ) {
            products.forEachIndexed { index, product ->
                val selected = selectedIndex == index
                Tab(
                    selected = selected,
                    onClick = {
                        onProductSelected(products[index])
                    },
                    text = {
                        Text(
                            text = product.title,
                            style = SmallBoldTextStyle,
                            color = if (selected) Color.White else Color.White
                        )
                    },
                    modifier = if (selected) Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(16))
                        .background(
                            colorResource(id = R.color.colorPrimary)
                        )
                    else Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(16))
                        .background(colorResource(id = R.color.blackTwo)),
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = 4.dp,
            backgroundColor = colorResource(id = R.color.blackTwo),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = selectedUIProduct?.title ?: "",
                        color = Color.White,
                        style = SmallBoldTextStyle
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = selectedProduct?.displayPrice ?: "",
                        color = Color.White,
                        style = SmallBoldTextStyle
                    )
                    Text(
                        text = selectedUIProduct?.billingPeriod ?: "",
                        color = colorResource(id = R.color.clay),
                        style = MediumTextStyle
                    )

                }

                Spacer(modifier = Modifier.height(16.dp))

                selectedUIProduct?.benefits?.forEach { benefit ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            tint = Color.White,
                            contentDescription = "",
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(colorResource(id = R.color.colorPrimary))
                                .padding(4.dp)
                        )
                        Text(
                            text = benefit,
                            color = Color.White,
                            style = SmallRegularTextStyle
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onSubscribeClicked() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.colorPrimary)),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.onboarding_continue_btn),
                color = Color.White,
                style = MediumBoldWideTextStyle
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.AccessTimeFilled,
                tint = colorResource(id = R.color.colorPrimary),
                contentDescription = stringResource(id = R.string.error_empty_make),
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(18.dp)
            )
            Text(
                text = stringResource(R.string.auto_renewable_cancel_anytime),
                style = VerySmallRegularTextStyle,
                color = colorResource(id = R.color.clay),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = stringResource(R.string.privacy_policy),
                style = VerySmallRegularTextStyle,
                color = colorResource(id = R.color.paywallUnselectedGray),
                modifier = Modifier
                    .padding(start = 16.dp)
                    .clickable {
                        val intent =
                            Intent(
                                Intent.ACTION_VIEW,
                                "https://strada.pics/privacy_policy.html".toUri()
                            )
                        context.startActivity(intent)
                    }
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(R.string.terms),
                style = VerySmallRegularTextStyle,
                color = colorResource(id = R.color.paywallUnselectedGray),
                modifier = Modifier
                    .clickable {
                        val intent =
                            Intent(Intent.ACTION_VIEW, "https://strada.pics/terms.html".toUri())
                        context.startActivity(intent)
                    }
                    .padding(end = 16.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
