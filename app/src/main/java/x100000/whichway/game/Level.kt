package x100000.whichway.game

internal enum class Level(
    val unlockScore: Int,
) {
    Level1(0),
    Level2(5),
    Level3(10),
    Level4(15),
    Level5(20),
    Level6(25),
    Level7(30),
    Level8(35),
    Level9(40),
    Level10(45),
    Level11(50),
    Level12(55),
    Level13(60),
    Level14(65),
    Level15(70),
    Level16(75),
    Level17(80),
    Level18(85),
    Level19(90),
    Level20(95),
    Level21(100),
    Level22(105),
    Level23(110),
    Level24(115),
    ;

    companion object {
        fun forScore(score: Int): Level =
            entries.lastOrNull { score >= it.unlockScore } ?: Level1

        fun forNumber(level: Int): Level =
            entries[(level.coerceIn(1, entries.size)) - 1]
    }
}
