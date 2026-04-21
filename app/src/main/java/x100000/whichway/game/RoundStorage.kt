package x100000.whichway.game

object RoundStorage {
    val Left = RoundData(
        prompt = "LEFT",
        validDirections = setOf(Direction.Left),
        complexity = Complexity.Low,
        commandId = GameCommand.LEFT,
    )
    val Right = RoundData(
        prompt = "RIGHT",
        validDirections = setOf(Direction.Right),
        complexity = Complexity.Low,
        commandId = GameCommand.RIGHT,
    )
    val Up = RoundData(
        prompt = "UP",
        validDirections = setOf(Direction.Up),
        complexity = Complexity.Low,
        commandId = GameCommand.UP,
    )
    val Down = RoundData(
        prompt = "DOWN",
        validDirections = setOf(Direction.Down),
        complexity = Complexity.Low,
        commandId = GameCommand.DOWN,
    )
    val NotLeft = RoundData(
        prompt = GameRules.stackedNotPhrase("LEFT"),
        validDirections = Direction.entries.filterTo(mutableSetOf()) { it != Direction.Left },
        complexity = Complexity.Low,
        commandId = GameCommand.NOT_LEFT,
    )
    val NotRight = RoundData(
        prompt = GameRules.stackedNotPhrase("RIGHT"),
        validDirections = Direction.entries.filterTo(mutableSetOf()) { it != Direction.Right },
        complexity = Complexity.Low,
        commandId = GameCommand.NOT_RIGHT,
    )
    val NotUp = RoundData(
        prompt = GameRules.stackedNotPhrase("UP"),
        validDirections = Direction.entries.filterTo(mutableSetOf()) { it != Direction.Up },
        complexity = Complexity.Low,
        commandId = GameCommand.NOT_UP,
    )
    val NotDown = RoundData(
        prompt = GameRules.stackedNotPhrase("DOWN"),
        validDirections = Direction.entries.filterTo(mutableSetOf()) { it != Direction.Down },
        complexity = Complexity.Low,
        commandId = GameCommand.NOT_DOWN,
    )
    val Nothing = RoundData(
        prompt = "NOTHING",
        validDirections = emptySet(),
        complexity = Complexity.Low,
        timeoutIsCorrect = true,
        commandId = GameCommand.NOTHING,
    )
    val NotNothing = RoundData(
        prompt = GameRules.stackedNotPhrase("NOTHING"),
        validDirections = Direction.entries.toSet(),
        complexity = Complexity.Low,
        commandId = GameCommand.NOT_NOTHING,
    )
    val OrUpDown = RoundData(
        prompt = GameRules.threeLinePrompt("UP", "OR", "DOWN"),
        validDirections = setOf(Direction.Up, Direction.Down),
        complexity = Complexity.Normal,
        commandId = GameCommand.OR_UP_DOWN,
    )
    val OrUpLeft = RoundData(
        prompt = GameRules.threeLinePrompt("UP", "OR", "LEFT"),
        validDirections = setOf(Direction.Up, Direction.Left),
        complexity = Complexity.Normal,
        commandId = GameCommand.OR_UP_LEFT,
    )
    val OrUpRight = RoundData(
        prompt = GameRules.threeLinePrompt("UP", "OR", "RIGHT"),
        validDirections = setOf(Direction.Up, Direction.Right),
        complexity = Complexity.Normal,
        commandId = GameCommand.OR_UP_RIGHT,
    )
    val OrDownLeft = RoundData(
        prompt = GameRules.threeLinePrompt("DOWN", "OR", "LEFT"),
        validDirections = setOf(Direction.Down, Direction.Left),
        complexity = Complexity.Normal,
        commandId = GameCommand.OR_DOWN_LEFT,
    )
    val OrDownRight = RoundData(
        prompt = GameRules.threeLinePrompt("DOWN", "OR", "RIGHT"),
        validDirections = setOf(Direction.Down, Direction.Right),
        complexity = Complexity.Normal,
        commandId = GameCommand.OR_DOWN_RIGHT,
    )
    val OrLeftRight = RoundData(
        prompt = GameRules.threeLinePrompt("LEFT", "OR", "RIGHT"),
        validDirections = setOf(Direction.Left, Direction.Right),
        complexity = Complexity.Normal,
        commandId = GameCommand.OR_LEFT_RIGHT,
    )
}
