package x100000.whichway.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import x100000.whichway.game.Direction
import x100000.whichway.game.TutorialResult
import x100000.whichway.game.TutorialState

private const val TUTORIAL_ADVANCE_FEEDBACK_MILLIS = 140L
private const val TUTORIAL_WRONG_FEEDBACK_MILLIS = 650L
private const val TUTORIAL_NO_TAP_DISPLAY_MILLIS = 2_000L

@Composable
internal fun TutorialPromptScreen(
    onStartTutorial: () -> Unit,
    onSkip: () -> Unit,
) {
    MenuScreen(
        title = "Tutorial?",
        items = listOf(
            MenuItem.TextLine("Learn each challenge with no timer."),
            MenuItem.Action(
                text = "Skip",
                onClick = onSkip,
            ),
            MenuItem.Action(
                text = "Start Tutorial",
                onClick = onStartTutorial,
            ),
        ),
    )
}

@Composable
internal fun TutorialInstructionsScreen(
    onStartTutorial: () -> Unit,
) {
    MenuScreen(
        title = "How to Play",
        items = listOf(
            MenuItem.TextLine(
                "Read the command. Tap the highlighted zone(s): left, right, top, or bottom.",
                maxLines = 4,
            ),
            MenuItem.Action(
                text = "Continue",
                onClick = onStartTutorial,
            ),
        ),
    )
}

@Composable
internal fun TutorialCompleteScreen(
    onRestart: () -> Unit,
    onBackHome: () -> Unit,
) {
    MenuScreen(
        title = "Tutorial Done",
        items = listOf(
            MenuItem.TextLine(
                text = "Nice. Every challenge is now unlocked in your brain.",
                maxLines = 4,
            ),
            MenuItem.Action(
                text = "To Main Menu",
                onClick = onBackHome,
            ),
            MenuItem.Action(
                text = "Restart Tutorial",
                onClick = onRestart,
            ),
        ),
    )
}

@Composable
internal fun TutorialPlayingScreen(
    state: TutorialState,
    onResolveTap: (Direction) -> TutorialResult?,
    onResolveManualAdvance: () -> TutorialResult?,
    onApplyResult: (TutorialResult) -> Unit,
) {
    val uiMetrics = rememberWatchUiMetrics()
    val scope = rememberCoroutineScope()
    val highlightTransition = rememberInfiniteTransition(label = "tutorialHighlight")
    val highlightAlpha by highlightTransition.animateFloat(
        initialValue = 0.26f,
        targetValue = 0.50f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1_600,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "tutorialHighlightAlpha",
    )
    var pressedDirection by remember { mutableStateOf<Direction?>(null) }
    var wrongHintVersion by remember { mutableIntStateOf(0) }
    var showWrongHint by remember { mutableStateOf(false) }
    var isResolving by remember { mutableStateOf(false) }
    val hasZoneMarkers = remember(state.roundData) {
        state.roundData.zoneFacts.values.any {
            it.color != null || it.number != null || it.suit != null || it.target
        }
    }

    LaunchedEffect(state.challengeIndex) {
        pressedDirection = null
        showWrongHint = false
        isResolving = false
    }

    LaunchedEffect(wrongHintVersion) {
        if (wrongHintVersion == 0) {
            return@LaunchedEffect
        }
        showWrongHint = true
        delay(TUTORIAL_WRONG_FEEDBACK_MILLIS)
        showWrongHint = false
        pressedDirection = null
    }

    fun handleResult(result: TutorialResult?) {
        when (result) {
            null -> Unit
            is TutorialResult.WrongTap -> {
                isResolving = false
                wrongHintVersion += 1
            }
            is TutorialResult.CorrectAdvance,
            TutorialResult.Complete,
            -> {
                isResolving = true
                scope.launch {
                    delay(TUTORIAL_ADVANCE_FEEDBACK_MILLIS)
                    onApplyResult(result)
                }
            }
        }
    }

    LaunchedEffect(state.challengeIndex, state.requiresManualAdvance, wrongHintVersion) {
        if (state.requiresManualAdvance) {
            delay(TUTORIAL_NO_TAP_DISPLAY_MILLIS)
            handleResult(onResolveManualAdvance())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor),
    ) {
        GameBoard(
            roundData = state.roundData,
            roundNumber = state.challengeIndex,
            pressedDirection = pressedDirection,
            onDirectionTapped = { direction ->
                if (isResolving) return@GameBoard
                pressedDirection = direction
                handleResult(onResolveTap(direction))
            },
            highlightDirections = state.roundData.validDirections,
            highlightAlpha = highlightAlpha,
            modifier = Modifier.fillMaxSize(),
        )

        Text(
            text = "Challenge ${state.displayChallengeNumber} / ${state.totalChallenges}",
            color = HudTextColor,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                lineHeight = 10.sp,
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 12.dp, start = 42.dp, end = 42.dp)
                .zIndex(1f),
        )

        CommandCard(
            prompt = if (state.requiresManualAdvance) "DO NOT TAP" else state.roundData.prompt,
            hasZoneMarkers = hasZoneMarkers,
            uiMetrics = uiMetrics,
            modifier = Modifier.align(Alignment.Center),
        )

        if (state.requiresManualAdvance || showWrongHint) {
            Text(
                text = if (state.requiresManualAdvance) {
                    "Correct answer: wait."
                } else {
                    "Try the highlighted answer."
                },
                color = if (showWrongHint) WrongTapFlashColor else ChargeOnColor,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Clip,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 86.dp, start = 18.dp, end = 18.dp)
                    .zIndex(1f),
            )
        }

    }
}
