package com.apkupdater.ui.tv

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.apkupdater.R
import com.apkupdater.data.ui.AppInstalled
import com.apkupdater.data.ui.AppUpdate
import com.apkupdater.data.ui.Source
import com.apkupdater.util.getAppIcon
import com.apkupdater.util.getAppName
import com.apkupdater.util.toAnnotatedString
import androidx.core.text.HtmlCompat
import com.apkupdater.data.ui.ApkMirrorSource
import com.apkupdater.data.ui.ApkPureSource


/*
 * One row vocabulary for every TV screen: a flat surface, a hairline, and a
 * focus state that is the only thing on screen allowed to glow. Rows are never
 * clickable containers with focusable children; the action pill carries focus,
 * and the row reports focus through its children.
 */
@Composable
fun TvActionRow(
	modifier: Modifier = Modifier,
	dimmed: Boolean = false,
	content: @Composable RowScope.() -> Unit
) {
	var focused by remember { mutableStateOf(false) }
	val colors = MaterialTheme.colorScheme
	Surface(
		modifier = modifier
			.fillMaxWidth()
			.height(TvDims.RowHeight)
			.alpha(if (dimmed) 0.55f else 1f)
			.onFocusChanged { focused = it.hasFocus },
		shape = RoundedCornerShape(10.dp),
		color = if (focused) colors.surfaceVariant else colors.surface,
		contentColor = colors.onSurface,
		border = BorderStroke(
			if (focused) 2.dp else 1.dp,
			if (focused) colors.primary else colors.outlineVariant
		)
	) {
		Row(
			modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
			verticalAlignment = Alignment.CenterVertically,
			content = content
		)
	}
}

@Composable
fun TvPillButton(
	text: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	primary: Boolean = false,
	icon: ImageVector? = null
) {
	var focused by remember { mutableStateOf(false) }
	val colors = MaterialTheme.colorScheme
	val container = when {
		primary -> colors.primary
		focused -> colors.surfaceVariant
		else -> Color.Transparent
	}
	val content = when {
		primary -> colors.onPrimary
		focused -> colors.onSurface
		else -> colors.onSurfaceVariant
	}
	Surface(
		onClick = onClick,
		modifier = modifier
			.height(44.dp)
			.onFocusChanged { focused = it.isFocused },
		shape = RoundedCornerShape(50),
		color = container,
		contentColor = content,
		border = BorderStroke(
			if (focused) 3.dp else 1.dp,
			when {
				focused && primary -> colors.onSurface
				focused -> colors.primary
				primary -> Color.Transparent
				else -> colors.outline
			}
		)
	) {
		Row(
			modifier = Modifier.fillMaxHeight().padding(horizontal = 18.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(8.dp)
		) {
			icon?.let { Icon(it, null, Modifier.size(18.dp), tint = content) }
			Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1)
		}
	}
}

@Composable
fun TvIconAction(
	icon: ImageVector,
	description: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	var focused by remember { mutableStateOf(false) }
	val colors = MaterialTheme.colorScheme
	Surface(
		onClick = onClick,
		modifier = modifier
			.size(44.dp)
			.onFocusChanged { focused = it.isFocused },
		shape = RoundedCornerShape(50),
		color = if (focused) colors.surfaceVariant else Color.Transparent,
		contentColor = if (focused) colors.onSurface else colors.onSurfaceVariant,
		border = BorderStroke(
			if (focused) 3.dp else 1.dp,
			if (focused) colors.primary else colors.outline
		)
	) {
		Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
			Icon(icon, description, Modifier.size(20.dp))
		}
	}
}

@Composable
fun TvFilterChip(
	label: String,
	selected: Boolean,
	onClick: () -> Unit
) {
	var focused by remember { mutableStateOf(false) }
	val colors = MaterialTheme.colorScheme
	Surface(
		onClick = onClick,
		modifier = Modifier
			.height(40.dp)
			.onFocusChanged { focused = it.isFocused },
		shape = RoundedCornerShape(50),
		color = if (selected) colors.primaryContainer else Color.Transparent,
		contentColor = if (selected) colors.onPrimaryContainer else colors.onSurfaceVariant,
		border = BorderStroke(
			if (focused) 3.dp else 1.dp,
			when {
				focused -> colors.primary
				selected -> Color.Transparent
				else -> colors.outline
			}
		)
	) {
		Row(
			modifier = Modifier.fillMaxHeight().padding(horizontal = 16.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(6.dp)
		) {
			if (selected) {
				Icon(Icons.Filled.Check, null, Modifier.size(16.dp))
			}
			Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 1)
		}
	}
}

@Composable
fun TvScreenHeader(
	title: String,
	subtitle: String,
	actions: @Composable RowScope.() -> Unit = {}
) = Row(
	modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
	verticalAlignment = Alignment.CenterVertically
) {
	Column(Modifier.weight(1f)) {
		Text(title, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onBackground)
		Spacer(Modifier.height(4.dp))
		Text(
			subtitle,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			maxLines = 2,
			overflow = TextOverflow.Ellipsis
		)
	}
	Row(
		horizontalArrangement = Arrangement.spacedBy(12.dp),
		verticalAlignment = Alignment.CenterVertically,
		content = actions
	)
}

@Composable
fun TvSection(title: String) = Text(
	text = title,
	style = MaterialTheme.typography.bodyLarge,
	color = MaterialTheme.colorScheme.onSurfaceVariant,
	modifier = Modifier.padding(top = 22.dp, bottom = 10.dp)
)

@Composable
fun TvAppIcon(packageName: String, size: Dp = TvDims.Icon) = AsyncImage(
	model = ImageRequest.Builder(LocalContext.current)
		.data(LocalContext.current.getAppIcon(packageName))
		.crossfade(true)
		.build(),
	contentDescription = null,
	contentScale = ContentScale.Fit,
	modifier = Modifier
		.size(size)
		.clip(RoundedCornerShape(12.dp))
		.background(MaterialTheme.colorScheme.surfaceVariant),
	error = painterResource(R.drawable.ic_root),
	placeholder = painterResource(R.drawable.ic_empty)
)

@Composable
fun TvImage(uri: Uri, size: Dp = TvDims.Icon) = AsyncImage(
	model = ImageRequest.Builder(LocalContext.current).data(uri).crossfade(true).build(),
	contentDescription = null,
	contentScale = ContentScale.Fit,
	modifier = Modifier
		.size(size)
		.clip(RoundedCornerShape(12.dp))
		.background(MaterialTheme.colorScheme.surfaceVariant),
	error = painterResource(R.drawable.ic_root),
	placeholder = painterResource(R.drawable.ic_empty)
)

/** Widest label that still tells the truth: name, version, source. */
@Composable
fun TvSourceTag(source: Source) = Row(
	verticalAlignment = Alignment.CenterVertically,
	horizontalArrangement = Arrangement.spacedBy(6.dp)
) {
	Icon(painterResource(source.resourceId), null, Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
	Text(source.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
}

@Composable
fun TvStatusPill(text: String, tone: TvTone = TvTone.Neutral) {
	val colors = MaterialTheme.colorScheme
	val (container, content) = when (tone) {
		TvTone.Neutral -> colors.surfaceVariant to colors.onSurfaceVariant
		TvTone.Accent -> colors.primaryContainer to colors.onPrimaryContainer
		TvTone.Attention -> colors.tertiaryContainer to colors.onTertiaryContainer
	}
	Box(
		modifier = Modifier
			.height(32.dp)
			.clip(RoundedCornerShape(50))
			.background(container)
			.padding(horizontal = 14.dp),
		contentAlignment = Alignment.Center
	) {
		Text(text, style = MaterialTheme.typography.labelLarge, color = content, maxLines = 1)
	}
}

enum class TvTone { Neutral, Accent, Attention }

@Composable
fun TvUpdating() = Row(
	verticalAlignment = Alignment.CenterVertically,
	horizontalArrangement = Arrangement.spacedBy(10.dp)
) {
	CircularProgressIndicator(
		modifier = Modifier.size(18.dp),
		color = MaterialTheme.colorScheme.primary,
		strokeWidth = 2.dp
	)
	Text(
		androidx.compose.ui.res.stringResource(R.string.tv_updating),
		style = MaterialTheme.typography.labelLarge,
		color = MaterialTheme.colorScheme.onSurfaceVariant
	)
}

@Composable
fun TvEmptyState(
	title: String,
	body: String,
	action: (@Composable () -> Unit)? = null
) = Box(Modifier.fillMaxSize()) {
	Column(
		modifier = Modifier.align(Alignment.Center).padding(bottom = 32.dp).width(640.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Text(
			title,
			style = MaterialTheme.typography.headlineSmall,
			color = MaterialTheme.colorScheme.onBackground,
			textAlign = TextAlign.Center
		)
		Spacer(Modifier.height(10.dp))
		Text(
			body,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			textAlign = TextAlign.Center
		)
		if (action != null) {
			Spacer(Modifier.height(24.dp))
			action()
		}
	}
}

/*
 * Loading looks like the list that is coming: icon well, title bar, meta bar.
 * A row of blank cards would read as "broken", not "busy".
 */
@Composable
fun TvSkeletonList(count: Int = 4) = Column(
	modifier = Modifier.fillMaxSize(),
	verticalArrangement = Arrangement.spacedBy(TvDims.RowGap)
) {
	repeat(count) { index ->
		Surface(
			modifier = Modifier.fillMaxWidth().height(TvDims.RowHeight),
			shape = RoundedCornerShape(10.dp),
			color = MaterialTheme.colorScheme.surface,
			border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
		) {
			Row(
				modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Box(
					Modifier
						.size(TvDims.Icon)
						.clip(RoundedCornerShape(12.dp))
						.tvShimmer()
				)
				Spacer(Modifier.width(18.dp))
				Column(Modifier.weight(1f)) {
					Box(
						Modifier
							.fillMaxWidth(if (index % 2 == 0) 0.34f else 0.28f)
							.height(20.dp)
							.clip(RoundedCornerShape(6.dp))
							.tvShimmer()
					)
					Spacer(Modifier.height(12.dp))
					Box(
						Modifier
							.fillMaxWidth(if (index % 2 == 0) 0.22f else 0.26f)
							.height(14.dp)
							.clip(RoundedCornerShape(6.dp))
							.tvShimmer()
					)
				}
			}
		}
	}
}

/** Palette-native shimmer: surfaceVariant sweeping through outlineVariant. */
@Composable
private fun Modifier.tvShimmer(): Modifier = composed {
	val low = MaterialTheme.colorScheme.surfaceVariant
	val high = MaterialTheme.colorScheme.outlineVariant
	val transition = rememberInfiniteTransition("tvShimmer")
	val shift by transition.animateFloat(
		initialValue = 0f,
		targetValue = 1f,
		animationSpec = infiniteRepeatable(
			animation = tween(1400, easing = LinearEasing)
		),
		label = "tvShimmer"
	)
	val x = shift * 1200f - 600f
	background(
		Brush.linearGradient(
			colors = listOf(low, high, low),
			start = Offset(x, 0f),
			end = Offset(x + 600f, 0f)
		)
	)
}

@Composable
fun TvErrorState(
	title: String,
	body: String,
	retryLabel: String,
	onRetry: () -> Unit
) = TvEmptyState(title, body) { TvPillButton(retryLabel, onRetry, primary = true) }

@Composable
fun TvSwitchRow(
	label: String,
	checked: Boolean,
	onChange: (Boolean) -> Unit
) {
	var focused by remember { mutableStateOf(false) }
	val colors = MaterialTheme.colorScheme
	Surface(
		onClick = { onChange(!checked) },
		modifier = Modifier
			.fillMaxWidth()
			.height(56.dp)
			.onFocusChanged { focused = it.isFocused },
		shape = RoundedCornerShape(8.dp),
		color = if (focused) colors.surfaceVariant else colors.surface,
		contentColor = colors.onSurface,
		border = BorderStroke(
			if (focused) 2.dp else 1.dp,
			if (focused) colors.primary else colors.outlineVariant
		)
	) {
		Row(
			modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				label,
				style = MaterialTheme.typography.bodyLarge,
				modifier = Modifier.weight(1f),
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
			Switch(checked = checked, onCheckedChange = null)
		}
	}
}

@Composable
fun TvCycleRow(
	label: String,
	value: String,
	onClick: () -> Unit
) {
	var focused by remember { mutableStateOf(false) }
	val colors = MaterialTheme.colorScheme
	Surface(
		onClick = onClick,
		modifier = Modifier
			.fillMaxWidth()
			.height(56.dp)
			.onFocusChanged { focused = it.isFocused },
		shape = RoundedCornerShape(8.dp),
		color = if (focused) colors.surfaceVariant else colors.surface,
		contentColor = colors.onSurface,
		border = BorderStroke(
			if (focused) 2.dp else 1.dp,
			if (focused) colors.primary else colors.outlineVariant
		)
	) {
		Row(
			modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				label,
				style = MaterialTheme.typography.bodyLarge,
				modifier = Modifier.weight(1f),
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
			Text(
				value,
				style = MaterialTheme.typography.labelLarge,
				color = if (focused) colors.primary else colors.onSurfaceVariant
			)
		}
	}
}

/*
 * Hours need a stepper, not a 24-step cycle. The row keeps the same chrome as
 * every other setting so it reads as one list, and the two pills are the only
 * focusable parts.
 */
@Composable
fun TvStepperRow(
	label: String,
	value: String,
	onMinus: () -> Unit,
	onPlus: () -> Unit
) {
	var focused by remember { mutableStateOf(false) }
	val colors = MaterialTheme.colorScheme
	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.height(56.dp)
			.onFocusChanged { focused = it.hasFocus },
		shape = RoundedCornerShape(8.dp),
		color = if (focused) colors.surfaceVariant else colors.surface,
		contentColor = colors.onSurface,
		border = BorderStroke(
			if (focused) 2.dp else 1.dp,
			if (focused) colors.primary else colors.outlineVariant
		)
	) {
		Row(
			modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 8.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				label,
				style = MaterialTheme.typography.bodyLarge,
				modifier = Modifier.weight(1f),
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
			TvPillButton(text = "−", onClick = onMinus)
			Text(
				value,
				style = MaterialTheme.typography.labelLarge,
				color = colors.onSurface,
				textAlign = TextAlign.Center,
				modifier = Modifier.width(84.dp)
			)
			TvPillButton(text = "+", onClick = onPlus)
		}
	}
}

@Composable
fun TvTextRow(label: String, value: String) {
	Row(
		modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			label,
			style = MaterialTheme.typography.bodyLarge,
			modifier = Modifier.weight(1f),
			maxLines = 1
		)
		Text(
			value,
			style = MaterialTheme.typography.labelLarge,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			maxLines = 1
		)
	}
}

@Composable
fun TvLinkRow(
	title: String,
	body: String,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	var focused by remember { mutableStateOf(false) }
	val colors = MaterialTheme.colorScheme
	Surface(
		onClick = onClick,
		modifier = modifier
			.fillMaxWidth()
			.onFocusChanged { focused = it.isFocused },
		shape = RoundedCornerShape(10.dp),
		color = if (focused) colors.surfaceVariant else colors.surface,
		border = BorderStroke(
			if (focused) 2.dp else 1.dp,
			if (focused) colors.primary else colors.outlineVariant
		)
	) {
		Column(Modifier.fillMaxWidth().padding(20.dp)) {
			Text(title, style = MaterialTheme.typography.titleMedium)
			Spacer(Modifier.height(4.dp))
			Text(
				body,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				maxLines = 2,
				overflow = TextOverflow.Ellipsis
			)
		}
	}
}

/* --- shared content formatting --- */

@Composable
fun AppUpdate.displayName(): String =
	name.ifBlank { LocalContext.current.getAppName(packageName) }.ifBlank { packageName }

fun AppUpdate.whatsNewLine(): String {
	val raw = if (source == ApkMirrorSource || source == ApkPureSource) {
		HtmlCompat.fromHtml(whatsNew.trim(), HtmlCompat.FROM_HTML_MODE_COMPACT).toAnnotatedString().text
	} else {
		whatsNew
	}
	return raw.replace(Regex("\\s+"), " ").trim()
}

fun AppInstalled.displayName(): String =
	name.ifBlank { packageName }

@Composable
fun TvMeta(text: String) = Text(
	text = text,
	style = MaterialTheme.typography.bodyMedium,
	color = MaterialTheme.colorScheme.onSurfaceVariant,
	maxLines = 1,
	overflow = TextOverflow.Ellipsis
)
