package x100000.whichway.game

import kotlin.random.Random

internal class GameSession(
    private val config: GameConfig,
    private val random: Random = Random.Default,
) {
    private val commandQueue = mutableListOf<GameCommand>()
    private val recentAnswerSets = ArrayDeque<Set<Direction>>()
    private var totalResponseTimeMs = 0L
    private var responseCount = 0
    private var totalSpentTimeMs = 0L
    private var state = GameScreenState.Playing(
        score = 0,
        lives = GameRules.STARTING_LIVES,
        charges = 0,
        mistakes = 0,
        roundNumber = 0,
        isRestartRound = false,
        config = config,
        roundData = nextRoundData(
            score = 0,
            previousScore = 0,
        ),
    )

    fun snapshot(): GameScreenState.Playing = state

    fun runStats(): RunStats = RunStats(
        totalResponseTimeMs = totalResponseTimeMs,
        responseCount = responseCount,
        totalSpentTimeMs = totalSpentTimeMs,
    )

    fun onZoneClick(
        direction: Direction,
        elapsedMillis: Int,
    ): GameSessionResult {
        val current = state
        totalSpentTimeMs += elapsedMillis.toLong()
        totalResponseTimeMs += elapsedMillis.toLong()
        responseCount += 1
        val correct = GameRules.isCorrectTap(current.roundData, direction)
        return if (correct) {
            val nextScore = current.score + 1
            val earnedCharge = !current.isRestartRound &&
                current.charges < GameRules.MAX_CHARGES &&
                GameRules.earnsCharge(
                    complexity = current.roundData.complexity,
                    elapsedMillis = elapsedMillis,
                    score = current.score,
                    config = current.config,
                )
            state = current.copy(
                score = nextScore,
                charges = if (earnedCharge) {
                    minOf(current.charges + 1, GameRules.MAX_CHARGES)
                } else {
                    current.charges
                },
                roundNumber = current.roundNumber + 1,
                isRestartRound = false,
                roundData = nextRoundData(
                    score = nextScore,
                    previousScore = current.score,
                ),
            )
            GameSessionResult.CorrectAdvance(
                state = state,
                earnedCharge = earnedCharge,
            )
        } else {
            onMistake()
        }
    }

    fun onTimeout(elapsedMillis: Int): GameSessionResult {
        val current = state
        totalSpentTimeMs += elapsedMillis.toLong()
        return if (GameRules.isCorrectTimeout(current.roundData)) {
            state = current.copy(
                roundNumber = current.roundNumber + 1,
                isRestartRound = false,
                roundData = nextRoundData(
                    score = current.score,
                    previousScore = current.score,
                ),
            )
            GameSessionResult.TimeoutAdvance(state)
        } else {
            onMistake()
        }
    }

    private fun onMistake(): GameSessionResult {
        val current = state
        if (current.charges > 0) {
            state = current.copy(
                charges = current.charges - 1,
                mistakes = current.mistakes + 1,
                roundNumber = current.roundNumber + 1,
                isRestartRound = true,
            )
            return GameSessionResult.ChargeReplay(state)
        }

        val nextLives = current.lives - 1
        if (nextLives <= 0) {
            return GameSessionResult.GameOver(
                GameScreenState.GameOver(
                    score = current.score,
                    newRecord = false,
                    config = current.config,
                ),
            )
        }

        state = current.copy(
            lives = nextLives,
            mistakes = current.mistakes + 1,
            roundNumber = current.roundNumber + 1,
            isRestartRound = false,
            roundData = nextRoundData(
                score = current.score,
                previousScore = current.score,
            ),
        )
        return GameSessionResult.LifeLost(state)
    }

    private fun nextRoundData(
        score: Int,
        previousScore: Int,
    ): RoundData {
        val commands = availableCommandsFor(score)
        val previousCommands = availableCommandsFor(previousScore)
        val newlyUnlockedCommands = commands - previousCommands.toSet()
        val roundData = when {
            newlyUnlockedCommands.isNotEmpty() ->
                GameRules.roundForCommand(
                    commandId = selectUnlockedCommand(commands, newlyUnlockedCommands),
                    random = random,
                )
            usesQueueSelection(commands) ->
                GameRules.roundForCommand(
                    commandId = popQueuedCommand(commands),
                    random = random,
                )
            else -> nextRandomRoundAvoidingTripleAnswers(commands)
        }
        rememberAnswerSet(roundData.validDirections)
        return roundData
    }

    private fun selectUnlockedCommand(
        commands: List<GameCommand>,
        newlyUnlockedCommands: List<GameCommand>,
    ): GameCommand {
        val forcedCommand = newlyUnlockedCommands.random(random)
        if (!usesQueueSelection(commands)) {
            commandQueue.clear()
            return forcedCommand
        }

        if (commandQueue.isEmpty()) {
            rebuildQueue(commands.filter { it != forcedCommand })
        } else {
            commandQueue += newlyUnlockedCommands.filter { it != forcedCommand && it !in commandQueue }
            commandQueue.shuffle(random)
        }
        commandQueue.remove(forcedCommand)
        commandQueue.add(0, forcedCommand)
        return commandQueue.removeAt(0)
    }

    private fun popQueuedCommand(commands: List<GameCommand>): GameCommand {
        if (commandQueue.isEmpty()) {
            rebuildQueue(commands)
        }
        return commandQueue.removeAt(0)
    }

    private fun nextRandomRoundAvoidingTripleAnswers(commands: List<GameCommand>): RoundData {
        var candidate = GameRules.roundForCommand(commands.random(random), random)
        repeat(RANDOM_ANSWER_REROLL_ATTEMPTS) {
            if (!wouldMakeTripleAnswer(candidate.validDirections)) {
                return candidate
            }
            candidate = GameRules.roundForCommand(commands.random(random), random)
        }
        return candidate
    }

    private fun wouldMakeTripleAnswer(answerSet: Set<Direction>): Boolean =
        recentAnswerSets.size >= 2 &&
            recentAnswerSets[recentAnswerSets.lastIndex] == answerSet &&
            recentAnswerSets[recentAnswerSets.lastIndex - 1] == answerSet

    private fun rememberAnswerSet(answerSet: Set<Direction>) {
        recentAnswerSets += answerSet
        while (recentAnswerSets.size > RECENT_ANSWER_HISTORY_SIZE) {
            recentAnswerSets.removeFirst()
        }
    }

    private fun rebuildQueue(commands: List<GameCommand>) {
        commandQueue.clear()
        commandQueue.addAll(commands)
        commandQueue.shuffle(random)
    }

    private fun usesQueueSelection(commands: List<GameCommand>): Boolean =
        commands.size >= MIN_COMMANDS_FOR_QUEUE_SELECTION

    private fun availableCommandsFor(score: Int): List<GameCommand> =
        GameRules.commandIdsForScore(
            score = GameRules.effectiveContentScoreFor(score, config),
            commandProfile = config.commandProfile,
            skipColors = config.skipColors,
            skipSuits = config.skipSuits,
            skipNot = config.skipNot,
        )

    private companion object {
        private const val MIN_COMMANDS_FOR_QUEUE_SELECTION = 6
        private const val RANDOM_ANSWER_REROLL_ATTEMPTS = 4
        private const val RECENT_ANSWER_HISTORY_SIZE = 2
    }
}
