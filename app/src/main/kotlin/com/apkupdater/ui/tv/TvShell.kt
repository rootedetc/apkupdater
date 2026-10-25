package com.apkupdater.ui.tv

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.apkupdater.BuildConfig
import com.apkupdater.data.ui.Screen
import com.apkupdater.viewmodel.AppsViewModel
import com.apkupdater.viewmodel.MainViewModel
import com.apkupdater.viewmodel.SearchViewModel
import com.apkupdater.viewmodel.SettingsViewModel
import com.apkupdater.viewmodel.UpdatesViewModel
import kotlinx.coroutines.delay


/*
 * TV shell: a permanent left rail, one content pane, no bottom bar and no
 * pull-to-refresh. Focus lands on the rail at launch; pressing right enters the
 * list, which is the only gesture a remote owes anyone.
 */
@Composable
fun TvShell(
	navController: NavHostController,
	mainViewModel: MainViewModel,
	appsViewModel: AppsViewModel,
	updatesViewModel: UpdatesViewModel,
	searchViewModel: SearchViewModel,
	settingsViewModel: SettingsViewModel
) {
	val badges by mainViewModel.badges.collectAsStateWithLifecycle()
	val entry by navController.currentBackStackEntryAsState()
	val currentRoute = entry?.destination?.route ?: Screen.Updates.route
	val firstItem = remember { FocusRequester() }
	val launchRoute = remember { mainViewModel.getLastRoute() }

	Row(
		modifier = Modifier
			.fillMaxSize()
			.padding(
				start = TvDims.ScreenHorizontal,
				end = TvDims.ScreenHorizontal,
				top = TvDims.ScreenTop,
				bottom = TvDims.ScreenBottom
			)
	) {
		TvNavRail(
			currentRoute = currentRoute,
			badges = badges,
			initialRoute = launchRoute,
			initialFocus = firstItem,
			onSelect = { route -> mainViewModel.navigateTo(navController, route) }
		)
		Spacer(Modifier.width(36.dp))
		Box(Modifier.weight(1f).fillMaxHeight()) {
			TvNavHost(
				navController = navController,
				mainViewModel = mainViewModel,
				appsViewModel = appsViewModel,
				updatesViewModel = updatesViewModel,
				searchViewModel = searchViewModel,
				settingsViewModel = settingsViewModel
			)
		}
	}

	LaunchedEffect(Unit) {
		delay(250)
		runCatching { firstItem.requestFocus() }
	}
}

@Composable
fun TvNavRail(
	currentRoute: String,
	badges: Map<String, String>,
	initialRoute: String,
	initialFocus: FocusRequester,
	onSelect: (String) -> Unit
) {
	val screens = listOf(Screen.Updates, Screen.Apps, Screen.Search, Screen.Settings)
	// Switching tabs tears down the content focus tree; without this the ring
	// drifts to a neighbouring rail item instead of staying on the one chosen.
	val railFocus = remember { screens.associate { it.route to FocusRequester() } }
	LaunchedEffect(currentRoute) {
		delay(60)
		runCatching { railFocus[currentRoute]?.requestFocus() }
	}

	Column(
		modifier = Modifier.width(TvDims.RailWidth).fillMaxHeight(),
		verticalArrangement = Arrangement.spacedBy(6.dp)
	) {
		Row(
			modifier = Modifier.padding(start = 10.dp, top = 6.dp, bottom = 26.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			TvAppIcon(BuildConfig.APPLICATION_ID, 30.dp)
			Spacer(Modifier.width(10.dp))
			Text(
				"APKUpdater",
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onBackground
			)
		}

		screens.forEach { screen ->
			val selected = screen.route == currentRoute
			TvNavItem(
				icon = iconFor(screen),
				label = stringResource(screen.resourceId),
				selected = selected,
				badge = badges[screen.route].orEmpty(),
				onClick = { onSelect(screen.route) },
				modifier = Modifier.focusRequester(railFocus.getValue(screen.route))
			)
		}

	}
}

@Composable
fun TvNavItem(
	icon: ImageVector,
	label: String,
	selected: Boolean,
	badge: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	var focused by remember { mutableStateOf(false) }
	val colors = MaterialTheme.colorScheme

	Surface(
		onClick = onClick,
		modifier = modifier
			.fillMaxWidth()
			.height(46.dp)
			.onFocusChanged { focused = it.isFocused },
		shape = RoundedCornerShape(10.dp),
		color = when {
			focused -> colors.surfaceVariant
			selected -> colors.primaryContainer
			else -> Color.Transparent
		},
		contentColor = when {
			focused -> colors.onSurface
			selected -> colors.onPrimaryContainer
			else -> colors.onSurfaceVariant
		},
		border = BorderStroke(
			if (focused) 2.dp else 1.dp,
			if (focused) colors.primary else Color.Transparent
		)
	) {
		Row(
			modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(icon, null, Modifier.size(22.dp))
			Spacer(Modifier.width(12.dp))
			Text(
				label,
				style = MaterialTheme.typography.labelLarge,
				maxLines = 1,
				modifier = Modifier.weight(1f)
			)
			if (badge.isNotEmpty()) {
				Text(
					text = badge,
					style = MaterialTheme.typography.labelMedium,
					color = colors.onTertiary,
					maxLines = 1,
					modifier = Modifier
						.background(colors.tertiary, RoundedCornerShape(50))
						.padding(horizontal = 8.dp, vertical = 3.dp)
				)
			}
		}
	}
}

@Composable
fun TvNavHost(
	navController: NavHostController,
	mainViewModel: MainViewModel,
	appsViewModel: AppsViewModel,
	updatesViewModel: UpdatesViewModel,
	searchViewModel: SearchViewModel,
	settingsViewModel: SettingsViewModel
) = NavHost(
	navController = navController,
	startDestination = mainViewModel.getLastRoute()
) {
	composable(Screen.Updates.route) { TvUpdatesScreen(updatesViewModel) }
	composable(Screen.Apps.route) { TvAppsScreen(appsViewModel) }
	composable(Screen.Search.route) { TvSearchScreen(searchViewModel) }
	composable(Screen.Settings.route) { TvSettingsScreen(settingsViewModel) }
}

private fun iconFor(screen: Screen): ImageVector = when (screen) {
	Screen.Updates -> Icons.Filled.Refresh
	Screen.Apps -> Icons.Filled.List
	Screen.Search -> Icons.Filled.Search
	Screen.Settings -> Icons.Filled.Settings
}
