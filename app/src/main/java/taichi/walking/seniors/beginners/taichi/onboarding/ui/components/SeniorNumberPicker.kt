package taichi.walking.seniors.beginners.taichi.onboarding.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.ExperimentalFoundationApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SeniorNumberPicker(
    values: List<Int>,
    selected: Int,
    unit: String,
    onValueChange: (Int) -> Unit
) {
    val initialIndex = values.indexOf(selected).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(selected, values) {
        val index = values.indexOf(selected).coerceAtLeast(0)
        if (index != listState.firstVisibleItemIndex) {
            listState.animateScrollToItem(index)
        }
    }

    LaunchedEffect(listState, values) {
        snapshotFlow {
            val info = listState.layoutInfo
            val visible = info.visibleItemsInfo
            if (visible.isEmpty()) null
            else {
                val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2
                visible.minByOrNull { item ->
                    abs((item.offset + item.size / 2) - viewportCenter)
                }?.index
            }
        }.distinctUntilChanged().collect { centeredIndex ->
            val centeredValue = centeredIndex?.let(values::getOrNull) ?: return@collect
            if (centeredValue != selected) onValueChange(centeredValue)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .align(Alignment.Center)
                .height(56.dp)
                .background(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(28.dp)
                )
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 92.dp),
            flingBehavior = flingBehavior
        ) {
            itemsIndexed(values) { _, value ->
                val isSelected = value == selected
                val alpha = when {
                    isSelected -> 1f
                    kotlin.math.abs(value - selected) <= 1 -> 0.55f
                    else -> 0.25f
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable { onValueChange(value) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$value $unit",
                        style = if (isSelected) {
                            MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        } else {
                            MaterialTheme.typography.bodyLarge
                        },
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                    )
                }
            }
        }
    }
}
