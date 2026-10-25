# PRODUCT.md — APKUpdater TV patch

## What this is

A fork of [APKUpdater](https://github.com/rumboalla/apkupdater) 3.0.3 (GPL-3.0) with a
purpose-built 10-foot interface for Android TV / Google TV. The upstream phone UI is
untouched; the TV UI replaces it only when `Prefs.androidTvUi` is on (the default on
leanback devices).

Product truth is unchanged from upstream:

- Aggregates updates from **GitHub, GitLab, F-Droid, F-Droid Izzy, APKMirror, APKPure,
  Aptoide** and Google Play.
- Background checks via WorkManager, notification when updates exist.
- Installs via session installer, root, or a browser hand-off for APKMirror.
- No ads, no tracking, GPL-3.0.

## Who uses it, and where

A single person on a couch, three metres from a 1080p/4K panel, holding a five-button
remote (D-pad, OK, Back, Home). The job is one sentence: *"update the apps on this TV
without typing or hunting with a mouse pointer."*

That scene is the constraint:

- No touch, no mouse, no scroll wheel, no pull-to-refresh.
- Focus is the cursor. If the user cannot see where focus is, the screen has failed.
- Reading size is governed by distance, not by density. 16sp is the floor for secondary
  text; 24sp carries row titles.
- Overscan crops up to 5% per edge on some panels.

## Jobs to be done

1. See at a glance whether anything needs updating, and how many.
2. Update one app, or check again, with two or three presses.
3. See which apps are installed and stop APKUpdater nagging about one of them.
4. Find an app that is not installed yet.
5. Turn sources and schedules on or off without tripping over a slider.

## Platform and build

- Kotlin 1.9, Jetpack Compose 1.5.1, Material 3 `1.2.0-alpha05`, minSdk 21, target 34.
- No new dependencies. No phone-layout changes. No navigation-graph changes: the four
  routes (`updates`, `apps`, `search`, `settings`) stay identical so notification
  intents keep working.
- Build identity for this fork: `com.apkupdater.tv`, label **APKUpdater TV**,
  version `3.0.3-tv.1`, signed with a fork-local release key (not the upstream one, so it
  installs beside upstream rather than over it).

## Out of scope

- Repo/bundle distribution formats (the iOS AltStore-repo equivalent). Android's
  equivalents are F-Droid indexes and Obtainium sources; APKUpdater already speaks
  F-Droid and GitHub/GitLab. Adding a new index format is a separate project.
- Phone and tablet layouts, translations for the new TV strings (English only for now,
  other locales fall back to English).
