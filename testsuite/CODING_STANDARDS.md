# Testsuite coding standards

Read by the reviewer, not the implementer. Scope: `testsuite/` only.

Mechanical rules live in tooling, not here: RuboCop (`testsuite/.rubocop.yml`, run `bundle exec rubocop <files>`) covers Ruby style, and `guidelines.md` and `pitfalls.md` in `documentation/` cover Gherkin naming and step conventions. This file holds the judgement calls reviewers keep raising. `#NNNN` is the uyuni PR where the point came up.

## Assertions

- Every scenario ends in a check that can fail. Navigation is not a check (#12615).
- A negative step must still fail when the thing under test is absent. A `find_all` block that never iterates passes vacuously (#12615).
- A step that waits for a state polls with `repeat_until_timeout` instead of a fixed `sleep`. Say why in a comment when polling is impossible (#12615).
- Cucumber captures are strings. Convert with `to_i` before arithmetic (#12615).

## Steps

- Search `features/step_definitions/` before adding a step. A new step that overlaps an existing one extends it or reuses it (#12615: `podman container ... should be running` already existed).
- Step names say what is observed or changed. `When` is a change, `Then` an observation, and the word "check" is noise (#12615).
- Scenario and step names match what the code does: proxy vs server, the resource actually touched (#12583).
- A new parameter on a shared helper, such as `RemoteNode`, is documented in its doc comment. Remove one that the final design no longer needs (#12654).
- Do not future-proof. Handle the repo, format or platform that exists today (#12324).

## Failures and error messages

- Raise with a message that names the node and which side holds the path: `Local file X does not exist on the controller`, `Remote file X does not exist on <host>` (#12571).
- Do not add a precondition that swallows the real error. `file_exists?` returns false when the node is unreachable, which hides the connection error (#12571).
- A workaround comment names the issue link and says what to remove once it is fixed (#12615).

## Where code runs

- Server code runs on the host or in a container. Use `run`, `extract` and `inject` and let `RemoteNode` choose. Calling `mgrctl` directly requires a `has_mgrctl` guard (#12654).
- Fix the shared helper's behaviour instead of chaining a workaround such as `mgrctl cp` then `extract` (#12654).
- A feature that needs `mgradm`, `mgrctl`, systemd units or `podman inspect uyuni-server` carries an environment tag. GitHub Actions runs a single all-in-one container with none of them (#12615).
- A feature that stops, restarts or fills the disk of the server stays out of parallel and shared run sets (`run_sets/secondary.yml`). Say so in a comment (#12615).
- A feature appears once in a run set. Do not include one that an adjacent feature already pulls in (#12583).

## Files and cleanup

- Write artifacts to a directory meant for them (`/srv`, a log directory), not into a product's data directory such as `/var/cache/rhn/repodata`. `/tmp` may be read-only on RKE2 (#12686).
- Remove temp files on every path, including failure and timeout, with `ensure` (#12686).
- Keep a comment that documents a default in step with the code. Update both together (#12634).

## Shell scripts in `features/upload_files/`

`.github/workflows/shellcheck.yml` covers quoting, `a && b || c` and unused variables. These remain for review:

- Use `command -v tool >/dev/null` to test for a tool. `which` has no `-q` (#12615).
- Create the parent directory before redirecting into a file (`mkdir -p`) (#12615).
- Guard any `rm -rf $(...)` against an empty or multi-line result (#12615).
- Query the path directly (`df --output=pcent "$DIR"`) instead of parsing `df | grep` output (#12615).

## Before pushing

- Run `bundle exec rubocop features Rakefile ext-tools`. CI runs the same command and #12615 reached review with 14 offenses.
- Run the acceptance suite only through Jenkins or the PR check, never locally.
- Grep `features/**/*.feature` for the old value whenever you remove or move a path, tag or default. The assertions live there, not in the step definitions.
