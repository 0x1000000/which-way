package x100000.whichway.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameSessionTest {

    @Test
    fun wrongTapWithoutCharges_losesLifeAndAdvancesRound() {
        val session = GameSession(
            config = GameConfig(),
            random = object : Random() {
                override fun nextBits(bitCount: Int): Int = 0
            },
        )

        val initial = session.snapshot()
        val wrongDirection = if (Direction.Left in initial.roundData.validDirections) {
            Direction.Right
        } else {
            Direction.Left
        }

        val transition = session.onZoneClick(
            direction = wrongDirection,
            elapsedMillis = GameRules.LOW_COMPLEXITY_TIMEOUT_MILLIS,
        )

        assertTrue(transition is GameSessionResult.LifeLost)
        val nextState = (transition as GameSessionResult.LifeLost).state
        assertEquals(GameRules.STARTING_LIVES - 1, nextState.lives)
        assertTrue(nextState.roundNumber > initial.roundNumber)
    }

    @Test
    fun correctTapAdvancesScore() {
        val session = GameSession(config = GameConfig(), random = Random(0))
        val initial = session.snapshot()
        val correctDirection = initial.roundData.validDirections.first()

        val transition = session.onZoneClick(
            direction = correctDirection,
            elapsedMillis = GameRules.LOW_COMPLEXITY_TIMEOUT_MILLIS,
        )

        assertTrue(transition is GameSessionResult.CorrectAdvance)
        val nextResult = transition as GameSessionResult.CorrectAdvance
        val nextState = nextResult.state
        assertEquals(1, nextState.score)
        assertTrue(!nextResult.earnedCharge)
        assertTrue(nextState.roundNumber > initial.roundNumber)
    }

    @Test
    fun fastCorrectTapAwardsCharge() {
        val session = GameSession(config = GameConfig(), random = Random(0))
        val initial = session.snapshot()
        val correctDirection = initial.roundData.validDirections.first()

        val transition = session.onZoneClick(
            direction = correctDirection,
            elapsedMillis = 0,
        )

        assertTrue(transition is GameSessionResult.CorrectAdvance)
        val nextResult = transition as GameSessionResult.CorrectAdvance
        assertTrue(nextResult.earnedCharge)
        assertEquals(1, nextResult.state.charges)
    }

    @Test
    fun fastCorrectTapDoesNotReportChargeWhenAlreadyFull() {
        val session = GameSession(config = GameConfig(), random = Random(0))

        repeat(GameRules.MAX_CHARGES) {
            val state = session.snapshot()
            val transition = session.onZoneClick(
                direction = state.roundData.validDirections.first(),
                elapsedMillis = 0,
            )
            assertTrue(transition is GameSessionResult.CorrectAdvance)
            assertTrue((transition as GameSessionResult.CorrectAdvance).earnedCharge)
        }

        val fullState = session.snapshot()
        assertEquals(GameRules.MAX_CHARGES, fullState.charges)

        val transition = session.onZoneClick(
            direction = fullState.roundData.validDirections.first(),
            elapsedMillis = 0,
        )

        assertTrue(transition is GameSessionResult.CorrectAdvance)
        val nextResult = transition as GameSessionResult.CorrectAdvance
        assertTrue(!nextResult.earnedCharge)
        assertEquals(GameRules.MAX_CHARGES, nextResult.state.charges)
    }

    @Test
    fun wrongFastTapDoesNotAwardCharge() {
        val session = GameSession(config = GameConfig(), random = Random(0))
        val initial = session.snapshot()
        val wrongDirection = if (Direction.Left in initial.roundData.validDirections) {
            Direction.Right
        } else {
            Direction.Left
        }

        val transition = session.onZoneClick(
            direction = wrongDirection,
            elapsedMillis = 0,
        )

        assertTrue(transition is GameSessionResult.LifeLost)
        val nextState = (transition as GameSessionResult.LifeLost).state
        assertEquals(0, initial.charges)
        assertEquals(0, nextState.charges)
    }

    @Test
    fun fastCorrectTapOnRestartRoundDoesNotAwardCharge() {
        val session = GameSession(config = GameConfig(), random = Random(0))

        val firstState = session.snapshot()
        val firstCorrect = session.onZoneClick(
            direction = firstState.roundData.validDirections.first(),
            elapsedMillis = 0,
        ) as GameSessionResult.CorrectAdvance
        assertEquals(1, firstCorrect.state.charges)

        val replaySource = firstCorrect.state
        val wrongDirection = Direction.entries.first { it !in replaySource.roundData.validDirections }
        val replayTransition = session.onZoneClick(
            direction = wrongDirection,
            elapsedMillis = 0,
        )

        assertTrue(replayTransition is GameSessionResult.ChargeReplay)
        val replayState = (replayTransition as GameSessionResult.ChargeReplay).state
        assertTrue(replayState.isRestartRound)
        assertEquals(0, replayState.charges)

        val replayCorrect = session.onZoneClick(
            direction = replayState.roundData.validDirections.first(),
            elapsedMillis = 0,
        )

        assertTrue(replayCorrect is GameSessionResult.CorrectAdvance)
        val nextResult = replayCorrect as GameSessionResult.CorrectAdvance
        assertTrue(!nextResult.earnedCharge)
        assertEquals(0, nextResult.state.charges)
        assertTrue(!nextResult.state.isRestartRound)
    }

    @Test
    fun runStats_tracksOnlyTapResponses_notTimeouts() {
        val session = GameSession(config = GameConfig(), random = Random(0))
        val firstState = session.snapshot()

        session.onZoneClick(
            direction = firstState.roundData.validDirections.first(),
            elapsedMillis = 321,
        )
        session.onTimeout(elapsedMillis = 2000)

        val stats = session.runStats()
        assertEquals(321L, stats.totalResponseTimeMs)
        assertEquals(1, stats.responseCount)
    }

    @Test
    fun smallCommandPool_usesRandomSelection_andCanRepeatImmediately() {
        val stubbornRandom = object : Random() {
            override fun nextBits(bitCount: Int): Int = 0
        }
        val session = GameSession(config = GameConfig(), random = stubbornRandom)
        val initial = session.snapshot()

        assertEquals(GameCommand.LEFT, initial.roundData.commandId)

        val transition = session.onZoneClick(
            direction = initial.roundData.validDirections.first(),
            elapsedMillis = GameRules.LOW_COMPLEXITY_TIMEOUT_MILLIS,
        ) as GameSessionResult.CorrectAdvance

        assertEquals(GameCommand.LEFT, transition.state.roundData.commandId)
    }

    @Test
    fun smallCommandPool_avoidsThreeIdenticalAnswerSetsInARow() {
        val alternatingAfterTwoLeftsRandom = object : Random() {
            private var calls = 0

            override fun nextBits(bitCount: Int): Int {
                calls += 1
                return when {
                    calls <= 2 -> 0
                    else -> 1 shl (bitCount - 1)
                }
            }
        }
        val session = GameSession(config = GameConfig(), random = alternatingAfterTwoLeftsRandom)
        val firstState = session.snapshot()

        assertEquals(setOf(Direction.Left), firstState.roundData.validDirections)

        val firstTransition = session.onZoneClick(
            direction = firstState.roundData.validDirections.first(),
            elapsedMillis = GameRules.LOW_COMPLEXITY_TIMEOUT_MILLIS,
        ) as GameSessionResult.CorrectAdvance
        assertEquals(setOf(Direction.Left), firstTransition.state.roundData.validDirections)

        val secondTransition = session.onZoneClick(
            direction = firstTransition.state.roundData.validDirections.first(),
            elapsedMillis = GameRules.LOW_COMPLEXITY_TIMEOUT_MILLIS,
        ) as GameSessionResult.CorrectAdvance

        assertNotEquals(setOf(Direction.Left), secondTransition.state.roundData.validDirections)
    }

    @Test
    fun levelThreeSizedCommandPool_usesQueue_withoutRepeatsInFirstCycle() {
        val session = GameSession(
            config = GameConfig(
                unlockFloor = Level.Level3.unlockScore,
                allowsProgression = false,
            ),
            random = Random(0),
        )
        val seenCommands = mutableListOf(session.snapshot().roundData.commandId!!)

        repeat(7) {
            val state = session.snapshot()
            val transition = session.onZoneClick(
                direction = state.roundData.validDirections.first(),
                elapsedMillis = GameRules.LOW_COMPLEXITY_TIMEOUT_MILLIS,
            ) as GameSessionResult.CorrectAdvance
            seenCommands += transition.state.roundData.commandId!!
        }

        assertEquals(8, seenCommands.distinct().size)
    }

    @Test
    fun newlyUnlockedCommand_isForcedNext_whenQueueStrategyStarts() {
        val session = GameSession(
            config = GameConfig(
                unlockFloor = Level.Level2.unlockScore,
                allowsProgression = true,
            ),
            random = Random(0),
        )

        repeat(5) { index ->
            val state = session.snapshot()
            val transition = session.onZoneClick(
                direction = state.roundData.validDirections.first(),
                elapsedMillis = GameRules.LOW_COMPLEXITY_TIMEOUT_MILLIS,
            ) as GameSessionResult.CorrectAdvance

            if (index == 4) {
                assertTrue(
                    transition.state.roundData.commandId in setOf(
                        GameCommand.NOT_LEFT,
                        GameCommand.NOT_RIGHT,
                        GameCommand.NOT_UP,
                        GameCommand.NOT_DOWN,
                    ),
                )
            }
        }
    }

    @Test
    fun newlyUnlockedCommand_isForcedNext_evenWhileStillInRandomSelectionPhase() {
        val session = GameSession(config = GameConfig(), random = Random(0))

        repeat(5) { index ->
            val state = session.snapshot()
            val transition = session.onZoneClick(
                direction = state.roundData.validDirections.first(),
                elapsedMillis = GameRules.LOW_COMPLEXITY_TIMEOUT_MILLIS,
            ) as GameSessionResult.CorrectAdvance

            if (index == 4) {
                assertTrue(
                    transition.state.roundData.commandId in setOf(
                        GameCommand.UP,
                        GameCommand.DOWN,
                    ),
                )
            }
        }
    }

    @Test
    fun queuePreservesRemainingCommands_whenNewCommandsUnlock() {
        val session = GameSession(
            config = GameConfig(
                unlockFloor = Level.Level3.unlockScore,
                allowsProgression = true,
            ),
            random = Random(0),
        )

        val seenBeforeUnlock = mutableSetOf<GameCommand>()
        repeat(4) {
            val state = session.snapshot()
            seenBeforeUnlock += state.roundData.commandId!!
            session.onZoneClick(
                direction = state.roundData.validDirections.first(),
                elapsedMillis = GameRules.LOW_COMPLEXITY_TIMEOUT_MILLIS,
            )
        }

        assertEquals(4, seenBeforeUnlock.size)

        val unlockingState = session.snapshot()
        val unlockTransition = session.onZoneClick(
            direction = unlockingState.roundData.validDirections.first(),
            elapsedMillis = GameRules.LOW_COMPLEXITY_TIMEOUT_MILLIS,
        ) as GameSessionResult.CorrectAdvance

        assertEquals(GameCommand.NOTHING, unlockTransition.state.roundData.commandId)

        val afterUnlock = session.onTimeout(
            elapsedMillis = GameRules.LOW_COMPLEXITY_TIMEOUT_MILLIS,
        ) as GameSessionResult.TimeoutAdvance

        assertTrue(afterUnlock.state.roundData.commandId !in seenBeforeUnlock)
        assertNotEquals(GameCommand.NOTHING, afterUnlock.state.roundData.commandId)
    }
}
