package x100000.whichway.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import x100000.whichway.R
import x100000.whichway.presentation.theme.WhichWayTheme

@Composable
internal fun HomeScreen(
    onStart: () -> Unit,
    onCustomGame: () -> Unit,
    onSettings: () -> Unit,
    onStatistics: () -> Unit,
) {
    MenuScreen(
        title = stringResource(R.string.app_name),
        items = listOf(
            MenuItem.Action(
                text = stringResource(R.string.start),
                onClick = onStart,
            ),
            MenuItem.Action(
                text = stringResource(R.string.custom_game),
                onClick = onCustomGame,
            ),
            MenuItem.Action(
                text = stringResource(R.string.settings),
                onClick = onSettings,
            ),
            MenuItem.Action(
                text = stringResource(R.string.statistics),
                onClick = onStatistics,
            ),
        ),
    )
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
private fun HomePreview() {
    WhichWayTheme {
        HomeScreen(
            onStart = {},
            onCustomGame = {},
            onSettings = {},
            onStatistics = {},
        )
    }
}
