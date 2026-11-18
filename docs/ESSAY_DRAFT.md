# Essay draft

> **Draft for Vaughn Scott to edit.** This is not the essay to paste into the contest form.
> Rewrite it in your own voice. The form asks for about 300 words on your background, the idea, and the technologies you used. Cut or expand until it sounds like you. Do not submit this file unchanged.

I spent years in sales, learning how to hear what someone actually wanted and how to explain a complicated thing without talking down. I am a student at San Francisco State University now, and the work I care about is building systems: programs that make a rule visible, so you can push on it and see where it holds.

I test theories by implementing them. A claim that has to survive a seed, an exhaustive search, and a chart that settles in public is a different object. Bell Game came out of that habit. I had been running small quantum-information experiments, asking why a shared entangled pair beats every local plan at the CHSH game, and why a perfect Popescu-Rohrlich box, which wins every round, is still not a usable theory of nature. The app is that question with a table you can sit at.

Alice and Bob each receive a random bit and each return a bit. They win when the parity of their answers matches the AND of the inputs. Agree on a plan in advance and the best you can do is three wins out of four. Share one entangled pair and measure at the right angles, and the win rate climbs to about 85.36 percent, then stops at Tsirelson's bound. A PR box hits 100 percent. The last screen shows the cost: one classical bit from Alice lets Bob pull more information out of her data than she sent. Quantum measurements do not do that. Classical plans do not either.

The project is Kotlin Multiplatform with Compose. The rules, the two-qubit state, the strategies, the statistics, and the information-causality protocol live in common code, and so does the interface. Android, the desktop, and the browser are thin shells. No accounts, no keys, no network. I wanted the physics to be the product, and the same product on every screen.

---

Word count of the essay body above, not counting this note: about 310 words. Trim it to the form's limit before you submit.
