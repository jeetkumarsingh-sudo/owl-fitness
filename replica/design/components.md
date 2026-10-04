# Gym Diary v2 — components

Code: `app/src/main/java/com/example/gymdiary3/ui/design/`. Tokens: `tokens.json`
(0 AA failures, `contrast.py`). Gallery screenshot: `DesignGalleryTest`.

Principle for every component: **remove before adding.** A component earns a
container only if the container carries meaning (the one primary action, an
input, a chart's plot area). Everything else is type, space and hairlines.

```
ScreenHeader      tab-root title (title 28/700) + optional subtitle + trailing slot
  tokens          GdType.title, Gd.Text, subtitle GdType.label/TextMuted
  a11y            title is the first heading read
  used on         Home, History, Progress, Body

DetailTopBar      pushed screens: back · title (section 17/600) · actions
  states          long title ellipsizes
  a11y            back button labelled "Back"

SectionHeader     sentence-case section title + optional quiet link ("View all ›")
  rule            no caps, no colour; grouping comes from the 32dp above it

ListRow           the replacement for "one card per item"
  slots           overline (status, semantic colour) · title · subtitle · leading · trailing
  states          default, pressed (ripple), long-press (delete)
  tokens          min height 56, gutter 20, Hairline between rows
  used on         every list

Hairline          1px Gd.Border, inset to the text column

InlineStats       "15 workouts · 63,922 kg · 6 PRs" — figures that don't deserve tiles
Metric/MetricRow  value (metric 22/600 tabular) over a muted label, optional delta line
                  in a semantic colour. No container.

StatusLabel       uppercase 11/600 overline in ONE semantic colour
  mapping         Progressing=Positive  Stable=Info  Stalling=Warning  Regressing=Danger

PrimaryButton     one per screen. Solid Gd.Accent, radius 10, height 52, no gradient
  states          enabled, pressed (ripple), disabled (SurfaceRaised / TextFaint)
SecondaryButton   1dp BorderStrong outline, Text label; compact = 40dp
TextAction        text only; muted by default, AccentText for a primary text action

SegmentedControl  range selector (4W 8W 3M 6M 1Y All / 7D 30D …)
  motion          selection pill slides (200ms, reduced motion = instant)
  a11y            each option Role.Tab + selected state

UnderlineTabs     Strength / Volume / Reps. 2dp accent underline slides to selection

WeekStrip         S M T W T F S over dots: done = filled, today = muted ring,
                  future = faint ring. A day fills with a 300ms scale-in when done.
  a11y            "3 of 7 days trained this week"

AnimatedNumber    tweens only when the value changes; never counts up on screen entry

TimeSeriesChart   line or bars on a real time axis
  must have       y-axis title with unit · tick labels · date ticks · straight segments
                  between real points · bars from zero · minimum y span (no inflated
                  trends) · tap/drag to inspect (tooltip: date + detail lines) ·
                  selected point/bar in accent · draw-in on range change
  empty           one muted line, e.g. "No sessions in this range"
  a11y            summary contentDescription, e.g. "Estimated 1RM, 8 sessions, 18→21 kg"

EmptyMessage      left-aligned title + one line + optional action
```

Removed from v1: ApexPanel (rounded card everywhere), ApexCta (gradient pill),
ApexBackground (animated glow), PrCelebration (confetti), `appear()` stagger,
SectionLabel caps style, ApexChip.

Motion inventory (P11 audit, 2026-10-04)
  rule            every animation explains a change of state; none loops or plays on idle.
                  Each one reads LocalReducedMotion (instant or fade-only), and the system
                  "remove animations" setting also zeroes Compose durations.
  tokens          Fast 120 · Base 200 · Slow 300 · Chart 450 · Highlight 1600 · Ease (0.2,0,0,1)
  navigation      tabs cross-fade (Base); drill-downs fade + 1/12-width nudge (Slow)
  selection       SegmentedControl pill and UnderlineTabs underline slide (Base)
  lists           itemMotion(): row insert fade (Base), reflow (Base), remove fade (Fast)
  data            chart draw-in (Chart); recovery / balance / muscle bars fill (Chart);
                  AnimatedNumber tweens a changed value (Slow), never counts up on entry
  logging         logged-set check scales in (Base); a PR row's accent wash fades (Highlight);
                  rest-timer bar slides in from the bottom (fade only when reduced) and
                  drains linearly, one step per second; week-strip day fills (Slow)
  haptics         set logged = Confirm, PR = LongPress, rest finished = LongPress
  removed in v2   ambient background, confetti, staggered list entrances, count-up stats
