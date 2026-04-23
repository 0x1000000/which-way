package x100000.whichway.game

enum class Direction {
    Up,
    Down,
    Left,
    Right,
}

enum class Complexity {
    VeryLow,
    Low,
    Normal,
    High,
    VeryHigh,
}

enum class CommandProfile {
    All,
    DirectionsOnly,
    NumbersOnly,
    MathOnly,
    SuitsOnly,
    TargetsOnly,
}

enum class TimeoutRampMode {
    NormalProgression,
    PracticeImmediateRamp,
}

data class GameConfig(
    val unlockFloor: Int = 0,
    val commandProfile: CommandProfile = CommandProfile.All,
    val timeoutRampMode: TimeoutRampMode = TimeoutRampMode.NormalProgression,
    val speedPercent: Int = 100,
    val skipColors: Boolean = false,
    val skipSuits: Boolean = false,
    val skipNot: Boolean = false,
    val tracksStats: Boolean = true,
    val allowsProgression: Boolean = true,
)

data class RunStats(
    val totalResponseTimeMs: Long = 0,
    val responseCount: Int = 0,
    val totalSpentTimeMs: Long = 0,
) {
    val averageResponseTimeMs: Int
        get() = if (responseCount == 0) {
            0
        } else {
            (totalResponseTimeMs / responseCount).toInt()
        }
}

enum class ColorTarget(
    val label: String,
) {
    Green("GREEN"),
    Blue("BLUE"),
    White("WHITE"),
    Yellow("YELLOW"),
}

enum class SuitTarget(
    val label: String,
    val symbol: String,
) {
    Diamonds("DIAMONDS", "\u2666"),
    Clubs("CLUBS", "\u2663"),
    Spades("SPADES", "\u2660"),
    Hearts("HEARTS", "\u2665"),
}

data class ZoneFacts(
    val color: ColorTarget? = null,
    val number: Int? = null,
    val suit: SuitTarget? = null,
    val target: Boolean = false,
)

data class RoundData(
    val prompt: String,
    val validDirections: Set<Direction>,
    val complexity: Complexity = Complexity.Low,
    val timeoutIsCorrect: Boolean = false,
    val zoneFacts: Map<Direction, ZoneFacts> = emptyMap(),
    val commandId: GameCommand? = null,
)

sealed interface GameScreenState {
    data object Start : GameScreenState

    data class Playing(
        val score: Int,
        val lives: Int,
        val charges: Int,
        val mistakes: Int,
        val roundNumber: Int,
        val isRestartRound: Boolean,
        val config: GameConfig,
        val roundData: RoundData,
    ) : GameScreenState

    data class GameOver(
        val score: Int,
        val newRecord: Boolean,
        val config: GameConfig,
    ) : GameScreenState
}

internal sealed interface GameSessionResult {
    data class CorrectAdvance(
        val state: GameScreenState.Playing,
        val earnedCharge: Boolean,
    ) : GameSessionResult
    data class ChargeReplay(val state: GameScreenState.Playing) : GameSessionResult
    data class LifeLost(val state: GameScreenState.Playing) : GameSessionResult
    data class TimeoutAdvance(val state: GameScreenState.Playing) : GameSessionResult
    data class GameOver(val state: GameScreenState.GameOver) : GameSessionResult
}
