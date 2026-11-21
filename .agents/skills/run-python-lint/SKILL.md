---
name: run-python-lint
description: Run Black and Pylint via python/linting/lint.sh locally (no container), committing Black reformatting of untouched code separately. Use when linting or formatting Python, or before finishing a Python change.
---

# Running Black and Pylint on Python code

Run from the repository root with `python/linting/lint.sh`. It runs
`black -t py36` and then `pylint` with `python/linting/pylintrc`.

## In the sandbox: always use `-l`

Without `-l` the script pulls and runs a container image, which does not work
in the sandbox. `-l` uses the locally installed `black` and `pylint`:

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
   `./python/linting/lint.sh -l <file>`. If it changes anything, hand the
   user a commit for just that reformatting, then make the functional change
   on top.
2. **Format afterwards.** After the change, run the script. Separate the
   formatting-only hunks from the functional ones (for example by
   stashing/reverting the functional change, formatting, and committing the
   formatting first, or by staging the hunks separately) and give the user
   two commits: formatting, then the change. A fixup/rebase on the user's
   side is also fine if the code was already committed.

Check the result with `git diff` before handing over: the formatting commit
must contain no functional changes, and the functional commit must be
already Black-clean (a second run of the script changes nothing).

## Pylint

Pylint output is a report; nothing is fixed automatically. A non-zero exit
code (for example 20) means Pylint reported findings. Fix findings in code you changed. Do not fix
pre-existing findings in untouched code. Do not add `# pylint: disable`
comments unless existing code in the same file does the same for the same
reason.

## Committing

Whether to prepare commits is up to the user. Only provide commit commands
if the user asks for them; then follow the commit instructions from the
global configuration, with the formatting commit first.
