package com.aura.dating.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.dating.core.designsystem.theme.Dimens
import com.aura.dating.core.designsystem.theme.LikeColor
import com.aura.dating.core.designsystem.theme.PassColor
import com.aura.dating.core.designsystem.theme.SuperLikeColor
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class SwipeDirection {
    LEFT,
    RIGHT,
    UP
}

@Composable
fun SwipeCard(
    modifier: Modifier = Modifier,
    key: Any? = null,
    onSwiped: (SwipeDirection) -> Unit,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offset = remember(key) { Animatable(Offset.Zero, Offset.VectorConverter) }
    val rotation = (offset.value.x / 20f).coerceIn(-15f, 15f)

    val dragThresholdX = 260f
    val dragThresholdY = -280f

    val likeAlpha = (offset.value.x / dragThresholdX).coerceIn(0f, 1f)
    val nopeAlpha = (-offset.value.x / dragThresholdX).coerceIn(0f, 1f)
    val superLikeAlpha = (-offset.value.y / (-dragThresholdY)).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .offset { IntOffset(offset.value.x.roundToInt(), offset.value.y.roundToInt()) }
            .rotate(rotation)
            .pointerInput(key) {
                detectDragGestures(
                    onDragEnd = {
                        coroutineScope.launch {
                            when {
                                offset.value.x > dragThresholdX -> {
                                    offset.animateTo(
                                        Offset(1500f, offset.value.y),
                                        spring(dampingRatio = Spring.DampingRatioLowBouncy)
                                    )
                                    onSwiped(SwipeDirection.RIGHT)
                                }
                                offset.value.x < -dragThresholdX -> {
                                    offset.animateTo(
                                        Offset(-1500f, offset.value.y),
                                        spring(dampingRatio = Spring.DampingRatioLowBouncy)
                                    )
                                    onSwiped(SwipeDirection.LEFT)
                                }
                                offset.value.y < dragThresholdY -> {
                                    offset.animateTo(
                                        Offset(offset.value.x, -2000f),
                                        spring(dampingRatio = Spring.DampingRatioLowBouncy)
                                    )
                                    onSwiped(SwipeDirection.UP)
                                }
                                else -> {
                                    offset.animateTo(
                                        Offset.Zero,
                                        spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                    )
                                }
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            offset.snapTo(
                                Offset(
                                    offset.value.x + dragAmount.x,
                                    offset.value.y + dragAmount.y
                                )
                            )
                        }
                    }
                )
            }
    ) {
        content()

        // LIKE Badge (Top Left)
        if (likeAlpha > 0.05f) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(Dimens.Spacing32)
                    .rotate(-15f)
                    .alpha(likeAlpha)
                    .border(
                        BorderStroke(4.dp, LikeColor),
                        RoundedCornerShape(Dimens.RadiusMedium)
                    )
                    .background(
                        Color.Black.copy(alpha = 0.4f),
                        RoundedCornerShape(Dimens.RadiusMedium)
                    )
                    .padding(horizontal = Dimens.Spacing16, vertical = Dimens.Spacing8)
            ) {
                Text(
                    text = "LIKE",
                    color = LikeColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }

        // NOPE Badge (Top Right)
        if (nopeAlpha > 0.05f) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(Dimens.Spacing32)
                    .rotate(15f)
                    .alpha(nopeAlpha)
                    .border(
                        BorderStroke(4.dp, PassColor),
                        RoundedCornerShape(Dimens.RadiusMedium)
                    )
                    .background(
                        Color.Black.copy(alpha = 0.4f),
                        RoundedCornerShape(Dimens.RadiusMedium)
                    )
                    .padding(horizontal = Dimens.Spacing16, vertical = Dimens.Spacing8)
            ) {
                Text(
                    text = "PASS",
                    color = PassColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }

        // SUPER LIKE Badge (Bottom Center)
        if (superLikeAlpha > 0.15f && likeAlpha < 0.3f && nopeAlpha < 0.3f) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .alpha(superLikeAlpha)
                    .border(
                        BorderStroke(4.dp, SuperLikeColor),
                        RoundedCornerShape(Dimens.RadiusMedium)
                    )
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        RoundedCornerShape(Dimens.RadiusMedium)
                    )
                    .padding(horizontal = Dimens.Spacing20, vertical = Dimens.Spacing10)
            ) {
                Text(
                    text = "SUPER LIKE",
                    color = SuperLikeColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}
