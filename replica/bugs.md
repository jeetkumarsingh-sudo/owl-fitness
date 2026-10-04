# Bugs — Gym Diary v2

Found by replica-test on 2026-10-04. BUG-001 to BUG-007 were each reproduced by a failing test before the fix, and the tests are kept. BUG-008 comes from reading the backup format.
Device column: JVM / Robolectric (sdk 35, 412dp unless noted). No emulator or phone was available.

### BUG-001: CSV export writes kg values under a "Weight (lbs)" header

- Severity: S2
- Flow / case: Settings → Export CSV / CSV-E1
- Screen: S12
- Build: 86c3188^ (present in v1 too)  Device: JVM

Steps
1. Set units to lbs.
2. Settings → Export CSV.

Expected: 100 kg reads 220.46 under "Weight (lbs)".
Actual: 100.0 under "Weight (lbs)". Volume, est. 1RM and body weight had the same problem.
Evidence: `CsvExportTest.CSV-E1` — expected 220.5, was 100.0
Suspected cause: the unit only changed the header; values were never converted.
Status: fixed in 86c3188

### BUG-002: CSV export splits columns on phones that use a decimal comma

- Severity: S2
- Flow / case: Settings → Export CSV / CSV-E2
- Screen: S12
- Build: 86c3188^ (present in v1 too)  Device: JVM, `Locale.GERMANY`

Steps
1. Phone language German (or French, Indonesian, …).
2. Export CSV, open it in a spreadsheet.

Expected: nine columns per set.
Actual: eleven. "500,0" and "116,7" each become two fields.
Evidence: `CsvExportTest.CSV-E2` — `[…, 100.0, 5, 500, 0, 116, 7] expected 9 but was 11`
Suspected cause: `"%.1f".format(...)` uses the default locale.
Status: fixed in 86c3188 (numbers are locale-independent, up to 2 decimals)

### BUG-003: CSV export interleaves exercises within a session

- Severity: S4
- Flow / case: Export CSV / CSV-E3
- Build: 86c3188^  Device: JVM

Steps
1. Log bench set 1, bench set 2, then fly set 1.
2. Export CSV.

Expected: bench, bench, fly.
Actual: bench 1, fly 1, bench 2 (sorted by set number alone).
Evidence: `CsvExportTest.CSV-E3`
Status: fixed in 86c3188 (sorted by time performed)

### BUG-004: Restoring a backup from a newer version stops halfway

- Severity: S3
- Flow / case: F06 Restore / F06-N2
- Screen: S12
- Build: 86c3188^ (present in v1 too)  Device: JVM

Steps
1. Restore a backup whose exercise list has an equipment type this build does not know (as a future version could write).

Expected: everything restores; the unknown type falls back to Other.
Actual: "Couldn't restore: No enum constant …EquipmentType.SANDBAG". Exercises listed before the bad one were already written; sessions were not.
Evidence: `DataFlowsTest.F06-N2` (re-run against the old mapping to confirm the failure)
Status: fixed in 86c3188 (whole file decoded and mapped before the first write; unknown enums fall back)

### BUG-005: A double tap on "Log set" logs the same set twice

- Severity: S3
- Flow / case: F01 / F01-E5
- Screen: S04
- Build: 86c3188^  Device: JVM

Steps
1. In the logger, double-tap "Log set 4".

Expected: one set 4.
Actual: two set 4s with identical values. With no workout running, the two logs could each start a session.
Evidence: `FlowUiTest.F01-E5` — expected 1, was 2
Status: fixed in 86c3188 (one tap per set number on screen; logs serialised in `LoggerViewModel`)

### BUG-006: An adjusted weight resets when the phone rotates

- Severity: S4
- Flow / case: F01 / F01-E6
- Screen: S04
- Build: 86c3188^  Device: JVM

Steps
1. Tap weight + (50 → 52.5).
2. Rotate the phone (or come back after the app was reclaimed).

Expected: 52.5.
Actual: back to the 50 kg prefill.
Evidence: `FlowUiTest.F01-E6`
Status: fixed in 86c3188 (`rememberSaveable`)

### BUG-007: The logger keeps saying "Stalling" with deload advice after a PR today

- Severity: S3
- Flow / case: F01 / F01-E7
- Screen: S04
- Build: 86c3188^  Device: JVM

Steps
1. A lift stalled for 6 sessions at 47.5 kg.
2. Log 50 × 9 today (a PR).

Expected: the header reflects today.
Actual: "STALLING · Target 42.5 kg × 11–12 · Same weight for 6 sessions" above a PR row.
Evidence: `FlowUiTest.F01-E7`, `replica/clone-screens/S04_logger.png` (before/after)
Status: fixed in 86c3188 ("PR TODAY", target muted, no deload reason)

### BUG-008: "Back up" claimed to save everything; program data is not in the backup

- Severity: S3
- Flow / case: F06 / F06-H1
- Screen: S12
- Build: 86c3188^ (backup format unchanged since v1)  Device: code + JVM

Steps
1. Plan and log program sessions.
2. Back up, reinstall, restore.

Expected: as the subtitle promised, everything returns.
Actual: workouts, body weight and exercises return; program days, schedules and program session logs do not (`GymDiaryBackup` has no program fields). Settings (units, rest, bar weight) are not included either.
Evidence: `system/backup/BackupModel.kt` fields; the old subtitle "Save everything to a JSON file"
Status: copy fixed in 86c3188 ("Workouts, body weight and exercises as a JSON file"). **Open:** add programs and settings to the backup format (optional fields, so older backups still load).

## To check (not reproduced)

- Body weight "log once a day" uses `todayStart + 24 h`. On a 25-hour DST day, a weigh-in just after midnight could update yesterday's entry. Not automated, because it needs the ViewModel with a controllable clock.
- `ProgramViewModel.logScheduledSession` (start a planned day → session + pre-filled exercise logs) is not covered by an automated test; it was verified by reading the code only.
- The share sheet for Back up / Export CSV (FileProvider) and the system file picker for Restore need a device (F06-M1, F06-M2).
