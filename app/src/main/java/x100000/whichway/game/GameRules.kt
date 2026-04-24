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
    private const val NON_BREAKING_SPACE = '\u00A0'

    val maxUnlockScore: Int
        get() = Level.entries.last().unlockScore

    private data class UnlockEntry(
        val level: Level,
        val commands: List<GameCommand>,
    )

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
    private val commandUnlocks = listOf(
        UnlockEntry(Level.Level1, listOf(GameCommand.LEFT, GameCommand.RIGHT)),
        UnlockEntry(Level.Level2, listOf(GameCommand.UP, GameCommand.DOWN)),
        UnlockEntry(
            Level.Level3,
            listOf(
                GameCommand.NOT_LEFT,
                GameCommand.NOT_RIGHT,
                GameCommand.NOT_UP,
                GameCommand.NOT_DOWN,
            ),
        ),
        UnlockEntry(Level.Level4, listOf(GameCommand.NOTHING)),
        UnlockEntry(Level.Level5, listOf(GameCommand.NOT_NOTHING)),
        UnlockEntry(Level.Level6, listOf(GameCommand.TARGET, GameCommand.NOT_TARGET)),
        UnlockEntry(
            Level.Level7,
            listOf(
                GameCommand.GREEN,
                GameCommand.BLUE,
                GameCommand.WHITE,
                GameCommand.YELLOW,
            ),
        ),
        UnlockEntry(Level.Level8, listOf(GameCommand.NUMBER)),
        UnlockEntry(
            Level.Level9,
            listOf(
                GameCommand.OR_UP_DOWN,
                GameCommand.OR_UP_LEFT,
                GameCommand.OR_UP_RIGHT,
                GameCommand.OR_DOWN_LEFT,
                GameCommand.OR_DOWN_RIGHT,
                GameCommand.OR_LEFT_RIGHT,
            ),
        ),
        UnlockEntry(Level.Level10, listOf(GameCommand.EVEN, GameCommand.ODD)),
        UnlockEntry(
            Level.Level11,
            listOf(
                GameCommand.NOT_GREEN,
                GameCommand.NOT_BLUE,
                GameCommand.NOT_WHITE,
                GameCommand.NOT_YELLOW,
            ),
        ),
        UnlockEntry(Level.Level12, listOf(GameCommand.NOT_NUMBER)),
        UnlockEntry(
            Level.Level13,
            listOf(
                GameCommand.OR_GREEN_BLUE,
                GameCommand.OR_GREEN_WHITE,
                GameCommand.OR_GREEN_YELLOW,
                GameCommand.OR_BLUE_WHITE,
                GameCommand.OR_BLUE_YELLOW,
                GameCommand.OR_WHITE_YELLOW,
            ),
        ),
        UnlockEntry(Level.Level14, listOf(GameCommand.LESS_THAN, GameCommand.GREATER_THAN)),
        UnlockEntry(
            Level.Level15,
            listOf(
                GameCommand.GREEN_OR_UP,
                GameCommand.BLUE_OR_RIGHT,
                GameCommand.WHITE_OR_DOWN,
                GameCommand.YELLOW_OR_LEFT,
            ),
        ),
        UnlockEntry(
            Level.Level16,
            listOf(
                GameCommand.DIAMONDS,
                GameCommand.CLUBS,
                GameCommand.SPADES,
                GameCommand.HEARTS,
            ),
        ),
        UnlockEntry(
            Level.Level17,
            listOf(
                GameCommand.NOT_DIAMONDS,
                GameCommand.NOT_CLUBS,
                GameCommand.NOT_SPADES,
                GameCommand.NOT_HEARTS,
            ),
        ),
        UnlockEntry(
            Level.Level18,
            listOf(
                GameCommand.HEARTS_OR_UP,
                GameCommand.SPADES_OR_RIGHT,
                GameCommand.DIAMONDS_OR_DOWN,
                GameCommand.CLUBS_OR_LEFT,
            ),
        ),
        UnlockEntry(
            Level.Level19,
            listOf(
                GameCommand.NOT_GREEN_AND_NOT_UP,
                GameCommand.NOT_BLUE_AND_NOT_RIGHT,
                GameCommand.NOT_WHITE_AND_NOT_DOWN,
                GameCommand.NOT_YELLOW_AND_NOT_LEFT,
            ),
        ),
        UnlockEntry(
            Level.Level20,
            listOf(
                GameCommand.UP_OR_NUMBER,
                GameCommand.RIGHT_OR_NUMBER,
                GameCommand.DOWN_OR_NUMBER,
                GameCommand.LEFT_OR_NUMBER,
            ),
        ),
        UnlockEntry(
            Level.Level21,
            listOf(
                GameCommand.GREEN_OR_NUMBER,
                GameCommand.BLUE_OR_NUMBER,
                GameCommand.WHITE_OR_NUMBER,
                GameCommand.YELLOW_OR_NUMBER,
            ),
        ),
        UnlockEntry(Level.Level22, listOf(GameCommand.ADDITION, GameCommand.SUBTRACTION)),
        UnlockEntry(
            Level.Level23,
            listOf(
                GameCommand.LESS_THAN_ARITHMETIC,
                GameCommand.GREATER_THAN_ARITHMETIC,
            ),
        ),
        UnlockEntry(Level.Level24, listOf(GameCommand.NOT_ADDITION, GameCommand.NOT_SUBTRACTION)),
    )

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
            for (entry in commandUnlocks) {
                if (score < entry.level.unlockScore) {
                    break
                }
                addAll(entry.commands)
            }
        }
            .filter { command -> matchesCommandProfile(command, commandProfile) }
            .filter { command -> !skipColors || !command.hasTag(CommandTag.Color) }
            .filter { command -> !skipSuits || !command.hasTag(CommandTag.Suit) }
            .filter { command -> !skipNot || !command.hasTag(CommandTag.Not) }

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
            CommandProfile.MathOnly -> command.isMathOnlyCommand()
            CommandProfile.SuitsOnly -> command.isTargetingOnlyTag(CommandTag.Suit)
            CommandProfile.TargetsOnly -> command.isTargetingOnlyTag(CommandTag.Target)
        }

    private fun GameCommand.isDirectionsOnlyCommand(): Boolean =
        isPureDomain(CommandTag.Direction) || hasTag(CommandTag.Nothing)

    private fun GameCommand.isNumbersOnlyCommand(): Boolean =
        isPureDomain(CommandTag.Number)

    private fun GameCommand.isMathOnlyCommand(): Boolean =
        tags.contains(CommandTag.Arithmetic) && !tags.contains(CommandTag.Not) && !tags.contains(CommandTag.Comparison)

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
        val effectiveScore = when (timeoutRampMode) {
            TimeoutRampMode.NormalProgression ->
                (score.coerceAtLeast(0) - lastCommandUnlockScore).coerceAtLeast(0)
            TimeoutRampMode.PracticeImmediateRamp ->
                score.coerceAtLeast(0)
        }
        return (effectiveScore / TIMEOUT_REDUCTION_SCORE_STEP)
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
