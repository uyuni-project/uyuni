# Deferred: consolidate ENV_VAR_BY_HOST / BASE_CHANNEL_BY_CLIENT / PKGARCH_BY_CLIENT

While building `products-susemanager.json`, the originating task considered folding
`ENV_VAR_BY_HOST`, `BASE_CHANNEL_BY_CLIENT`, and `PKGARCH_BY_CLIENT` in
`testsuite/features/support/constants.rb` into one minion-keyed structure (all three
are keyed by the same minion tag, e.g. `'sles15sp7_minion'`, each holding one field).

**Decision: deferred, not attempted.** These hashes are used well beyond product-sync,
via the full Hash API (`.each`, `.select`, `.key?`), not just `[]` lookups:

- `ENV_VAR_BY_HOST` — 88 references across 8 files. Most (~59) are `Before(@tag)` hooks
  in `env.rb` doing `ENV.key?(ENV_VAR_BY_HOST[tag])`; also `remote_node.rb` (`.key?` for
  host validation), `command_steps.rb` (`.each` to iterate every known host when
  extracting logs, plus a certificate-serial check loop).
- `BASE_CHANNEL_BY_CLIENT` — used in `api_common.rb` (activation key creation),
  `navigation_steps.rb` (parent-channel dropdown selection), `common_steps.rb` (default
  base channel radio button), `command_steps.rb` (bootstrap repository creation), and
  `commonlib.rb` (`wait_for_reposync_running`, which does a `.select` over
  `BASE_CHANNEL_BY_CLIENT[product]` to find every minion sharing the monitoring
  server's base channel).
- `PKGARCH_BY_CLIENT` — used in `navigation_steps.rb` for arch-specific package
  row checks (install/remove tests).

Given the full Hash API is in play, "consolidate" only has a safe, zero-call-site-change
form: load one new minion-keyed JSON and rebuild these three as real Ruby `Hash` objects
from it in `constants.rb`, so every one of the 8 files keeps calling `.each`/`.select`/
`.key?`/`[]` on an object with the exact same shape as today. That would still cut three
parallel, hand-maintained tables down to one JSON source of truth for `env_var`,
`base_channel_by_flavor`, and `arch` per minion tag — worth doing, but as its own
narrowly-scoped, independently reviewable change, not bundled into the product-sync
refactor.

**Do not** attempt to replace the `.each`/`.select`/`.key?` call sites themselves with
bespoke accessor methods — that's a rewrite of working code across 8 files for zero
behavioral gain over the zero-touch Hash-rebuild approach above.
