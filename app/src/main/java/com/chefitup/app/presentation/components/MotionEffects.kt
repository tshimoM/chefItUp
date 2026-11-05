package com.chefitup.app.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chefitup.app.R
import com.chefitup.app.presentation.theme.ChefBannerEnter
import com.chefitup.app.presentation.theme.ChefBannerExit
import com.chefitup.app.presentation.theme.ChefMotion
import com.chefitup.app.presentation.theme.EmberGold
import com.chefitup.app.presentation.theme.FlameDeep
import com.chefitup.app.presentation.theme.FlameOrange
import kotlinx.coroutines.delay

@Composable
fun StaggeredAppear(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((index * ChefMotion.Stagger).toLong().coerceAtMost(420L))
        visible = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = ChefMotion.decelerateTween(ChefMotion.Medium),
        label = "stagger_alpha"
    )
    val offsetY by animateFloatAsState(
        targetValue = if (visible) 0f else 18f,
        animationSpec = ChefMotion.decelerateTween(ChefMotion.Medium),
        label = "stagger_y"
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.94f,
        animationSpec = ChefMotion.softSpring(),
        label = "stagger_scale"
    )
    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha
            translationY = offsetY
            this.scaleX = scale
            this.scaleY = scale
        }
    ) {
        content()
    }
}

@Composable
fun FavouriteBurstButton(
    isFavourite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }
    var previous by remember { mutableStateOf(isFavourite) }

    LaunchedEffect(isFavourite) {
        if (isFavourite && !previous) {
            scale.snapTo(0.7f)
            scale.animateTo(1.22f, ChefMotion.snappySpring())
            scale.animateTo(1f, ChefMotion.softSpring())
        }
        previous = isFavourite
    }

    IconButton(onClick = onClick, modifier = modifier.scale(scale.value)) {
        Icon(
            imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = stringResource(R.string.cd_favourite),
            tint = if (isFavourite) FlameOrange else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AnimatedOfflineBanner(visible: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = visible,
        enter = ChefBannerEnter,
        exit = ChefBannerExit,
        modifier = modifier
    ) {
        OfflineBanner()
    }
}

@Composable
fun CelebrationOverlay(
    visible: Boolean,
    title: String,
    body: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(ChefMotion.decelerateTween()) + scaleIn(
            animationSpec = ChefMotion.softSpring(),
            initialScale = 0.86f
        ),
        exit = fadeOut(tween(ChefMotion.Fast)) + scaleOut(targetScale = 0.94f),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f))
                .padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(FlameOrange, FlameDeep, EmberGold.copy(alpha = 0.92f))
                        )
                    )
                    .padding(28.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.92f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.celebration_continue),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

fun celebrationBody(
    xp: Int,
    leveledUp: Boolean,
    levelLabel: String,
    newBadgeCount: Int
): String {
    val parts = mutableListOf("+$xp XP")
    if (leveledUp) parts += "Level up → $levelLabel"
    if (newBadgeCount > 0) parts += "$newBadgeCount new badge${if (newBadgeCount == 1) "" else "s"}"
    return parts.joinToString(" · ")
}