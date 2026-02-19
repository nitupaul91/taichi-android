package taichi.walking.seniors.beginners.util

import taichi.walking.seniors.beginners.R
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.databinding.adapters.TextViewBindingAdapter

object ViewBindings {

    @JvmStatic
    @BindingAdapter("highlightBackground")
    fun highlightBackground(viewGroup: ViewGroup, isSelected: Boolean) {
        if (isSelected)
            viewGroup.background = ResourcesCompat.getDrawable(
                viewGroup.resources,
                R.drawable.background_round_day_night_20,
                null
            )
        else
            viewGroup.background = ResourcesCompat.getDrawable(
                viewGroup.resources,
                R.drawable.background_round_white_20,
                null
            )
    }

    @JvmStatic
    @BindingAdapter(
        "isSelected",
        "backgroundSelected",
        "backgroundUnselected",
        "backgroundSelectedTint"
    )
    fun highlightBackground(
        viewGroup: ViewGroup,
        isSelected: Boolean,
        backgroundSelected: Drawable,
        backgroundUnselected: Drawable?,
        backgroundSelectedTint: Int?,
    ) {
        if (isSelected) {
            viewGroup.background = backgroundSelected
            backgroundSelectedTint?.let { viewGroup.background.setTint(backgroundSelectedTint) }
        } else {
            viewGroup.background = backgroundUnselected
        }
    }

    @JvmStatic
    @BindingAdapter("selectionTextColor")
    fun setTextColor(textView: TextView, isSelected: Boolean) {
        val color = if (isSelected) R.color.bodyTextColor4 else R.color.bodyTextColor2
        textView.setTextColor(ContextCompat.getColor(textView.context, color))
    }

    @BindingAdapter("android:text")
    @JvmStatic
    fun setText(view: EditText, oldText: String?, text: String?) {
        TextViewBindingAdapter.setText(view, text)
        if (text == null) return

        if (oldText.isNullOrEmpty()) {
            view.setSelection(text.length)
        }
    }

    @JvmStatic
    @BindingAdapter("tint")
    fun ImageView.setImageTint(@ColorInt color: Int) {
        setColorFilter(color)
    }
}

@BindingAdapter("insertStars")
fun LinearLayout.insertStars(count: Int) {
    repeat(count) {
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, 10, 0)
        val imageView = ImageView(context)
        imageView.setImageResource(R.drawable.ic_star)
        addView(imageView, layoutParams)
    }
    val remainingStars = 5 - count
    repeat(remainingStars) {
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 0, 10, 0)
        val imageView = ImageView(context)
        imageView.setImageResource(R.drawable.ic_star_empty)
        addView(imageView, layoutParams)
    }
}

@BindingAdapter("isVisible")
fun setIsVisible(view: View, isVisible: Boolean) {
    view.isInvisible = !isVisible
}

@BindingAdapter("isVisibleGone")
fun setIsVisibleGone(view: View, isVisible: Boolean) {
    view.isVisible = isVisible
}