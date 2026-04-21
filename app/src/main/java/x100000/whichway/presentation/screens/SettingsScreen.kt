package x100000.whichway.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import x100000.whichway.R
import x100000.whichway.data.DEFAULT_SPEED_PERCENT
import x100000.whichway.data.FAST_SPEED_PERCENT
import x100000.whichway.data.SLOW_SPEED_PERCENT
import x100000.whichway.data.SavedGameData

@Composable
internal fun SettingsScreen(
    savedData: SavedGameData,
    onSetSpeedPercent: (Int) -> Unit,
    onToggleSkipColors: () -> Unit,
    onToggleSkipSuits: () -> Unit,
    onToggleSkipNot: () -> Unit,
    onBack: () -> Unit,
) {
    MenuScreen(
        title = stringResource(R.string.settings),
        items = listOf(
            MenuItem.Radio(
                text = stringResource(R.string.speed_slower),
                selected = savedData.speedPercent == SLOW_SPEED_PERCENT,
                onSelect = { onSetSpeedPercent(SLOW_SPEED_PERCENT) },
            ),
            MenuItem.Radio(
                text = stringResource(R.string.speed_normal),
                selected = savedData.speedPercent == DEFAULT_SPEED_PERCENT,
                onSelect = { onSetSpeedPercent(DEFAULT_SPEED_PERCENT) },
            ),
            MenuItem.Radio(
                text = stringResource(R.string.speed_faster),
                selected = savedData.speedPercent == FAST_SPEED_PERCENT,
                onSelect = { onSetSpeedPercent(FAST_SPEED_PERCENT) },
            ),
            MenuItem.Toggle(
                text = stringResource(R.string.skip_colors),
                checked = savedData.skipColors,
                onToggle = onToggleSkipColors,
            ),
            MenuItem.Toggle(
                text = stringResource(R.string.skip_suits),
                checked = savedData.skipSuits,
                onToggle = onToggleSkipSuits,
            ),
            MenuItem.Toggle(
                text = stringResource(R.string.skip_not),
                checked = savedData.skipNot,
                onToggle = onToggleSkipNot,
            ),
            MenuItem.Action(
                text = stringResource(R.string.home),
                onClick = onBack,
            ),
        ),
    )
}
