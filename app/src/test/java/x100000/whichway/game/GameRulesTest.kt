package x100000.whichway.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameRulesTest {

    @Test
    fun directionRound_requiresMatchingZone() {
        val round = RoundData(
            prompt = "LEFT",
            validDirections = setOf(Direction.Left),
        )

        assertTrue(GameRules.isCorrectTap(round, Direction.Left))
        assertFalse(GameRules.isCorrectTap(round, Direction.Right))
    }

    @Test
    fun negatedDirectionRound_acceptsAnyOtherZone() {
        val round = RoundData(
            prompt = "NOT\nUP",
            validDirections = setOf(Direction.Left, Direction.Right, Direction.Down),
        )

        assertFalse(GameRules.isCorrectTap(round, Direction.Up))
        assertTrue(GameRules.isCorrectTap(round, Direction.Left))
        assertTrue(GameRules.isCorrectTap(round, Direction.Right))
        assertTrue(GameRules.isCorrectTap(round, Direction.Down))
    }

    @Test
    fun orDirectionRound_acceptsEitherListedDirection() {
        val round = RoundData(
            prompt = "UP\nOR\nDOWN",
            validDirections = setOf(Direction.Up, Direction.Down),
        )

        assertTrue(GameRules.isCorrectTap(round, Direction.Up))
        assertTrue(GameRules.isCorrectTap(round, Direction.Down))
        assertFalse(GameRules.isCorrectTap(round, Direction.Left))
        assertFalse(GameRules.isCorrectTap(round, Direction.Right))
    }

    @Test
    fun targetRound_acceptsOnlyMarkedDirection() {
        val round = GameRules.roundForCommand(GameCommand.TARGET, Random(0))
        val targetDirection = round.zoneFacts.entries.first { it.value.target }.key

        assertEquals("", round.prompt)
        assertEquals(Complexity.VeryLow, round.complexity)
        assertEquals(setOf(targetDirection), round.validDirections)
        assertTrue(GameRules.isCorrectTap(round, targetDirection))
        Direction.entries.filter { it != targetDirection }.forEach { direction ->
            assertFalse(GameRules.isCorrectTap(round, direction))
        }
    }

    @Test
    fun notTargetRound_acceptsAnyDirectionExceptMarkedOne() {
        val round = GameRules.roundForCommand(GameCommand.NOT_TARGET, Random(0))
        val blockedDirection = round.zoneFacts.entries.first { it.value.target }.key

        assertEquals("NOT", round.prompt)
        assertEquals(Complexity.VeryLow, round.complexity)
        assertEquals(3, round.validDirections.size)
        assertFalse(GameRules.isCorrectTap(round, blockedDirection))
        round.validDirections.forEach { direction ->
            assertTrue(GameRules.isCorrectTap(round, direction))
        }
    }

    @Test
    fun nothingRound_rejectsAllTapsAndAcceptsTimeout() {
        val round = RoundData(
            prompt = "NOTHING",
            validDirections = emptySet(),
            timeoutIsCorrect = true,
        )

        Direction.entries.forEach { direction ->
            assertFalse(GameRules.isCorrectTap(round, direction))
        }
        assertTrue(GameRules.isCorrectTimeout(round))
    }

    @Test
    fun notNothingRound_acceptsAnyTapButFailsOnTimeout() {
        val round = RoundData(
            prompt = "NOT\nNOTHING",
            validDirections = Direction.entries.toSet(),
        )

        assertFalse(GameRules.isCorrectTimeout(round))
        Direction.entries.forEach { direction ->
            assertTrue(GameRules.isCorrectTap(round, direction))
        }
    }

    @Test
    fun colorRound_acceptsOnlyMatchingZone() {
        val round = RoundData(
            prompt = ColorTarget.Green.label,
            validDirections = setOf(Direction.Right),
            zoneFacts = mapOf(
                Direction.Up to ZoneFacts(color = ColorTarget.Blue),
                Direction.Right to ZoneFacts(color = ColorTarget.Green),
                Direction.Down to ZoneFacts(color = ColorTarget.White),
                Direction.Left to ZoneFacts(color = ColorTarget.Yellow),
            ),
        )

        assertTrue(GameRules.isCorrectTap(round, Direction.Right))
        assertFalse(GameRules.isCorrectTap(round, Direction.Up))
    }

    @Test
    fun suitRound_acceptsOnlyMatchingSuitZone() {
        val round = GameRules.roundForCommand(GameCommand.DIAMONDS, Random(0))
        val targetDirection = round.zoneFacts.entries.first { it.value.suit == SuitTarget.Diamonds }.key

        assertEquals(SuitTarget.Diamonds.label, round.prompt)
        assertEquals(Complexity.Normal, round.complexity)
        assertEquals(setOf(targetDirection), round.validDirections)
        assertTrue(GameRules.isCorrectTap(round, targetDirection))
        Direction.entries.filter { it != targetDirection }.forEach { direction ->
            assertFalse(GameRules.isCorrectTap(round, direction))
        }
    }

    @Test
    fun negatedSuitRound_acceptsAnyZoneExceptBlockedSuit() {
        val round = GameRules.roundForCommand(GameCommand.NOT_DIAMONDS, Random(0))
        val blockedDirection = round.zoneFacts.entries.first { it.value.suit == SuitTarget.Diamonds }.key

        assertEquals("NOT\n${SuitTarget.Diamonds.label}", round.prompt)
        assertEquals(Complexity.Normal, round.complexity)
        assertEquals(3, round.validDirections.size)
        assertFalse(GameRules.isCorrectTap(round, blockedDirection))
        round.validDirections.forEach { direction ->
            assertTrue(GameRules.isCorrectTap(round, direction))
        }
    }

    @Test
    fun negatedColorRound_acceptsAnyZoneExceptBlockedColor() {
        val round = RoundData(
            prompt = "NOT\nWHITE",
            validDirections = setOf(Direction.Up, Direction.Right, Direction.Left),
            zoneFacts = mapOf(
                Direction.Up to ZoneFacts(color = ColorTarget.Blue),
                Direction.Right to ZoneFacts(color = ColorTarget.Green),
                Direction.Down to ZoneFacts(color = ColorTarget.White),
                Direction.Left to ZoneFacts(color = ColorTarget.Yellow),
            ),
        )

        assertFalse(GameRules.isCorrectTap(round, Direction.Down))
        assertTrue(GameRules.isCorrectTap(round, Direction.Up))
        assertTrue(GameRules.isCorrectTap(round, Direction.Right))
        assertTrue(GameRules.isCorrectTap(round, Direction.Left))
    }

    @Test
    fun orColorRound_acceptsZonesForEitherColor() {
        val round = RoundData(
            prompt = "GREEN\nOR\nBLUE",
            validDirections = setOf(Direction.Up, Direction.Right),
            zoneFacts = mapOf(
                Direction.Up to ZoneFacts(color = ColorTarget.Green),
                Direction.Right to ZoneFacts(color = ColorTarget.Blue),
                Direction.Down to ZoneFacts(color = ColorTarget.White),
                Direction.Left to ZoneFacts(color = ColorTarget.Yellow),
            ),
        )

        assertTrue(GameRules.isCorrectTap(round, Direction.Up))
        assertTrue(GameRules.isCorrectTap(round, Direction.Right))
        assertFalse(GameRules.isCorrectTap(round, Direction.Down))
    }

    @Test
    fun colorOrDirectionRound_acceptsUnionOfTwoCategories() {
        val round = RoundData(
            prompt = "GREEN\nOR\nLEFT",
            validDirections = setOf(Direction.Up, Direction.Left),
            zoneFacts = mapOf(
                Direction.Up to ZoneFacts(color = ColorTarget.Green),
                Direction.Right to ZoneFacts(color = ColorTarget.Blue),
                Direction.Down to ZoneFacts(color = ColorTarget.White),
                Direction.Left to ZoneFacts(color = ColorTarget.Yellow),
            ),
        )

        assertTrue(GameRules.isCorrectTap(round, Direction.Up))
        assertTrue(GameRules.isCorrectTap(round, Direction.Left))
        assertFalse(GameRules.isCorrectTap(round, Direction.Right))
    }

    @Test
    fun notColorAndNotDirectionRound_usesIntersection() {
        val round = RoundData(
            prompt = "NOT\u00A0GREEN\nAND\nNOT\u00A0UP",
            validDirections = setOf(Direction.Down, Direction.Left),
            zoneFacts = mapOf(
                Direction.Up to ZoneFacts(color = ColorTarget.Green),
                Direction.Right to ZoneFacts(color = ColorTarget.Blue),
                Direction.Down to ZoneFacts(color = ColorTarget.White),
                Direction.Left to ZoneFacts(color = ColorTarget.Yellow),
            ),
        )

        assertFalse(GameRules.isCorrectTap(round, Direction.Up))
        assertFalse(GameRules.isCorrectTap(round, Direction.Right))
        assertTrue(GameRules.isCorrectTap(round, Direction.Down))
        assertTrue(GameRules.isCorrectTap(round, Direction.Left))
    }

    @Test
    fun numberRound_acceptsMatchingNumberOnly() {
        val round = RoundData(
            prompt = "17",
            validDirections = setOf(Direction.Right),
            zoneFacts = mapOf(
                Direction.Up to ZoneFacts(number = 12),
                Direction.Right to ZoneFacts(number = 17),
                Direction.Down to ZoneFacts(number = 34),
                Direction.Left to ZoneFacts(number = 88),
            ),
        )

        assertTrue(GameRules.isCorrectTap(round, Direction.Right))
        assertFalse(GameRules.isCorrectTap(round, Direction.Up))
    }

    @Test
    fun notNumberRound_acceptsAllButBlockedNumber() {
        val round = RoundData(
            prompt = "NOT\n17",
            validDirections = setOf(Direction.Up, Direction.Down, Direction.Left),
            zoneFacts = mapOf(
                Direction.Up to ZoneFacts(number = 12),
                Direction.Right to ZoneFacts(number = 17),
                Direction.Down to ZoneFacts(number = 34),
                Direction.Left to ZoneFacts(number = 88),
            ),
        )

        assertFalse(GameRules.isCorrectTap(round, Direction.Right))
        assertTrue(GameRules.isCorrectTap(round, Direction.Up))
        assertTrue(GameRules.isCorrectTap(round, Direction.Down))
        assertTrue(GameRules.isCorrectTap(round, Direction.Left))
    }

    @Test
    fun directionOrNumberRound_acceptsDirectionAndMatchingNumber() {
        val round = RoundData(
            prompt = "LEFT\nOR\n17",
            validDirections = setOf(Direction.Left, Direction.Right),
            zoneFacts = mapOf(
                Direction.Up to ZoneFacts(number = 12),
                Direction.Right to ZoneFacts(number = 17),
                Direction.Down to ZoneFacts(number = 34),
                Direction.Left to ZoneFacts(number = 88),
            ),
        )

        assertTrue(GameRules.isCorrectTap(round, Direction.Left))
        assertTrue(GameRules.isCorrectTap(round, Direction.Right))
        assertFalse(GameRules.isCorrectTap(round, Direction.Up))
    }

    @Test
    fun parityRound_acceptsMatchingParityOnly() {
        val round = GameRules.roundForCommand(GameCommand.EVEN, Random(0))

        assertEquals("EVEN", round.prompt)
        assertTrue(round.validDirections.isNotEmpty())
        assertTrue(round.validDirections.size < Direction.entries.size)
        round.zoneFacts.forEach { (direction, facts) ->
            val value = facts.number!!
            assertEquals(value % 2 == 0, GameRules.isCorrectTap(round, direction))
        }
    }

    @Test
    fun suitOrDirectionRound_acceptsUnionOfSuitAndDirection() {
        val round = GameRules.roundForCommand(GameCommand.HEARTS_OR_UP, Random(0))

        assertEquals("HEARTS\nOR\nUP", round.prompt)
        assertTrue(round.validDirections.isNotEmpty())
        assertTrue(round.validDirections.size < Direction.entries.size)
        round.zoneFacts.forEach { (direction, facts) ->
            assertEquals(
                facts.suit == SuitTarget.Hearts || direction == Direction.Up,
                GameRules.isCorrectTap(round, direction),
            )
        }
    }

    @Test
    fun colorOrNumberRound_usesExactNumberAndAcceptsUnion() {
        val round = GameRules.roundForCommand(GameCommand.BLUE_OR_NUMBER, Random(0))
        val promptLines = round.prompt.split("\n")
        val targetNumber = promptLines.last().toInt()

        assertEquals("BLUE", promptLines.first())
        assertEquals("OR", promptLines[1])
        assertTrue(promptLines.last().all { it.isDigit() })
        round.zoneFacts.forEach { (direction, facts) ->
            assertEquals(
                facts.color == ColorTarget.Blue || facts.number == targetNumber,
                GameRules.isCorrectTap(round, direction),
            )
        }
    }

    @Test
    fun additionRound_promptEvaluatesToSingleCorrectZone() {
        val round = GameRules.roundForCommand(GameCommand.ADDITION, Random(0))
        val (left, right) = round.prompt.split(" + ").map(String::toInt)
        val expectedResult = left + right

        assertEquals(Complexity.High, round.complexity)
        assertEquals(1, round.validDirections.size)
        assertEquals(expectedResult, round.zoneFacts.getValue(round.validDirections.first()).number)
        assertEquals(4, round.zoneFacts.values.mapNotNull { it.number }.distinct().size)
        assertTrue(round.zoneFacts.values.mapNotNull { it.number }.all { it in 1..17 })
        assertTrue(left >= 0)
        assertTrue(right >= 0)
        assertTrue(expectedResult in 2..16)
    }

    @Test
    fun subtractionRound_promptEvaluatesToSingleCorrectZone() {
        val round = GameRules.roundForCommand(GameCommand.SUBTRACTION, Random(0))
        val (left, right) = round.prompt.split(" - ").map(String::toInt)
        val expectedResult = left - right

        assertEquals(Complexity.High, round.complexity)
        assertEquals(1, round.validDirections.size)
        assertEquals(expectedResult, round.zoneFacts.getValue(round.validDirections.first()).number)
        assertEquals(4, round.zoneFacts.values.mapNotNull { it.number }.distinct().size)
        assertTrue(round.zoneFacts.values.mapNotNull { it.number }.all { it in 1..17 })
        assertTrue(left > 0)
        assertTrue(right >= 0)
        assertTrue(expectedResult in 2..16)
    }

    @Test
    fun negatedAdditionRound_rejectsComputedResultAndAcceptsOtherZones() {
        val round = GameRules.roundForCommand(GameCommand.NOT_ADDITION, Random(0))
        val expression = round.prompt.removePrefix("NOT\n")
        val (left, right) = expression.split(" + ").map(String::toInt)
        val blockedResult = left + right
        val blockedDirection = round.zoneFacts.entries.first { it.value.number == blockedResult }.key

        assertEquals(Complexity.VeryHigh, round.complexity)
        assertEquals(3, round.validDirections.size)
        assertFalse(GameRules.isCorrectTap(round, blockedDirection))
        round.validDirections.forEach { direction ->
            assertTrue(GameRules.isCorrectTap(round, direction))
        }
        assertEquals(4, round.zoneFacts.values.mapNotNull { it.number }.distinct().size)
        assertTrue(round.zoneFacts.values.mapNotNull { it.number }.all { it in 1..17 })
    }

    @Test
    fun negatedSubtractionRound_rejectsComputedResultAndAcceptsOtherZones() {
        val round = GameRules.roundForCommand(GameCommand.NOT_SUBTRACTION, Random(0))
        val expression = round.prompt.removePrefix("NOT\n")
        val (left, right) = expression.split(" - ").map(String::toInt)
        val blockedResult = left - right
        val blockedDirection = round.zoneFacts.entries.first { it.value.number == blockedResult }.key

        assertEquals(Complexity.VeryHigh, round.complexity)
        assertEquals(3, round.validDirections.size)
        assertFalse(GameRules.isCorrectTap(round, blockedDirection))
        round.validDirections.forEach { direction ->
            assertTrue(GameRules.isCorrectTap(round, direction))
        }
        assertEquals(4, round.zoneFacts.values.mapNotNull { it.number }.distinct().size)
        assertTrue(round.zoneFacts.values.mapNotNull { it.number }.all { it in 1..17 })
    }

    @Test
    fun lessThanRound_acceptsNumbersBelowThreshold() {
        val round = GameRules.roundForCommand(GameCommand.LESS_THAN, Random(0))
        val threshold = round.prompt.removePrefix("< ").toInt()

        assertEquals(Complexity.High, round.complexity)
        assertTrue(threshold in 3..15)
        assertEquals(2, round.validDirections.size)
        round.zoneFacts.forEach { (direction, facts) ->
            val value = facts.number!!
            assertEquals(value < threshold, GameRules.isCorrectTap(round, direction))
        }
    }

    @Test
    fun greaterThanRound_acceptsNumbersAboveThreshold() {
        val round = GameRules.roundForCommand(GameCommand.GREATER_THAN, Random(0))
        val threshold = round.prompt.removePrefix("> ").toInt()

        assertEquals(Complexity.High, round.complexity)
        assertTrue(threshold in 3..15)
        assertEquals(2, round.validDirections.size)
        round.zoneFacts.forEach { (direction, facts) ->
            val value = facts.number!!
            assertEquals(value > threshold, GameRules.isCorrectTap(round, direction))
        }
    }

    @Test
    fun arithmeticComparisonRound_usesExpressionThreshold() {
        val round = GameRules.roundForCommand(GameCommand.GREATER_THAN_ARITHMETIC, Random(1))
        val expression = round.prompt.removePrefix("> ")
        val (left, operator, right) = expression.split(" ")
        val threshold = when (operator) {
            "+" -> left.toInt() + right.toInt()
            "-" -> left.toInt() - right.toInt()
            else -> error("Unexpected operator: $operator")
        }

        assertEquals(Complexity.VeryHigh, round.complexity)
        assertTrue(threshold in 3..15)
        assertEquals(2, round.validDirections.size)
        round.zoneFacts.forEach { (direction, facts) ->
            val value = facts.number!!
            assertEquals(value > threshold, GameRules.isCorrectTap(round, direction))
        }
    }

    @Test
    fun arithmeticCommands_canUseZeroOperand() {
        val rounds = List(50) { index -> GameRules.roundForCommand(GameCommand.ADDITION, Random(index)) } +
            List(50) { index -> GameRules.roundForCommand(GameCommand.SUBTRACTION, Random(index + 100)) }

        assertTrue(
            rounds.any { round ->
                val expression = round.prompt
                expression.startsWith("0 + ") ||
                    expression.endsWith(" + 0") ||
                    expression.endsWith(" - 0")
            },
        )
    }

    @Test
    fun orCommands_useThreeSecondTimeout() {
        val round = GameRules.roundForCommand(GameCommand.OR_UP_DOWN, Random(0))

        assertEquals(Complexity.Normal, round.complexity)
        assertEquals(GameRules.NORMAL_COMPLEXITY_TIMEOUT_MILLIS, GameRules.timeoutMillisFor(round.complexity))
    }

    @Test
    fun notAndCommands_useFiveSecondTimeout() {
        val round = GameRules.roundForCommand(GameCommand.NOT_GREEN_AND_NOT_UP, Random(0))

        assertEquals(Complexity.VeryHigh, round.complexity)
        assertEquals(GameRules.VERY_HIGH_COMPLEXITY_TIMEOUT_MILLIS, GameRules.timeoutMillisFor(round.complexity))
    }

    @Test
    fun arithmeticCommands_useHighComplexityTimeout() {
        val additionRound = GameRules.roundForCommand(GameCommand.ADDITION, Random(0))
        val subtractionRound = GameRules.roundForCommand(GameCommand.SUBTRACTION, Random(1))
        val lessThanRound = GameRules.roundForCommand(GameCommand.LESS_THAN, Random(2))
        val greaterThanRound = GameRules.roundForCommand(GameCommand.GREATER_THAN, Random(3))

        assertEquals(Complexity.High, additionRound.complexity)
        assertEquals(GameRules.HIGH_COMPLEXITY_TIMEOUT_MILLIS, GameRules.timeoutMillisFor(additionRound.complexity))
        assertEquals(Complexity.High, subtractionRound.complexity)
        assertEquals(GameRules.HIGH_COMPLEXITY_TIMEOUT_MILLIS, GameRules.timeoutMillisFor(subtractionRound.complexity))
        assertEquals(Complexity.High, lessThanRound.complexity)
        assertEquals(GameRules.HIGH_COMPLEXITY_TIMEOUT_MILLIS, GameRules.timeoutMillisFor(lessThanRound.complexity))
        assertEquals(Complexity.High, greaterThanRound.complexity)
        assertEquals(GameRules.HIGH_COMPLEXITY_TIMEOUT_MILLIS, GameRules.timeoutMillisFor(greaterThanRound.complexity))
    }

    @Test
    fun negatedArithmeticCommands_useVeryHighComplexityTimeout() {
        val additionRound = GameRules.roundForCommand(GameCommand.NOT_ADDITION, Random(0))
        val subtractionRound = GameRules.roundForCommand(GameCommand.NOT_SUBTRACTION, Random(1))
        val lessThanArithmeticRound = GameRules.roundForCommand(GameCommand.LESS_THAN_ARITHMETIC, Random(2))
        val greaterThanArithmeticRound = GameRules.roundForCommand(GameCommand.GREATER_THAN_ARITHMETIC, Random(3))

        assertEquals(Complexity.VeryHigh, additionRound.complexity)
        assertEquals(GameRules.VERY_HIGH_COMPLEXITY_TIMEOUT_MILLIS, GameRules.timeoutMillisFor(additionRound.complexity))
        assertEquals(Complexity.VeryHigh, subtractionRound.complexity)
        assertEquals(GameRules.VERY_HIGH_COMPLEXITY_TIMEOUT_MILLIS, GameRules.timeoutMillisFor(subtractionRound.complexity))
        assertEquals(Complexity.VeryHigh, lessThanArithmeticRound.complexity)
        assertEquals(GameRules.VERY_HIGH_COMPLEXITY_TIMEOUT_MILLIS, GameRules.timeoutMillisFor(lessThanArithmeticRound.complexity))
        assertEquals(Complexity.VeryHigh, greaterThanArithmeticRound.complexity)
        assertEquals(GameRules.VERY_HIGH_COMPLEXITY_TIMEOUT_MILLIS, GameRules.timeoutMillisFor(greaterThanArithmeticRound.complexity))
    }

    @Test
    fun normalCommands_keepDefaultTimeout() {
        val round = GameRules.roundForCommand(GameCommand.LEFT, Random(0))

        assertEquals(Complexity.Low, round.complexity)
        assertEquals(GameRules.LOW_COMPLEXITY_TIMEOUT_MILLIS, GameRules.timeoutMillisFor(round.complexity))
    }

    @Test
    fun predefinedCommands_returnStoredRoundData() {
        val round = GameRules.roundForCommand(GameCommand.LEFT, Random(0))

        assertTrue(round === RoundStorage.Left)
    }

    @Test
    fun dynamicCommands_createFreshRoundData() {
        val round = GameRules.roundForCommand(GameCommand.GREEN, Random(0))

        assertFalse(round === RoundStorage.Left)
        assertEquals(ColorTarget.Green.label, round.prompt)
    }

    @Test
    fun chargeConstants_matchDesignRules() {
        assertEquals(3, GameRules.MAX_CHARGES)
        assertEquals(0.65f, GameRules.FAST_CHARGE_THRESHOLD_PROGRESS)
    }

    @Test
    fun earnsCharge_usesElapsedTimeWindow() {
        val config = GameConfig()

        assertTrue(GameRules.earnsCharge(Complexity.Low, elapsedMillis = 700, score = 0, config = config))
        assertFalse(GameRules.earnsCharge(Complexity.Low, elapsedMillis = 701, score = 0, config = config))
    }

    @Test
    fun complexityMapping_drivesTimeoutsSeparately() {
        assertEquals(GameRules.VERY_LOW_COMPLEXITY_TIMEOUT_MILLIS, GameRules.timeoutMillisFor(Complexity.VeryLow))
        assertEquals(GameRules.LOW_COMPLEXITY_TIMEOUT_MILLIS, GameRules.timeoutMillisFor(Complexity.Low))
        assertEquals(GameRules.NORMAL_COMPLEXITY_TIMEOUT_MILLIS, GameRules.timeoutMillisFor(Complexity.Normal))
        assertEquals(GameRules.HIGH_COMPLEXITY_TIMEOUT_MILLIS, GameRules.timeoutMillisFor(Complexity.High))
        assertEquals(GameRules.VERY_HIGH_COMPLEXITY_TIMEOUT_MILLIS, GameRules.timeoutMillisFor(Complexity.VeryHigh))
    }

    @Test
    fun speedScaling_adjustsTimeoutsFromSettings() {
        assertEquals(2667, GameRules.timeoutMillisFor(Complexity.Low, speedPercent = 75))
        assertEquals(2000, GameRules.timeoutMillisFor(Complexity.Low, speedPercent = 100))
        assertEquals(1600, GameRules.timeoutMillisFor(Complexity.Low, speedPercent = 125))
    }

    @Test
    fun normalProgressionTimeoutRamp_startsOnlyAfterLastCommandUnlock() {
        val config = GameConfig(timeoutRampMode = TimeoutRampMode.NormalProgression, speedPercent = 100)

        assertEquals(2000, GameRules.timeoutMillisFor(Complexity.Low, score = 0, config = config))
        assertEquals(2000, GameRules.timeoutMillisFor(Complexity.Low, score = Level.Level24.unlockScore, config = config))
        assertEquals(2000, GameRules.timeoutMillisFor(Complexity.Low, score = Level.Level24.unlockScore + 4, config = config))
        assertEquals(1940, GameRules.timeoutMillisFor(Complexity.Low, score = Level.Level24.unlockScore + 5, config = config))
        assertEquals(1880, GameRules.timeoutMillisFor(Complexity.Low, score = Level.Level24.unlockScore + 10, config = config))
    }

    @Test
    fun practiceTimeoutRamp_reducesEveryFiveScore() {
        val config = GameConfig(timeoutRampMode = TimeoutRampMode.PracticeImmediateRamp, speedPercent = 100)

        assertEquals(2000, GameRules.timeoutMillisFor(Complexity.Low, score = 0, config = config))
        assertEquals(2000, GameRules.timeoutMillisFor(Complexity.Low, score = 4, config = config))
        assertEquals(1940, GameRules.timeoutMillisFor(Complexity.Low, score = 5, config = config))
        assertEquals(1880, GameRules.timeoutMillisFor(Complexity.Low, score = 10, config = config))
    }

    @Test
    fun fixedLevelStart_usesUnlockFloorForTimeoutRamp() {
        val config = GameConfig(
            unlockFloor = Level.Level18.unlockScore,
            timeoutRampMode = TimeoutRampMode.NormalProgression,
            speedPercent = 100,
            allowsProgression = true,
        )

        assertEquals(2000, GameRules.timeoutMillisFor(Complexity.Low, score = 0, config = config))
    }

    @Test
    fun fixedLevelStart_progressesFromChosenLevelUsingRunScore() {
        val config = GameConfig(
            unlockFloor = Level.Level10.unlockScore,
            timeoutRampMode = TimeoutRampMode.NormalProgression,
            speedPercent = 100,
            allowsProgression = true,
        )

        assertEquals(Level.Level10.unlockScore, GameRules.effectiveContentScoreFor(score = 0, config = config))
        assertEquals(Level.Level11.unlockScore - 1, GameRules.effectiveContentScoreFor(score = 4, config = config))
        assertEquals(Level.Level11.unlockScore, GameRules.effectiveContentScoreFor(score = 5, config = config))
        assertEquals(10, GameRules.levelForScore(GameRules.effectiveContentScoreFor(score = 0, config = config)))
        assertEquals(11, GameRules.levelForScore(GameRules.effectiveContentScoreFor(score = 5, config = config)))
    }

    @Test
    fun customModeTimeoutRamp_usesCurrentRunScore_notUnlockFloor() {
        val config = GameConfig(
            unlockFloor = GameRules.maxUnlockScore,
            commandProfile = CommandProfile.TargetsOnly,
            timeoutRampMode = TimeoutRampMode.PracticeImmediateRamp,
            speedPercent = 100,
            allowsProgression = false,
        )

        assertEquals(GameRules.maxUnlockScore, GameRules.effectiveContentScoreFor(score = 0, config = config))
        assertEquals(0, GameRules.effectiveTimeoutScoreFor(score = 0, config = config))
        assertEquals(5, GameRules.effectiveTimeoutScoreFor(score = 5, config = config))
        assertEquals(GameRules.VERY_LOW_COMPLEXITY_TIMEOUT_MILLIS, GameRules.timeoutMillisFor(Complexity.VeryLow, score = 0, config = config))
        assertEquals(1455, GameRules.timeoutMillisFor(Complexity.VeryLow, score = 5, config = config))
    }

    @Test
    fun fixedExtendedLevelStart_usesPostCommandSpeedupFromUnlockFloor() {
        val config = GameConfig(
            unlockFloor = GameRules.unlockScoreForLevel(25),
            timeoutRampMode = TimeoutRampMode.NormalProgression,
            speedPercent = 100,
            allowsProgression = true,
        )

        assertEquals(1940, GameRules.timeoutMillisFor(Complexity.Low, score = 0, config = config))
    }

    @Test
    fun timeoutRamp_clampsBaseTimeoutBeforeSpeedScaling() {
        val standardConfig = GameConfig(timeoutRampMode = TimeoutRampMode.NormalProgression, speedPercent = 100)
        val fasterConfig = GameConfig(timeoutRampMode = TimeoutRampMode.NormalProgression, speedPercent = 125)
        val scoreThatHitsClamp = Level.Level24.unlockScore + 5 * 100

        assertEquals(GameRules.MIN_TIMEOUT_MILLIS, GameRules.timeoutMillisFor(Complexity.VeryLow, score = scoreThatHitsClamp, config = standardConfig))
        assertEquals(160, GameRules.timeoutMillisFor(Complexity.VeryLow, score = scoreThatHitsClamp, config = fasterConfig))
    }

    @Test
    fun timeoutRamp_appliesSettingsAfterReduction() {
        val config = GameConfig(timeoutRampMode = TimeoutRampMode.NormalProgression, speedPercent = 125)

        assertEquals(1552, GameRules.timeoutMillisFor(Complexity.Low, score = Level.Level24.unlockScore + 5, config = config))
    }

    @Test
    fun speedLabel_reflectsFinalEffectiveTimeoutDelta() {
        assertEquals(
            "S:0%",
            GameRules.speedLabelFor(Complexity.Low, score = 0, config = GameConfig(speedPercent = 100)),
        )
        assertEquals(
            "S:+20%",
            GameRules.speedLabelFor(Complexity.Low, score = 0, config = GameConfig(speedPercent = 125)),
        )
        assertEquals(
            "S:-33%",
            GameRules.speedLabelFor(Complexity.Low, score = 0, config = GameConfig(speedPercent = 75)),
        )
        assertEquals(
            "S:0%",
            GameRules.speedLabelFor(Complexity.Low, score = 50, config = GameConfig(speedPercent = 100)),
        )
        assertEquals(
            "S:+3%",
            GameRules.speedLabelFor(Complexity.Low, score = Level.Level24.unlockScore + 5, config = GameConfig(speedPercent = 100)),
        )
        assertEquals(
            "S:+22%",
            GameRules.speedLabelFor(Complexity.Low, score = Level.Level24.unlockScore + 5, config = GameConfig(speedPercent = 125)),
        )
    }

    @Test
    fun factualRound_canShowFourDifferentColors() {
        val round = RoundData(
            prompt = ColorTarget.Yellow.label,
            validDirections = setOf(Direction.Left),
            zoneFacts = mapOf(
                Direction.Up to ZoneFacts(color = ColorTarget.Blue),
                Direction.Right to ZoneFacts(color = ColorTarget.Green),
                Direction.Down to ZoneFacts(color = ColorTarget.White),
                Direction.Left to ZoneFacts(color = ColorTarget.Yellow),
            ),
        )

        assertEquals(4, round.zoneFacts.size)
        assertEquals(
            ColorTarget.entries.toSet(),
            round.zoneFacts.values.mapNotNull { it.color }.toSet(),
        )
    }

    @Test
    fun factualRound_canShowFourDifferentNumbers() {
        val round = RoundData(
            prompt = "17",
            validDirections = setOf(Direction.Right),
            zoneFacts = mapOf(
                Direction.Up to ZoneFacts(number = 12),
                Direction.Right to ZoneFacts(number = 17),
                Direction.Down to ZoneFacts(number = 34),
                Direction.Left to ZoneFacts(number = 88),
            ),
        )

        assertEquals(4, round.zoneFacts.size)
        assertEquals(setOf(12, 17, 34, 88), round.zoneFacts.values.mapNotNull { it.number }.toSet())
    }

    @Test
    fun factualRound_canShowSingleTargetMarker() {
        val round = GameRules.roundForCommand(GameCommand.TARGET, Random(0))

        assertEquals(1, round.zoneFacts.values.count { it.target })
    }

    @Test
    fun factualRound_canShowFourDifferentSuits() {
        val round = GameRules.roundForCommand(GameCommand.HEARTS, Random(0))

        assertEquals(SuitTarget.entries.toSet(), round.zoneFacts.values.mapNotNull { it.suit }.toSet())
    }

    @Test
    fun progression_unlocksExpectedCommandsAtThresholds() {
        assertEquals(listOf(GameCommand.LEFT, GameCommand.RIGHT), GameRules.commandIdsForScore(0))
        assertTrue(GameRules.commandIdsForScore(5).containsAll(listOf(GameCommand.UP, GameCommand.DOWN)))
        assertTrue(
            GameRules.commandIdsForScore(10).containsAll(
                listOf(GameCommand.NOT_LEFT, GameCommand.NOT_RIGHT, GameCommand.NOT_UP, GameCommand.NOT_DOWN),
            ),
        )
        assertTrue(GameRules.commandIdsForScore(15).contains(GameCommand.NOTHING))
        assertTrue(GameRules.commandIdsForScore(20).contains(GameCommand.NOT_NOTHING))
        assertTrue(
            GameRules.commandIdsForScore(25).containsAll(
                listOf(GameCommand.TARGET, GameCommand.NOT_TARGET),
            ),
        )
        assertTrue(
            GameRules.commandIdsForScore(30).containsAll(
                listOf(GameCommand.GREEN, GameCommand.BLUE, GameCommand.WHITE, GameCommand.YELLOW),
            ),
        )
        assertTrue(GameRules.commandIdsForScore(35).contains(GameCommand.NUMBER))
        assertTrue(GameRules.commandIdsForScore(40).contains(GameCommand.OR_UP_DOWN))
        assertTrue(GameRules.commandIdsForScore(45).contains(GameCommand.EVEN))
        assertTrue(GameRules.commandIdsForScore(45).contains(GameCommand.ODD))
        assertTrue(GameRules.commandIdsForScore(50).contains(GameCommand.NOT_GREEN))
        assertTrue(GameRules.commandIdsForScore(55).contains(GameCommand.NOT_NUMBER))
        assertTrue(GameRules.commandIdsForScore(60).contains(GameCommand.OR_GREEN_BLUE))
        assertTrue(GameRules.commandIdsForScore(65).contains(GameCommand.LESS_THAN))
        assertTrue(GameRules.commandIdsForScore(65).contains(GameCommand.GREATER_THAN))
        assertTrue(GameRules.commandIdsForScore(70).contains(GameCommand.GREEN_OR_UP))
        assertTrue(
            GameRules.commandIdsForScore(75).containsAll(
                listOf(GameCommand.DIAMONDS, GameCommand.CLUBS, GameCommand.SPADES, GameCommand.HEARTS),
            ),
        )
        assertTrue(GameRules.commandIdsForScore(80).contains(GameCommand.NOT_DIAMONDS))
        assertTrue(GameRules.commandIdsForScore(85).contains(GameCommand.HEARTS_OR_UP))
        assertTrue(GameRules.commandIdsForScore(90).contains(GameCommand.NOT_GREEN_AND_NOT_UP))
        assertTrue(GameRules.commandIdsForScore(95).contains(GameCommand.UP_OR_NUMBER))
        assertTrue(GameRules.commandIdsForScore(100).contains(GameCommand.BLUE_OR_NUMBER))
        assertTrue(
            GameRules.commandIdsForScore(105).containsAll(
                listOf(GameCommand.ADDITION, GameCommand.SUBTRACTION),
            ),
        )
        assertTrue(
            GameRules.commandIdsForScore(110).containsAll(
                listOf(GameCommand.LESS_THAN_ARITHMETIC, GameCommand.GREATER_THAN_ARITHMETIC),
            ),
        )
        assertTrue(
            GameRules.commandIdsForScore(115).containsAll(
                listOf(GameCommand.NOT_ADDITION, GameCommand.NOT_SUBTRACTION),
            ),
        )
    }

    @Test
    fun levelForScore_advancesAtEachUnlockThreshold() {
        assertEquals(1, GameRules.levelForScore(0))
        assertEquals(1, GameRules.levelForScore(4))
        assertEquals(2, GameRules.levelForScore(5))
        assertEquals(3, GameRules.levelForScore(10))
        assertEquals(6, GameRules.levelForScore(25))
        assertEquals(7, GameRules.levelForScore(30))
        assertEquals(8, GameRules.levelForScore(35))
        assertEquals(10, GameRules.levelForScore(45))
        assertEquals(11, GameRules.levelForScore(50))
        assertEquals(15, GameRules.levelForScore(70))
        assertEquals(16, GameRules.levelForScore(75))
        assertEquals(17, GameRules.levelForScore(80))
        assertEquals(18, GameRules.levelForScore(85))
        assertEquals(19, GameRules.levelForScore(90))
        assertEquals(20, GameRules.levelForScore(95))
        assertEquals(21, GameRules.levelForScore(100))
        assertEquals(22, GameRules.levelForScore(105))
        assertEquals(23, GameRules.levelForScore(110))
        assertEquals(24, GameRules.levelForScore(115))
        assertEquals(25, GameRules.levelForScore(120))
        assertEquals(25, GameRules.levelForScore(124))
        assertEquals(26, GameRules.levelForScore(125))
        assertEquals(56, GameRules.levelForScore(1000))
    }

    @Test
    fun progression_doesNotUnlockCommandsBeforeThresholds() {
        assertFalse(GameRules.commandIdsForScore(24).contains(GameCommand.TARGET))
        assertFalse(GameRules.commandIdsForScore(24).contains(GameCommand.NOT_TARGET))
        assertFalse(GameRules.commandIdsForScore(34).contains(GameCommand.NUMBER))
        assertFalse(GameRules.commandIdsForScore(44).contains(GameCommand.EVEN))
        assertFalse(GameRules.commandIdsForScore(49).contains(GameCommand.NOT_GREEN))
        assertFalse(GameRules.commandIdsForScore(54).contains(GameCommand.NOT_NUMBER))
        assertFalse(GameRules.commandIdsForScore(59).contains(GameCommand.OR_GREEN_BLUE))
        assertFalse(GameRules.commandIdsForScore(64).contains(GameCommand.LESS_THAN))
        assertFalse(GameRules.commandIdsForScore(64).contains(GameCommand.GREATER_THAN))
        assertFalse(GameRules.commandIdsForScore(69).contains(GameCommand.GREEN_OR_UP))
        assertFalse(GameRules.commandIdsForScore(74).contains(GameCommand.DIAMONDS))
        assertFalse(GameRules.commandIdsForScore(74).contains(GameCommand.HEARTS))
        assertFalse(GameRules.commandIdsForScore(79).contains(GameCommand.NOT_DIAMONDS))
        assertFalse(GameRules.commandIdsForScore(84).contains(GameCommand.HEARTS_OR_UP))
        assertFalse(GameRules.commandIdsForScore(89).contains(GameCommand.NOT_GREEN_AND_NOT_UP))
        assertFalse(GameRules.commandIdsForScore(94).contains(GameCommand.UP_OR_NUMBER))
        assertFalse(GameRules.commandIdsForScore(99).contains(GameCommand.GREEN_OR_NUMBER))
        assertFalse(GameRules.commandIdsForScore(104).contains(GameCommand.ADDITION))
        assertFalse(GameRules.commandIdsForScore(104).contains(GameCommand.SUBTRACTION))
        assertFalse(GameRules.commandIdsForScore(109).contains(GameCommand.LESS_THAN_ARITHMETIC))
        assertFalse(GameRules.commandIdsForScore(109).contains(GameCommand.GREATER_THAN_ARITHMETIC))
        assertFalse(GameRules.commandIdsForScore(114).contains(GameCommand.NOT_ADDITION))
        assertFalse(GameRules.commandIdsForScore(114).contains(GameCommand.NOT_SUBTRACTION))
    }

    @Test
    fun directionsOnlyProfile_excludesTargetAndNumberFamilies() {
        val commands = GameRules.commandIdsForScore(
            score = GameRules.maxUnlockScore,
            commandProfile = CommandProfile.DirectionsOnly,
        )

        assertTrue(commands.contains(GameCommand.NOT_NOTHING))
        assertTrue(commands.contains(GameCommand.OR_UP_DOWN))
        assertFalse(commands.contains(GameCommand.TARGET))
        assertFalse(commands.contains(GameCommand.NOT_TARGET))
        assertFalse(commands.contains(GameCommand.GREEN))
        assertFalse(commands.contains(GameCommand.NUMBER))
        assertFalse(commands.contains(GameCommand.ADDITION))
    }

    @Test
    fun numbersOnlyProfile_includesParityAndArithmeticAndExcludesMixedFamilies() {
        val commands = GameRules.commandIdsForScore(
            score = GameRules.maxUnlockScore,
            commandProfile = CommandProfile.NumbersOnly,
        )

        assertTrue(commands.contains(GameCommand.NUMBER))
        assertTrue(commands.contains(GameCommand.EVEN))
        assertTrue(commands.contains(GameCommand.ODD))
        assertTrue(commands.contains(GameCommand.NOT_NUMBER))
        assertTrue(commands.contains(GameCommand.LESS_THAN))
        assertTrue(commands.contains(GameCommand.GREATER_THAN))
        assertTrue(commands.contains(GameCommand.ADDITION))
        assertTrue(commands.contains(GameCommand.LESS_THAN_ARITHMETIC))
        assertTrue(commands.contains(GameCommand.GREATER_THAN_ARITHMETIC))
        assertTrue(commands.contains(GameCommand.NOT_SUBTRACTION))
        assertFalse(commands.contains(GameCommand.UP_OR_NUMBER))
        assertFalse(commands.contains(GameCommand.BLUE_OR_NUMBER))
        assertFalse(commands.contains(GameCommand.LEFT))
        assertFalse(commands.contains(GameCommand.GREEN))
    }

    @Test
    fun mathOnlyProfile_containsOnlyAdditionAndSubtraction() {
        val commands = GameRules.commandIdsForScore(
            score = GameRules.maxUnlockScore,
            commandProfile = CommandProfile.MathOnly,
        )

        assertEquals(
            setOf(
                GameCommand.ADDITION,
                GameCommand.SUBTRACTION,
            ),
            commands.toSet(),
        )
    }

    @Test
    fun suitsOnlyProfile_containsOnlySuitCommands() {
        val commands = GameRules.commandIdsForScore(
            score = GameRules.maxUnlockScore,
            commandProfile = CommandProfile.SuitsOnly,
        )

        assertEquals(
            setOf(
                GameCommand.DIAMONDS,
                GameCommand.CLUBS,
                GameCommand.SPADES,
                GameCommand.HEARTS,
                GameCommand.NOT_DIAMONDS,
                GameCommand.NOT_CLUBS,
                GameCommand.NOT_SPADES,
                GameCommand.NOT_HEARTS,
            ),
            commands.toSet(),
        )
    }

    @Test
    fun suitsOnlyProfile_withSkipSuits_enabledWouldBeEmpty() {
        val commands = GameRules.commandIdsForScore(
            score = GameRules.maxUnlockScore,
            commandProfile = CommandProfile.SuitsOnly,
            skipSuits = true,
        )

        assertTrue(commands.isEmpty())
    }

    @Test
    fun customModeCommandPools_remainNonEmpty_withRelevantSkipSettings() {
        val directionsOnlyCommands = GameRules.commandIdsForScore(
            score = GameRules.maxUnlockScore,
            commandProfile = CommandProfile.DirectionsOnly,
            skipColors = true,
            skipSuits = true,
            skipNot = true,
        )
        val numbersOnlyCommands = GameRules.commandIdsForScore(
            score = GameRules.maxUnlockScore,
            commandProfile = CommandProfile.NumbersOnly,
            skipColors = true,
            skipSuits = true,
            skipNot = true,
        )
        val mathOnlyCommands = GameRules.commandIdsForScore(
            score = GameRules.maxUnlockScore,
            commandProfile = CommandProfile.MathOnly,
            skipColors = true,
            skipSuits = true,
            skipNot = true,
        )
        val targetsOnlyCommands = GameRules.commandIdsForScore(
            score = GameRules.maxUnlockScore,
            commandProfile = CommandProfile.TargetsOnly,
            skipColors = true,
            skipSuits = true,
            skipNot = true,
        )

        assertTrue(directionsOnlyCommands.isNotEmpty())
        assertTrue(numbersOnlyCommands.isNotEmpty())
        assertTrue(mathOnlyCommands.isNotEmpty())
        assertTrue(targetsOnlyCommands.isNotEmpty())
    }

    @Test
    fun skipColors_removesColorFamiliesButKeepsNumbers() {
        val commands = GameRules.commandIdsForScore(
            score = GameRules.maxUnlockScore,
            skipColors = true,
        )

        assertFalse(commands.contains(GameCommand.GREEN))
        assertFalse(commands.contains(GameCommand.NOT_GREEN))
        assertFalse(commands.contains(GameCommand.GREEN_OR_UP))
        assertFalse(commands.contains(GameCommand.BLUE_OR_NUMBER))
        assertFalse(commands.contains(GameCommand.NOT_GREEN_AND_NOT_UP))
        assertTrue(commands.contains(GameCommand.NUMBER))
        assertTrue(commands.contains(GameCommand.ADDITION))
    }

    @Test
    fun skipSuits_removesSuitFamiliesButKeepsOtherCommands() {
        val commands = GameRules.commandIdsForScore(
            score = GameRules.maxUnlockScore,
            skipSuits = true,
        )

        assertFalse(commands.contains(GameCommand.DIAMONDS))
        assertFalse(commands.contains(GameCommand.CLUBS))
        assertFalse(commands.contains(GameCommand.SPADES))
        assertFalse(commands.contains(GameCommand.HEARTS))
        assertFalse(commands.contains(GameCommand.NOT_DIAMONDS))
        assertFalse(commands.contains(GameCommand.NOT_CLUBS))
        assertFalse(commands.contains(GameCommand.NOT_SPADES))
        assertFalse(commands.contains(GameCommand.NOT_HEARTS))
        assertFalse(commands.contains(GameCommand.HEARTS_OR_UP))
        assertTrue(commands.contains(GameCommand.LEFT))
        assertTrue(commands.contains(GameCommand.NUMBER))
    }

    @Test
    fun skipNot_removesNegatedCommandsButKeepsPositiveCommands() {
        val commands = GameRules.commandIdsForScore(
            score = GameRules.maxUnlockScore,
            skipNot = true,
        )

        assertFalse(commands.contains(GameCommand.NOT_LEFT))
        assertFalse(commands.contains(GameCommand.NOT_TARGET))
        assertFalse(commands.contains(GameCommand.NOT_GREEN))
        assertFalse(commands.contains(GameCommand.NOT_DIAMONDS))
        assertFalse(commands.contains(GameCommand.NOT_NUMBER))
        assertFalse(commands.contains(GameCommand.NOT_ADDITION))
        assertTrue(commands.contains(GameCommand.LEFT))
        assertTrue(commands.contains(GameCommand.TARGET))
        assertTrue(commands.contains(GameCommand.GREEN))
        assertTrue(commands.contains(GameCommand.DIAMONDS))
        assertTrue(commands.contains(GameCommand.ADDITION))
    }

    @Test
    fun commandTags_classifyFamiliesAndOperators() {
        assertTrue(GameCommand.GREEN.tags.contains(CommandTag.Color))
        assertTrue(GameCommand.EVEN.tags.containsAll(setOf(CommandTag.Number, CommandTag.Parity)))
        assertTrue(GameCommand.NOT_DIAMONDS.tags.containsAll(setOf(CommandTag.Suit, CommandTag.Not)))
        assertTrue(GameCommand.ADDITION.tags.containsAll(setOf(CommandTag.Number, CommandTag.Arithmetic)))
        assertTrue(GameCommand.LESS_THAN.tags.containsAll(setOf(CommandTag.Number, CommandTag.Comparison)))
        assertTrue(GameCommand.GREATER_THAN_ARITHMETIC.tags.containsAll(setOf(CommandTag.Number, CommandTag.Arithmetic, CommandTag.Comparison)))
        assertTrue(GameCommand.GREEN_OR_UP.tags.containsAll(setOf(CommandTag.Color, CommandTag.Direction, CommandTag.Or)))
        assertTrue(GameCommand.NOTHING.tags.contains(CommandTag.Nothing))
    }

    @Test
    fun targetsOnlyProfile_containsOnlyTargetCommands() {
        val commands = GameRules.commandIdsForScore(
            score = GameRules.maxUnlockScore,
            commandProfile = CommandProfile.TargetsOnly,
        )

        assertEquals(setOf(GameCommand.TARGET, GameCommand.NOT_TARGET), commands.toSet())
    }

    @Test
    fun unlockScoreForLevel_returnsThresholdForLevel() {
        assertEquals(0, GameRules.unlockScoreForLevel(1))
        assertEquals(5, GameRules.unlockScoreForLevel(2))
        assertEquals(15, GameRules.unlockScoreForLevel(4))
        assertEquals(65, GameRules.unlockScoreForLevel(14))
        assertEquals(100, GameRules.unlockScoreForLevel(21))
        assertEquals(105, GameRules.unlockScoreForLevel(22))
        assertEquals(115, GameRules.unlockScoreForLevel(24))
        assertEquals(120, GameRules.unlockScoreForLevel(25))
        assertEquals(275, GameRules.unlockScoreForLevel(999))
    }

}
