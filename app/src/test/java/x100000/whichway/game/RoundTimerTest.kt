package x100000.whichway.game

import org.junit.Assert.assertEquals
import org.junit.Test

class RoundTimerTest {
    @Test
    fun deadlineUsesElapsedTimeEvenWithoutRenderingUpdates() {
        var now = 10_000L
        val timer = RoundTimer(2_000, 200) { now }
        timer.resume()
        now += 1_000
        assertEquals(1_000, timer.responseMillis)
        assertEquals(0.5f, timer.progress, 0.0001f)
        assertEquals(1_200L, timer.remainingMillis)
        now += 1_000
        assertEquals(0f, timer.progress, 0.0001f)
        assertEquals(200L, timer.remainingMillis)
        now += 200
        assertEquals(0L, timer.remainingMillis)
        assertEquals(2_000, timer.responseMillis)
    }

    @Test
    fun pauseExcludesTimeAndResumeDoesNotRestartRound() {
        var now = 0L
        val timer = RoundTimer(2_000, 200) { now }
        timer.resume()
        now = 750
        timer.pause()
        now += 60_000
        timer.pause()
        assertEquals(750, timer.responseMillis)
        timer.resume()
        timer.resume()
        now += 1_250
        assertEquals(200L, timer.remainingMillis)
        timer.pause()
        now += 60_000
        timer.resume()
        now += 199
        assertEquals(1L, timer.remainingMillis)
        now += 1
        assertEquals(0L, timer.remainingMillis)
    }

    @Test
    fun delayedUpdatesClampProgressAndNewRoundStartsFresh() {
        var now = 0L
        val timer = RoundTimer(2_000, 200) { now }
        timer.resume()
        now = 30_000
        assertEquals(0f, timer.progress, 0.0001f)
        assertEquals(0L, timer.remainingMillis)
        val next = RoundTimer(2_000, 200) { now }
        next.resume()
        assertEquals(1f, next.progress, 0.0001f)
        assertEquals(2_200L, next.remainingMillis)
    }
}
