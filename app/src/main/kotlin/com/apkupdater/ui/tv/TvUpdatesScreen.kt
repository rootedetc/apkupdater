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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apkupdater.R
import com.apkupdater.data.ui.Source
import com.apkupdater.data.ui.UpdatesUiState
import com.apkupdater.viewmodel.UpdatesViewModel


/*
 * Updates at ten feet. One row per update, one primary action per row, and a
 * single skip affordance that stays out of the way until someone needs it.
 * The header always stays on screen so the user knows whether the list is
 * empty because everything is current or because a refresh is running.
 */
@Composable
fun TvUpdatesScreen(viewModel: UpdatesViewModel) {
	val state by viewModel.state().collectAsStateWithLifecycle()
	val handler = LocalUriHandler.current
	val (listState, onRowFocused) = rememberTvRowList()

	Column(Modifier.fillMaxSize()) {
		TvScreenHeader(
			title = stringResource(R.string.tab_updates),
			subtitle = UpdatesSubtitle(state)
		) {
			TvPillButton(
				text = stringResource(R.string.tv_check_updates),
				onClick = { viewModel.refresh() },
				primary = true
			)
		}

		when (val current = state) {
			is UpdatesUiState.Loading -> TvSkeletonList(4)
			is UpdatesUiState.Error -> TvErrorState(
				title = stringResource(R.string.tv_error_title),
				body = stringResource(R.string.tv_error_body),
				retryLabel = stringResource(R.string.tv_try_again),
				onRetry = { viewModel.refresh() }
			)
			is UpdatesUiState.Success -> if (current.updates.isEmpty()) {
				// The header keeps the single primary action; the empty state explains.
				TvEmptyState(
					title = stringResource(R.string.tv_up_to_date_title),
					body = stringResource(R.string.tv_up_to_date_body)
				)
			} else {
				LazyColumn(
					state = listState,
					modifier = Modifier.fillMaxSize().tvWholeRows(),
					verticalArrangement = Arrangement.spacedBy(TvDims.RowGap)
				) {
					itemsIndexed(current.updates, key = { _, app -> app.id }) { index, app ->
						Box(Modifier.onFocusChanged { if (it.hasFocus) onRowFocused(index) }) {
						UpdateRow(
							displayName = app.displayName(),
							oldVersion = app.oldVersion,
							version = app.version,
							packageName = app.packageName,
							source = app.source,
							whatsNew = app.whatsNewLine(),
							isInstalling = app.isInstalling,
							onInstall = { viewModel.install(app, handler) },
							onSkip = { viewModel.ignoreVersion(app.id) }
						)
						}
					}
				}
			}
		}
	}
}

@Composable
private fun UpdatesSubtitle(state: UpdatesUiState): String = when (state) {
	is UpdatesUiState.Loading -> stringResource(R.string.tv_updates_checking)
	// The error state below carries the title and the recovery action.
	is UpdatesUiState.Error -> ""
	is UpdatesUiState.Success -> when {
		state.updates.size == 1 -> stringResource(R.string.tv_updates_one)
		state.updates.size > 1 -> stringResource(R.string.tv_updates_ready, state.updates.size)
		// Empty: the empty state below carries the message, no duplicate line here.
		else -> ""
	}
}

/*
 * Plain arguments instead of AppUpdate keep the row cheap to recompose and
 * make it obvious that everything here fits on two short lines. whatsNewLine()
 * is resolved once, outside the layout.
 */
@Composable
private fun UpdateRow(
	displayName: String,
	oldVersion: String,
	version: String,
	packageName: String,
	source: Source,
	whatsNew: String,
	isInstalling: Boolean,
	onInstall: () -> Unit,
	onSkip: () -> Unit
) = TvActionRow {
	TvAppIcon(packageName)
	Spacer(Modifier.width(18.dp))
	Column(Modifier.weight(1f)) {
		Text(
			text = displayName,
			style = MaterialTheme.typography.titleLarge,
			color = MaterialTheme.colorScheme.onSurface,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)
		Spacer(Modifier.height(4.dp))
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Text(
				text = "$oldVersion  →  $version",
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.primary,
				maxLines = 1
			)
			TvSourceTag(source)
		}
		if (whatsNew.isNotBlank()) {
			Spacer(Modifier.height(4.dp))
			Text(
				text = whatsNew,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}
	}
	Spacer(Modifier.width(24.dp))
	if (isInstalling) {
		TvUpdating()
	} else {
		Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
			TvPillButton(
				text = stringResource(R.string.tv_update),
				onClick = onInstall,
				primary = true
			)
			TvIconAction(
				icon = Icons.Filled.Close,
				description = stringResource(R.string.tv_skip_version),
				onClick = onSkip
			)
		}
	}
}
