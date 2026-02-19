package taichi.walking.seniors.beginners.ui.rateapp

import taichi.walking.seniors.beginners.R
import taichi.walking.seniors.beginners.ui.styles.SmallBoldTextStyle
import taichi.walking.seniors.beginners.ui.styles.SmallRegularTextStyle
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri

@Composable
fun RateAppDialogView(onDismiss: () -> Unit) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(colorResource(R.color.blackTwo), shape = MaterialTheme.shapes.medium)
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.enjoying_the_app),
                    style = SmallBoldTextStyle,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.rate_app_subtitle),
                    color = Color.LightGray,
                    style = SmallRegularTextStyle,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row {
                    TextButton(onClick = {
                        openPlayStore(context)

                        onDismiss()
                    }) {
                        Text(
                            text = stringResource(R.string.rate_now),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRateAppDialog() {
    RateAppDialogView(
        onDismiss = { }
    )
}

fun openPlayStore(context: Context) {
    val packageName = context.packageName
    val playStoreIntent = Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())

    try {
        context.startActivity(playStoreIntent)
    } catch (e: ActivityNotFoundException) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=$packageName".toUri()
            )
        )
    }
}
