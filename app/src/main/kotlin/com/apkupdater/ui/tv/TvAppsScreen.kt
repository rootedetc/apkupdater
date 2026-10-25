package com.apkupdater.ui.tv

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apkupdater.R
import com.apkupdater.data.ui.AppsUiState
import com.apkupdater.viewmodel.AppsViewModel


/*
 * Installed apps at ten feet. Filters live in the header and read as "what is
 * included", not as excluded flags, so a lit chip always means those apps are
 * on screen. Every row offers exactly one action: ignore or include.
 */
@Composable
fun TvAppsScreen(viewModel: AppsViewModel) {
	val state by viewModel.state().collectAsStateWithLifecycle()
	val filters = appsFilters(state)
	val (listState, onRowFocused) = rememberTvRowList()

	Column(Modifier.fillMaxSize()) {
		TvScreenHeader(
			title = stringResource(R.string.tab_apps),
			subtitle = AppsSubtitle(state)
		) {
			Row(
				horizontalArrangement = Arrangement.spacedBy(10.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				TvFilterChip(
					label = stringResource(R.string.tv_filter_system),
					selected = !filters.excludeSystem,
					onClick = { viewModel.onSystemClick() }
				)
				TvFilterChip(
					label = stringResource(R.string.tv_filter_store),
					selected = !filters.excludeAppStore,
					onClick = { viewModel.onAppStoreClick() }
				)
				TvFilterChip(
					label = stringResource(R.string.tv_filter_disabled),
					selected = !filters.excludeDisabled,
					onClick = { viewModel.onDisabledClick() }
				)
			}
		}

		when (val current = state) {
			is AppsUiState.Loading -> TvSkeletonList(5)
			is AppsUiState.Error -> TvErrorState(
				title = stringResource(R.string.tv_error_title),
				body = stringResource(R.string.tv_error_body),
				retryLabel = stringResource(R.string.tv_try_again),
				onRetry = { viewModel.refresh() }
			)
			is AppsUiState.Success -> if (current.apps.isEmpty()) {
				TvEmptyState(
					title = stringResource(R.string.tv_apps_empty_title),
					body = stringResource(R.string.tv_apps_empty_body)
				)
			} else {
				LazyColumn(
					state = listState,
					modifier = Modifier.fillMaxSize().tvWholeRows(),
					verticalArrangement = Arrangement.spacedBy(TvDims.RowGap)
				) {
					itemsIndexed(current.apps, key = { _, app -> app.packageName }) { index, app ->
						Box(Modifier.onFocusChanged { if (it.hasFocus) onRowFocused(index) }) {
						TvActionRow(dimmed = app.ignored) {
							TvAppIcon(app.packageName, 56.dp)
							Spacer(Modifier.width(18.dp))
							Column(Modifier.weight(1f)) {
								Text(
									text = app.displayName(),
									style = MaterialTheme.typography.titleLarge,
									color = MaterialTheme.colorScheme.onSurface,
									maxLines = 1,
									overflow = TextOverflow.Ellipsis
								)
								Spacer(Modifier.height(3.dp))
								Text(
									text = "${app.version}  ·  ${app.packageName}",
									style = MaterialTheme.typography.bodySmall,
									color = MaterialTheme.colorScheme.onSurfaceVariant,
									maxLines = 1,
									overflow = TextOverflow.Ellipsis
								)
							}
							if (app.ignored) {
								TvStatusPill(stringResource(R.string.tv_ignored), TvTone.Attention)
								Spacer(Modifier.width(12.dp))
							}
							TvPillButton(
								text = if (app.ignored) {
									stringResource(R.string.tv_include)
								} else {
									stringResource(R.string.tv_ignore)
								},
								onClick = { viewModel.ignore(app.packageName) }
							)
						}
						}
					}
				}
			}
		}
	}
}

/** Filters are only real in Loading and Success; Error keeps them neutral but usable. */
private data class AppsFilterFlags(
	val excludeSystem: Boolean,
	val excludeAppStore: Boolean,
	val excludeDisabled: Boolean
)

private fun appsFilters(state: AppsUiState): AppsFilterFlags = when (state) {
	is AppsUiState.Loading -> AppsFilterFlags(state.excludeSystem, state.excludeAppStore, state.excludeDisabled)
	is AppsUiState.Success -> AppsFilterFlags(state.excludeSystem, state.excludeAppStore, state.excludeDisabled)
	is AppsUiState.Error -> AppsFilterFlags(false, false, false)
}

@Composable
private fun AppsSubtitle(state: AppsUiState): String = when (state) {
	is AppsUiState.Success -> when (state.apps.size) {
		1 -> stringResource(R.string.tv_apps_shown_one)
		else -> stringResource(R.string.tv_apps_shown, state.apps.size)
	}
	else -> ""
}
