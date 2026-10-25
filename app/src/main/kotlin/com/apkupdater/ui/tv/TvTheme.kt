package com.apkupdater.ui.tv

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


/*
 * Ten-foot palette. Cool near-black for the room, one blue accent for focus and
 * primary action, one amber for attention (pending updates). Every text pair
 * below clears WCAG AA at TV viewing distance; the accent is never used as
 * decoration, only for focus, action and state.
 */
private val TvColors = darkColorScheme(
	primary = Color(0xFF9CC6FF),
	onPrimary = Color(0xFF06203A),
	primaryContainer = Color(0xFF1B3A5C),
	onPrimaryContainer = Color(0xFFD6E6FF),
	secondary = Color(0xFFBFC8D6),
	onSecondary = Color(0xFF1A1F26),
	secondaryContainer = Color(0xFF2A313B),
	onSecondaryContainer = Color(0xFFE6ECF4),
	tertiary = Color(0xFFFFC46B),
	onTertiary = Color(0xFF2A1A00),
	tertiaryContainer = Color(0xFF4A3312),
	onTertiaryContainer = Color(0xFFFFE2B8),
	background = Color(0xFF0B0E12),
	onBackground = Color(0xFFF2F5F9),
	surface = Color(0xFF11151A),
	onSurface = Color(0xFFF2F5F9),
	surfaceVariant = Color(0xFF1C222A),
	onSurfaceVariant = Color(0xFFAEB8C6),
	outline = Color(0xFF5C6878),
	outlineVariant = Color(0xFF262E38),
	error = Color(0xFFFFB4AB),
	onError = Color(0xFF3B0907),
	errorContainer = Color(0xFF5C1A16),
	onErrorContainer = Color(0xFFFFDAD5),
	inverseSurface = Color(0xFFE6ECF4),
	inverseOnSurface = Color(0xFF1A1F26),
	inversePrimary = Color(0xFF2C5F92),
	surfaceTint = Color(0xFF9CC6FF),
	scrim = Color(0xFF000000)
)

/*
 * TV type scale. Sizes are deliberately larger than the phone scale: at three
 * metres, 16sp is the floor for secondary text and 24sp carries row titles.
 */
private val TvTypography = Typography(
	displaySmall = TextStyle(
		fontSize = 40.sp,
		lineHeight = 46.sp,
		fontWeight = FontWeight.SemiBold,
		letterSpacing = (-0.6).sp
	),
	headlineSmall = TextStyle(
		fontSize = 28.sp,
		lineHeight = 34.sp,
		fontWeight = FontWeight.SemiBold,
		letterSpacing = (-0.2).sp
	),
	titleLarge = TextStyle(
		fontSize = 24.sp,
		lineHeight = 30.sp,
		fontWeight = FontWeight.SemiBold
	),
	titleMedium = TextStyle(
		fontSize = 21.sp,
		lineHeight = 27.sp,
		fontWeight = FontWeight.Medium
	),
	bodyLarge = TextStyle(
		fontSize = 19.sp,
		lineHeight = 26.sp
	),
	bodyMedium = TextStyle(
		fontSize = 16.sp,
		lineHeight = 23.sp
	),
	bodySmall = TextStyle(
		fontSize = 14.sp,
		lineHeight = 19.sp
	),
	labelLarge = TextStyle(
		fontSize = 16.sp,
		lineHeight = 20.sp,
		fontWeight = FontWeight.SemiBold,
		letterSpacing = 0.2.sp
	),
	labelMedium = TextStyle(
		fontSize = 13.sp,
		lineHeight = 16.sp,
		fontWeight = FontWeight.SemiBold,
		letterSpacing = 1.2.sp
	)
)

object TvDims {
	/** Overscan-safe margin. TVs crop up to 5% of each edge. */
	val ScreenHorizontal = 40.dp
	val ScreenTop = 28.dp
	val ScreenBottom = 20.dp
	val RailWidth = 200.dp
	val RowHeight = 96.dp
	val RowGap = 10.dp
	val Icon = 64.dp
}

@Composable
fun TvTheme(content: @Composable () -> Unit) = MaterialTheme(
	colorScheme = TvColors,
	typography = TvTypography,
	content = content
)
