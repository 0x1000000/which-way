package x100000.whichway.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.lerp
import androidx.wear.compose.foundation.AnchorType
import androidx.wear.compose.foundation.CurvedAlignment
import androidx.wear.compose.foundation.CurvedDirection
import androidx.wear.compose.foundation.CurvedLayout
import androidx.wear.compose.foundation.CurvedTextStyle
import androidx.wear.compose.foundation.basicCurvedText
import androidx.wear.compose.foundation.curvedComposable
import x100000.whichway.R
import x100000.whichway.game.GameRules
import x100000.whichway.presentation.icons.HudHeartIcon

@Composable
internal fun OrbitHud(
    lives: Int,
    charges: Int,
    score: Int,
    level: Int?,
    isLevelSpeedupActive: Boolean,
    speedLabel: String?,
    uiMetrics: WatchUiMetrics,
    modifier: Modifier = Modifier,
) {
    val scoreLabel = androidx.compose.ui.res.stringResource(R.string.score_value, score)
    val levelLabel = level?.let { androidx.compose.ui.res.stringResource(R.string.level_value, it) }
    val density = LocalDensity.current
    val heartSize = with(density) { uiMetrics.hudChargeFontSize.toDp() }
    val levelPulse = remember { Animatable(0f) }
    var previousLevel by remember { mutableStateOf(level) }

    LaunchedEffect(level) {
        val oldLevel = previousLevel
        if (level != null && oldLevel != null && level > oldLevel) {
            levelPulse.snapTo(0f)
            levelPulse.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
            )
            levelPulse.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
            )
        } else if (level == null) {
            levelPulse.snapTo(0f)
        }
        previousLevel = level
    }
    Box(modifier = modifier) {
        repeat(GameRules.STARTING_LIVES) { index ->
            val lifeAnchor = 225f + (index - 1) * uiMetrics.hudChargeSpacingDegrees
            CurvedLayout(
                modifier = Modifier.fillMaxSize(),
                anchor = lifeAnchor,
                anchorType = AnchorType.Center,
                radialAlignment = CurvedAlignment.Radial.Inner,
            ) {
                val heartColor = if (index < lives) LifeOnColor else LifeOffColor.copy(alpha = 0.45f)
                curvedComposable {
                    HudHeartIcon(
                        color = heartColor,
                        modifier = Modifier.size(heartSize),
                    )
                }
            }
        }

        repeat(GameRules.MAX_CHARGES) { index ->
            val chargeAnchor = 135f + (index - 1) * uiMetrics.hudChargeSpacingDegrees
            CurvedLayout(
                modifier = Modifier.fillMaxSize(),
                anchor = chargeAnchor,
                anchorType = AnchorType.Center,
                angularDirection = CurvedDirection.Angular.Reversed,
                radialAlignment = CurvedAlignment.Radial.Inner,
            ) {
                val filledIndex = GameRules.MAX_CHARGES - 1 - index
                val chargeColor = if (filledIndex < charges) ChargeOnColor else ChargeOffColor.copy(alpha = 0.50f)
                basicCurvedText(
                    text = "\u25A3",
                    style = {
                        CurvedTextStyle(
                            color = chargeColor,
                            fontSize = uiMetrics.hudChargeFontSize,
                        )
                    },
                )
            }
        }

        CurvedLayout(
            modifier = Modifier.fillMaxSize(),
            anchor = 315f,
            anchorType = AnchorType.Center,
            radialAlignment = CurvedAlignment.Radial.Inner,
        ) {
            basicCurvedText(
                text = scoreLabel,
                style = {
                    CurvedTextStyle(
                        background = HudTextBackdropColor,
                        color = HudTextColor,
                        fontSize = uiMetrics.hudInfoFontSize,
                    )
                },
            )
        }

        if (levelLabel != null) {
            val speedupLevelTextColor = androidx.compose.ui.graphics.Color(0xFFFFB347)
            val baseLevelTextColor = if (isLevelSpeedupActive) speedupLevelTextColor else HudTextColor
            val baseLevelBackdrop = HudTextBackdropColor
            val levelTextColor = lerp(baseLevelTextColor, FlashAccentTextColor, levelPulse.value)
            val levelBackdrop = lerp(baseLevelBackdrop, FlashAccentTextColor.copy(alpha = 0.42f), levelPulse.value)
            val levelFontSize = uiMetrics.hudInfoFontSize * (1f + 0.16f * levelPulse.value)
            CurvedLayout(
                modifier = Modifier.fillMaxSize(),
                anchor = 45f,
                anchorType = AnchorType.Center,
                angularDirection = CurvedDirection.Angular.Reversed,
                radialAlignment = CurvedAlignment.Radial.Inner,
            ) {
                basicCurvedText(
                    text = levelLabel,
                    style = {
                        CurvedTextStyle(
                            background = levelBackdrop,
                            color = levelTextColor,
                            fontSize = levelFontSize,
                        )
                    },
                )
            }
        }

        if (speedLabel != null) {
            CurvedLayout(
                modifier = Modifier.fillMaxSize(),
                anchor = 0f,
                anchorType = AnchorType.Center,
                radialAlignment = CurvedAlignment.Radial.Inner,
            ) {
                basicCurvedText(
                    text = speedLabel,
                    style = {
                        CurvedTextStyle(
                            background = HudTextBackdropColor,
                            color = HudTextColor,
                            fontSize = uiMetrics.hudInfoFontSize,
                        )
                    },
                )
            }
        }
    }
}
