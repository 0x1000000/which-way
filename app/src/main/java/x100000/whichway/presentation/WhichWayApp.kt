package x100000.whichway.presentation

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlin.math.max
import x100000.whichway.data.GameDataRepository
import x100000.whichway.data.SavedGameData
import x100000.whichway.game.CommandProfile
import x100000.whichway.game.GameConfig
import x100000.whichway.game.GameRules
import x100000.whichway.game.GameScreenState
import x100000.whichway.game.GameSession
import x100000.whichway.game.GameSessionResult
import x100000.whichway.game.RunStats
import x100000.whichway.game.TimeoutRampMode
import x100000.whichway.game.TutorialCommands
import x100000.whichway.game.TutorialResult
import x100000.whichway.game.TutorialSession
import x100000.whichway.game.TutorialState

private sealed interface AppScreen {
    data object TutorialPrompt : AppScreen
    data class TutorialInstructions(val forceBeginning: Boolean) : AppScreen
    data object Home : AppScreen
    data object CustomGameMenu : AppScreen
    data object SettingsMenu : AppScreen
    data object StatisticsMenu : AppScreen
    data class Playing(val state: GameScreenState.Playing) : AppScreen
    data class TutorialPlaying(val state: TutorialState) : AppScreen
    data object TutorialComplete : AppScreen
    data class GameOver(
        val state: GameScreenState.GameOver,
        val sessionAverageResponseTimeMs: Int,
        val sessionSpentTimeMs: Long,
    ) : AppScreen
}

@Composable
fun WhichWayApp(repository: GameDataRepository) {
    val context = LocalContext.current
    val loadedSavedData by produceState<SavedGameData?>(initialValue = null, repository) {
        repository.gameDataFlow.collect { value = it }
    }
    val savedData = loadedSavedData ?: return Box(
        modifier = Modifier.fillMaxSize(),
    )
    val scope = rememberCoroutineScope()
    var appScreen by remember {
        mutableStateOf<AppScreen>(
            if (savedData.tutorialPromptDismissed) AppScreen.Home else AppScreen.TutorialPrompt,
        )
    }
    var session by remember { mutableStateOf<GameSession?>(null) }
    var tutorialSession by remember { mutableStateOf<TutorialSession?>(null) }
    var showExitPrompt by remember { mutableStateOf(false) }
    var isPlayingBusy by remember { mutableStateOf(false) }

    fun gameConfig(
        unlockFloor: Int,
        commandProfile: CommandProfile,
        timeoutRampMode: TimeoutRampMode,
        tracksStats: Boolean,
        allowsProgression: Boolean,
        skipSuits: Boolean = savedData.skipSuits,
    ) = GameConfig(
        unlockFloor = unlockFloor,
        commandProfile = commandProfile,
        timeoutRampMode = timeoutRampMode,
        speedPercent = savedData.speedPercent,
        skipColors = savedData.skipColors,
        skipSuits = skipSuits,
        skipNot = savedData.skipNot,
        tracksStats = tracksStats,
        allowsProgression = allowsProgression,
    )

    fun trackedProgressionConfig(unlockFloor: Int = 0) = gameConfig(
        unlockFloor = unlockFloor,
        commandProfile = CommandProfile.All,
        timeoutRampMode = TimeoutRampMode.NormalProgression,
        tracksStats = true,
        allowsProgression = true,
    )

    fun fixedLevelConfig(level: Int) = trackedProgressionConfig(
        unlockFloor = GameRules.unlockScoreForLevel(level),
    )

    fun practiceConfig(
        commandProfile: CommandProfile,
        skipSuits: Boolean = savedData.skipSuits,
    ) = gameConfig(
        unlockFloor = GameRules.maxUnlockScore,
        commandProfile = commandProfile,
        timeoutRampMode = TimeoutRampMode.PracticeImmediateRamp,
        tracksStats = false,
        allowsProgression = false,
        skipSuits = skipSuits,
    )

    fun normalConfig() = trackedProgressionConfig()
    fun directionsOnlyConfig() = practiceConfig(CommandProfile.DirectionsOnly)
    fun numbersOnlyConfig() = practiceConfig(CommandProfile.NumbersOnly)
    fun mathOnlyConfig() = practiceConfig(CommandProfile.MathOnly)
    fun suitsOnlyConfig() = practiceConfig(CommandProfile.SuitsOnly, skipSuits = false)
    fun targetsOnlyConfig() = practiceConfig(CommandProfile.TargetsOnly)

    fun startGame(config: GameConfig) {
        showExitPrompt = false
        isPlayingBusy = false
        tutorialSession = null
        session = GameSession(config = config)
        appScreen = AppScreen.Playing(session!!.snapshot())
    }

    fun startTutorial(forceBeginning: Boolean) {
        showExitPrompt = false
        isPlayingBusy = false
        session = null
        val startIndex = if (forceBeginning || savedData.tutorialCompleted) {
            0
        } else {
            savedData.tutorialChallengeIndex.coerceIn(0, TutorialCommands.all.lastIndex)
        }
        tutorialSession = TutorialSession(startChallengeIndex = startIndex)
        scope.launch {
            repository.saveTutorialProgress(startIndex)
        }
        appScreen = AppScreen.TutorialPlaying(tutorialSession!!.snapshot())
    }

    fun finishGame(
        state: GameScreenState.GameOver,
        runStats: RunStats,
    ) {
        showExitPrompt = false
        isPlayingBusy = false
        session = null
        tutorialSession = null
        scope.launch {
            repository.saveSpentTime(runStats.totalSpentTimeMs)
            repository.saveParticipation(
                responseTimeTotalMs = runStats.totalResponseTimeMs,
                responseCount = runStats.responseCount,
            )
            if (state.config.tracksStats) {
                repository.saveNormalRun(
                    score = state.score,
                    speedPercent = state.config.speedPercent,
                )
            }
        }
        val newRecord = state.config.tracksStats && state.score > savedData.bestScore
        appScreen = AppScreen.GameOver(
            state = state.copy(newRecord = newRecord),
            sessionAverageResponseTimeMs = runStats.averageResponseTimeMs,
            sessionSpentTimeMs = runStats.totalSpentTimeMs,
        )
    }

    fun stopGame(current: GameScreenState.Playing) {
        finishGame(
            state = GameScreenState.GameOver(
                score = current.score,
                newRecord = false,
                config = current.config,
            ),
            runStats = session?.runStats() ?: RunStats(),
        )
    }

    fun applyTransition(result: GameSessionResult) {
        isPlayingBusy = false
        when (result) {
            is GameSessionResult.CorrectAdvance -> appScreen = AppScreen.Playing(result.state)
            is GameSessionResult.ChargeReplay -> appScreen = AppScreen.Playing(result.state)
            is GameSessionResult.LifeLost -> appScreen = AppScreen.Playing(result.state)
            is GameSessionResult.TimeoutAdvance -> appScreen = AppScreen.Playing(result.state)
            is GameSessionResult.GameOver -> finishGame(
                state = result.state,
                runStats = session?.runStats() ?: RunStats(),
            )
        }
    }

    fun applyTutorialResult(result: TutorialResult) {
        when (result) {
            is TutorialResult.CorrectAdvance -> {
                scope.launch {
                    repository.saveTutorialProgress(result.state.challengeIndex)
                }
                appScreen = AppScreen.TutorialPlaying(result.state)
            }
            is TutorialResult.WrongTap -> appScreen = AppScreen.TutorialPlaying(result.state)
            TutorialResult.Complete -> {
                tutorialSession = null
                scope.launch {
                    repository.completeTutorial()
                }
                appScreen = AppScreen.TutorialComplete
            }
        }
    }

    fun dismissTutorialPrompt() {
        scope.launch {
            repository.dismissTutorialPrompt()
        }
        appScreen = AppScreen.Home
    }

    BackHandler {
        when (val screen = appScreen) {
            AppScreen.TutorialPrompt -> dismissTutorialPrompt()
            is AppScreen.TutorialInstructions -> {
                appScreen = if (savedData.tutorialPromptDismissed) {
                    AppScreen.Home
                } else {
                    AppScreen.TutorialPrompt
                }
            }
            AppScreen.Home -> (context as? Activity)?.finish()
            AppScreen.CustomGameMenu,
            AppScreen.SettingsMenu,
            AppScreen.StatisticsMenu,
            -> appScreen = AppScreen.Home

            is AppScreen.Playing -> {
                if (isPlayingBusy) {
                    return@BackHandler
                }
                if (showExitPrompt) {
                    showExitPrompt = false
                    appScreen = AppScreen.Home
                } else {
                    showExitPrompt = true
                }
            }

            is AppScreen.TutorialPlaying -> {
                tutorialSession = null
                appScreen = AppScreen.Home
            }

            AppScreen.TutorialComplete -> appScreen = AppScreen.Home

            is AppScreen.GameOver -> {
                showExitPrompt = false
                appScreen = AppScreen.Home
            }
        }
    }

    val discoveredLevel = GameRules.levelForScore(savedData.bestScore)
    val halfDiscoveredLevel = (discoveredLevel * 0.5f).toInt()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val screen = appScreen) {
            AppScreen.TutorialPrompt -> TutorialPromptScreen(
                onStartTutorial = {
                    appScreen = AppScreen.TutorialInstructions(forceBeginning = true)
                },
                onSkip = ::dismissTutorialPrompt,
            )

            is AppScreen.TutorialInstructions -> TutorialInstructionsScreen(
                onStartTutorial = { startTutorial(forceBeginning = screen.forceBeginning) },
            )

            AppScreen.Home -> HomeScreen(
                onStart = { startGame(normalConfig()) },
                onTutorial = {
                    appScreen = AppScreen.TutorialInstructions(forceBeginning = false)
                },
                onCustomGame = { appScreen = AppScreen.CustomGameMenu },
                onSettings = { appScreen = AppScreen.SettingsMenu },
                onStatistics = { appScreen = AppScreen.StatisticsMenu },
            )

            AppScreen.CustomGameMenu -> CustomGameScreen(
                discoveredLevel = discoveredLevel,
                halfDiscoveredLevel = halfDiscoveredLevel,
                onStartWithLastLevels = { startGame(fixedLevelConfig(discoveredLevel)) },
                onStartWithHalfLevels = { startGame(fixedLevelConfig(halfDiscoveredLevel)) },
                onDirectionsOnly = { startGame(directionsOnlyConfig()) },
                onNumbersOnly = { startGame(numbersOnlyConfig()) },
                onMathOnly = { startGame(mathOnlyConfig()) },
                onSuitsOnly = { startGame(suitsOnlyConfig()) },
                onTargetsOnly = { startGame(targetsOnlyConfig()) },
                onBack = { appScreen = AppScreen.Home },
            )

            AppScreen.SettingsMenu -> SettingsScreen(
                savedData = savedData,
                onSetSpeedPercent = { speedPercent ->
                    scope.launch {
                        repository.updateSpeedPercent(speedPercent)
                    }
                },
                onToggleSkipColors = {
                    scope.launch {
                        repository.updateSkipColors(!savedData.skipColors)
                    }
                },
                onToggleSkipSuits = {
                    scope.launch {
                        repository.updateSkipSuits(!savedData.skipSuits)
                    }
                },
                onToggleSkipNot = {
                    scope.launch {
                        repository.updateSkipNot(!savedData.skipNot)
                    }
                },
                onResetTutorial = {
                    tutorialSession = null
                    scope.launch {
                        repository.resetTutorial()
                    }
                    appScreen = AppScreen.TutorialPrompt
                },
                onBack = { appScreen = AppScreen.Home },
            )

            AppScreen.StatisticsMenu -> StatisticsScreen(
                savedData = savedData,
                onBack = { appScreen = AppScreen.Home },
            )

            is AppScreen.Playing -> PlayingScreen(
                state = screen.state,
                isPaused = showExitPrompt,
                onContinue = { showExitPrompt = false },
                onExit = {
                    val activeScreen = appScreen
                    if (activeScreen is AppScreen.Playing) {
                        stopGame(activeScreen.state)
                    }
                },
                onResolve = { direction, elapsedMillis, isTimeout ->
                    val activeSession = session
                    val activeScreen = appScreen
                    if (activeSession == null ||
                        activeScreen !is AppScreen.Playing ||
                        activeScreen.state.roundNumber != screen.state.roundNumber
                    ) {
                        return@PlayingScreen null
                    }

                    if (isTimeout) {
                        activeSession.onTimeout(elapsedMillis)
                    } else {
                        activeSession.onZoneClick(direction = direction!!, elapsedMillis = elapsedMillis)
                    }
                },
                onBusyStateChanged = { busy -> isPlayingBusy = busy },
                onApplyTransition = ::applyTransition,
            )

            is AppScreen.TutorialPlaying -> TutorialPlayingScreen(
                state = screen.state,
                onResolveTap = { direction ->
                    val activeSession = tutorialSession
                    val activeScreen = appScreen
                    if (activeSession == null ||
                        activeScreen !is AppScreen.TutorialPlaying ||
                        activeScreen.state.challengeIndex != screen.state.challengeIndex
                    ) {
                        return@TutorialPlayingScreen null
                    }
                    activeSession.onZoneClick(direction)
                },
                onResolveManualAdvance = {
                    val activeSession = tutorialSession
                    val activeScreen = appScreen
                    if (activeSession == null ||
                        activeScreen !is AppScreen.TutorialPlaying ||
                        activeScreen.state.challengeIndex != screen.state.challengeIndex
                    ) {
                        return@TutorialPlayingScreen null
                    }
                    activeSession.onManualAdvance()
                },
                onApplyResult = ::applyTutorialResult,
            )

            AppScreen.TutorialComplete -> TutorialCompleteScreen(
                onRestart = {
                    appScreen = AppScreen.TutorialInstructions(forceBeginning = true)
                },
                onBackHome = { appScreen = AppScreen.Home },
            )

            is AppScreen.GameOver -> GameOverScreen(
                state = screen.state,
                bestScore = if (screen.state.config.tracksStats) {
                    max(savedData.bestScore, screen.state.score)
                } else {
                    savedData.bestScore
                },
                bestScoreSpeedPercent = if (screen.state.newRecord && screen.state.config.tracksStats) {
                    screen.state.config.speedPercent
                } else {
                    savedData.bestScoreSpeedPercent
                },
                averageResponseTimeMs = screen.sessionAverageResponseTimeMs,
                sessionSpentTimeMs = screen.sessionSpentTimeMs,
                onRestart = { startGame(screen.state.config) },
                onBackToStart = {
                    showExitPrompt = false
                    appScreen = AppScreen.Home
                },
            )
        }
    }
}
