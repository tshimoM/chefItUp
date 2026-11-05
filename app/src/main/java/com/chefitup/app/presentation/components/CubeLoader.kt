package com.chefitup.app.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chefitup.app.presentation.theme.Charcoal
import com.chefitup.app.presentation.theme.CharcoalSoft
import com.chefitup.app.presentation.theme.EmberGold
import com.chefitup.app.presentation.theme.FlameDeep
import com.chefitup.app.presentation.theme.FlameOrange
import com.chefitup.app.presentation.theme.FlameSoft
import com.chefitup.app.presentation.theme.Sage
import kotlin.math.cos
import kotlin.math.sin

/**
 * 3D spinning cube loader inspired by Uiverse (andrew-demchenk0),
 * recolored to Chef it Up flame / charcoal / gold brand.
 */
@Composable
fun BrandCubeLoader(
    modifier: Modifier = Modifier,
    size: Dp = 78.dp
) {
    val transition = rememberInfiniteTransition(label = "cube_spin")
    val angleDeg by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cube_y"
    )

    Box(
        modifier = modifier.size(size * 1.85f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 1.85f)) {
            val cubePx = size.toPx()
            val cx = this.size.width / 2f
            val cy = this.size.height / 2f - cubePx * 0.06f
            val half = cubePx / 2f
            val rotX = Math.toRadians(-30.0)
            val rotY = Math.toRadians(angleDeg.toDouble())

            fun project(x: Float, y: Float, z: Float): Pair<Offset, Float> {
                // Rotate around Y, then X (matches CSS rotateX(-30) rotateY(t))
                var x1 = x * cos(rotY).toFloat() - z * sin(rotY).toFloat()
                var z1 = x * sin(rotY).toFloat() + z * cos(rotY).toFloat()
                val y1 = y * cos(rotX).toFloat() - z1 * sin(rotX).toFloat()
                val z2 = y * sin(rotX).toFloat() + z1 * cos(rotX).toFloat()
                val persp = 420f / (420f + z2)
                return Offset(cx + x1 * persp, cy + y1 * persp) to z2
            }

            val verts = arrayOf(
                floatArrayOf(-half, -half, half),  // 0 front-top-left
                floatArrayOf(half, -half, half),   // 1 front-top-right
                floatArrayOf(half, half, half),    // 2 front-bottom-right
                floatArrayOf(-half, half, half),   // 3 front-bottom-left
                floatArrayOf(-half, -half, -half), // 4 back-top-left
                floatArrayOf(half, -half, -half),  // 5 back-top-right
                floatArrayOf(half, half, -half),   // 6 back-bottom-right
                floatArrayOf(-half, half, -half)   // 7 back-bottom-left
            )

            val projected = verts.map { v ->
                project(v[0], v[1], v[2])
            }

            // Soft ground glow under the cube (CSS ::before blur substitute)
            val shadowCenter = Offset(cx, cy + half * 1.35f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        FlameOrange.copy(alpha = 0.45f),
                        EmberGold.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = shadowCenter,
                    radius = half * 1.35f
                ),
                radius = half * 1.2f,
                center = shadowCenter
            )

            data class Face(
                val indices: IntArray,
                val colors: List<Color>,
                val avgZ: Float
            )

            fun face(indices: IntArray, colors: List<Color>): Face {
                val avgZ = indices.map { projected[it].second }.average().toFloat()
                return Face(indices, colors, avgZ)
            }

            val sideGradient = listOf(
                CharcoalSoft,
                FlameDeep,
                FlameOrange,
                FlameSoft,
                EmberGold.copy(alpha = 0.95f)
            )
            val topColors = listOf(Charcoal, CharcoalSoft, FlameDeep.copy(alpha = 0.9f))
            val sageAccent = listOf(Sage.copy(alpha = 0.85f), FlameDeep, FlameOrange)

            val faces = listOf(
                face(intArrayOf(0, 1, 2, 3), sideGradient),       // front
                face(intArrayOf(5, 4, 7, 6), sageAccent),         // back
                face(intArrayOf(4, 0, 3, 7), sideGradient),       // left
                face(intArrayOf(1, 5, 6, 2), sideGradient),       // right
                face(intArrayOf(4, 5, 1, 0), topColors),          // top
                face(intArrayOf(3, 2, 6, 7), listOf(Charcoal, FlameDeep)) // bottom
            ).sortedByDescending { it.avgZ } // paint far faces first

            faces.forEach { f ->
                drawCubeFace(
                    points = f.indices.map { projected[it].first },
                    colors = f.colors
                )
            }
        }
    }
}

private fun DrawScope.drawCubeFace(points: List<Offset>, colors: List<Color>) {
    if (points.size < 4) return
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        lineTo(points[1].x, points[1].y)
        lineTo(points[2].x, points[2].y)
        lineTo(points[3].x, points[3].y)
        close()
    }
    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = colors,
            startY = points.minOf { it.y },
            endY = points.maxOf { it.y }
        )
    )
    // Subtle edge so faces read in 3D
    drawPath(
        path = path,
        color = Color.White.copy(alpha = 0.08f),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2f)
    )
}
