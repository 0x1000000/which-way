package x100000.whichway.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Text
import x100000.whichway.R

@Composable
internal fun ExitPrompt(
    onContinue: () -> Unit,
    onExit: () -> Unit,
    uiMetrics: WatchUiMetrics,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xAA000000)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.76f)
                .aspectRatio(1f)
                .background(
                    color = Color(0xF0101317),
                    shape = CircleShape,
                )
                .padding(
                    horizontal = uiMetrics.exitCardPaddingHorizontal,
                    vertical = uiMetrics.exitCardPaddingVertical,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = onExit,
                    modifier = Modifier.fillMaxWidth(STANDARD_BUTTON_WIDTH_FRACTION),
                ) {
                    Text(
                        text = stringResource(R.string.stop),
                        fontSize = uiMetrics.secondaryButtonFontSize,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(modifier = Modifier.height(uiMetrics.mediumGap))
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth(STANDARD_BUTTON_WIDTH_FRACTION),
                ) {
                    Text(
                        text = stringResource(R.string.continue_game),
                        fontSize = uiMetrics.secondaryButtonFontSize,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
