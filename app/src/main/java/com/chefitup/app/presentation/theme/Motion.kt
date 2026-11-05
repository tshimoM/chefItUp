package com.chefitup.app.presentation.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically

/** Shared motion tokens — intentional presence, not noise. */
object ChefMotion {
    val Emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val Standard = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
    val Decelerate = CubicBezierEasing(0f, 0f, 0.2f, 1f)

    const val Fast = 220
    const val Medium = 380
    const val Slow = 560
    const val Stagger = 55

    fun <T> emphasizedTween(duration: Int = Medium) =
        tween<T>(durationMillis = duration, easing = Emphasized)

    fun <T> decelerateTween(duration: Int = Medium) =
        tween<T>(durationMillis = duration, easing = Decelerate)

    fun <T> softSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    fun <T> snappySpring() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
}

val ChefEnterFadeSlide = fadeIn(ChefMotion.emphasizedTween()) +
    slideInVertically(
        animationSpec = ChefMotion.emphasizedTween(),
        initialOffsetY = { it / 12 }
    )

val ChefExitFadeSlide = fadeOut(tween(ChefMotion.Fast)) +
    slideOutVertically(
        animationSpec = tween(ChefMotion.Fast),
        targetOffsetY = { -it / 16 }
    )

val ChefScaleIn = fadeIn(ChefMotion.emphasizedTween(ChefMotion.Medium)) +
    scaleIn(
        animationSpec = ChefMotion.emphasizedTween(ChefMotion.Medium),
        initialScale = 0.92f
    )

val ChefBannerEnter = fadeIn(ChefMotion.decelerateTween(ChefMotion.Medium)) +
    slideInVertically(
        animationSpec = ChefMotion.decelerateTween(ChefMotion.Medium),
        initialOffsetY = { -it }
    )

val ChefBannerExit = fadeOut(tween(ChefMotion.Fast)) +
    slideOutVertically(
        animationSpec = tween(ChefMotion.Fast),
        targetOffsetY = { -it / 2 }
    )

val ChefStepForward = fadeIn(ChefMotion.emphasizedTween(ChefMotion.Medium)) +
    slideInHorizontally(
        animationSpec = ChefMotion.emphasizedTween(ChefMotion.Medium),
        initialOffsetX = { it / 5 }
    ) + scaleIn(
        animationSpec = ChefMotion.emphasizedTween(ChefMotion.Medium),
        initialScale = 0.96f
    )

val ChefStepBackward = fadeIn(ChefMotion.emphasizedTween(ChefMotion.Medium)) +
    slideInHorizontally(
        animationSpec = ChefMotion.emphasizedTween(ChefMotion.Medium),
        initialOffsetX = { -it / 5 }
    )

val ChefStepExitForward = fadeOut(tween(ChefMotion.Fast)) +
    slideOutHorizontally(
        animationSpec = tween(ChefMotion.Fast),
        targetOffsetX = { -it / 5 }
    ) + scaleOut(
        animationSpec = tween(ChefMotion.Fast),
        targetScale = 0.96f
    )

val ChefStepExitBackward = fadeOut(tween(ChefMotion.Fast)) +
    slideOutHorizontally(
        animationSpec = tween(ChefMotion.Fast),
        targetOffsetX = { it / 5 }
    )
