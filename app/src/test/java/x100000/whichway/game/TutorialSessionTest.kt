package x100000.whichway.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class TutorialSessionTest {

    @Test
    fun tutorialCommands_includeCuratedCoreMechanics() {
        assertEquals(
            listOf(
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
            ),
            TutorialCommands.all,
        )
    }

    @Test
    fun correctTapAdvancesToNextChallenge() {
        val session = TutorialSession(random = Random(0))
        val initial = session.snapshot()

        val result = session.onZoneClick(initial.roundData.validDirections.first())

        assertTrue(result is TutorialResult.CorrectAdvance)
        val next = (result as TutorialResult.CorrectAdvance).state
        assertEquals(initial.challengeIndex + 1, next.challengeIndex)
        assertEquals(TutorialCommands.all.size, next.totalChallenges)
    }

    @Test
    fun wrongTapDoesNotAdvanceOrComplete() {
        val session = TutorialSession(random = Random(0))
        val initial = session.snapshot()
        val wrongDirection = Direction.entries.first { it !in initial.roundData.validDirections }

        val result = session.onZoneClick(wrongDirection)

        assertTrue(result is TutorialResult.WrongTap)
        val next = (result as TutorialResult.WrongTap).state
        assertEquals(initial.challengeIndex, next.challengeIndex)
        assertFalse(next.requiresManualAdvance)
    }

    @Test
    fun nothingChallengeRequiresManualAdvance() {
        val nothingIndex = TutorialCommands.all.indexOf(GameCommand.NOTHING)
        val session = TutorialSession(startChallengeIndex = nothingIndex, random = Random(0))
        val state = session.snapshot()

        assertTrue(state.requiresManualAdvance)
        assertTrue(session.onManualAdvance() is TutorialResult.CorrectAdvance)
    }

    @Test
    fun finalChallengeCompletesTutorial() {
        val session = TutorialSession(
            startChallengeIndex = TutorialCommands.all.lastIndex,
            random = Random(0),
        )
        val state = session.snapshot()
        val result = session.onZoneClick(state.roundData.validDirections.first())

        assertTrue(result is TutorialResult.Complete)
    }
}
