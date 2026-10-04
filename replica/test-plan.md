# Test plan: Gym Diary v2

Build: 86c3188 (+ unit-read fix)  Date: 2026-10-04  Env: JVM — Robolectric (real Compose UI, real Room in memory), seed data from `screenshots/SampleData.kt`

**How this differs from the skill's default.** replica-test assumes a web app driven by Playwright. Gym Diary is an Android app and this machine has no emulator, system image or attached device. So:
- **Automation**: Compose interaction tests and real-database tests on the JVM (`app/src/test/.../flows/`), plus screenshot renders.
- **Manual pass**: anything that needs Android itself (share sheet, file picker, haptics, TalkBack) is listed as manual and **not run**.

`auto` column: **ui** = Compose test driving taps/typing (`FlowUiTest`), **db** = real Room database (`DataFlowsTest`), **unit** = pure JVM, **shot** = rendered and inspected, **manual** = needs a device.

| case | flow | type | steps | expected | auto | result |
| --- | --- | --- | --- | --- | --- | --- |
| F01-H1 | log a workout | happy | start session, log a set, finish | session kept with an end time; no active session | db | pass |
| F01-H2 | | happy | open logger mid-exercise, tap "Log set 4" | the pre-filled 50 kg × 8 is what gets logged | ui | pass |
| F01-H3 | | happy | tap weight +, reps −, log | 52.5 kg × 7 logged | ui | pass |
| F01-E1 | | edge: finish with no sets | start, finish | session deleted, finish reports none | db | pass |
| F01-E2 | | edge: app killed mid-workout, reopened 13 h later | initialize | session with sets closed at start + 1 h; empty one deleted | db | pass |
| F01-E3 | | edge: start pressed twice | start, start | one session | db | pass |
| F01-E4 | | edge: accents and emoji | search "dév" for "Développé couché 💪" | found | ui | pass |
| F01-E5 | | edge: double tap on submit | double-tap "Log set 4" | one set logged | ui | **fail → fixed** (BUG-005) |
| F01-E6 | | edge: rotation mid-entry | weight +, rotate | 52.5 kept | ui | **fail → fixed** (BUG-006) |
| F01-E7 | | edge: stall broken today | stalled lift, PR set today | header says PR today, no deload advice | ui | **fail → fixed** (BUG-007) |
| F01-M1 | | manual | log a set, a PR, let rest run out | three distinct haptics | manual | not run |
| F02-H1 | exercise progress | happy | open Bench Press, switch 4W…All and Strength/Volume/Reps | labelled axes, units, dates, honest scale | shot | pass (S08 ×3) |
| F02-M1 | | manual | tap and drag across the chart | tooltip follows the nearest session | manual | not run |
| F03-H1 | review a session | happy | open latest session | sets, PRs, volume by muscle | shot | pass (S06) |
| F04-H1 | body weight | happy | type "72,4", tap Log | 72.4 logged; Log disabled while empty | ui | pass |
| F04-N1 | | negative | type "abc." | letters dropped, Log stays disabled | ui | pass |
| F05-H1 | programs | happy | day already planned today → Start | shows "Planned", no second "Plan today"; Start passes that schedule | ui | pass |
| F05-E1 | | edge | rest day | no "Plan today" on rest days | ui | pass |
| F05-H2 | | happy | program log: type "62,5" and "8", Save | saved as 62.5 kg × 8 | ui | pass |
| F06-H1 | backup / restore | happy | back up, restore into an empty install | sessions, notes, sets (incl. a note with quotes), weights, custom exercise all back | db | pass |
| F06-H2 | | happy | tap units, rest, bar, Export, Back up, Restore | each reports its value | ui | pass |
| F06-E1 | | edge: restore twice | restore the same file twice | nothing duplicated | db | pass |
| F06-E2 | | edge: file shared without ".json" | restore "Document from a friend" | restores | db | pass |
| F06-N1 | | negative: corrupt file | restore a truncated file | failure, nothing written | db | pass |
| F06-N2 | | negative: backup from a newer version | unknown equipment type | restores fully, unknown → Other | db | **fail → fixed** (BUG-004) |
| F06-M1 | | manual | tap Back up / Export CSV | share sheet opens with the file | manual | not run ¹ |
| F06-M2 | | manual | Restore from backup → system picker → pick file | toast "Backup restored" | manual | not run |
| CSV-H1 | export | happy | names with commas and quotes | each stays one field | unit | pass |
| CSV-E1 | | edge: pounds user | export in lbs | values converted, match header | unit | **fail → fixed** (BUG-001) |
| CSV-E2 | | edge: comma-decimal locale | export on a German-locale phone | nine columns per row | unit | **fail → fixed** (BUG-002) |
| CSV-E3 | | edge | three sets, two exercises | listed in the order performed | unit | **fail → fixed** (BUG-003) |
| F07-M1 | share a session | manual | share as text, share as image | chooser opens; image renders | manual | not run |
| F08-H1 | delete | happy | long-press a logged set → Delete | asks first, then deletes that set | ui | pass |
| F08-H2 | | happy | long-press a session → Delete | asks first, then deletes that session | ui | pass |
| X-E1–3 | all | edge: daylight saving (New York) | sessions across both 2026 changes | day gaps, "Yesterday", streak, Monday week unchanged | unit | pass |
| X-E4 | all | edge: 60-character exercise name | logger and workout | title ellipsizes; rows wrap to two lines | shot | pass |
| X-E5 | all | edge: 360dp phone, text at 130% | six screens | no clipping or orphaned units | shot | pass (after P12 fixes) |
| X-E6 | all | edge: 840dp window | home, logger | content capped and centred | shot | pass |
| X-M1 | all | manual | TalkBack through F01 | every control announced | manual | not run |
| X-M2 | all | manual | gesture nav, keyboard over inputs | nothing hidden behind bars or IME | manual | not run |

¹ Robolectric on Windows can't resolve the FileProvider cache root, so the share step can't run here. The file contents are tested through `BackupManager.backupJson()`; `res/xml/file_paths.xml` declares `cache-path "."`, which covers the cache directory on Android.

Not applicable: second user's data, expired session, slow network/offline (single-user, offline-only), payments.

## Summary

- **Cases**: 42 (counting X-E1–3 as three). All 35 automated cases pass; 7 of them passed only after the fix their failure prompted. The 7 manual cases are not run (no device).
- **Bugs**: 8 found, 0 S1, 2 S2, 4 S3, 2 S4. All 8 fixed or mitigated; BUG-008's underlying gap (programs not in backups) stays open as a follow-up.
- **Suite**: 163 JVM tests green.
