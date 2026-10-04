# Build log — Gym Diary v2

One line per screen: ID · date · status · what is missing · what was harder than expected.
Screenshots: `replica/clone-screens/` (rendered by the Roborazzi tests in `app/src/test/.../screenshots`).

| ID | Date | Status | Missing | Harder than expected |
|---|---|---|---|---|
| Shell | 2026-10-04 | done | — | Edge-to-edge + IME insets; window theme was light (white launch flash) — fixed |
| S01 Home | 2026-10-04 | done | — | Render review caught a locale day-name format, a Sunday week start, hours-vs-days and a stall rule that hid plateaus |
| S03 Picker (replaces S02 + S03) | 2026-10-04 | done | — | Library and history disagree after imports; picker lists the union |
| S13 Active workout (new) | 2026-10-04 | done | — | "Up next" needs the split the session is turning into, not only today's recommendation |
| S04 Logger | 2026-10-04 | done | Edit a logged set in place (delete + re-log works) | Pre-fill mixed today's weight with the plan's reps; a deload target showed "met" |
| S05 History | 2026-10-04 | done | — | Month timeline needs calendar-day gaps (not 24h blocks); P14 critique: a line only for breaks of 2+ days, since "1 rest day" on every other row was noise |
| S06 Summary | 2026-10-04 | done | — | e1RM PRs fired on most sessions; PR redefined as a new heaviest weight |
| S07 Progress | 2026-10-04 | done | — | Colour noise from status colours on every row; status moved to a single overline |
| S08 Exercise detail | 2026-10-04 | done | — | Date ticks ran past "now"; minimum y-span needed so a flat lift does not look like a cliff |
| S09 Body | 2026-10-04 | done | — | Daily ±0.5 kg wobble looked dramatic on a tight scale; floor of max(3 kg, 6%) |
| S10 Programs | 2026-10-04 | done | — | Up-next row showed the raw "Day 5 — UPPER VOLUME" name while the list showed "Upper Volume" — unified |
| S11 Program log | 2026-10-04 | done | — | Five outlined fields per row did not fit 412dp; replaced by a compact 5-column grid |
| S12 Settings | 2026-10-04 | done | — | CSV export lived in the 900-line WorkoutViewModel; moved to SettingsViewModel so it could be deleted |
| Cleanup | 2026-10-04 | done | — | Deleted the v1 layer: 9 ui/components files, Apex/Owl themes, WorkoutViewModel, ProgressViewModel, the unused charts library and the JitPack repo |
