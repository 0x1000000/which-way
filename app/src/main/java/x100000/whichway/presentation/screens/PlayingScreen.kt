package x100000.whichway.presentation

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import x100000.whichway.game.Direction
import x100000.whichway.game.GameRules
import x100000.whichway.game.GameScreenState
import x100000.whichway.game.GameSessionResult
import x100000.whichway.game.RoundData
import x100000.whichway.game.ZoneFacts

private const val DEFAULT_FEEDBACK_DURATION_MILLIS = 500L
private const val REPLAY_FEEDBACK_DURATION_MILLIS = 500L
private const val SUCCESS_FEEDBACK_DURATION_MILLIS = 200L
private const val CHARGE_EARNED_FEEDBACK_DURATION_MILLIS = 500L
private const val FLASH_FADE_IN_MILLIS = 150
private const val FLASH_FADE_OUT_MILLIS = 150

@Composable
internal fun FlashOverlay(
    flashColor: Color,
    flashSymbol: String?,
    flashSymbolColor: Color,
    symbolFontSize: androidx.compose.ui.unit.TextUnit,
    animationProgress: Float,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(flashColor.copy(alpha = flashColor.alpha * animationProgress)),
    ) {
        flashSymbol?.let { symbol ->
            val badgeSize = with(density) { symbolFontSize.toDp() * 2.35f }
            val textSizePx = with(density) { symbolFontSize.toPx() }
            val textPaint = remember(symbol, textSizePx) {
                Paint().apply {
                    color = android.graphics.Color.WHITE
                    textAlign = Paint.Align.CENTER
                    textSize = textSizePx
                    isFakeBoldText = true
                    isAntiAlias = true
                }
            }
            textPaint.color = flashSymbolColor.toArgb()
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(badgeSize)
                    .scale(0.92f + 0.08f * animationProgress)
                    .drawBehind {
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.86f),
                            radius = size.minDimension / 2f,
                            center = Offset(size.width / 2f, size.height / 2f),
                        )
                        drawIntoCanvas { canvas ->
                            val baseline = size.height / 2f -
                                (textPaint.descent() + textPaint.ascent()) / 2f
                            canvas.nativeCanvas.drawText(
                                symbol,
                                size.width / 2f,
                                baseline,
                                textPaint,
                            )
                        }
                    },
            )
        }
    }
}

@Composable
internal fun PlayingScreen(
    state: GameScreenState.Playing,
    isPaused: Boolean,
    onContinue: () -> Unit,
    onExit: () -> Unit,
    onResolve: (direction: Direction?, elapsedMillis: Int, isTimeout: Boolean) -> GameSessionResult?,
    onBusyStateChanged: (Boolean) -> Unit,
    onApplyTransition: (GameSessionResult) -> Unit,
 ) {
    val uiMetrics = rememberWatchUiMetrics()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor),
    ) {
        val currentLevel = remember(state.score, state.config) {
            val effectiveScore = GameRules.effectiveContentScoreFor(state.score, state.config)
            GameRules.levelForScore(effectiveScore)
        }
        val showLevelIndicator = remember(state.config) {
            state.config.commandProfile == x100000.whichway.game.CommandProfile.All &&
                state.config.allowsProgression &&
                state.config.tracksStats
        }
        val timeoutMillis = remember(state.roundData.complexity, state.score, state.config) {
            GameRules.timeoutMillisFor(
                complexity = state.roundData.complexity,
                score = state.score,
                config = state.config,
            )
        }
        val speedLabel = remember(state.roundData.complexity, state.score, state.config) {
            GameRules.speedLabelFor(
                complexity = state.roundData.complexity,
                score = state.score,
                config = state.config,
            )
        }
        val isLevelSpeedupActive = remember(state.score, state.config) {
            GameRules.isScoreSpeedupActive(
                score = state.score,
                config = state.config,
            )
        }
        val hasZoneMarkers = remember(state.roundData) {
            state.roundData.zoneFacts.values.any { it.color != null || it.number != null || it.suit != null || it.target }
        }
        val progress = remember { Animatable(1f) }
        val scope = rememberCoroutineScope()
        val haptics = LocalHapticFeedback.current
        var pressedDirection by remember { mutableStateOf<Direction?>(null) }
        var flashColor by remember { mutableStateOf<Color?>(null) }
        var flashSymbol by remember { mutableStateOf<String?>(null) }
        var flashSymbolColor by remember { mutableStateOf(Color.White) }
        var isResolving by remember { mutableStateOf(false) }
        val flashProgress = remember { Animatable(0f) }
        val isBusy = isResolving || flashColor != null

        LaunchedEffect(isBusy) {
            onBusyStateChanged(isBusy)
        }

        suspend fun showFeedback(
            color: Color,
            symbol: String? = null,
            durationMillis: Long = DEFAULT_FEEDBACK_DURATION_MILLIS,
            withHaptics: Boolean = true,
            onFinished: () -> Unit,
        ) {
            isResolving = true
            progress.stop()
            flashColor = color
            flashSymbol = symbol
            flashSymbolColor = if (symbol == "+\u25A0" || symbol == "\u21BB") FlashAccentTextColor else Color.White
            flashProgress.snapTo(0f)
            if (withHaptics) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            flashProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = FLASH_FADE_IN_MILLIS, easing = LinearEasing),
            )
            val holdMillis = (durationMillis - FLASH_FADE_IN_MILLIS - FLASH_FADE_OUT_MILLIS).coerceAtLeast(0L)
            delay(holdMillis)
            flashProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = FLASH_FADE_OUT_MILLIS, easing = LinearEasing),
            )
            flashColor = null
            flashSymbol = null
            flashSymbolColor = Color.White
            onFinished()
        }

        LaunchedEffect(state.roundNumber) {
            pressedDirection = null
            isResolving = false
            flashColor = null
            flashSymbol = null
            progress.snapTo(1f)
        }

        LaunchedEffect(state.roundNumber, isPaused) {
            if (isPaused || isResolving) {
                return@LaunchedEffect
            }
            val remainingDuration = (timeoutMillis * progress.value)
                .roundToInt()
                .coerceAtLeast(1)
            progress.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = remainingDuration,
                    easing = LinearEasing,
                ),
            )
            delay(GameRules.TIMEOUT_GRACE_MILLIS.toLong())
            if (!isPaused && !isResolving) {
                val transition = onResolve(null, timeoutMillis, true)
                if (transition == null) {
                    progress.snapTo(1f)
                    return@LaunchedEffect
                }
                when (transition) {
                    is GameSessionResult.TimeoutAdvance -> onApplyTransition(transition)
                    is GameSessionResult.ChargeReplay -> {
                        showFeedback(
                            color = ReplayFlashColor,
                            symbol = "\u21BB",
                            durationMillis = REPLAY_FEEDBACK_DURATION_MILLIS,
                            onFinished = { onApplyTransition(transition) },
                        )
                    }
                    is GameSessionResult.LifeLost -> {
                        showFeedback(
                            color = TimeoutFlashColor,
                            onFinished = { onApplyTransition(transition) },
                        )
                    }
                    is GameSessionResult.GameOver -> {
                        showFeedback(
                            color = TimeoutFlashColor,
                            onFinished = { onApplyTransition(transition) },
                        )
                    }
                    is GameSessionResult.CorrectAdvance -> onApplyTransition(transition)
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            GameBoard(
                roundData = state.roundData,
                roundNumber = state.roundNumber,
                pressedDirection = pressedDirection,
                onDirectionTapped = { direction ->
                    if (isResolving || isPaused) return@GameBoard
                    pressedDirection = direction
                    val correct = GameRules.isCorrectTap(state.roundData, direction)
                    if (correct) {
                        isResolving = true
                        val elapsedMillis = ((1f - progress.value) * timeoutMillis)
                            .roundToInt()
                            .coerceIn(0, timeoutMillis)
                        val transition = onResolve(direction, elapsedMillis, false)
                        if (transition == null) {
                            isResolving = false
                            return@GameBoard
                        }
                        when (transition) {
                            is GameSessionResult.CorrectAdvance -> {
                                scope.launch {
                                    progress.stop()
                                    showFeedback(
                                        color = SuccessFlashColor,
                                        symbol = if (transition.earnedCharge) "+\u25A0" else null,
                                        durationMillis = if (transition.earnedCharge) {
                                            CHARGE_EARNED_FEEDBACK_DURATION_MILLIS
                                        } else {
                                            SUCCESS_FEEDBACK_DURATION_MILLIS
                                        },
                                        withHaptics = false,
                                        onFinished = { onApplyTransition(transition) },
                                    )
                                }
                            }
                            else -> onApplyTransition(transition)
                        }
                    } else {
                        scope.launch {
                            val elapsedMillis = ((1f - progress.value) * timeoutMillis)
                                .roundToInt()
                                .coerceIn(0, timeoutMillis)
                            val transition = onResolve(direction, elapsedMillis, false) ?: return@launch
                            progress.stop()
                            when (transition) {
                                is GameSessionResult.ChargeReplay -> {
                                    showFeedback(
                                        color = ReplayFlashColor,
                                        symbol = "\u21BB",
                                        durationMillis = REPLAY_FEEDBACK_DURATION_MILLIS,
                                        onFinished = { onApplyTransition(transition) },
                                    )
                                }
                                is GameSessionResult.LifeLost -> {
                                    showFeedback(
                                        color = WrongTapFlashColor,
                                        onFinished = { onApplyTransition(transition) },
                                    )
                                }
                                is GameSessionResult.GameOver -> {
                                    showFeedback(
                                        color = WrongTapFlashColor,
                                        onFinished = { onApplyTransition(transition) },
                                    )
                                }
                                else -> Unit
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )

            CircularTimeoutIndicator(
                progress = progress,
                uiMetrics = uiMetrics,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(0f),
            )

            OrbitHud(
                lives = state.lives,
                charges = state.charges,
                score = state.score,
                level = if (showLevelIndicator) currentLevel else null,
                isLevelSpeedupActive = isLevelSpeedupActive,
                speedLabel = speedLabel,
                uiMetrics = uiMetrics,
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f),
            )

            CommandCard(
                prompt = state.roundData.prompt,
                hasZoneMarkers = hasZoneMarkers,
                uiMetrics = uiMetrics,
                modifier = Modifier.align(Alignment.Center),
            )

            flashColor?.let { activeFlashColor ->
                FlashOverlay(
                    flashColor = activeFlashColor,
                    flashSymbol = flashSymbol,
                    flashSymbolColor = flashSymbolColor,
                    symbolFontSize = uiMetrics.feedbackSymbolFontSize,
                    animationProgress = flashProgress.value,
                    modifier = Modifier.zIndex(2f),
                )
            }

            if (isPaused) {
                ExitPrompt(
                    onContinue = onContinue,
                    onExit = onExit,
                    uiMetrics = uiMetrics,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}
