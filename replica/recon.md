# Gym Diary — recon map (redesign v2)

Self-redesign of our own app: sourced from the code and the running app, not
public pages. `features.csv` is the parity checklist: `original` = v1 (Apex
rollout, commit 80d50a2), `clone` = v2. Rows v2 removes on purpose are
`removed` with a reason, so a deliberate removal is not scored as a gap.

## Scope

- Platform: Android, Kotlin + Jetpack Compose (Material3), Hilt, Room v11, DataStore.
- Slice: the whole app. Core loop = **start workout → log sets → finish → see progress.**
- For: a lifter tracking their own training. Single user, offline, no account.
- Constraint: **no database schema change.** All v2 features derive from existing tables.

## Screen inventory

| ID | Screen | Route | Purpose | States |
|---|---|---|---|---|
| S01 | Home | `home` (tab) | streak hero, start button, lifetime stats, weight, last workout, insights peek | idle, live session, empty |
| S02 | Muscle picker | `muscle` | 7-tile grid to choose a muscle | filled |
| S03 | Exercise list | `exercise/{muscle}` | exercises for a muscle, add custom, long-press delete | empty, filled, add dialog |
| S04 | Set logger | `set/{muscle}/{exercise}` | log one set at a time for one exercise | first set, mid-session, rest timer, plates shown |
| S05 | History | `history` (tab) | flat list of sessions | empty, filled, delete dialog |
| S06 | Session summary | `summary/{id}` | stats, per-exercise sets, PR badges, volume by muscle, share | PR, no PR, empty session |
| S07 | Progress | `progress` (tab) | up to 5 insight cards, weekly volume, one card per exercise | empty, filled |
| S08 | Exercise analytics | `analytics/{exercise}` | best 1RM, total volume, recommendation, two unlabeled charts | no data, <2 points, filled |
| S09 | Body weight | `weight` (tab) | log weight, 3 stats, unlabeled chart, swipe-to-delete history | <2 entries, filled |
| S10 | Program tracker | `program_tracker` | week calendar, program days | empty, scheduled, done |
| S11 | Program session log | `program_log/{id}` | 5×(kg, reps) grid per exercise | filled |
| S12 | Settings | `settings` | units, rest default, bar weight, export/backup/restore | — |
| S13 | Active workout (added in v2) | `workout` | exercises in the live session, up next, finish | just started, mid-session |

## User flows (happy-path taps)

```
F01 Log a workout (the core loop)
    S01 Start → dialog "Start now" → (back to S01) Resume → S02 muscle → S03 exercise → S04 log set ×N
    → back → back → S02 next muscle → S03 → S04 … → back to S01 → Finish → S05
    taps to first logged set: 6. Every new exercise: +2 screens (muscle, then exercise).
    edge: no exercises for muscle, stale session >12h auto-closes, empty session deleted on finish
F02 Check an exercise's progress   S07 → scroll to card → S08            (3 taps, no time range)
F03 Review a past session          S05 → S06                              (2 taps)
F04 Log body weight                S09 type → Log                         (2 taps)
F05 Follow a program               S01 "Follow a program" → S10 → day → S11 → Finish → S06
F06 Restore a backup               S01 gear → S12 → Restore → file picker
F07 Share a session                S06 → share text | share image
F08 Delete a session / exercise / weight   long-press or swipe
```

**Number to beat:** F01 = 6 taps to the first set and 2 extra screens per exercise.
Target for v2: 3 taps to the first set, 1 screen to add an exercise.

## Components (v1)

ApexPanel (rounded 24dp card, hairline border, top highlight, press-scale) is
used for nearly every block — that is the "everything is a card" problem.
ApexCta (gradient pill), SectionLabel (ALL CAPS tracked), CountUpText,
WeekActivityBars, ApexChip, ShimmerBox, ApexLineChart (no axes), ApexBars,
PrCelebration (90-particle confetti), ApexScaffold, ApexBackground (animated
radial glow), `appear()` stagger.

## Data model (existing — no changes in v2)

```
WorkoutSession  id, startTime, endTime?, name?, notes?            (Room)
WorkoutSet      id, timestamp, muscle, exercise, setNumber, reps, weight(kg),
                isAssisted, sessionId?, rpe?, notes?               (Room)
Exercise        name, primaryMuscleGroup, equipment, movementPattern, trackingType, isCustom
BodyWeight      id, timestamp, weight(kg)
ProgramDay / ProgramExercise / SessionSchedule / SessionExerciseLog
UserSettings    weightUnit, defaultRestSeconds, barWeight          (DataStore)
FitnessInsight  type, message, exerciseName?, severity, dataPoints  (derived, not stored)
```

Everything v2 adds is derived: progression status per exercise, per-muscle
recovery, session split labels (push / pull / legs), chart series by range.

## Audit — what is wrong (phase 1) and what goes (phase 2)

Grouped by the user's critique. "Remove" = deleted outright, not restyled.

**Too many cards / equal visual weight**
- S01 uses 7 separate panels; every number sits in its own rounded box. → One page, three groups separated by space and a hairline, not containers.
- S07 stacks up to 5 bordered insight cards plus one large card per exercise. → Rows.
- S06 wraps every exercise in a 24dp panel. → Rows with dividers.

**Too rounded / decorated**
- 24dp radii, gradient pill CTA, gradient-border "accent edge", top highlight on every panel. → 8–12dp radii, solid button, plain surfaces.
- ApexBackground animates a radial glow forever. → **Remove.** Flat near-black.
- PrCelebration: 90 confetti particles. → **Remove.** Replace with an inline "New PR" row that settles in once, plus a haptic.
- `appear()` stagger on every list. → **Remove.** Lists animate only on insert/remove.

**Too much text / shouting**
- ALL CAPS tracked labels on every heading (SectionLabel). → Sentence case section titles; caps only for tiny meta labels.
- Insight messages are full sentences ("…has stalled at 50kg for 3 sessions. Consider adding reps, tempo, or a technique variation."). → Render from `dataPoints`: label / exercise / state / action.
- Expanded display face on every headline. → Reserve the expanded face for the one big number per screen.

**Accent overuse**
- Crimson on section labels, exercise names, times, "VIEW SUMMARY", muscle chips, chart bars. → Crimson only for the primary action, active state, PRs, selected chart point.

**Charts that teach nothing (S08, S09)**
- No axes, no units, no dates, no time range, index-spaced x (uneven dates look even), y-range stretched to min..max (exaggerates small changes). → Date-scaled x with ticks, labeled y with units, honest baseline, range selector, tap-to-inspect.

**Workflow**
- F01 makes the user navigate muscle → exercise for every exercise. → An active-workout screen that lists today's exercises, plus one combined exercise picker (recent first, muscle filter).
- S04 shows only "SET n" and the last session; it never shows the sets already done today. → Set table: today's sets with ✓, the previous session alongside, the next set pre-filled with a suggested target.

**Information architecture**
- Tabs: Home / History / Progress / Weight. Weight alone is thin for a tab. → Home / History / Progress / **Body** (weight + muscle recovery).
- Home tries to show everything. → Home answers: what now, how am I doing, one thing to know.

**Removed outright in v2:** ApexBackground, PrCelebration confetti, `appear()`, ApexCta gradient, SectionLabel caps style, Home lifetime-stats tiles, Home body-weight tile, Home "intelligence" stack, S07 per-exercise big cards (replaced by rows), S08 recommendation paragraph, S02 as a separate screen (merged into the picker).

## Size

12 screens → 12 (2 merged, 1 added: active workout), 8 flows, no new entities.
Hard parts: honest date-scaled charts with tap inspection; progression and
recovery logic that is right on real, messy history; the workout screen
being faster than v1 without losing any v1 logging feature.
Size: M.
