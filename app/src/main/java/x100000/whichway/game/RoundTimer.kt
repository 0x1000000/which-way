package x100000.whichway.game

/** Gameplay time is independent of animation scale and render frame rate. */
internal class RoundTimer(
    private val durationMillis: Int,
    private val graceMillis: Int,
    private val nowMillis: () -> Long,
) {
    private var accumulatedMillis = 0L
    private var startedAt: Long? = null

    fun resume() {
        if (startedAt == null) startedAt = nowMillis()
    }

    fun pause() {
        accumulatedMillis = elapsed()
        startedAt = null
    }

    private fun elapsed(): Long = accumulatedMillis +
        (startedAt?.let { (nowMillis() - it).coerceAtLeast(0L) } ?: 0L)

    val responseMillis: Int
        get() = elapsed().coerceIn(0L, durationMillis.toLong()).toInt()

    val progress: Float
        get() = 1f - responseMillis.toFloat() / durationMillis

    val remainingMillis: Long
        get() = (durationMillis.toLong() + graceMillis - elapsed()).coerceAtLeast(0L)
}
