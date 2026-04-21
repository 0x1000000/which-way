package x100000.whichway.game

import kotlin.math.roundToInt
import kotlin.random.Random

object GameRules {
    const val STARTING_LIVES = 3
    const val MAX_CHARGES = 3
    const val MIN_TIMEOUT_MILLIS = 200
    const val VERY_LOW_COMPLEXITY_TIMEOUT_MILLIS = 1_500
    const val LOW_COMPLEXITY_TIMEOUT_MILLIS = 2_000
    const val NORMAL_COMPLEXITY_TIMEOUT_MILLIS = 3_000
    const val HIGH_COMPLEXITY_TIMEOUT_MILLIS = 4_000
    const val VERY_HIGH_COMPLEXITY_TIMEOUT_MILLIS = 5_000
    const val TIMEOUT_REDUCTION_SCORE_STEP = 5
    const val TIMEOUT_REDUCTION_STEP_PERCENT = 3
    const val TIMEOUT_GRACE_MILLIS = 200
    const val FAST_CHARGE_THRESHOLD_PROGRESS = 0.65f
    private const val REROLL_ATTEMPTS = 4
    private const val MIN_COMMANDS_FOR_REPEAT_AVOIDANCE = 8
    private const val NON_BREAKING_SPACE = '\u00A0'

    val maxUnlockScore: Int
        get() = Level.entries.last().unlockScore

    private val lastCommandLevel = Level.entries.last().ordinal + 1
    private val lastCommandUnlockScore = Level.entries.last().unlockScore
    private val timeoutReductionFractionPerStep = TIMEOUT_REDUCTION_STEP_PERCENT / 100f
    private val maxTimeoutReductionSteps =
        generateSequence(0 to VERY_HIGH_COMPLEXITY_TIMEOUT_MILLIS) { (steps, timeoutMillis) ->
            if (timeoutMillis <= MIN_TIMEOUT_MILLIS) {
                null
            } else {
                (steps + 1) to reducedTimeoutMillis(VERY_HIGH_COMPLEXITY_TIMEOUT_MILLIS, steps + 1)
            }
        }.last().first
    private val maxLevel = lastCommandLevel + maxTimeoutReductionSteps

    fun levelForScore(score: Int): Int {
        if (score < lastCommandUnlockScore) {
            return Level.forScore(score).ordinal + 1
        }

        val postCommandSteps = ((score - lastCommandUnlockScore) / TIMEOUT_REDUCTION_SCORE_STEP)
            .coerceAtLeast(0)
            .coerceAtMost(maxTimeoutReductionSteps)
        return (lastCommandLevel + postCommandSteps).coerceAtMost(maxLevel)
    }

    fun unlockScoreForLevel(level: Int): Int {
        val clampedLevel = level.coerceIn(1, maxLevel)
        if (clampedLevel <= lastCommandLevel) {
            return Level.forNumber(clampedLevel).unlockScore
        }

        val postCommandLevels = clampedLevel - lastCommandLevel
        return lastCommandUnlockScore + postCommandLevels * TIMEOUT_REDUCTION_SCORE_STEP
    }

    fun commandIdsForScore(
        score: Int,
        commandProfile: CommandProfile = CommandProfile.All,
        skipColors: Boolean = false,
        skipSuits: Boolean = false,
        skipNot: Boolean = false,
    ): List<GameCommand> =
        buildList {
            add(GameCommand.LEFT)
            add(GameCommand.RIGHT)
            if (score >= Level.Level2.unlockScore) {
                add(GameCommand.UP)
                add(GameCommand.DOWN)
            }
            if (score >= Level.Level3.unlockScore) {
                add(GameCommand.NOT_LEFT)
                add(GameCommand.NOT_RIGHT)
                add(GameCommand.NOT_UP)
                add(GameCommand.NOT_DOWN)
            }
            if (score >= Level.Level4.unlockScore) {
                add(GameCommand.NOTHING)
            }
            if (score >= Level.Level5.unlockScore) {
                add(GameCommand.NOT_NOTHING)
            }
            if (score >= Level.Level6.unlockScore) {
                add(GameCommand.TARGET)
                add(GameCommand.NOT_TARGET)
            }
            if (score >= Level.Level7.unlockScore) {
                add(GameCommand.GREEN)
                add(GameCommand.BLUE)
                add(GameCommand.WHITE)
                add(GameCommand.YELLOW)
            }
            if (score >= Level.Level8.unlockScore) {
                add(GameCommand.DIAMONDS)
                add(GameCommand.CLUBS)
                add(GameCommand.SPADES)
                add(GameCommand.HEARTS)
            }
            if (score >= Level.Level9.unlockScore) {
                add(GameCommand.OR_UP_DOWN)
                add(GameCommand.OR_UP_LEFT)
                add(GameCommand.OR_UP_RIGHT)
                add(GameCommand.OR_DOWN_LEFT)
                add(GameCommand.OR_DOWN_RIGHT)
                add(GameCommand.OR_LEFT_RIGHT)
            }
            if (score >= Level.Level10.unlockScore) {
                add(GameCommand.NOT_GREEN)
                add(GameCommand.NOT_BLUE)
                add(GameCommand.NOT_WHITE)
                add(GameCommand.NOT_YELLOW)
            }
            if (score >= Level.Level11.unlockScore) {
                add(GameCommand.NOT_DIAMONDS)
                add(GameCommand.NOT_CLUBS)
                add(GameCommand.NOT_SPADES)
                add(GameCommand.NOT_HEARTS)
            }
            if (score >= Level.Level12.unlockScore) {
                add(GameCommand.OR_GREEN_BLUE)
                add(GameCommand.OR_GREEN_WHITE)
                add(GameCommand.OR_GREEN_YELLOW)
                add(GameCommand.OR_BLUE_WHITE)
                add(GameCommand.OR_BLUE_YELLOW)
                add(GameCommand.OR_WHITE_YELLOW)
            }
            if (score >= Level.Level13.unlockScore) {
                add(GameCommand.GREEN_OR_UP)
                add(GameCommand.BLUE_OR_RIGHT)
                add(GameCommand.WHITE_OR_DOWN)
                add(GameCommand.YELLOW_OR_LEFT)
            }
            if (score >= Level.Level14.unlockScore) {
                add(GameCommand.HEARTS_OR_UP)
                add(GameCommand.SPADES_OR_RIGHT)
                add(GameCommand.DIAMONDS_OR_DOWN)
                add(GameCommand.CLUBS_OR_LEFT)
            }
            if (score >= Level.Level15.unlockScore) {
                add(GameCommand.NOT_GREEN_AND_NOT_UP)
                add(GameCommand.NOT_BLUE_AND_NOT_RIGHT)
                add(GameCommand.NOT_WHITE_AND_NOT_DOWN)
                add(GameCommand.NOT_YELLOW_AND_NOT_LEFT)
            }
            if (score >= Level.Level16.unlockScore) {
                add(GameCommand.NUMBER)
            }
            if (score >= Level.Level17.unlockScore) {
                add(GameCommand.EVEN)
                add(GameCommand.ODD)
            }
            if (score >= Level.Level18.unlockScore) {
                add(GameCommand.NOT_NUMBER)
            }
            if (score >= Level.Level19.unlockScore) {
                add(GameCommand.LESS_THAN)
                add(GameCommand.GREATER_THAN)
            }
            if (score >= Level.Level20.unlockScore) {
                add(GameCommand.UP_OR_NUMBER)
                add(GameCommand.RIGHT_OR_NUMBER)
                add(GameCommand.DOWN_OR_NUMBER)
                add(GameCommand.LEFT_OR_NUMBER)
            }
            if (score >= Level.Level21.unlockScore) {
                add(GameCommand.GREEN_OR_NUMBER)
                add(GameCommand.BLUE_OR_NUMBER)
                add(GameCommand.WHITE_OR_NUMBER)
                add(GameCommand.YELLOW_OR_NUMBER)
            }
            if (score >= Level.Level22.unlockScore) {
                add(GameCommand.ADDITION)
                add(GameCommand.SUBTRACTION)
            }
            if (score >= Level.Level23.unlockScore) {
                add(GameCommand.LESS_THAN_ARITHMETIC)
                add(GameCommand.GREATER_THAN_ARITHMETIC)
            }
            if (score >= Level.Level24.unlockScore) {
                add(GameCommand.NOT_ADDITION)
                add(GameCommand.NOT_SUBTRACTION)
            }
        }
            .filter { command -> matchesCommandProfile(command, commandProfile) }
            .filter { command -> !skipColors || !command.hasTag(CommandTag.Color) }
            .filter { command -> !skipSuits || !command.hasTag(CommandTag.Suit) }
            .filter { command -> !skipNot || !command.hasTag(CommandTag.Not) }

    fun nextRound(
        score: Int,
        previousScore: Int = score,
        previousRound: RoundData?,
        config: GameConfig = GameConfig(),
        random: Random = Random.Default,
    ): RoundData {
        val effectiveScore = effectiveContentScoreFor(score, config)
        val previousEffectiveScore = effectiveContentScoreFor(previousScore, config)
        val commands = commandIdsForScore(
            score = effectiveScore,
            commandProfile = config.commandProfile,
            skipColors = config.skipColors,
            skipSuits = config.skipSuits,
            skipNot = config.skipNot,
        )
        val newlyUnlockedCommands = if (effectiveScore > previousEffectiveScore) {
            commands - commandIdsForScore(
                score = previousEffectiveScore,
                commandProfile = config.commandProfile,
                skipColors = config.skipColors,
                skipSuits = config.skipSuits,
                skipNot = config.skipNot,
            ).toSet()
        } else {
            emptyList()
        }
        if (previousRound == null) {
            return roundForCommand(commands.random(random), random)
        }
        if (newlyUnlockedCommands.isNotEmpty()) {
            return roundForCommand(newlyUnlockedCommands.random(random), random)
        }
        if (commands.size < MIN_COMMANDS_FOR_REPEAT_AVOIDANCE) {
            return roundForCommand(commands.random(random), random)
        }
        var candidate = roundForCommand(commands.random(random), random)
        repeat(REROLL_ATTEMPTS) {
            if (candidate.commandId != previousRound.commandId) {
                return candidate
            }
            candidate = roundForCommand(commands.random(random), random)
        }

        commands.shuffled(random).forEach { command ->
            val distinctCommandCandidate = roundForCommand(command, random)
            if (distinctCommandCandidate.commandId != previousRound.commandId) {
                return distinctCommandCandidate
            }
        }

        return candidate
    }

    fun roundForCommand(
        commandId: GameCommand,
        random: Random = Random.Default,
    ): RoundData = commandId.createRound(random)

    fun isCorrectTap(roundData: RoundData, tappedDirection: Direction): Boolean =
        tappedDirection in roundData.validDirections

    fun isCorrectTimeout(roundData: RoundData): Boolean =
        roundData.timeoutIsCorrect

    fun earnsCharge(
        complexity: Complexity,
        elapsedMillis: Int,
        score: Int,
        config: GameConfig,
    ): Boolean {
        val chargeWindowMillis = (timeoutMillisFor(complexity, score, config) * (1f - FAST_CHARGE_THRESHOLD_PROGRESS))
            .roundToInt()
        return elapsedMillis <= chargeWindowMillis
    }

    fun timeoutMillisFor(complexity: Complexity): Int =
        when (complexity) {
            Complexity.VeryLow -> VERY_LOW_COMPLEXITY_TIMEOUT_MILLIS
            Complexity.Low -> LOW_COMPLEXITY_TIMEOUT_MILLIS
            Complexity.Normal -> NORMAL_COMPLEXITY_TIMEOUT_MILLIS
            Complexity.High -> HIGH_COMPLEXITY_TIMEOUT_MILLIS
            Complexity.VeryHigh -> VERY_HIGH_COMPLEXITY_TIMEOUT_MILLIS
        }

    fun timeoutMillisFor(
        complexity: Complexity,
        speedPercent: Int,
    ): Int =
        ((timeoutMillisFor(complexity).toFloat() * 100f) / speedPercent.coerceAtLeast(1))
            .roundToInt()
            .coerceAtLeast(1)

    fun timeoutMillisFor(
        complexity: Complexity,
        score: Int,
        config: GameConfig,
    ): Int {
        val effectiveScore = effectiveTimeoutScoreFor(score, config)
        val rampedTimeoutMillis = reducedTimeoutMillis(
            baseTimeoutMillis = timeoutMillisFor(complexity),
            reductionSteps = timeoutReductionSteps(effectiveScore, config.timeoutRampMode),
        )
        return ((rampedTimeoutMillis.toFloat() * 100f) / config.speedPercent.coerceAtLeast(1))
            .roundToInt()
            .coerceAtLeast(1)
    }

    fun isScoreSpeedupActive(
        score: Int,
        config: GameConfig,
    ): Boolean {
        val effectiveScore = effectiveTimeoutScoreFor(score, config)
        return timeoutReductionSteps(effectiveScore, config.timeoutRampMode) > 0
    }

    internal fun effectiveContentScoreFor(
        score: Int,
        config: GameConfig,
    ): Int =
        if (config.allowsProgression) {
            config.unlockFloor + score.coerceAtLeast(0)
        } else {
            config.unlockFloor
        }

    internal fun effectiveTimeoutScoreFor(
        score: Int,
        config: GameConfig,
    ): Int =
        if (config.allowsProgression) {
            config.unlockFloor + score.coerceAtLeast(0)
        } else {
            score.coerceAtLeast(0)
        }

    fun speedLabelFor(
        complexity: Complexity,
        score: Int,
        config: GameConfig,
    ): String {
        val baseTimeoutMillis = timeoutMillisFor(complexity)
        val effectiveTimeoutMillis = timeoutMillisFor(complexity, score, config)
        val deltaPercent = (((baseTimeoutMillis - effectiveTimeoutMillis).toFloat() / baseTimeoutMillis) * 100f)
            .roundToInt()
        return when {
            deltaPercent > 0 -> "S:+${deltaPercent}%"
            deltaPercent < 0 -> "S:${deltaPercent}%"
            else -> "S:0%"
        }
    }

    internal fun stackedNotPhrase(operand: String): String =
        "NOT\n$operand"

    internal fun inlineNotPhrase(operand: String): String =
        "NOT${NON_BREAKING_SPACE}$operand"

    internal fun threeLinePrompt(
        first: String,
        operator: String,
        second: String,
    ): String = "$first\n$operator\n$second"

    private fun matchesCommandProfile(
        command: GameCommand,
        commandProfile: CommandProfile,
    ): Boolean =
        when (commandProfile) {
            CommandProfile.All -> true
            CommandProfile.DirectionsOnly -> command.isDirectionsOnlyCommand()
            CommandProfile.NumbersOnly -> command.isNumbersOnlyCommand()
            CommandProfile.SuitsOnly -> command.isTargetingOnlyTag(CommandTag.Suit)
            CommandProfile.TargetsOnly -> command.isTargetingOnlyTag(CommandTag.Target)
        }

    private fun GameCommand.isDirectionsOnlyCommand(): Boolean =
        isPureDomain(CommandTag.Direction) || hasTag(CommandTag.Nothing)

    private fun GameCommand.isNumbersOnlyCommand(): Boolean =
        isPureDomain(CommandTag.Number)

    private fun GameCommand.isTargetingOnlyTag(tag: CommandTag): Boolean =
        isPureDomain(tag)

    private fun GameCommand.isPureDomain(tag: CommandTag): Boolean =
        hasTag(tag) && !tags.any { it in DOMAIN_TAGS && it != tag }

    private fun GameCommand.hasTag(tag: CommandTag): Boolean =
        tag in tags

    private val DOMAIN_TAGS = setOf(
        CommandTag.Direction,
        CommandTag.Color,
        CommandTag.Suit,
        CommandTag.Target,
        CommandTag.Number,
    )

    private fun timeoutReductionSteps(
        score: Int,
        timeoutRampMode: TimeoutRampMode,
    ): Int {
        val scoreStep = when (timeoutRampMode) {
            TimeoutRampMode.NormalProgression -> TIMEOUT_REDUCTION_SCORE_STEP
            TimeoutRampMode.PracticeImmediateRamp -> TIMEOUT_REDUCTION_SCORE_STEP
        }
        val effectiveScore = when (timeoutRampMode) {
            TimeoutRampMode.NormalProgression ->
                (score.coerceAtLeast(0) - lastCommandUnlockScore).coerceAtLeast(0)
            TimeoutRampMode.PracticeImmediateRamp ->
                score.coerceAtLeast(0)
        }
        return (effectiveScore / scoreStep)
            .coerceAtMost(maxTimeoutReductionSteps)
    }

    private fun reducedTimeoutMillis(
        baseTimeoutMillis: Int,
        reductionSteps: Int,
    ): Int =
        (baseTimeoutMillis * (1f - timeoutReductionFractionPerStep * reductionSteps))
            .roundToInt()
            .coerceAtLeast(MIN_TIMEOUT_MILLIS)
}
