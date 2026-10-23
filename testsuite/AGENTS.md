# Testsuite instructions

Applies to everything under `testsuite/`. The root `AGENTS.md` routes testsuite-only work here. Its repository-wide sections still apply; its subsystem sections do not.

Write and review changes here against [`CODING_STANDARDS.md`](CODING_STANDARDS.md) in this directory. Name the section of the rule when flagging a violation.

Do not run acceptance tests locally, including as routine validation. CI runs them through Jenkins or the PR check.

RuboCop, ShellCheck and the Cucumber dry run already enforce the mechanical rules in CI. Do not restate what they report.
