---
name: write-changelog
description: Write and validate a package changelog fragment (<package>.changes.<author>.<topic>) for a change, using the same checker as the GitHub "Changelogs" workflow. Use when a change needs a changelog entry or when asked to add or check a changelog.
---

# Writing changelog entries

## Is a changelog needed?

Only for changes that are interesting for users of the software: new
features, behaviour changes, bug fixes, removed or changed
options/APIs. **Internal changes need no changelog**: refactorings,
tests, build/CI changes, code cleanups, developer-only documentation,
performance work without visible effect. If none of the changed files
needs an entry, say so instead of writing one.

The CI check is skipped when the PR description contains
`[x] No changelog needed`; mention that option for internal changes.

## Which file

Never edit the consolidated `<package>.changes` file (CI fails if a
`*.changes` file is modified). Add a fragment next to it:

```
<package>.changes.<author>.<topic>
```

- `<package>` is the package owning the changed files. Packages and their
  base paths are listed in `rel-eng/packages/<package>` (for example
  `spacewalk-java` -> `java/`). The fragment goes into that base directory.
- `<author>` is the author's short name; look at existing fragments in the
  directory for the convention, or ask.
- `<topic>` is a short kebab-case description, e.g. `clm-hotfix-target-flag`.
- A change touching several packages needs a fragment in each package.
- Every package with changed files needs a fragment (otherwise
  "Changelog not added"), and a fragment without other changes in its
  package is rejected ("Changelog added without changes").

## Format

```
- Add a hotfix flag to software targets of content lifecycle
  environments
- Create missing hotfix channels on startup
```

- Each entry starts with `- `; continuation lines are indented with exactly
  two spaces.
- **Maximum 67 characters per line**, including the `- ` and the two-space
  indent.
- Start with a capital letter (also after a sentence-ending dot).
- Space after `.` `,` `;` `:` (no `a,b`), no double spaces, no trailing
  whitespace, no empty lines, newline at the end of the file.
- No duplicate entries in a file.
- Keep entries short and user-oriented: say what changed, not which classes
  or functions. One entry per visible change.
- Trackers are written as in OBS (`bsc#1234567`, `jsc#SUMA-123`, `gh#...`).
  If used, they must also appear in the PR title or a commit message and
  vice versa; `bsc#` bugs must exist in Bugzilla (SUSE Manager product).

## Quoting words that contain dots

The checker flags any `.` (or `,;:`) directly followed by a non-space as
"Wrong spacing". Wrap file names, configuration keys and other dotted
names in backticks (or quotes) so they are ignored; quoted text is skipped
for the capitalization and spacing checks:

```
- Fix reading of `server.satellite.rhn_parent` from `rhn.conf`
```

Only a dot that ends a sentence stays unquoted. Version numbers such as
`5.1.2` are accepted unquoted.

## Validate locally

The workflow runs `.github/workflows/changelogs/changelogs.py` with the
changed files as arguments. It needs the `python-bugzilla` module. In the
sandbox, install it into a throwaway venv outside the repository:

```bash
python3 -m venv /tmp/chlogvenv && /tmp/chlogvenv/bin/pip -q install 'python-bugzilla~=3.2.0'
```

Then, from the repository root, pass all changed files of the branch,
committed or not, including untracked files such as new fragments:

```bash
base=$(git merge-base <target-branch> HEAD)
/tmp/chlogvenv/bin/python -I .github/workflows/changelogs/changelogs.py \
    $(git diff --name-only "$base") $(git ls-files --others --exclude-standard)
```

`<target-branch>` is the branch the PR goes to (for example
`origin/master`). `git diff "$base"` compares the branch point with the
working tree, so it covers commits, staged and unstaged edits, but not
commits that landed on the target branch later. Check that the untracked
list contains only files that belong to the change. The checker needs the
other changed files to know which packages are affected; passing only the
fragment fails with "Changelog added without changes". Without `--pr-number` and `--tracker-file`,
PR/tracker/Bugzilla checks are skipped. Success prints
`Changelog test passed`; otherwise each problem is listed with file and
line.

## Committing

Whether to prepare commits is up to the user. Only provide commit commands
if the user asks for them; then follow the commit instructions from the
global configuration.
