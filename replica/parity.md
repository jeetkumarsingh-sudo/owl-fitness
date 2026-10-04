# Parity — Gym Diary v2 against v1

Date: 2026-10-04. "The original" here is Gym Diary v1, the same app before this redesign. The question is: can a user still do everything they could, and is each flow at least as good?

## Verdict: shippable

- All 34 must-haves done. Feature score **99.7** (97.7 if the three deliberately removed motion effects count as missing).
- No open S1 or S2 bugs (`bugs.md`: 8 found, all fixed or mitigated).
- 163 JVM tests green.
- **The caveat that matters**: no device was available. The 7 device-only checks in `test-plan.md` (share sheet, file picker, haptics, TalkBack, system insets) have **not** been run. Until they are, "shippable" means shippable on the evidence a JVM can give.

Not "better than the original" by the skill's definition, which requires replica-entrepreneur's fixes from user reviews. That step wasn't run; the improvement agenda came from your own brief.

## Feature parity

| | |
| --- | --- |
| Score | 99.7 / 100 (60 features counted) |
| Must-haves | 34 of 34 |
| Weakest area | home 96.4: lifetime volume dropped (workout count kept as the History header) |
| Removed on purpose, not scored | ambient animated background, confetti, staggered list entrances (the brief rules out decorative motion) |
| Added in v2, not scored | 20 features: progression status, labelled charts with ranges, tap-to-inspect, recovery + today's recommendation, month timeline, PR list, active workout screen, rest ±15 s, tablet width cap, … |

Full machine output: `python parity.py replica/features.csv --markdown`.

## Layout diff: not measured

The skill's layout score compares the clone's screenshots with the original's at the same viewport. Two reasons it isn't run:
- **No captures**: v1 was mapped from its code, so no v1 screenshots exist.
- **Wrong question**: the brief asked for a new layout (fewer cards, less text, a different information architecture). A high layout match with v1 would measure failure.

The clone's renders are in `replica/clone-screens/`, named by stable recon ID: S01–S13, plus the R_* responsive and edge renders.

## Behaviour diff

| flow | v1 does | v2 does | keep / fix |
| --- | --- | --- | --- |
| F01 first logged set | 6 taps: Start → "Start now" → Resume → muscle → exercise → Log | 3 taps via Up next (Start → exercise → Log), 4 via the picker | keep |
| F01 each further exercise | +2 screens (muscle, then exercise) | 1 tap on an Up next row, or 1 picker screen | keep |
| F01 what to lift | a weight-only bump | double progression within the lift's rep range; deload or "back to" when stalled/regressing; "PR today" once beaten | keep |
| F01 double tap on Log | logged twice | logs once | fixed (BUG-005) |
| F02 exercise progress | Progress → scroll to that lift's card → analytics; unlabelled charts, no range | Progress → lift row (1 tap); axes, units, dates, 4W–All, Strength/Volume/Reps, tap a point | keep |
| F03 review a session | History → session (2 taps) | same | keep |
| F04 delete a weigh-in | swipe | long-press, then confirm (same gesture as sets and sessions) | keep. Note: less discoverable than swipe |
| F05 programs | tracker → day → log | same, plus an Up next row with Start; rest days can't be planned; no duplicate plans | keep |
| F06 restore | aborted halfway on an unknown value | decodes first, never writes partially, unknown values fall back | fixed (BUG-004) |
| F06 export CSV | kg values under a lbs header; decimal comma split columns | values in your unit, locale-safe, sets in order | fixed (BUG-001–003) |
| F08 delete set / session | long-press | long-press, then confirm | keep |

## Final visual critique (P14)

Checked every screen for AI-dashboard patterns, excess containers, accent misuse and redundant text.

**Already clean**: no gradients, glows, emoji, sparkle icons, stacked insight cards or count-up numbers. Home has exactly one container (Today). Accent appears only on the primary action, the active tab/underline, PRs, the running rest timer, and a selected chart point.

**Removed in this pass**
- "1 rest day" between almost every History session. A one-day rest is the rhythm; only breaks of 2+ days get a line now.
- The Settings footer ("Gym Diary") and the "Save everything" claim on Back up, which was untrue (programs aren't backed up).
- "All insights" (dashboard jargon) → "Progress", where the link actually goes.

**Corrected**
- One wording for next actions on every screen: "Next: 50 kg × 8", "Deload: 42.5 kg × 11–12", "Back to 90 kg × 5". The workout screen had its own unit-less variant that contradicted Home.
- "heaviest 47.5 kg × 6" next to "last session 47.5 × 9" looked like an error. It now shows the best set at the heaviest weight.
- The logger said "Stalling, deload" above a PR set today; it now says PR today.

## Next five things to build

1. **Run the APK on a phone** and do the 7 manual cases (`test-plan.md`, all "not run"). This is the largest remaining risk.
2. **Programs and settings in backups** (BUG-008 follow-up): optional fields, so older backups still load.
3. **Edit a logged set in place.** Delete and re-log works today, but it's two steps for a typo (build-log S04).
4. **Automated test for "start a planned day"** (`ProgramViewModel.logScheduledSession`), currently verified by reading only.
5. **Body weight "once a day" on a 25-hour DST day** (to-check list): inject the clock into `BodyViewModel` and test it.
