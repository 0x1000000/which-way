package x100000.whichway.presentation

import androidx.compose.ui.graphics.Color
import x100000.whichway.game.ColorTarget
import x100000.whichway.game.Direction
import x100000.whichway.game.SuitTarget

internal val BackgroundColor = Color(0xFF050608)
internal val ZoneBaseColors = mapOf(
    Direction.Up to Color(0xFF112533),
    Direction.Right to Color(0xFF173326),
    Direction.Down to Color(0xFF322313),
    Direction.Left to Color(0xFF2A1530),
)
internal val ZonePressedColor = Color(0xFFCAD5E2)
internal val LifeOnColor = Color(0xFFFF6B6B)
internal val LifeOffColor = Color(0xFF6A2B2B)
internal val IndicatorTrackColor = Color(0x223E566F)
internal val IndicatorColor = Color(0xB3D7E2EE)
internal val WrongTapFlashColor = Color(0xAAFF2020)
internal val TimeoutFlashColor = Color(0x88FF9D2D)
internal val ReplayFlashColor = Color(0x99FFD54A)
internal val SuccessFlashColor = Color(0x1FFFFFFF)
internal val FlashAccentTextColor = Color(0xFFFFB347)
internal val HudTextColor = Color(0xFFD2D8E0)
internal val HudTextBackdropColor = Color(0xCC000000)
internal val SelectedMenuButtonColor = Color(0xFF284053)
internal val MenuInfoRowColor = Color(0xFF10161C)
internal val ChargeOnColor = Color(0xFFFFD54A)
internal val ChargeOffColor = Color(0xFF8E97A4)
internal const val STANDARD_BUTTON_WIDTH_FRACTION = 0.82f
internal const val WIDE_BUTTON_WIDTH_FRACTION = 0.9f

internal fun ColorTarget.toUiColor(): Color =
    when (this) {
        ColorTarget.Green -> Color(0xFF37D67A)
        ColorTarget.Blue -> Color(0xFF4D9CFF)
        ColorTarget.White -> Color(0xFFF2F3F5)
        ColorTarget.Yellow -> Color(0xFFFFD54A)
    }

internal fun SuitTarget.toUiColor(): Color =
    when (this) {
        SuitTarget.Diamonds, SuitTarget.Hearts -> Color(0xFFFF6B6B)
        SuitTarget.Clubs, SuitTarget.Spades -> Color(0xFFF2F3F5)
    }
