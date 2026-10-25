# DESIGN.md — APKUpdater TV (10-foot interface)

Mode: **Operate**. The tool should disappear into the task. Everything here serves
remote-first scanning at three metres.

## The world

Google TV native, not a phone app stretched: one dark surface, one blue accent that
means *focus or primary action*, one amber that means *attention*. No cards inside
cards, no gradients, no glass, no ambient decoration. The left rail is permanent. The
only thing that glows is where you are.

## Palette (Material 3 roles, defined in `ui/tv/TvTheme.kt`)

| Role | Hex | Use |
|---|---|---|
| background | `#0B0E12` | room |
| surface | `#11151A` | rows, nav rail base |
| surfaceVariant | `#1C222A` | focused row, focused pill, thumbnails |
| primary | `#9CC6FF` | focus ring, primary button, version delta |
| onPrimary | `#06203A` | label on primary |
| primaryContainer | `#1B3A5C` | selected rail item, selected filter |
| onPrimaryContainer | `#D6E6FF` | label on selected rail item |
| tertiary / onTertiary | `#FFC46B` / `#2A1A00` | pending-update badge, ignored state |
| onBackground / onSurface | `#F2F5F9` | titles, values |
| onSurfaceVariant | `#AEB8C6` | secondary text (8.8:1 on surface) |
| outline | `#5C6878` | button borders (3.1:1 on surface) |
| outlineVariant | `#262E38` | row hairlines |
| error | `#FFB4AB` | failure text |

Rules: accent is never decoration. Never raw hex in a screen file; always a role.
TV is always dark; the phone theme preference does not reach this surface.

## Type scale (`TvTypography`)

| Role | Size / line | Weight | Job |
|---|---|---|---|
| displaySmall | 40 / 46, −0.6 | 600 | screen title |
| headlineSmall | 28 / 34 | 600 | empty-state title |
| titleLarge | 24 / 30 | 600 | row title, search input |
| titleMedium | 21 / 27 | 500 | nav brand, link rows |
| bodyLarge | 19 / 26 | 400 | setting labels |
| bodyMedium | 16 / 23 | 400 | secondary text, subtitles, metadata |
| bodySmall | 14 / 19 | 400 | third line (what's new, package name) |
| labelLarge | 16 / 20, 600 | buttons, chips, values |
| labelMedium | 13 / 16, +1.2 | badges |

No text below 13sp anywhere, and 13sp only for a count badge.

## Geometry

- Overscan margin: 40dp sides, 28dp top, 20dp bottom.
- Rail: 200dp wide; items 46dp tall, 6dp apart.
- Content pane gap after rail: 36dp.
- Row: 96dp tall, 10dp apart, 10dp radius, 1dp hairline.
- App icon: 64dp in lists, 56dp on Apps, 30dp in the rail lockup.
- Pills: 44dp tall, fully rounded, 18dp side padding. Chips: 40dp.
- Settings rows: 56dp tall, 8dp radius, 6dp apart.

## Components (`ui/tv/TvComponents.kt`)

`TvActionRow` — the only list container. Flat surface + hairline; when any child holds
focus the row becomes `surfaceVariant` with a 2dp `primary` ring. Rows are **not**
clickable containers with focusable children: the action pill carries focus, and the
row reports `hasFocus`.

`TvPillButton` — primary (filled) or secondary (outlined ghost). 3dp ring on focus; on a
filled button the ring is `onSurface` so it reads on blue. Small controls get the
heavier ring because a 2dp line disappears at three metres.

`TvIconAction` — 44×44 icon-only secondary action (skip version).

`TvFilterChip` — filter state, with a check when on. `selected` always means *included*,
never *excluded*.

`TvScreenHeader` — title, one-line subtitle, right-aligned actions. No eyebrow, no
kicker, no section numbers.

`TvStatusPill` / `TvUpdating` — state, not decoration.

`TvSkeletonList` — loading is a skeleton of *rows* (icon well, title bar, meta bar) in
the real row chrome, never a lone spinner in the middle of content. The shimmer is
palette-native: `surfaceVariant` sweeping through `outlineVariant`.

`TvEmptyState` / `TvErrorState` — headline, one explanatory line, at most one action.

`TvSwitchRow` / `TvCycleRow` / `TvStepperRow` / `TvTextRow` / `TvLinkRow` — settings
vocabulary. The switch is a read-out (`onCheckedChange = null`); the whole row toggles.
Cycles are used only for 2–3 option settings; hours use a stepper because 24 presses is
not a UI, and the stepper keeps the same 56dp row chrome so it reads as one list.
One-shot actions (About, Copy app list) use `TvLinkRow`, never a cycle row that cycles
nothing.

## Focus and navigation

- Initial focus lands on the selected rail item; pressing right enters the content.
  Screens never steal focus on their own.
- Selected ≠ focused. Selected is a filled `primaryContainer`; focused is a 2dp
  `primary` ring. Both can be true.
- Back always works: the nav graph keeps its routes and NavHost owns the back stack.
- Every action is reachable with D-pad only. No long-press-only affordances.
- A text field never traps the remote: down hands focus to the first result, left hands
  it back to the rail, and losing focus dismisses the IME. Cursor movement inside the
  field is not part of the remote contract.
- Lists scroll by focus; a focused row is always fully visible (96dp rows fit 4 per
  540dp-tall screen).

## Copy

- Verbs name the action: *Check for updates*, *Update*, *Skip this version*, *Install*,
  *Include*, *Ignore*.
- One primary action per state. The header keeps the persistent refresh; an empty
  state only explains (it never repeats that button); an error state owns its retry.
- Empty states teach: what was checked and what will appear here.
- Errors name the problem and the recovery in one sentence, then offer the retry.
- No exclamation marks, no "Oops", no emoji.

## Motion

Focus changes are instant colour/border swaps (no scale bounce). The only authored
motion is the loading shimmer, and it disappears the moment data arrives. Respect the
system "remove animations" setting by keeping everything non-essential off the timeline.

## Verification

Built and captured on an Android TV emulator/device (`adb exec-out screencap`), at
1080p, in dark. Checks: focus visible on every screen, no clipped text at font scale
1.0 and 1.3, contrast of secondary text ≥ 4.5:1, no screen requires touch.
