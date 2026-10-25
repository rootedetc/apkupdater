package com.apkupdater.ui.tv

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apkupdater.R
import com.apkupdater.data.ui.SearchUiState
import com.apkupdater.viewmodel.SearchViewModel
import kotlinx.coroutines.delay


@Composable
fun TvSearchScreen(viewModel: SearchViewModel) {
	var value by remember { mutableStateOf("") }
	val state by viewModel.state().collectAsStateWithLifecycle()
	val uriHandler = LocalUriHandler.current
	val firstResult = remember { FocusRequester() }

	// Results stay on screen when the query shrinks below the search threshold.
	LaunchedEffect(value) {
		if (value.length >= 3) {
			delay(600)
			viewModel.search(value)
		}
	}

	val results = (state as? SearchUiState.Success)?.updates

	Column(Modifier.fillMaxSize()) {
		TvScreenHeader(
			title = stringResource(R.string.tab_search),
			subtitle = when {
				// Guidance stays in the header, where the keyboard cannot cover it.
				value.length < 3 -> stringResource(R.string.tv_search_body)
				results == null -> ""
				results.size == 1 -> stringResource(R.string.tv_search_one_result)
				results.isNotEmpty() -> stringResource(R.string.tv_search_results, results.size)
				else -> ""
			}
		)
		TvSearchField(
			value = value,
			onValueChange = { value = it },
			// A text field swallows the D-pad, so down has to be handed to the
			// list by hand or the results are unreachable.
			onMoveDown = { runCatching { firstResult.requestFocus() } }
		)
		Spacer(Modifier.height(20.dp))
		when (val current = state) {
			is SearchUiState.Loading -> TvSkeletonList(4)
			is SearchUiState.Error -> TvErrorState(
				title = stringResource(R.string.tv_error_title),
				body = stringResource(R.string.tv_error_body),
				retryLabel = stringResource(R.string.tv_try_again),
				onRetry = { if (value.length >= 3) viewModel.search(value) }
			)
			is SearchUiState.Success -> {
				if (current.updates.isEmpty()) {
					// Below three characters the field is the whole screen: the
					// keyboard is up and a centred empty state would sit under it.
					if (value.length >= 3) {
						TvEmptyState(
							title = stringResource(R.string.tv_search_no_matches),
							body = stringResource(R.string.tv_search_no_matches_body, value)
						)
					}
				} else {
					LazyColumn(
						modifier = Modifier.fillMaxSize(),
						verticalArrangement = Arrangement.spacedBy(TvDims.RowGap)
					) {
						itemsIndexed(current.updates, key = { _, app -> app.id }) { index, app ->
							TvActionRow {
								if (app.iconUri != Uri.EMPTY) {
									TvImage(app.iconUri)
								} else {
									TvAppIcon(app.packageName)
								}
								Spacer(Modifier.width(18.dp))
								Column(Modifier.weight(1f)) {
									Text(
										app.displayName(),
										style = MaterialTheme.typography.titleLarge,
										maxLines = 1
									)
									Spacer(Modifier.height(3.dp))
									Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
										Text(
											app.version,
											style = MaterialTheme.typography.bodyMedium,
											color = MaterialTheme.colorScheme.onSurfaceVariant,
											maxLines = 1
										)
										TvSourceTag(app.source)
									}
									val whatsNew = app.whatsNewLine()
									if (whatsNew.isNotBlank()) {
										Spacer(Modifier.height(3.dp))
										Text(
											whatsNew,
											style = MaterialTheme.typography.bodySmall,
											color = MaterialTheme.colorScheme.onSurfaceVariant,
											maxLines = 1
										)
									}
								}
								Spacer(Modifier.width(24.dp))
								if (app.isInstalling) {
									TvUpdating()
								} else {
									TvPillButton(
										text = stringResource(R.string.tv_install),
										onClick = { viewModel.install(app, uriHandler) },
										modifier = if (index == 0) {
											Modifier.focusRequester(firstResult)
										} else {
											Modifier
										},
										primary = true
									)
								}
							}
						}
					}
				}
			}
		}
	}
}

/**
 * The single text input of the TV interface. The field itself takes D-pad focus,
 * so the focus ring has to be loud enough to read from the couch.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TvSearchField(
	value: String,
	onValueChange: (String) -> Unit,
	onMoveDown: () -> Unit
) {
	var focused by remember { mutableStateOf(false) }
	val keyboard = LocalSoftwareKeyboardController.current
	val focusManager = LocalFocusManager.current
	val colors = MaterialTheme.colorScheme

	Surface(
		modifier = Modifier.fillMaxWidth().height(56.dp),
		shape = RoundedCornerShape(10.dp),
		color = colors.surface,
		border = BorderStroke(
			if (focused) 2.dp else 1.dp,
			if (focused) colors.primary else colors.outlineVariant
		)
	) {
		Row(
			modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				Icons.Filled.Search,
				contentDescription = null,
				modifier = Modifier.size(20.dp),
				tint = colors.onSurfaceVariant
			)
			Spacer(Modifier.width(12.dp))
			Box(Modifier.weight(1f)) {
				BasicTextField(
					value = value,
					onValueChange = onValueChange,
					singleLine = true,
					textStyle = MaterialTheme.typography.titleMedium.copy(color = colors.onSurface),
					cursorBrush = SolidColor(colors.primary),
					modifier = Modifier
						.fillMaxWidth()
						.onPreviewKeyEvent { event ->
							// The text field would eat these keys, which strands a
							// remote user inside the field. Down goes to the results,
							// left goes back to the rail.
							if (event.type != KeyEventType.KeyDown) {
								false
							} else when (event.key) {
								Key.DirectionDown -> {
									keyboard?.hide()
									onMoveDown()
									true
								}
								Key.DirectionLeft -> {
									keyboard?.hide()
									focusManager.moveFocus(FocusDirection.Left)
									true
								}
								else -> false
							}
						}
						.onFocusChanged {
						focused = it.isFocused
						// Focus the field and the IME appears; move down to the
						// results and it gets out of the way.
						if (it.isFocused) keyboard?.show() else keyboard?.hide()
					}
				)
				if (value.isEmpty()) {
					Text(
						text = stringResource(R.string.tv_search_hint),
						style = MaterialTheme.typography.titleMedium,
						color = colors.onSurfaceVariant
					)
				}
			}
		}
	}
}
