package x100000.whichway.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.requestFocusOnHierarchyActive
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CheckboxButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.RadioButton
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TitleCard
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight

internal sealed interface MenuItem {
    data class Action(
        val text: String,
        val onClick: () -> Unit,
        val selected: Boolean = false,
    ) : MenuItem

    data class Toggle(
        val text: String,
        val checked: Boolean,
        val onToggle: () -> Unit,
    ) : MenuItem

    data class Radio(
        val text: String,
        val selected: Boolean,
        val onSelect: () -> Unit,
    ) : MenuItem

    data class Info(
        val label: String,
        val value: String,
    ) : MenuItem

    data class TextLine(
        val text: String,
    ) : MenuItem

    data class SummaryCard(
        val title: String,
        val value: String,
        val supportingLines: List<String> = emptyList(),
    ) : MenuItem

    data class Badge(
        val text: String,
        val backgroundColor: Color,
        val contentColor: Color,
    ) : MenuItem
}

@Composable
internal fun MenuScreen(
    title: String,
    items: List<MenuItem>,
    initialAnchorItemIndex: Int = if (items.isNotEmpty()) 1 else 0,
) {
    val listState = rememberTransformingLazyColumnState(
        initialAnchorItemIndex = initialAnchorItemIndex,
    )
    val transformationSpec = rememberTransformationSpec()

    ScreenScaffold(
        scrollState = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor),
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
        ) {
            TransformingLazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .requestFocusOnHierarchyActive(),
                contentPadding = PaddingValues(
                    horizontal = 0.dp,
                    vertical = 0.dp,
                ),
            ) {
                item(key = "header") {
                    val transformation = SurfaceTransformation(transformationSpec)
                    ListHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .transformedHeight(this, transformationSpec),
                        transformation = transformation,
                    ) {
                        Text(
                            text = title,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                itemsIndexed(
                    items = items,
                    key = { _, item -> item.stableKey() },
                    contentType = { _, item -> item::class.simpleName ?: "menu_item" },
                ) { index, item ->
                    val transformation = SurfaceTransformation(transformationSpec)
                    when (item) {
                        is MenuItem.Action -> ActionMenuRow(
                            item = item,
                            transformation = transformation,
                            modifier = Modifier
                                .transformedHeight(this, transformationSpec)
                                .minimumVerticalContentPadding(ButtonDefaults.minimumVerticalListContentPadding)
                                .padding(bottom = if (index < items.lastIndex) 8.dp else 0.dp),
                        )

                        is MenuItem.Toggle -> ToggleMenuRow(
                            item = item,
                            transformation = transformation,
                            modifier = Modifier
                                .transformedHeight(this, transformationSpec)
                                .minimumVerticalContentPadding(ButtonDefaults.minimumVerticalListContentPadding)
                                .padding(bottom = if (index < items.lastIndex) 8.dp else 0.dp),
                        )

                        is MenuItem.Radio -> RadioMenuRow(
                            item = item,
                            transformation = transformation,
                            modifier = Modifier
                                .transformedHeight(this, transformationSpec)
                                .minimumVerticalContentPadding(ButtonDefaults.minimumVerticalListContentPadding)
                                .padding(bottom = if (index < items.lastIndex) 8.dp else 0.dp),
                        )

                        is MenuItem.Info -> InfoMenuRow(
                            item = item,
                            modifier = Modifier
                                .transformedHeight(this, transformationSpec)
                                .padding(bottom = if (index < items.lastIndex) 8.dp else 0.dp),
                        )

                        is MenuItem.TextLine -> TextLineMenuRow(
                            item = item,
                            modifier = Modifier
                                .transformedHeight(this, transformationSpec)
                                .padding(bottom = if (index < items.lastIndex) 8.dp else 0.dp),
                        )

                        is MenuItem.SummaryCard -> SummaryCardMenuRow(
                            item = item,
                            modifier = Modifier
                                .transformedHeight(this, transformationSpec)
                                .padding(bottom = if (index < items.lastIndex) 8.dp else 0.dp),
                        )

                        is MenuItem.Badge -> BadgeMenuRow(
                            item = item,
                            modifier = Modifier
                                .transformedHeight(this, transformationSpec)
                                .padding(bottom = if (index < items.lastIndex) 8.dp else 0.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionMenuRow(
    item: MenuItem.Action,
    transformation: SurfaceTransformation,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = item.onClick,
        modifier = modifier.fillMaxWidth(),
        transformation = transformation,
    ) {
        Text(
            text = item.text,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2,
            overflow = TextOverflow.Clip,
        )
    }
}

@Composable
private fun ToggleMenuRow(
    item: MenuItem.Toggle,
    transformation: SurfaceTransformation,
    modifier: Modifier = Modifier,
) {
    CheckboxButton(
        checked = item.checked,
        onCheckedChange = { item.onToggle() },
        modifier = modifier.fillMaxWidth(),
        transformation = transformation,
        label = {
            Text(
                text = item.text,
                maxLines = 2,
                overflow = TextOverflow.Clip,
            )
        },
    )
}

@Composable
private fun RadioMenuRow(
    item: MenuItem.Radio,
    transformation: SurfaceTransformation,
    modifier: Modifier = Modifier,
) {
    RadioButton(
        selected = item.selected,
        onSelect = item.onSelect,
        modifier = modifier.fillMaxWidth(),
        transformation = transformation,
        label = {
            Text(
                text = item.text,
                maxLines = 2,
                overflow = TextOverflow.Clip,
            )
        },
    )
}

@Composable
private fun InfoMenuRow(
    item: MenuItem.Info,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp,
                vertical = 4.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Clip,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.value,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Clip,
            )
        }
    }
}

@Composable
private fun BadgeMenuRow(
    item: MenuItem.Badge,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = item.backgroundColor,
                    shape = RoundedCornerShape(50),
                )
                .padding(
                    horizontal = 14.dp,
                    vertical = 8.dp,
                ),
        ) {
            Text(
                text = item.text,
                color = item.contentColor,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Clip,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun TextLineMenuRow(
    item: MenuItem.TextLine,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp,
                vertical = 6.dp,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = item.text,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Clip,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SummaryCardMenuRow(
    item: MenuItem.SummaryCard,
    modifier: Modifier = Modifier,
) {
    TitleCard(
        onClick = {},
        enabled = false,
        modifier = modifier.fillMaxWidth(),
        title = {
            Text(
                text = item.title,
                maxLines = 1,
                overflow = TextOverflow.Clip,
            )
        },
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
        ) {
            if (item.value.isNotEmpty()) {
                Text(
                    text = item.value,
                    style = MaterialTheme.typography.displaySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Clip,
                )
            }
            item.supportingLines.forEachIndexed { index, line ->
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Clip,
                )
            }
        }
    }
}

private fun MenuItem.stableKey(): String =
    when (this) {
        is MenuItem.Action -> "action:$text"
        is MenuItem.Toggle -> "toggle:$text"
        is MenuItem.Radio -> "radio:$text"
        is MenuItem.Info -> "info:$label"
        is MenuItem.TextLine -> "text_line:$text"
        is MenuItem.SummaryCard -> "summary_card:$title:$value:${supportingLines.joinToString("|")}"
        is MenuItem.Badge -> "badge:$text"
    }
