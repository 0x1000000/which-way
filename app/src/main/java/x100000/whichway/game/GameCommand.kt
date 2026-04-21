package x100000.whichway.game

import kotlin.random.Random

enum class CommandTag {
    Direction,
    Color,
    Suit,
    Target,
    Number,
    Parity,
    Arithmetic,
    Comparison,
    Not,
    Or,
    And,
    Nothing,
}

enum class GameCommand(
    val complexity: Complexity,
    vararg tags: CommandTag,
) {
    LEFT(Complexity.Low, CommandTag.Direction) {
        override fun createRound(random: Random): RoundData = RoundStorage.Left
    },
    RIGHT(Complexity.Low, CommandTag.Direction) {
        override fun createRound(random: Random): RoundData = RoundStorage.Right
    },
    TARGET(Complexity.VeryLow, CommandTag.Target) {
        override fun createRound(random: Random): RoundData = RoundFactory.targetRound(random)
    },
    NOT_TARGET(Complexity.VeryLow, CommandTag.Target, CommandTag.Not) {
        override fun createRound(random: Random): RoundData = RoundFactory.notTargetRound(random)
    },
    DIAMONDS(Complexity.Normal, CommandTag.Suit) {
        override fun createRound(random: Random): RoundData = RoundFactory.suitRound(target = SuitTarget.Diamonds, random = random)
    },
    CLUBS(Complexity.Normal, CommandTag.Suit) {
        override fun createRound(random: Random): RoundData = RoundFactory.suitRound(target = SuitTarget.Clubs, random = random)
    },
    SPADES(Complexity.Normal, CommandTag.Suit) {
        override fun createRound(random: Random): RoundData = RoundFactory.suitRound(target = SuitTarget.Spades, random = random)
    },
    HEARTS(Complexity.Normal, CommandTag.Suit) {
        override fun createRound(random: Random): RoundData = RoundFactory.suitRound(target = SuitTarget.Hearts, random = random)
    },
    NOT_DIAMONDS(Complexity.Normal, CommandTag.Suit, CommandTag.Not) {
        override fun createRound(random: Random): RoundData = RoundFactory.negatedSuitRound(blocked = SuitTarget.Diamonds, random = random)
    },
    NOT_CLUBS(Complexity.Normal, CommandTag.Suit, CommandTag.Not) {
        override fun createRound(random: Random): RoundData = RoundFactory.negatedSuitRound(blocked = SuitTarget.Clubs, random = random)
    },
    NOT_SPADES(Complexity.Normal, CommandTag.Suit, CommandTag.Not) {
        override fun createRound(random: Random): RoundData = RoundFactory.negatedSuitRound(blocked = SuitTarget.Spades, random = random)
    },
    NOT_HEARTS(Complexity.Normal, CommandTag.Suit, CommandTag.Not) {
        override fun createRound(random: Random): RoundData = RoundFactory.negatedSuitRound(blocked = SuitTarget.Hearts, random = random)
    },
    UP(Complexity.Low, CommandTag.Direction) {
        override fun createRound(random: Random): RoundData = RoundStorage.Up
    },
    DOWN(Complexity.Low, CommandTag.Direction) {
        override fun createRound(random: Random): RoundData = RoundStorage.Down
    },
    NOT_LEFT(Complexity.Low, CommandTag.Direction, CommandTag.Not) {
        override fun createRound(random: Random): RoundData = RoundStorage.NotLeft
    },
    NOT_RIGHT(Complexity.Low, CommandTag.Direction, CommandTag.Not) {
        override fun createRound(random: Random): RoundData = RoundStorage.NotRight
    },
    NOT_UP(Complexity.Low, CommandTag.Direction, CommandTag.Not) {
        override fun createRound(random: Random): RoundData = RoundStorage.NotUp
    },
    NOT_DOWN(Complexity.Low, CommandTag.Direction, CommandTag.Not) {
        override fun createRound(random: Random): RoundData = RoundStorage.NotDown
    },
    NOTHING(Complexity.Low, CommandTag.Nothing) {
        override fun createRound(random: Random): RoundData = RoundStorage.Nothing
    },
    NOT_NOTHING(Complexity.Low, CommandTag.Nothing, CommandTag.Not) {
        override fun createRound(random: Random): RoundData = RoundStorage.NotNothing
    },
    GREEN(Complexity.Normal, CommandTag.Color) {
        override fun createRound(random: Random): RoundData = RoundFactory.colorRound(target = ColorTarget.Green, random = random)
    },
    BLUE(Complexity.Normal, CommandTag.Color) {
        override fun createRound(random: Random): RoundData = RoundFactory.colorRound(target = ColorTarget.Blue, random = random)
    },
    WHITE(Complexity.Normal, CommandTag.Color) {
        override fun createRound(random: Random): RoundData = RoundFactory.colorRound(target = ColorTarget.White, random = random)
    },
    YELLOW(Complexity.Normal, CommandTag.Color) {
        override fun createRound(random: Random): RoundData = RoundFactory.colorRound(target = ColorTarget.Yellow, random = random)
    },
    OR_UP_DOWN(Complexity.Normal, CommandTag.Direction, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundStorage.OrUpDown
    },
    OR_UP_LEFT(Complexity.Normal, CommandTag.Direction, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundStorage.OrUpLeft
    },
    OR_UP_RIGHT(Complexity.Normal, CommandTag.Direction, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundStorage.OrUpRight
    },
    OR_DOWN_LEFT(Complexity.Normal, CommandTag.Direction, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundStorage.OrDownLeft
    },
    OR_DOWN_RIGHT(Complexity.Normal, CommandTag.Direction, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundStorage.OrDownRight
    },
    OR_LEFT_RIGHT(Complexity.Normal, CommandTag.Direction, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundStorage.OrLeftRight
    },
    NOT_GREEN(Complexity.Normal, CommandTag.Color, CommandTag.Not) {
        override fun createRound(random: Random): RoundData = RoundFactory.negatedColorRound(blocked = ColorTarget.Green, random = random)
    },
    NOT_BLUE(Complexity.Normal, CommandTag.Color, CommandTag.Not) {
        override fun createRound(random: Random): RoundData = RoundFactory.negatedColorRound(blocked = ColorTarget.Blue, random = random)
    },
    NOT_WHITE(Complexity.Normal, CommandTag.Color, CommandTag.Not) {
        override fun createRound(random: Random): RoundData = RoundFactory.negatedColorRound(blocked = ColorTarget.White, random = random)
    },
    NOT_YELLOW(Complexity.Normal, CommandTag.Color, CommandTag.Not) {
        override fun createRound(random: Random): RoundData = RoundFactory.negatedColorRound(blocked = ColorTarget.Yellow, random = random)
    },
    OR_GREEN_BLUE(Complexity.Normal, CommandTag.Color, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundFactory.orColorRound(ColorTarget.Green, ColorTarget.Blue, random)
    },
    OR_GREEN_WHITE(Complexity.Normal, CommandTag.Color, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundFactory.orColorRound(ColorTarget.Green, ColorTarget.White, random)
    },
    OR_GREEN_YELLOW(Complexity.Normal, CommandTag.Color, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundFactory.orColorRound(ColorTarget.Green, ColorTarget.Yellow, random)
    },
    OR_BLUE_WHITE(Complexity.Normal, CommandTag.Color, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundFactory.orColorRound(ColorTarget.Blue, ColorTarget.White, random)
    },
    OR_BLUE_YELLOW(Complexity.Normal, CommandTag.Color, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundFactory.orColorRound(ColorTarget.Blue, ColorTarget.Yellow, random)
    },
    OR_WHITE_YELLOW(Complexity.Normal, CommandTag.Color, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundFactory.orColorRound(ColorTarget.White, ColorTarget.Yellow, random)
    },
    GREEN_OR_UP(Complexity.Normal, CommandTag.Color, CommandTag.Direction, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundFactory.colorOrDirectionRound(ColorTarget.Green, Direction.Up, random)
    },
    BLUE_OR_RIGHT(Complexity.Normal, CommandTag.Color, CommandTag.Direction, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundFactory.colorOrDirectionRound(ColorTarget.Blue, Direction.Right, random)
    },
    WHITE_OR_DOWN(Complexity.Normal, CommandTag.Color, CommandTag.Direction, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundFactory.colorOrDirectionRound(ColorTarget.White, Direction.Down, random)
    },
    YELLOW_OR_LEFT(Complexity.Normal, CommandTag.Color, CommandTag.Direction, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundFactory.colorOrDirectionRound(ColorTarget.Yellow, Direction.Left, random)
    },
    HEARTS_OR_UP(Complexity.Normal, CommandTag.Suit, CommandTag.Direction, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundFactory.suitOrDirectionRound(SuitTarget.Hearts, Direction.Up, random, this)
    },
    SPADES_OR_RIGHT(Complexity.Normal, CommandTag.Suit, CommandTag.Direction, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundFactory.suitOrDirectionRound(SuitTarget.Spades, Direction.Right, random, this)
    },
    DIAMONDS_OR_DOWN(Complexity.Normal, CommandTag.Suit, CommandTag.Direction, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundFactory.suitOrDirectionRound(SuitTarget.Diamonds, Direction.Down, random, this)
    },
    CLUBS_OR_LEFT(Complexity.Normal, CommandTag.Suit, CommandTag.Direction, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundFactory.suitOrDirectionRound(SuitTarget.Clubs, Direction.Left, random, this)
    },
    NOT_GREEN_AND_NOT_UP(Complexity.VeryHigh, CommandTag.Color, CommandTag.Direction, CommandTag.Not, CommandTag.And) {
        override fun createRound(random: Random): RoundData = RoundFactory.notColorAndNotDirectionRound(ColorTarget.Green, Direction.Up, random)
    },
    NOT_BLUE_AND_NOT_RIGHT(Complexity.VeryHigh, CommandTag.Color, CommandTag.Direction, CommandTag.Not, CommandTag.And) {
        override fun createRound(random: Random): RoundData = RoundFactory.notColorAndNotDirectionRound(ColorTarget.Blue, Direction.Right, random)
    },
    NOT_WHITE_AND_NOT_DOWN(Complexity.VeryHigh, CommandTag.Color, CommandTag.Direction, CommandTag.Not, CommandTag.And) {
        override fun createRound(random: Random): RoundData = RoundFactory.notColorAndNotDirectionRound(ColorTarget.White, Direction.Down, random)
    },
    NOT_YELLOW_AND_NOT_LEFT(Complexity.VeryHigh, CommandTag.Color, CommandTag.Direction, CommandTag.Not, CommandTag.And) {
        override fun createRound(random: Random): RoundData = RoundFactory.notColorAndNotDirectionRound(ColorTarget.Yellow, Direction.Left, random)
    },
    NUMBER(Complexity.Normal, CommandTag.Number) {
        override fun createRound(random: Random): RoundData = RoundFactory.numberRound(random)
    },
    EVEN(Complexity.Normal, CommandTag.Number, CommandTag.Parity) {
        override fun createRound(random: Random): RoundData = RoundFactory.parityRound(matchesEven = true, random = random, commandId = this)
    },
    ODD(Complexity.Normal, CommandTag.Number, CommandTag.Parity) {
        override fun createRound(random: Random): RoundData = RoundFactory.parityRound(matchesEven = false, random = random, commandId = this)
    },
    NOT_NUMBER(Complexity.Normal, CommandTag.Number, CommandTag.Not) {
        override fun createRound(random: Random): RoundData = RoundFactory.notNumberRound(random)
    },
    LESS_THAN(Complexity.High, CommandTag.Number, CommandTag.Comparison) {
        override fun createRound(random: Random): RoundData = RoundFactory.lessThanRound(random)
    },
    GREATER_THAN(Complexity.High, CommandTag.Number, CommandTag.Comparison) {
        override fun createRound(random: Random): RoundData = RoundFactory.greaterThanRound(random)
    },
    UP_OR_NUMBER(Complexity.Normal, CommandTag.Direction, CommandTag.Number, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundFactory.directionOrNumberRound(Direction.Up, random)
    },
    RIGHT_OR_NUMBER(Complexity.Normal, CommandTag.Direction, CommandTag.Number, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundFactory.directionOrNumberRound(Direction.Right, random)
    },
    DOWN_OR_NUMBER(Complexity.Normal, CommandTag.Direction, CommandTag.Number, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundFactory.directionOrNumberRound(Direction.Down, random)
    },
    LEFT_OR_NUMBER(Complexity.Normal, CommandTag.Direction, CommandTag.Number, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundFactory.directionOrNumberRound(Direction.Left, random)
    },
    GREEN_OR_NUMBER(Complexity.Normal, CommandTag.Color, CommandTag.Number, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundFactory.colorOrNumberRound(ColorTarget.Green, random, this)
    },
    BLUE_OR_NUMBER(Complexity.Normal, CommandTag.Color, CommandTag.Number, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundFactory.colorOrNumberRound(ColorTarget.Blue, random, this)
    },
    WHITE_OR_NUMBER(Complexity.Normal, CommandTag.Color, CommandTag.Number, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundFactory.colorOrNumberRound(ColorTarget.White, random, this)
    },
    YELLOW_OR_NUMBER(Complexity.Normal, CommandTag.Color, CommandTag.Number, CommandTag.Or) {
        override fun createRound(random: Random): RoundData = RoundFactory.colorOrNumberRound(ColorTarget.Yellow, random, this)
    },
    ADDITION(Complexity.High, CommandTag.Number, CommandTag.Arithmetic) {
        override fun createRound(random: Random): RoundData = RoundFactory.additionRound(random)
    },
    SUBTRACTION(Complexity.High, CommandTag.Number, CommandTag.Arithmetic) {
        override fun createRound(random: Random): RoundData = RoundFactory.subtractionRound(random)
    },
    LESS_THAN_ARITHMETIC(Complexity.VeryHigh, CommandTag.Number, CommandTag.Arithmetic, CommandTag.Comparison) {
        override fun createRound(random: Random): RoundData = RoundFactory.lessThanArithmeticRound(random)
    },
    GREATER_THAN_ARITHMETIC(Complexity.VeryHigh, CommandTag.Number, CommandTag.Arithmetic, CommandTag.Comparison) {
        override fun createRound(random: Random): RoundData = RoundFactory.greaterThanArithmeticRound(random)
    },
    NOT_ADDITION(Complexity.VeryHigh, CommandTag.Number, CommandTag.Arithmetic, CommandTag.Not) {
        override fun createRound(random: Random): RoundData = RoundFactory.negatedAdditionRound(random)
    },
    NOT_SUBTRACTION(Complexity.VeryHigh, CommandTag.Number, CommandTag.Arithmetic, CommandTag.Not) {
        override fun createRound(random: Random): RoundData = RoundFactory.negatedSubtractionRound(random)
    };

    val tags: Set<CommandTag> = tags.toSet()

    abstract fun createRound(random: Random): RoundData
}
