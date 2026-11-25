---
name: run-python-lint
description: Run Black and Pylint locally with python/linting/lint.sh -l. Use when linting or formatting Python, or before finishing a Python change.
---

# Running Black and Pylint on Python code

Run from the repository root with `python/linting/lint.sh`. It runs
`black -t py36` and then `pylint` with `python/linting/pylintrc`.
For `susemanager/`, use its Makefile `pylint` target instead (see
`AGENTS.md`).

## In the sandbox: always use `-l`

Without `-l` the script pulls and runs a container image, which does not work
in the sandbox. `-l` uses the locally installed `black` and `pylint`;
check first that both are available (`command -v black pylint`):

```bash
./python/linting/lint.sh -l <path> [<path> ...]   # files or directories
./python/linting/lint.sh -l -a                    # all uncommitted modified .py files
```

- Paths are relative to the repository root.
- `-a` only covers files that differ from `HEAD` (`git diff HEAD`, added,
  copied, modified, renamed). It does not see files already committed, so
  pass explicit paths after committing.
- If `black` or `pylint` is missing the script exits with code 4 or 5;
  report that instead of installing tools.
- Local tool versions can differ from the CI image. CI's result is
  authoritative; mention this when reporting.

## Black rewrites files in place

Black is not a check here: it reformats every file it is given. It also
reformats code you did not touch if the file does not follow the current
style. CI runs the same Black and fails if any reformatting would apply, so
those unrelated changes must be committed too.

**Keep formatting-only changes in their own commit**, separate from the
functional change, so the functional diff stays reviewable. Two ways:

1. **Format first (preferred).** Before editing a file, run
   `./python/linting/lint.sh -l <file>`. If it changes anything, keep that
   reformatting as a separate change (its own commit if the user asks for
   commits), then make the functional change on top.
2. **Format afterwards.** After the change, run the script. Separate the
   formatting-only hunks from the functional ones so they can become two
   commits: formatting, then the change. If the user asks for commits, do
   this for example by stashing the functional change, formatting, and
   committing the formatting first, or by staging the hunks separately;
   otherwise tell the user which hunks are formatting-only. A fixup/rebase
   on the user's side is also fine if the code was already committed.

Check the result with `git diff` before handing over: the formatting change
must contain no functional changes, and the functional change must be
already Black-clean (a second run of the script changes nothing).

## Pylint

Pylint output is a report; nothing is fixed automatically. Its exit code is
a bit mask of the message categories found (for example 20 = warning 4 +
convention 16), so a non-zero code means findings, not a tool failure. Fix
findings in code you changed; compare their line numbers with
`git diff -U0 -- <file>`. Do not fix pre-existing findings in untouched
code. Do not add `# pylint: disable` comments unless existing code in the
same file does the same for the same reason.
