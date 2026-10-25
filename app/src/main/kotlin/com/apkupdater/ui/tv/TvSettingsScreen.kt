package com.apkupdater.ui.tv

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apkupdater.BuildConfig
import com.apkupdater.R
import com.apkupdater.data.ui.SettingsUiState
import com.apkupdater.util.isAndroidTv
import com.apkupdater.viewmodel.SettingsViewModel
import kotlinx.coroutines.delay


@Composable
fun TvSettingsScreen(viewModel: SettingsViewModel) {
	val state by viewModel.state.collectAsStateWithLifecycle()
	val aboutRow = remember { FocusRequester() }
	val firstLink = remember { FocusRequester() }

	// Switching between the settings page and About replaces the whole focus
	// tree. Without this the D-pad focus falls back to the rail, which makes
	// About look like a dead end. The very first composition is exempt: the
	// shell owns launch focus.
	var seen by remember { mutableStateOf(false) }
	LaunchedEffect(state) {
		if (!seen) {
			seen = true
			return@LaunchedEffect
		}
		delay(80)
		runCatching {
			if (state == SettingsUiState.Settings) aboutRow.requestFocus() else firstLink.requestFocus()
		}
	}

	// About is a sub-page inside one route, so the system Back button has to
	// close it explicitly or it would exit the app from a detail screen.
	BackHandler(enabled = state == SettingsUiState.About) { viewModel.setSettings() }

	if (state == SettingsUiState.Settings) {
		TvSettingsPage(viewModel, aboutRow)
	} else {
		TvAboutPage(viewModel, firstLink)
	}
}

@Composable
private fun TvSettingsPage(viewModel: SettingsViewModel, aboutRow: FocusRequester) {
	// Preferences are read straight from the ViewModel. Bumping tick after every
	// write is what makes this page read them again, so the read below is on purpose.
	var tick by remember { mutableStateOf(0) }
	val context = LocalContext.current
	val permissionLauncher = rememberLauncherForActivityResult(
		ActivityResultContracts.RequestPermission()
	) {}

	Column(Modifier.fillMaxSize()) {
		// Snapshot read: the setters below bump tick, which repaints these rows.
		@Suppress("UNUSED_VARIABLE")
		val revision = tick
		TvScreenHeader(
			title = stringResource(R.string.tab_settings),
			subtitle = "APKUpdater ${BuildConfig.VERSION_NAME} · TV layout"
		)
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			verticalArrangement = Arrangement.spacedBy(6.dp),
			contentPadding = PaddingValues(bottom = 24.dp)
		) {
			// The list content is memoized; reading tick inside it keeps every row
			// tied to the preference writes above.
			@Suppress("UNUSED_VARIABLE")
			val listRevision = tick
			item {
				TvSection(stringResource(R.string.tv_section_interface))
			}
			item {
				TvCycleRow(
					label = stringResource(R.string.theme),
					value = when (viewModel.getTheme()) {
						1 -> stringResource(R.string.theme_dark)
						2 -> stringResource(R.string.theme_light)
						else -> stringResource(R.string.theme_system)
					},
					onClick = {
						viewModel.setTheme((viewModel.getTheme() + 1) % 3)
						tick++
					}
				)
			}
			item {
				TvSwitchRow(
					label = stringResource(R.string.play_text_animations),
					checked = viewModel.getPlayTextAnimations(),
					onChange = {
						viewModel.setPlayTextAnimations(it)
						tick++
					}
				)
			}
			if (!context.isAndroidTv()) {
				item {
					TvSwitchRow(
						label = stringResource(R.string.settings_android_tv_ui),
						checked = viewModel.getAndroidTvUi(),
						onChange = {
							viewModel.setAndroidTvUi(it)
							tick++
						}
					)
				}
			}

			item {
				TvSection(stringResource(R.string.settings_sources))
			}
			item {
				TvSwitchRow(
					label = stringResource(R.string.source_github),
					checked = viewModel.getUseGitHub(),
					onChange = {
						viewModel.setUseGitHub(it)
						tick++
					}
				)
			}
			item {
				TvSwitchRow(
					label = stringResource(R.string.source_gitlab),
					checked = viewModel.getUseGitLab(),
					onChange = {
						viewModel.setUseGitLab(it)
						tick++
					}
				)
			}
			item {
				TvSwitchRow(
					label = stringResource(R.string.source_apkmirror),
					checked = viewModel.getUseApkMirror(),
					onChange = {
						viewModel.setUseApkMirror(it)
						tick++
					}
				)
			}
			item {
				TvSwitchRow(
					label = stringResource(R.string.source_fdroid),
					checked = viewModel.getUseFdroid(),
					onChange = {
						viewModel.setUseFdroid(it)
						tick++
					}
				)
			}
			item {
				TvSwitchRow(
					label = stringResource(R.string.source_izzy),
					checked = viewModel.getUseIzzy(),
					onChange = {
						viewModel.setUseIzzy(it)
						tick++
					}
				)
			}
			item {
				TvSwitchRow(
					label = stringResource(R.string.source_aptoide),
					checked = viewModel.getUseAptoide(),
					onChange = {
						viewModel.setUseAptoide(it)
						tick++
					}
				)
			}
			item {
				TvSwitchRow(
					label = stringResource(R.string.source_apkpure),
					checked = viewModel.getUseApkPure(),
					onChange = {
						viewModel.setUseApkPure(it)
						tick++
					}
				)
			}

			item {
				TvSection(stringResource(R.string.settings_options))
			}
			item {
				TvSwitchRow(
					label = stringResource(R.string.root_install),
					checked = viewModel.getRootInstall(),
					onChange = {
						viewModel.setRootInstall(it)
						tick++
					}
				)
			}
			item {
				TvSwitchRow(
					label = stringResource(R.string.ignore_alpha),
					checked = viewModel.getIgnoreAlpha(),
					onChange = {
						viewModel.setIgnoreAlpha(it)
						tick++
					}
				)
			}
			item {
				TvSwitchRow(
					label = stringResource(R.string.ignore_beta),
					checked = viewModel.getIgnoreBeta(),
					onChange = {
						viewModel.setIgnoreBeta(it)
						tick++
					}
				)
			}
			item {
				TvSwitchRow(
					label = stringResource(R.string.ignore_preRelease),
					checked = viewModel.getIgnorePreRelease(),
					onChange = {
						viewModel.setIgnorePreRelease(it)
						tick++
					}
				)
			}
			item {
				TvSwitchRow(
					label = stringResource(R.string.use_safe_stores),
					checked = viewModel.getUseSafeStores(),
					onChange = {
						viewModel.setUseSafeStores(it)
						tick++
					}
				)
			}

			item {
				TvSection(stringResource(R.string.tv_section_schedule))
			}
			item {
				TvSwitchRow(
					label = stringResource(R.string.settings_alarm),
					checked = viewModel.getEnableAlarm(),
					onChange = {
						viewModel.setEnableAlarm(it, permissionLauncher)
						tick++
					}
				)
			}
			item {
				TvStepperRow(
					label = stringResource(R.string.settings_hour),
					value = "%02d:00".format(viewModel.getAlarmHour()),
					onMinus = {
						viewModel.setAlarmHour((viewModel.getAlarmHour() + 23) % 24)
						tick++
					},
					onPlus = {
						viewModel.setAlarmHour((viewModel.getAlarmHour() + 1) % 24)
						tick++
					}
				)
			}
			item {
				TvCycleRow(
					label = stringResource(R.string.frequency),
					value = when (viewModel.getAlarmFrequency()) {
						1 -> stringResource(R.string.settings_alarm_3day)
						2 -> stringResource(R.string.settings_alarm_weekly)
						else -> stringResource(R.string.settings_alarm_daily)
					},
					onClick = {
						viewModel.setAlarmFrequency((viewModel.getAlarmFrequency() + 1) % 3)
						tick++
					}
				)
			}

			item {
				TvSection(stringResource(R.string.settings_utils))
			}
			item {
				TvLinkRow(
					title = stringResource(R.string.tv_about),
					body = stringResource(R.string.tv_about_body),
					onClick = { viewModel.setAbout() },
					modifier = Modifier.focusRequester(aboutRow)
				)
			}
			item {
				TvLinkRow(
					title = stringResource(R.string.copy_app_list),
					body = stringResource(R.string.tv_copy_body),
					onClick = { viewModel.copyAppList() }
				)
			}
		}
	}
}

@Composable
private fun TvAboutPage(viewModel: SettingsViewModel, firstLink: FocusRequester) {
	val uriHandler = LocalUriHandler.current

	Column(Modifier.fillMaxSize()) {
		TvScreenHeader(
			title = stringResource(R.string.tv_about),
			subtitle = stringResource(R.string.tv_about_subtitle),
			actions = {
				TvPillButton(
					text = stringResource(R.string.tv_back),
					onClick = { viewModel.setSettings() }
				)
			}
		)
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			verticalArrangement = Arrangement.spacedBy(10.dp),
			contentPadding = PaddingValues(bottom = 24.dp)
		) {
			item {
				TvLinkRow(
					title = "GitHub - APKUpdater",
					body = stringResource(R.string.tv_link_source),
					onClick = { uriHandler.openUri("https://github.com/rumboalla/apkupdater") },
					modifier = Modifier.focusRequester(firstLink)
				)
			}
			item {
				TvLinkRow(
					title = "Malaria Consortium",
					body = stringResource(R.string.tv_link_donate),
					onClick = { uriHandler.openUri("https://www.malariaconsortium.org/support-us/donate.htm") }
				)
			}
			item {
				TvLinkRow(
					title = "New Incentives",
					body = stringResource(R.string.tv_link_donate),
					onClick = { uriHandler.openUri("https://www.newincentives.org/donate") }
				)
			}
			item {
				TvLinkRow(
					title = "Sightsavers",
					body = stringResource(R.string.tv_link_donate),
					onClick = { uriHandler.openUri("https://donate.sightsavers.org/smxpatron/global/donate.html") }
				)
			}
			item {
				TvTextRow(
					label = stringResource(R.string.tv_version),
					value = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
				)
			}
		}
	}
}
