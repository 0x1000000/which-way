package x100000.whichway.game

import kotlin.random.Random

object RoundFactory {
    private val numberPool = (1..17).toList()
    private const val MIN_OPERAND = 0
    private const val MIN_RESULT = 2
    private const val MAX_RESULT = 16
    private const val MIN_COMPARISON_THRESHOLD = 3
    private const val MAX_COMPARISON_THRESHOLD = 15

    fun colorRound(target: ColorTarget, random: Random): RoundData {
        val facts = createColorFacts(random)
        return RoundData(
            prompt = target.label,
            validDirections = directionsMatching(facts) { it.color == target },
            complexity = GameCommand.valueOf(target.label).complexity,
            zoneFacts = facts,
            commandId = GameCommand.valueOf(target.label),
        )
    }

    fun suitRound(target: SuitTarget, random: Random): RoundData {
        val facts = createSuitFacts(random)
        return RoundData(
            prompt = target.label,
            validDirections = directionsMatching(facts) { it.suit == target },
            complexity = GameCommand.valueOf(target.label).complexity,
            zoneFacts = facts,
            commandId = GameCommand.valueOf(target.label),
        )
    }

    fun negatedSuitRound(blocked: SuitTarget, random: Random): RoundData {
        val facts = createSuitFacts(random)
        return RoundData(
            prompt = GameRules.stackedNotPhrase(blocked.label),
            validDirections = directionsMatching(facts) { it.suit != blocked },
            complexity = GameCommand.valueOf("NOT_${blocked.label}").complexity,
            zoneFacts = facts,
            commandId = GameCommand.valueOf("NOT_${blocked.label}"),
        )
    }

    fun targetRound(random: Random): RoundData {
        val targetDirection = Direction.entries.random(random)
        val facts = createTargetFacts(targetDirection)
        return RoundData(
            prompt = "",
            validDirections = setOf(targetDirection),
            complexity = GameCommand.TARGET.complexity,
            zoneFacts = facts,
            commandId = GameCommand.TARGET,
        )
    }

    fun notTargetRound(random: Random): RoundData {
        val blockedDirection = Direction.entries.random(random)
        val facts = createTargetFacts(blockedDirection)
        return RoundData(
            prompt = "NOT",
            validDirections = Direction.entries.filterTo(mutableSetOf()) { it != blockedDirection },
            complexity = GameCommand.NOT_TARGET.complexity,
            zoneFacts = facts,
            commandId = GameCommand.NOT_TARGET,
        )
    }

    fun negatedColorRound(blocked: ColorTarget, random: Random): RoundData {
        val facts = createColorFacts(random)
        return RoundData(
            prompt = GameRules.stackedNotPhrase(blocked.label),
            validDirections = directionsMatching(facts) { it.color != blocked },
            complexity = GameCommand.valueOf("NOT_${blocked.label}").complexity,
            zoneFacts = facts,
            commandId = GameCommand.valueOf("NOT_${blocked.label}"),
        )
    }

    fun orColorRound(first: ColorTarget, second: ColorTarget, random: Random): RoundData {
        val facts = createColorFacts(random)
        return RoundData(
            prompt = GameRules.threeLinePrompt(first.label, "OR", second.label),
            validDirections = directionsMatching(facts) { it.color == first || it.color == second },
            complexity = Complexity.Normal,
            zoneFacts = facts,
            commandId = GameCommand.valueOf("OR_${first.label}_${second.label}"),
        )
    }

    fun colorOrDirectionRound(
        color: ColorTarget,
        direction: Direction,
        random: Random,
    ): RoundData {
        repeat(8) {
            val facts = createColorFacts(random)
            val colorDirection = directionMatching(facts) { it.color == color }
            if (colorDirection != direction) {
                return RoundData(
                    prompt = GameRules.threeLinePrompt(color.label, "OR", direction.name.uppercase()),
                    validDirections = setOf(colorDirection, direction),
                    complexity = Complexity.Normal,
                    zoneFacts = facts,
                    commandId = GameCommand.valueOf("${color.label}_OR_${direction.name.uppercase()}"),
                )
            }
        }
        val fallbackFacts = createColorFacts(random)
        return RoundData(
            prompt = GameRules.threeLinePrompt(color.label, "OR", direction.name.uppercase()),
            validDirections = setOf(directionMatching(fallbackFacts) { it.color == color }, direction),
            complexity = Complexity.Normal,
            zoneFacts = fallbackFacts,
            commandId = GameCommand.valueOf("${color.label}_OR_${direction.name.uppercase()}"),
        )
    }

    fun suitOrDirectionRound(
        suit: SuitTarget,
        direction: Direction,
        random: Random,
        commandId: GameCommand,
    ): RoundData {
        repeat(8) {
            val facts = createSuitFacts(random)
            val suitDirection = directionMatching(facts) { it.suit == suit }
            if (suitDirection != direction) {
                return RoundData(
                    prompt = GameRules.threeLinePrompt(suit.label, "OR", direction.name.uppercase()),
                    validDirections = setOf(suitDirection, direction),
                    complexity = commandId.complexity,
                    zoneFacts = facts,
                    commandId = commandId,
                )
            }
        }
        val fallbackFacts = createSuitFacts(random)
        return RoundData(
            prompt = GameRules.threeLinePrompt(suit.label, "OR", direction.name.uppercase()),
            validDirections = setOf(directionMatching(fallbackFacts) { it.suit == suit }, direction),
            complexity = commandId.complexity,
            zoneFacts = fallbackFacts,
            commandId = commandId,
        )
    }

    fun notColorAndNotDirectionRound(
        blockedColor: ColorTarget,
        blockedDirection: Direction,
        random: Random,
    ): RoundData {
        val facts = createColorFacts(random)
        return RoundData(
            prompt = GameRules.threeLinePrompt(
                GameRules.inlineNotPhrase(blockedColor.label),
                "AND",
                GameRules.inlineNotPhrase(blockedDirection.name.uppercase()),
            ),
            validDirections = directionsMatching(facts) {
                it.color != blockedColor && directionForFacts(facts, it) != blockedDirection
            },
            complexity = Complexity.VeryHigh,
            zoneFacts = facts,
            commandId = GameCommand.valueOf("NOT_${blockedColor.label}_AND_NOT_${blockedDirection.name.uppercase()}"),
        )
    }

    fun numberRound(random: Random): RoundData {
        val facts = createNumberFacts(random)
        val targetDirection = Direction.entries.random(random)
        val targetNumber = facts.getValue(targetDirection).number!!
        return RoundData(
            prompt = targetNumber.toString(),
            validDirections = setOf(targetDirection),
            complexity = GameCommand.NUMBER.complexity,
            zoneFacts = facts,
            commandId = GameCommand.NUMBER,
        )
    }

    fun notNumberRound(random: Random): RoundData {
        val facts = createNumberFacts(random)
        val blockedDirection = Direction.entries.random(random)
        val blockedNumber = facts.getValue(blockedDirection).number!!
        return RoundData(
            prompt = GameRules.stackedNotPhrase(blockedNumber.toString()),
            validDirections = Direction.entries.filterTo(mutableSetOf()) { it != blockedDirection },
            complexity = GameCommand.NOT_NUMBER.complexity,
            zoneFacts = facts,
            commandId = GameCommand.NOT_NUMBER,
        )
    }

    fun parityRound(
        matchesEven: Boolean,
        random: Random,
        commandId: GameCommand,
    ): RoundData {
        val facts = createParityFacts(random)
        return RoundData(
            prompt = if (matchesEven) "EVEN" else "ODD",
            validDirections = directionsMatching(facts) { zoneFacts ->
                val value = zoneFacts.number ?: return@directionsMatching false
                (value % 2 == 0) == matchesEven
            },
            complexity = commandId.complexity,
            zoneFacts = facts,
            commandId = commandId,
        )
    }

    fun lessThanRound(random: Random): RoundData =
        comparisonRound(
            commandId = GameCommand.LESS_THAN,
            operator = "<",
            threshold = random.nextInt(MIN_COMPARISON_THRESHOLD, MAX_COMPARISON_THRESHOLD + 1),
            random = random,
        )

    fun greaterThanRound(random: Random): RoundData =
        comparisonRound(
            commandId = GameCommand.GREATER_THAN,
            operator = ">",
            threshold = random.nextInt(MIN_COMPARISON_THRESHOLD, MAX_COMPARISON_THRESHOLD + 1),
            random = random,
        )

    fun directionOrNumberRound(
        direction: Direction,
        random: Random,
    ): RoundData {
        repeat(8) {
            val facts = createNumberFacts(random)
            val numberDirection = Direction.entries.random(random)
            if (numberDirection != direction) {
                val number = facts.getValue(numberDirection).number!!
                return RoundData(
                    prompt = GameRules.threeLinePrompt(direction.name.uppercase(), "OR", number.toString()),
                    validDirections = setOf(direction, numberDirection),
                    complexity = Complexity.Normal,
                    zoneFacts = facts,
                    commandId = GameCommand.valueOf("${direction.name.uppercase()}_OR_NUMBER"),
                )
            }
        }
        val facts = createNumberFacts(random)
        val numberDirection = Direction.entries.first { it != direction }
        val number = facts.getValue(numberDirection).number!!
        return RoundData(
            prompt = GameRules.threeLinePrompt(direction.name.uppercase(), "OR", number.toString()),
            validDirections = setOf(direction, numberDirection),
            complexity = Complexity.Normal,
            zoneFacts = facts,
            commandId = GameCommand.valueOf("${direction.name.uppercase()}_OR_NUMBER"),
        )
    }

    fun colorOrNumberRound(
        color: ColorTarget,
        random: Random,
        commandId: GameCommand,
    ): RoundData {
        val facts = createColorAndNumberFacts(random)
        val targetNumber = facts.values.random(random).number!!
        return mixedRound(
            prompt = GameRules.threeLinePrompt(color.label, "OR", targetNumber.toString()),
            commandId = commandId,
            facts = facts,
        ) { zoneFacts ->
            zoneFacts.color == color || zoneFacts.number == targetNumber
        }
    }

    fun additionRound(random: Random): RoundData {
        val result = random.nextInt(MIN_RESULT, MAX_RESULT + 1)
        val left = random.nextInt(MIN_OPERAND, result + 1)
        val right = result - left
        return arithmeticRound(
            prompt = "$left + $right",
            result = result,
            complexity = GameCommand.ADDITION.complexity,
            commandId = GameCommand.ADDITION,
            random = random,
        )
    }

    fun subtractionRound(random: Random): RoundData {
        val result = random.nextInt(MIN_RESULT, MAX_RESULT + 1)
        val right = random.nextInt(MIN_OPERAND, MAX_RESULT - result + 1)
        val left = result + right
        return arithmeticRound(
            prompt = "$left - $right",
            result = result,
            complexity = GameCommand.SUBTRACTION.complexity,
            commandId = GameCommand.SUBTRACTION,
            random = random,
        )
    }

    fun negatedAdditionRound(random: Random): RoundData {
        val result = random.nextInt(MIN_RESULT, MAX_RESULT + 1)
        val left = random.nextInt(MIN_OPERAND, result + 1)
        val right = result - left
        return negatedArithmeticRound(
            prompt = "$left + $right",
            result = result,
            complexity = GameCommand.NOT_ADDITION.complexity,
            commandId = GameCommand.NOT_ADDITION,
            random = random,
        )
    }

    fun negatedSubtractionRound(random: Random): RoundData {
        val result = random.nextInt(MIN_RESULT, MAX_RESULT + 1)
        val right = random.nextInt(MIN_OPERAND, MAX_RESULT - result + 1)
        val left = result + right
        return negatedArithmeticRound(
            prompt = "$left - $right",
            result = result,
            complexity = GameCommand.NOT_SUBTRACTION.complexity,
            commandId = GameCommand.NOT_SUBTRACTION,
            random = random,
        )
    }

    fun lessThanArithmeticRound(random: Random): RoundData {
        val (expression, threshold) = arithmeticExpression(random)
        return comparisonRound(
            commandId = GameCommand.LESS_THAN_ARITHMETIC,
            operator = "<",
            threshold = threshold,
            thresholdPrompt = expression,
            random = random,
        )
    }

    fun greaterThanArithmeticRound(random: Random): RoundData {
        val (expression, threshold) = arithmeticExpression(random)
        return comparisonRound(
            commandId = GameCommand.GREATER_THAN_ARITHMETIC,
            operator = ">",
            threshold = threshold,
            thresholdPrompt = expression,
            random = random,
        )
    }

    private fun createColorFacts(random: Random): Map<Direction, ZoneFacts> =
        Direction.entries
            .zip(ColorTarget.entries.shuffled(random))
            .associate { (direction, color) ->
                direction to ZoneFacts(color = color)
            }

    private fun createSuitFacts(random: Random): Map<Direction, ZoneFacts> =
        Direction.entries
            .zip(SuitTarget.entries.shuffled(random))
            .associate { (direction, suit) ->
                direction to ZoneFacts(suit = suit)
            }

    private fun createTargetFacts(targetDirection: Direction): Map<Direction, ZoneFacts> =
        Direction.entries.associateWith { direction ->
            ZoneFacts(target = direction == targetDirection)
        }

    private fun createNumberFacts(random: Random): Map<Direction, ZoneFacts> {
        val numbers = numberPool.shuffled(random).take(Direction.entries.size)
        return Direction.entries.zip(numbers).associate { (direction, number) ->
            direction to ZoneFacts(number = number)
        }
    }

    private fun createParityFacts(random: Random): Map<Direction, ZoneFacts> {
        repeat(8) {
            val facts = createNumberFacts(random)
            val parities = facts.values.mapNotNull { it.number?.mod(2) }.toSet()
            if (parities.size > 1) {
                return facts
            }
        }
        return mapOf(
            Direction.Up to ZoneFacts(number = 2),
            Direction.Right to ZoneFacts(number = 4),
            Direction.Down to ZoneFacts(number = 7),
            Direction.Left to ZoneFacts(number = 9),
        )
    }

    private fun createColorAndNumberFacts(random: Random): Map<Direction, ZoneFacts> {
        val colors = ColorTarget.entries.shuffled(random)
        val numbers = numberPool.shuffled(random).take(Direction.entries.size)
        return Direction.entries.indices.associate { index ->
            val direction = Direction.entries[index]
            direction to ZoneFacts(
                color = colors[index],
                number = numbers[index],
            )
        }
    }

    private fun arithmeticRound(
        prompt: String,
        result: Int,
        complexity: Complexity,
        commandId: GameCommand,
        random: Random,
    ): RoundData {
        val facts = createArithmeticFacts(result, random)
        return RoundData(
            prompt = prompt,
            validDirections = directionsMatching(facts) { it.number == result },
            complexity = complexity,
            zoneFacts = facts,
            commandId = commandId,
        )
    }

    private fun negatedArithmeticRound(
        prompt: String,
        result: Int,
        complexity: Complexity,
        commandId: GameCommand,
        random: Random,
    ): RoundData {
        val facts = createArithmeticFacts(result, random)
        return RoundData(
            prompt = GameRules.stackedNotPhrase(prompt),
            validDirections = directionsMatching(facts) { it.number != result },
            complexity = complexity,
            zoneFacts = facts,
            commandId = commandId,
        )
    }

    private fun createArithmeticFacts(
        correctResult: Int,
        random: Random,
    ): Map<Direction, ZoneFacts> {
        val targetDirection = Direction.entries.random(random)
        val distractors = numberPool
            .filter { it != correctResult }
            .shuffled(random)
            .take(Direction.entries.size - 1)
        val numbersByDirection = buildMap {
            put(targetDirection, correctResult)
            Direction.entries
                .filter { it != targetDirection }
                .zip(distractors)
                .forEach { (direction, number) ->
                    put(direction, number)
                }
        }
        return Direction.entries.associateWith { direction ->
            ZoneFacts(number = numbersByDirection.getValue(direction))
        }
    }

    private fun comparisonRound(
        commandId: GameCommand,
        operator: String,
        threshold: Int,
        random: Random,
        thresholdPrompt: String = threshold.toString(),
    ): RoundData {
        val facts = createComparisonFacts(
            matchingPredicate = { value ->
                when (operator) {
                    "<" -> value < threshold
                    ">" -> value > threshold
                    else -> error("Unsupported comparison operator: $operator")
                }
            },
            random = random,
        )
        return RoundData(
            prompt = "$operator $thresholdPrompt",
            validDirections = directionsMatching(facts) { zoneFacts ->
                val value = zoneFacts.number ?: return@directionsMatching false
                when (operator) {
                    "<" -> value < threshold
                    ">" -> value > threshold
                    else -> false
                }
            },
            complexity = commandId.complexity,
            zoneFacts = facts,
            commandId = commandId,
        )
    }

    private fun createComparisonFacts(
        matchingPredicate: (Int) -> Boolean,
        random: Random,
    ): Map<Direction, ZoneFacts> {
        val matchingPool = numberPool.filter(matchingPredicate)
        val nonMatchingPool = numberPool.filterNot(matchingPredicate)
        require(matchingPool.size >= 2) { "Comparison round requires at least two matching values." }
        require(nonMatchingPool.size >= 2) { "Comparison round requires at least two non-matching values." }

        val allNumbers = (
            matchingPool.shuffled(random).take(2) +
                nonMatchingPool.shuffled(random).take(2)
            ).shuffled(random)

        return Direction.entries.zip(allNumbers).associate { (direction, number) ->
            direction to ZoneFacts(number = number)
        }
    }

    private fun mixedRound(
        prompt: String,
        commandId: GameCommand,
        facts: Map<Direction, ZoneFacts>,
        predicate: (ZoneFacts) -> Boolean,
    ): RoundData =
        RoundData(
            prompt = prompt,
            validDirections = directionsMatching(facts, predicate),
            complexity = commandId.complexity,
            zoneFacts = facts,
            commandId = commandId,
        )


    private fun arithmeticExpression(random: Random): Pair<String, Int> =
        if (random.nextBoolean()) {
            val result = random.nextInt(MIN_COMPARISON_THRESHOLD, MAX_COMPARISON_THRESHOLD + 1)
            val left = random.nextInt(MIN_OPERAND, result + 1)
            val right = result - left
            "$left + $right" to result
        } else {
            val result = random.nextInt(MIN_COMPARISON_THRESHOLD, MAX_COMPARISON_THRESHOLD + 1)
            val right = random.nextInt(MIN_OPERAND, MAX_RESULT - result + 1)
            val left = result + right
            "$left - $right" to result
        }

    private fun directionsMatching(
        facts: Map<Direction, ZoneFacts>,
        predicate: (ZoneFacts) -> Boolean,
    ): Set<Direction> =
        facts.filterValues(predicate).keys

    private fun directionMatching(
        facts: Map<Direction, ZoneFacts>,
        predicate: (ZoneFacts) -> Boolean,
    ): Direction =
        facts.entries.first { (_, zoneFacts) -> predicate(zoneFacts) }.key

    private fun directionForFacts(
        facts: Map<Direction, ZoneFacts>,
        zoneFacts: ZoneFacts,
    ): Direction =
        facts.entries.first { (_, value) -> value == zoneFacts }.key

}
