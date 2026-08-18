# Android ↔ iOS parity gap (analysis 2026-08-18)

> **Status (uncommitted on `main`):** video/timer sync, progress restore, exit-completion, music and
> offline caching are in. **Android is pinned back to the v1 media set and the Chair tab is hidden**
> — see "Blocked on media" below. Remaining: type picker, video orientation, config fields,
> printable plan.

## Blocked on media — Android is on v1, Chair is hidden

The v2/chair videos are Apple HEVC-with-alpha (the container carries an `almo` alpha-mode box). The
instructor is transparent; iOS composites him over `workout_background.jpg`. **Android's MediaCodec
cannot decode that alpha layer**, so it renders the base layer and he appears on the flat blue matte.

Verified rather than assumed: `ffprobe` reports `pix_fmt=yuv420p` and a decoded frame has
`alpha min/max = 255/255` — stock FFmpeg cannot see Apple's alpha plane, which is also why the alpha
cannot be recovered server-side from the published files. Any conversion must start from the
original masters, or decode through an alpha-aware Apple pipeline on macOS.

Until an Android-decodable rendition exists (VP9-with-alpha in WebM):
- `WorkoutApi` uses `workouts/generate` and `workouts/walking/generate` (v1, opaque MP4). v1
  hardcodes `language = "en"` server-side, so localized workout content only returns with v2.
- `CHAIR_TAB_ENABLED = false` in `TaiChiHomeRoot` hides the Chair tab. Everything behind it — the
  endpoint, progress plumbing, journey screen — stays wired, so re-enabling is one line.
- The `media3-exoplayer-hls` dependency and the studio backdrop stay in place for the same reason.

## Divergence window

Android's last feature sync with iOS was `a5d7979` **2026-04-10** ("update android to match iOS").
Everything Android shipped since is plumbing — TikTok SDK, billing library, API level, Meta SDK,
paywall fix. iOS shipped features continuously through **2026-08-17**.

So the gap is **~4 months of iOS feature work**, not a few screens.

---

## P0 — the reported bug: video stalls but audio + timer keep running

**Root cause, confirmed.** `WorkoutViewModel.startExerciseTimer` (line 103) is a pure wall-clock
loop:

```kotlin
while (isActive && !isWorkoutComplete) {
    delay(1000)
    if (isPaused) continue
    exerciseElapsedSeconds += 1
    ...
}
```

It consults only `isPaused`. It has **no knowledge of player state whatsoever**.

`isVideoPlaying` looks like it should carry that, but every write to it
(`WorkoutViewModel` lines 53, 133, 166, 185, 189-190, 199, 208, 234) is **user intent** —
start / pause / toggle. Nothing ever writes it from ExoPlayer.

And the exercise video player is created in `WorkoutScreen.kt:459` with **no listener attached at
all** — no `onPlayerError`, no `onPlaybackStateChanged`. The only `Player.Listener` in the file is
on `instructionAudioPlayer` (line 510) and only reads duration.

Compounding it: video and instruction audio are two **independent** ExoPlayer instances started
together. If the video stalls but the audio loads, audio narrates over a frozen black frame while
the timer advances exercises. That is exactly the reported symptom.

**iOS already solves this** (`WorkoutView.swift`):
- `setupStatusObserver` fires `onVideoLoadFailed` on `item.status == .failed` (line 1889)
- the handler auto-pauses the workout and shows an overlay (line 878):
  ```swift
  onVideoLoadFailed: {
      videoLoadFailed = true
      if !viewModel.isPaused { viewModel.togglePause() }
  }
  ```
- plus a "No Internet Connection" overlay with a download hint (line 725)

**Fix:** attach a `Player.Listener` to the exercise player; on `onPlayerError` and on
`STATE_BUFFERING` beyond a grace period, pause the ViewModel (which stops timer *and* audio cues,
since both hang off `isPaused`) and show an offline overlay. Resume on recovery.

---

## P1 — the newer videos (one-line endpoint change, big content change)

| | Android calls | iOS calls |
|---|---|---|
| Tai chi | `workouts/generate` | `workouts/v2/generate` |
| Walking | `workouts/walking/generate` | `workouts/walking/v2/generate` |
| Chair | — | `workouts/chair/generate` |

All five endpoints already exist in `WorkoutController.kt`. Android is simply still on v1.

**Second effect worth noting:** v2 resolves the workout language from the `Accept-Language` header
(`WorkoutRequestLanguageResolver.resolve`), while `walking/generate` v1 **hardcodes `language = "en"`**.
Android ships 11 locale folders (ar, cs, da, de, es, fr, in, it, no, pt, ro) — more than iOS — but
its workouts come back English regardless. Moving to v2 unlocks localisation Android already paid for.

---

## P1 — seated / chair tai chi (backend is already done)

Backend support is complete: `/workouts/chair/generate`, chair fields in `ExerciseProgressEntity`,
`ExerciseProgressUploadRequest`, and the exercise analytics. **Only the Android client is missing.**

Android `PracticeProgramVariant` (`PracticeModels.kt:15`) has only `TAI_CHI` and `TAI_CHI_WALKING`.
iOS `PracticeProgressStore.swift` has three variants plus a `chairMinutes` field on the day summary.

Scope, from iOS commit `06002a4`: ContentView (the tab), JourneyHomeView (+203/-137),
JourneyHomeViewModel, PracticeProgressStore (+93), WorkoutPreviewView, WorkoutPlanService,
ExerciseProgressSyncService. This is the largest single item here.

---

## P1 — progress restore across devices

iOS uses two endpoints: `progress/iOS/upload` **and** `progress/iOS/restore`
(`ExerciseProgressSyncService.swift:4-5`), called from `JourneyHomeView` on appear.

Android's `ExerciseProgressApi` has **upload only**. Android users lose all tai chi progress and
their cached plan on reinstall or a new device. iOS fixed this in `81d8765`.

---

## P2 — workout section fixes and UI

- **Exit-completion heuristic** (`923ab9b`): if ≤2 exercises remain, exiting counts the workout as
  complete instead of abandoned. Android lacks it, so Android under-reports completions — this also
  distorts retention/engagement metrics, not just UX.
- **Video orientation / aspect ratio** (`b72e7df`, `035ea0b`): iOS observes `presentationSize` and
  sizes the surface from it. Android has no `onVideoSizeChanged` handling in the workout screen.
- **Transparent-background workout video + image asset background** (`ba3febc`, `186911c`).
- **Intensity picker extracted to a sheet** (`1d81bed`, `40cee36`) — Android has intensity, but not
  the reworked picker.

## P2 — pick-tai-chi-type screen (remote config gated)

`0793675` added an onboarding screen choosing tai chi / walking / chair, gated by remote config,
with three program images. Android already has a `remoteconfig` module, so the gating mechanism
exists. Depends on chair (P1) to be fully meaningful.

## P3 — smaller

- **Config upload is much thinner on Android.** `AndroidUserConfigEntity` carries fcmToken,
  deviceLanguage, region, appVersion. `IosUserConfigEntity` also carries **timeZone, locale**,
  deviceModel, deviceOS, and accessibility text sizing (`fcb255f`). Timezone in particular drives
  notification send times — without it Android notification scheduling is guessing.
- **Printable plan PDF** (`16a6bf3`) — `PrintablePlanPDFGenerator.swift` has no Android equivalent.
- **Onboarding rating prompt** (`874528f`) — Android has in-app review
  (`InAppReviewHelper`, `RateAppDialogView`) but not the onboarding-placed prompt.

---

## Explicitly NOT gaps

- **Localisation.** Android has 11 locale folders vs iOS's recently-added DE/ES. Android is ahead;
  the real problem is the v1 endpoint forcing English workout content (see P1).
- **App rating.** Android already has Play in-app review.
- **Meta / Facebook SDK.** Android added it `8527701` (2026-08-17); now aligned with iOS `229b80a`.

---

## Implemented 2026-08-18

**1. Video/timer desync — fixed.**
`WorkoutViewModel` gained `isVideoStalled` and a single `isClockBlocked = isPaused || isVideoStalled`
gate that both the preview and exercise loops honour. `WorkoutScreen` now attaches a `Player.Listener`
to the exercise player (it had none): `onPlayerError` marks a stall immediately, and sustained
`STATE_BUFFERING` marks one after a 2s grace so normal loop-restart buffering does not pause the
workout. Instruction audio is gated on the stall so it never narrates over a frozen frame, while the
video keeps `playWhenReady` so ExoPlayer recovers on its own; errors additionally retry `prepare()`
every 3s, since ExoPlayer will not retry by itself. A "Connection lost" overlay explains the pause.

Kept deliberately separate from `isPaused` rather than reusing it the way iOS does: recovery is then
automatic, and a user who deliberately paused stays paused when the video comes back.

**2. v2 workout endpoints — done.**
`workouts/v2/generate` and `workouts/walking/v2/generate`, with an explicit `Accept-Language` header
(per-call, not an interceptor — these are the only endpoints the backend resolves a language from).
This ships the current video set and unlocks localized workout content for the 11 locales the app
already ships.

> **The v2 media set is HLS (`.m3u8`), v1 was plain MP4.** All three v2/chair backend services build
> `$EXERCISE_VIDEO_HLS_BASE_URL/<id>.m3u8`. The app only had `media3-exoplayer`, and
> `DefaultMediaSourceFactory` resolves `HlsMediaSource$Factory` **reflectively** — so this compiled
> fine and then crashed at playback with `ClassNotFoundException`. Fixed by adding
> `androidx.media3:media3-exoplayer-hls:1.5.1`. Release builds are safe without an extra ProGuard
> rule: media3-exoplayer's own consumer rules already `-keepclasseswithmembers` that factory
> (verified in the AAR's `proguard.txt`).
>
> Hardened alongside it: `setMediaItem` throws **synchronously** during composition, so this class of
> failure bypassed the stall listener entirely and killed the process. Player setup is now wrapped
> and an unplayable media item degrades to the stalled overlay.

## Video framing — measured, not guessed

Probed a real asset (`videos/hls/f0770f3e6e0f68359d7838cf`):

```
codec_name=hevc  codec_tag_string=hvc1  pix_fmt=yuv420p  width=1328  height=1552
```

**Opaque HEVC, no alpha**, and close to square (aspect 0.86). A tall phone is ~0.45, so this video
can never fill the screen without heavy cropping:
- `RESIZE_MODE_ZOOM` fills by cropping — cut off the instructor's head and feet.
- `RESIZE_MODE_FIT` preserves the frame — but letterboxes onto black.

iOS uses `.resizeAspect` (fit) **and paints a full-screen `workout_background.jpg` behind it**
(`WorkoutView.workoutBackdrop`, commit `186911c`). The backdrop is what fills the screen, not the
video — that is why iOS reads as full-screen without looking zoomed.

Android now does the same: `RESIZE_MODE_FIT` over a full-screen backdrop, ported from the iOS asset
and re-encoded to WebP at 1440px (117KB vs the original 1.6MB JPEG).

## Music (iOS parity)

Seven bundled tracks (the same `.m4a` files as iOS, +10.3MB APK), `off / shuffle / track` modes at
volume 0.35, defaulting from the `music_on_by_default` Firebase Remote Config A/B — the same key iOS
uses. One looping ExoPlayer per session that follows pause **and** the video stall, so music never
plays over a frozen workout. A music button in the workout top bar opens a picker sheet.

Shuffle resolves to a concrete track once per session rather than per play, so pausing does not
restart the music with a different track.

## Offline (iOS parity)

`WorkoutMediaCache` — a process-wide media3 `SimpleCache` (1.5GB LRU) behind a `CacheDataSource`.
Two effects, matching iOS:
- **Playback is cached automatically**, so watching a workout makes it available offline next time.
- **"Download for offline use"** on the workout preview screen pre-fetches the whole day via
  `CacheWriter`, showing "Downloading X of Y…" — the same placement and copy as iOS's Download All.

Failures skip rather than abort the batch: one unreachable video should not block the rest of the
day. `FLAG_IGNORE_CACHE_ON_ERROR` means an unwritable cache degrades to streaming instead of
breaking playback.

Music defaults to **on** (`music_on_by_default` = true in `remote_config_values.xml`). Note the
Firebase console value overrides this local default, so it must be set there too — or left unset —
for music to actually start on by default in production.

**Device-testing fixes (second pass):**
- The studio backdrop was removed and the player is back on `RESIZE_MODE_ZOOM`. Resize mode and
  backdrop are a **pair, chosen by media set**:
  - **v1 (current):** opaque, framed to fill a phone screen -> `RESIZE_MODE_ZOOM`, no backdrop.
    FIT letterboxed it into black bands that read as if the screen were inset under the system bars.
  - **v2 (when the Android rendition lands):** near-square 1328x1552 -> `RESIZE_MODE_FIT` over the
    studio backdrop, exactly as iOS does. Switch both together.
- The download button was toned down: white with a thin green outline and body-sized text rather
  than a second solid-green CTA competing with "Start Day N", and it settles into a "Ready for
  offline" check state once the day is cached.
- **That state is read from the cache, not held in memory.** `WorkoutMediaCache.areAllCached` checks
  every URL's cached byte count against its content length, so reopening a previously downloaded day
  resumes the finished state. It is keyed on the day's URL set, which scopes it per day *and* per
  practice kind automatically — day 1 of tai chi and day 1 of walking share a day number but not
  their exercises. After a prefetch the state is re-read from disk rather than assumed, because
  prefetch skips individual failures and the day may still be incomplete.
- "Download for Offline" moved out of the bottom bar — stacking it under "Start Day N" crowded the
  primary CTA. It now sits directly under the Exercises header as a full-width green button, the
  same placement iOS uses, and the header gained the "N Items" count to match.

**Not verified on device** — no Android emulator in this session. Music mixing levels and the
download progress UI both want a real run.

## Suggested order

1. **P0 video/timer desync** — small, self-contained, fixes a live bad experience.
2. **v2 endpoints** — near one-line, immediately ships the newer videos *and* localised content.
3. **Progress restore** — one new API call; stops real data loss.
4. **Exit-completion heuristic** — small, and corrects completion metrics.
5. **Chair tai chi** — the big one; backend already done.
6. Type picker, video orientation, config fields, printable plan.
