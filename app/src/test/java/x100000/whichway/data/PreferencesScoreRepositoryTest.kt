package x100000.whichway.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PreferencesScoreRepositoryTest {

    @Test
    fun gameDataFlow_defaultsToExpectedValues() = runTest {
        val repository = PreferencesGameDataRepository(createDataStore())

        assertEquals(SavedGameData(), repository.gameDataFlow.first())
    }

    @Test
    fun saveNormalRun_updatesLastAndBestScoreAndSpeed() = runTest {
        val repository = PreferencesGameDataRepository(createDataStore())

        repository.saveSpentTime(1_500L)
        repository.saveParticipation(responseTimeTotalMs = 900L, responseCount = 2)
        repository.saveNormalRun(score = 7, speedPercent = FAST_SPEED_PERCENT)

        assertEquals(
            SavedGameData(
                lastScore = 7,
                lastScoreSpeedPercent = FAST_SPEED_PERCENT,
                bestScore = 7,
                bestScoreSpeedPercent = FAST_SPEED_PERCENT,
                totalNormalRuns = 1,
                totalSpentTimeMs = 1_500L,
                totalNormalResponseTimeMs = 900L,
                totalNormalResponseCount = 2,
            ),
            repository.gameDataFlow.first(),
        )
    }

    @Test
    fun saveNormalRun_keepsExistingBestWhenLowerScoreArrivesLater() = runTest {
        val repository = PreferencesGameDataRepository(createDataStore())

        repository.saveSpentTime(700L)
        repository.saveParticipation(responseTimeTotalMs = 500L, responseCount = 1)
        repository.saveNormalRun(score = 12, speedPercent = DEFAULT_SPEED_PERCENT)
        repository.saveSpentTime(900L)
        repository.saveParticipation(responseTimeTotalMs = 800L, responseCount = 2)
        repository.saveNormalRun(score = 5, speedPercent = SLOW_SPEED_PERCENT)

        val saved = repository.gameDataFlow.first()
        assertEquals(5, saved.lastScore)
        assertEquals(SLOW_SPEED_PERCENT, saved.lastScoreSpeedPercent)
        assertEquals(12, saved.bestScore)
        assertEquals(DEFAULT_SPEED_PERCENT, saved.bestScoreSpeedPercent)
        assertEquals(2, saved.totalNormalRuns)
        assertEquals(1_600L, saved.totalSpentTimeMs)
        assertEquals(1_300L, saved.totalNormalResponseTimeMs)
        assertEquals(3, saved.totalNormalResponseCount)
    }

    @Test
    fun saveSpentTime_accumulatesForPracticeAndNormalRuns() = runTest {
        val repository = PreferencesGameDataRepository(createDataStore())

        repository.saveSpentTime(400L)
        repository.saveSpentTime(600L)

        assertEquals(1_000L, repository.gameDataFlow.first().totalSpentTimeMs)
        assertEquals(0, repository.gameDataFlow.first().totalNormalRuns)
    }

    @Test
    fun saveParticipation_accumulatesForPracticeAndNormalRuns() = runTest {
        val repository = PreferencesGameDataRepository(createDataStore())

        repository.saveParticipation(responseTimeTotalMs = 300L, responseCount = 1)
        repository.saveParticipation(responseTimeTotalMs = 700L, responseCount = 2)

        val saved = repository.gameDataFlow.first()
        assertEquals(2, saved.totalNormalRuns)
        assertEquals(1_000L, saved.totalNormalResponseTimeMs)
        assertEquals(3, saved.totalNormalResponseCount)
    }

    @Test
    fun updateSpeedPercent_persistsSpeedSetting() = runTest {
        val repository = PreferencesGameDataRepository(createDataStore())

        repository.updateSpeedPercent(SLOW_SPEED_PERCENT)

        assertEquals(SLOW_SPEED_PERCENT, repository.gameDataFlow.first().speedPercent)
    }

    @Test
    fun updateSkipColors_persistsSetting() = runTest {
        val repository = PreferencesGameDataRepository(createDataStore())

        repository.updateSkipColors(true)

        assertTrue(repository.gameDataFlow.first().skipColors)
    }

    @Test
    fun updateSkipSuits_persistsSetting() = runTest {
        val repository = PreferencesGameDataRepository(createDataStore())

        repository.updateSkipSuits(true)

        assertTrue(repository.gameDataFlow.first().skipSuits)
    }

    @Test
    fun updateSkipNot_persistsSetting() = runTest {
        val repository = PreferencesGameDataRepository(createDataStore())

        repository.updateSkipNot(true)

        assertTrue(repository.gameDataFlow.first().skipNot)
    }

    @Test
    fun averageResponseTimeMs_usesOnlyStoredTotals() {
        val data = SavedGameData(
            totalNormalResponseTimeMs = 999L,
            totalNormalResponseCount = 4,
        )

        assertEquals(250, data.averageResponseTimeMs)
    }

    @Test
    fun averageResponseTimeMs_isZeroWithoutResponses() {
        assertEquals(0, SavedGameData().averageResponseTimeMs)
    }

    private fun createDataStore(): DataStore<Preferences> {
        return FakePreferencesDataStore()
    }

    private class FakePreferencesDataStore(
        initialPreferences: Preferences = emptyPreferences(),
    ) : DataStore<Preferences> {
        private val state = MutableStateFlow(initialPreferences)

        override val data: Flow<Preferences> = state

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            val updated = transform(state.value)
            state.value = updated
            return updated
        }
    }
}
