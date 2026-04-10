package taichi.walking.seniors.beginners.util

import android.content.Context
import android.content.Intent

fun sharePlainText(context: Context, text: String, title: String? = null) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(shareIntent, title))
}
