package x100000.whichway.game

import kotlin.random.Random

internal data class TutorialState(
    val challengeIndex: Int,
    val totalChallenges: Int,
    val roundData: RoundData,
) {
    val displayChallengeNumber: Int
        get() = challengeIndex + 1

    val requiresManualAdvance: Boolean
        get() = roundData.validDirections.isEmpty() && roundData.timeoutIsCorrect
}

internal sealed interface TutorialResult {
    data class CorrectAdvance(val state: TutorialState) : TutorialResult
    data class WrongTap(val state: TutorialState) : TutorialResult
    data object Complete : TutorialResult
}

internal class TutorialSession(
    startChallengeIndex: Int = 0,
    private val random: Random = Random.Default,
) {
    private val commands = TutorialCommands.all
    private var state = createState(
        challengeIndex = startChallengeIndex.coerceIn(0, commands.lastIndex),
    )

    fun snapshot(): TutorialState = state

    fun onZoneClick(direction: Direction): TutorialResult {
        if (!GameRules.isCorrectTap(state.roundData, direction)) {
            return TutorialResult.WrongTap(state)
        }
        return advance()
    }

    fun onManualAdvance(): TutorialResult {
        if (!state.requiresManualAdvance) {
            return TutorialResult.WrongTap(state)
        }
        return advance()
    }

    private fun advance(): TutorialResult {
        val nextIndex = state.challengeIndex + 1
        if (nextIndex >= commands.size) {
            return TutorialResult.Complete
        }
        state = createState(nextIndex)
        return TutorialResult.CorrectAdvance(state)
    }

    private fun createState(challengeIndex: Int): TutorialState =
        TutorialState(
            challengeIndex = challengeIndex,
            totalChallenges = commands.size,
            roundData = GameRules.roundForCommand(
                commandId = commands[challengeIndex],
                random = random,
            ),
        )
}

internal object TutorialCommands {
    val all: List<GameCommand> = listOf(
        GameCommand.LEFT,
        GameCommand.UP,
        GameCommand.NOT_LEFT,
        GameCommand.NOTHING,
        GameCommand.NOT_NOTHING,
        GameCommand.TARGET,
        GameCommand.GREEN,
        GameCommand.NUMBER,
        GameCommand.OR_UP_DOWN,
        GameCommand.EVEN,
        GameCommand.LESS_THAN,
        GameCommand.GREEN_OR_UP,
        GameCommand.HEARTS,
        GameCommand.ADDITION,
        GameCommand.NOT_GREEN_AND_NOT_UP,
    )
}
