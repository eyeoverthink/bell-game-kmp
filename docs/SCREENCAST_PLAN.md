# Bell Game screencast plan

A 4-minute shot list for a 3–5 minute screen recording. Record the desktop app
(`./gradlew :composeApp:run`) at 1920×1080, or a window about 1180×800. One take
with a short voiceover is enough. No music bed is required.

## 0:00–0:25 — The rule

Open on **The table**. Read the sentence under the title out loud: Alice gets
`x`, Bob gets `y`, they win when `a XOR b` equals `x AND y`. Point at the three
regime buttons: Classical, Quantum, PR box.

## 0:25–1:20 — The local wall

Stay on Classical. Show the four switches and the four deals under them. Flip
one switch and point out that the locked-in win rate changes, and that it never
says 100%. Set the table back to all zeros (75%). Press **Run** on **Live**
pace. Let the gauge sit on the brass tick. Say: any plan they write down
beforehand is stuck at three wins out of four, and a shared coin flip does not
help, because it only mixes those plans.

## 1:20–2:30 — The qubit bench

Switch to **Quantum** and press **Run** for a few seconds so the dot climbs
past 75%. Then open **Bench**. Drag Alice's second angle off 90° and watch the
analytic win rate drop. Hit **Tsirelson** to snap back to 0°, 90°, 45°, −45°.
Read the CHSH value, about 2.828, and the line "Sitting on Tsirelson's bound."
Press **Sample 2,000** and let the chart settle on the ion line. Mention the
aligned preset: everyone measures Z, the outputs always agree, and the score
falls back to 75%.

## 2:30–3:30 — The perfect box and the leak

Open **Causality**. Select **PR box**, depth 3 (8 bits). Point at the meter:
the white mark is the one bit Alice is allowed to send, and the bar is far
past it. Press **Deal one hand** and show Bob naming the bit he pointed at.
Press **Run 400 hands** and note the sampled success near 100%. Switch the
resource to **Quantum** without changing the depth. The meter drops under the
white mark. Say: the same nesting, fed with the qubit correlation instead of
the perfect box, stays inside the one-bit budget. Classical does too.

## 3:30–4:05 — The ledger and the close

Open **Ledger** and show the table score and the bench sample against the same
three lines. End on **Story**, holding on the last paragraph: nature, as far
as we can play it, stops at the qubit line.

## Capture notes

- Pause the table before changing screens so the numbers are readable.
- If the window is narrow, the navigation moves to the bottom. Record wide.
- Optional cold start with a filled quantum score: `BELL_PRELOAD=1 ./gradlew :composeApp:run`.
- Optional deep link on the web build: open `index.html#causality`.
