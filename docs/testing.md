# Testing

All automated tests run on the JVM with no emulator or device needed:

```bash
./gradlew testDebugUnitTest
```

The suite has 163 tests in four layers.

| Layer | What it covers | Where |
| --- | --- | --- |
| Domain | Progression analysis (double progression, stall and regression rules, deloads), personal-record detection, recovery windows, session split naming, chart scales | `domain/`, `ui/design/chart/` |
| Presentation | The pure state builders behind each screen: insight wording, training calendar and streaks, logger targets, progress ordering | `presentation/` |
| Flows | Real user journeys. Compose screens are driven by taps, long-presses and typing (Robolectric), and data flows run against a real in-memory Room database. | `flows/` |
| Screenshots | Every screen rendered with realistic sample data (Roborazzi), including empty states, small phones at 130% text, tablet width and very long names | `screenshots/` |

Screenshot tests write PNGs to `app/build/screenshots/`. The images in
[`screenshots/`](screenshots) come from these tests.

## Flow coverage

| Area | Cases |
| --- | --- |
| Logging a workout | Start, log and finish; empty sessions are discarded; an abandoned session auto-closes after 12 hours; starting twice creates one session; steppers adjust a set before it is logged; a double tap logs one set; adjusted values survive rotation; a personal record set today replaces a stale "stalling" status |
| Exercise search | Accented and emoji names are found |
| History | Long-press to delete a session, with confirmation |
| Body weight | Decimal comma input; letters and a lone "." are rejected |
| Programs | Plan a day, start it, rest days can't be planned, duplicate plans are prevented, program log entries save |
| Backup and restore | Full round trip into an empty install; restoring twice adds nothing; files without a `.json` extension; corrupt files change nothing; backups from a newer version restore in full |
| CSV export | Fields with commas and quotes, conversion to pounds, comma-decimal locales, sets in the order performed |
| Settings | Units, default rest, bar weight, export, backup and restore controls |
| Calendar | Rest-day gaps, "Yesterday", streaks and Monday-start weeks across daylight-saving changes |

## Issues found and fixed during testing

| Severity | Issue | Fix |
| --- | --- | --- |
| High | CSV export wrote kilogram values under a "Weight (lbs)" header | Values are converted to the chosen unit |
| High | On phones that use a decimal comma, CSV numbers split into extra columns | Numbers are always written with a dot |
| Medium | Restoring a backup containing an unknown equipment type stopped partway | Unknown values fall back to defaults, and the file is fully decoded before anything is written |
| Medium | A double tap on "Log set" recorded the same set twice | One log per set number; logs are serialised |
| Medium | The logger kept advising a deload after a personal record was set that day | It shows "PR today" instead |
| Medium | The backup description claimed to include everything | Corrected: workouts, body weight and exercises (programs and settings are not yet included) |
| Low | CSV export interleaved sets from different exercises | Sets are listed in the order performed |
| Low | An adjusted weight reset when the phone rotated | The value is now saved across configuration changes |

## Checks that need a device

These depend on Android system UI and still need a manual pass on a device:

- The share sheet for backup and CSV export
- The system file picker for restore
- Haptic feedback for a logged set, a personal record and the end of a rest period
- A TalkBack pass through logging a workout
- Gesture navigation and the keyboard over input fields
