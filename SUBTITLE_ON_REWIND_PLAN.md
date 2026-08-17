# Plan: Show subtitle after rewind

## Goal

When the user rewinds (seeks backward) during playback, automatically show the
selected subtitle track for a short window, then restore the previous subtitle
state. This mirrors the Apple TV "subtitles on rewind" behavior.

The feature is **opt-in** via a single settings toggle and is only offered when a
subtitle track is already selected.

## Behavior / UX

- Toggle: `Show subtitle after rewind` (boolean, default **off**).
- The toggle is **only visible** in settings when a subtitle track is selected
  (i.e. `playerPreferences.preferredSubtitleLanguage` is non-empty). When
  subtitles are disabled, the toggle is hidden.
- On a backward seek, if the toggle is on:
  1. Capture the current text-track state (selected index or disabled).
  2. Force the **already-selected** subtitle track on (no guessing — we know
     which language because the toggle only exists when one is chosen).
  3. After `min(rewindDeltaMs, SUBTITLE_REWIND_MAX_MS)` (cap `30s`), restore the
     prior subtitle state (on or off).
- Forward seeks / normal playback are unaffected.
- Multiple rapid rewinds cancel the prior revert timer and restart the window
  from the newest rewind.

## Settings placement

`feature/settings/.../screens/player/PlayerPreferencesScreen.kt` (the "Player"
settings screen, alongside `remember selections` / `resume`). The trigger is a
seek, so this is the natural home. Conditional visibility keeps the Subtitle
screen unchanged.

## Implementation

### 1. Preference model
`core/model/.../PlayerPreferences.kt`
- Add `val showSubtitleAfterRewind: Boolean = false`.
- Add a `DEFAULT_*` const if the datastore serializer needs it (follow existing
  pattern for new boolean prefs).

### 2. Datastore / repository
- Extend the `PlayerPreferences` serializer defaults and
  `PreferencesRepository.updatePlayerPreferences { ... }` (already used) — no new
  API needed, just the new field.

### 3. Settings UI
`feature/settings/.../screens/player/PlayerPreferencesScreen.kt`
- Add a `PreferenceSwitch` row, rendered **only when**
  `uiState.preferences.preferredSubtitleLanguage.isNotEmpty()`.
- Wire to a new `PlayerPreferencesUiEvent.ToggleShowSubtitleAfterRewind` and
  handle it in `PlayerPreferencesViewModel` (mirror existing toggles like
  `ToggleAutoplay`).

### 4. Engine logic
`feature/player/.../service/PlayerService.kt`
- In `playbackStateListener.onPositionDiscontinuity`
  (`PlayerService.kt:129`): detect rewind via
  `reason == DISCONTINUITY_REASON_SEEK && newPosition.positionMs < oldPosition.positionMs`.
  This single hook catches every seek path (gesture, seek bar, DPAD, rewind key,
  double-tap).
- Guard with `playerPreferences.showSubtitleAfterRewind` and skip if a text track
  is already selected (nothing to do).
- Capture prior state with `player.getManuallySelectedTrackIndex(C.TRACK_TYPE_TEXT)`
  (`extensions/Player.kt:57`) — `-1` means disabled.
- Enable the selected track via `player.switchTrack(C.TRACK_TYPE_TEXT, index)`
  using the index of `preferredSubtitleLanguage` within `currentTracks`.
- `serviceScope.launch { delay(showMs) ; restoreSubtitles() }`, cancelling any
  in-flight job first.
- `restoreSubtitles()` re-applies the captured state (disable or reselect index).

### 5. Constants
- `private const val SUBTITLE_REWIND_MAX_MS = 30_000L` in `PlayerService`.kt.

## Edge cases

- Subs already on at rewind → no-op (capture = on, restore = on).
- Subs turned off by user mid-movie, then rewind → re-enabled for window, then
  turned back off (restore = disabled).
- Rapid repeated rewinds → cancel prior timer, restart window.
- Media item change / service destroy → cancel the revert job.
- Pause during the window → pure `delay` drifts slightly; acceptable for v1
  (re-occurrence just restarts the window).

## Files touched

- `core/model/.../PlayerPreferences.kt` (new field + default)
- `feature/settings/.../screens/player/PlayerPreferencesScreen.kt` (switch row,
  conditional)
- `feature/settings/.../screens/player/PlayerPreferencesViewModel.kt` (event)
- `feature/player/.../service/PlayerService.kt` (detect + toggle + revert)
- Datastore serializer defaults (if required by pref pattern)

## Testing

- Build + install on a device.
- Video with a subtitle track, `preferredSubtitleLanguage` set, toggle ON:
  - Rewind 10s → subtitle appears, disappears ~10s later.
  - Rewind 2min via seek bar → subtitle capped at 30s.
  - Rewind while subs already on → no change.
  - Toggle OFF / no subtitle language selected → toggle hidden, no auto-sub.
