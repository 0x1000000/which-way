package x100000.whichway.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt

data class SavedGameData(
    val lastScore: Int = 0,
    val lastScoreSpeedPercent: Int = DEFAULT_SPEED_PERCENT,
    val bestScore: Int = 0,
    val bestScoreSpeedPercent: Int = DEFAULT_SPEED_PERCENT,
    val totalNormalRuns: Int = 0,
    val totalSpentTimeMs: Long = 0L,
    val totalNormalResponseTimeMs: Long = 0L,
    val totalNormalResponseCount: Int = 0,
    val speedPercent: Int = DEFAULT_SPEED_PERCENT,
    val skipColors: Boolean = false,
    val skipSuits: Boolean = false,
    val skipNot: Boolean = false,
) {
    val averageResponseTimeMs: Int
        get() = if (totalNormalResponseCount == 0) {
            0
        } else {
            (totalNormalResponseTimeMs.toDouble() / totalNormalResponseCount).roundToInt()
        }

    val gamesPlayed: Int
        get() = totalNormalRuns
}

interface GameDataRepository {
    val gameDataFlow: Flow<SavedGameData>

    suspend fun saveNormalRun(
        score: Int,
        speedPercent: Int,
    )

    suspend fun saveParticipation(
        responseTimeTotalMs: Long,
        responseCount: Int,
    )

    suspend fun saveSpentTime(spentTimeTotalMs: Long)

    suspend fun updateSpeedPercent(speedPercent: Int)

    suspend fun updateSkipColors(skipColors: Boolean)

    suspend fun updateSkipSuits(skipSuits: Boolean)

    suspend fun updateSkipNot(skipNot: Boolean)
}

private val Context.scoreDataStore: DataStore<Preferences> by preferencesDataStore(name = "scores")

class PreferencesGameDataRepository(
    private val dataStore: DataStore<Preferences>,
) : GameDataRepository {

    override val gameDataFlow: Flow<SavedGameData> =
        dataStore.data.map { preferences ->
            SavedGameData(
                lastScore = preferences[LAST_SCORE_KEY] ?: 0,
                lastScoreSpeedPercent = preferences[LAST_SCORE_SPEED_PERCENT_KEY] ?: DEFAULT_SPEED_PERCENT,
                bestScore = preferences[BEST_SCORE_KEY] ?: 0,
                bestScoreSpeedPercent = preferences[BEST_SCORE_SPEED_PERCENT_KEY] ?: DEFAULT_SPEED_PERCENT,
                totalNormalRuns = preferences[TOTAL_NORMAL_RUNS_KEY] ?: 0,
                totalSpentTimeMs = preferences[TOTAL_SPENT_TIME_MS_KEY] ?: 0L,
                totalNormalResponseTimeMs = preferences[TOTAL_NORMAL_RESPONSE_TIME_MS_KEY] ?: 0L,
                totalNormalResponseCount = preferences[TOTAL_NORMAL_RESPONSE_COUNT_KEY] ?: 0,
                speedPercent = preferences[SPEED_PERCENT_KEY] ?: DEFAULT_SPEED_PERCENT,
                skipColors = preferences[SKIP_COLORS_KEY] ?: false,
                skipSuits = preferences[SKIP_SUITS_KEY] ?: false,
                skipNot = preferences[SKIP_NOT_KEY] ?: false,
            )
        }

    override suspend fun saveNormalRun(
        score: Int,
        speedPercent: Int,
    ) {
        dataStore.edit { preferences ->
            val bestScore = preferences[BEST_SCORE_KEY] ?: 0
            preferences[LAST_SCORE_KEY] = score
            preferences[LAST_SCORE_SPEED_PERCENT_KEY] = speedPercent
            if (score > bestScore) {
                preferences[BEST_SCORE_KEY] = score
                preferences[BEST_SCORE_SPEED_PERCENT_KEY] = speedPercent
            }
        }
    }

    override suspend fun saveParticipation(
        responseTimeTotalMs: Long,
        responseCount: Int,
    ) {
        dataStore.edit { preferences ->
            preferences[TOTAL_NORMAL_RUNS_KEY] = (preferences[TOTAL_NORMAL_RUNS_KEY] ?: 0) + 1
            preferences[TOTAL_NORMAL_RESPONSE_TIME_MS_KEY] =
                (preferences[TOTAL_NORMAL_RESPONSE_TIME_MS_KEY] ?: 0L) + responseTimeTotalMs
            preferences[TOTAL_NORMAL_RESPONSE_COUNT_KEY] =
                (preferences[TOTAL_NORMAL_RESPONSE_COUNT_KEY] ?: 0) + responseCount
        }
    }

    override suspend fun saveSpentTime(spentTimeTotalMs: Long) {
        dataStore.edit { preferences ->
            preferences[TOTAL_SPENT_TIME_MS_KEY] = (preferences[TOTAL_SPENT_TIME_MS_KEY] ?: 0L) + spentTimeTotalMs
        }
    }

    override suspend fun updateSpeedPercent(speedPercent: Int) {
        dataStore.edit { preferences ->
            preferences[SPEED_PERCENT_KEY] = speedPercent
        }
    }

    override suspend fun updateSkipColors(skipColors: Boolean) {
        dataStore.edit { preferences ->
            preferences[SKIP_COLORS_KEY] = skipColors
        }
    }

    override suspend fun updateSkipSuits(skipSuits: Boolean) {
        dataStore.edit { preferences ->
            preferences[SKIP_SUITS_KEY] = skipSuits
        }
    }

    override suspend fun updateSkipNot(skipNot: Boolean) {
        dataStore.edit { preferences ->
            preferences[SKIP_NOT_KEY] = skipNot
        }
    }

    private companion object {
        val LAST_SCORE_KEY = intPreferencesKey("last_score")
        val LAST_SCORE_SPEED_PERCENT_KEY = intPreferencesKey("last_score_speed_percent")
        val BEST_SCORE_KEY = intPreferencesKey("best_score")
        val BEST_SCORE_SPEED_PERCENT_KEY = intPreferencesKey("best_score_speed_percent")
        val TOTAL_NORMAL_RUNS_KEY = intPreferencesKey("total_normal_runs")
        val TOTAL_SPENT_TIME_MS_KEY = longPreferencesKey("total_normal_spent_time_ms")
        val TOTAL_NORMAL_RESPONSE_TIME_MS_KEY = longPreferencesKey("total_normal_response_time_ms")
        val TOTAL_NORMAL_RESPONSE_COUNT_KEY = intPreferencesKey("total_normal_response_count")
        val SPEED_PERCENT_KEY = intPreferencesKey("speed_percent")
        val SKIP_COLORS_KEY = booleanPreferencesKey("skip_colors")
        val SKIP_SUITS_KEY = booleanPreferencesKey("skip_suits")
        val SKIP_NOT_KEY = booleanPreferencesKey("skip_not")
    }
}

fun createGameDataRepository(context: Context): GameDataRepository =
    PreferencesGameDataRepository(context.scoreDataStore)

const val SLOW_SPEED_PERCENT = 75
const val DEFAULT_SPEED_PERCENT = 100
const val FAST_SPEED_PERCENT = 125
