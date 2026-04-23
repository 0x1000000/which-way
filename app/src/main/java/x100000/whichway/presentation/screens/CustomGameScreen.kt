package x100000.whichway.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import x100000.whichway.R

@Composable
internal fun CustomGameScreen(
    discoveredLevel: Int,
    halfDiscoveredLevel: Int,
    onStartWithLastLevels: () -> Unit,
    onStartWithHalfLevels: () -> Unit,
    onDirectionsOnly: () -> Unit,
    onNumbersOnly: () -> Unit,
    onMathOnly: () -> Unit,
    onSuitsOnly: () -> Unit,
    onTargetsOnly: () -> Unit,
    onBack: () -> Unit,
) {
    val items = buildList<MenuItem> {
        add(
            MenuItem.Action(
                text = stringResource(R.string.start_with_levels, discoveredLevel),
                onClick = onStartWithLastLevels,
            ),
        )
        if (halfDiscoveredLevel > 1 && halfDiscoveredLevel != discoveredLevel) {
            add(
                MenuItem.Action(
                    text = stringResource(R.string.start_with_levels, halfDiscoveredLevel),
                    onClick = onStartWithHalfLevels,
                ),
            )
        }
        add(
            MenuItem.Action(
                text = stringResource(R.string.targets_only),
                onClick = onTargetsOnly,
            ),
        )
        add(
            MenuItem.Action(
                text = stringResource(R.string.math_only),
                onClick = onMathOnly,
            ),
        )
        add(
            MenuItem.Action(
                text = stringResource(R.string.numbers_only),
                onClick = onNumbersOnly,
            ),
        )
        add(
            MenuItem.Action(
                text = stringResource(R.string.directions_only),
                onClick = onDirectionsOnly,
            ),
        )
        add(
            MenuItem.Action(
                text = stringResource(R.string.suits_only),
                onClick = onSuitsOnly,
            ),
        )
        add(
            MenuItem.Action(
                text = stringResource(R.string.home),
                onClick = onBack,
            ),
        )
    }

    MenuScreen(
        title = stringResource(R.string.custom_game),
        items = items,
    )
}
