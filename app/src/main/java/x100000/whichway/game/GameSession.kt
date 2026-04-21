package x100000.whichway.game

import kotlin.random.Random

internal class GameSession(
    private val config: GameConfig,
    private val random: Random = Random.Default,
) {
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
            roundData = GameRules.nextRound(score = 0, previousScore = 0, previousRound = null, config = config, random = random),
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
                roundData = GameRules.nextRound(
                    score = nextScore,
                    previousScore = current.score,
                    previousRound = current.roundData,
                    config = current.config,
                    random = random,
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
                roundData = GameRules.nextRound(
                    score = current.score,
                    previousScore = current.score,
                    previousRound = current.roundData,
                    config = current.config,
                    random = random,
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
            roundData = GameRules.nextRound(
                score = current.score,
                previousScore = current.score,
                previousRound = current.roundData,
                config = current.config,
                random = random,
            ),
        )
        return GameSessionResult.LifeLost(state)
    }
}
