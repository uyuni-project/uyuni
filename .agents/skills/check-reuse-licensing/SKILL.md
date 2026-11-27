---
name: check-reuse-licensing
description: Add SPDX headers or REUSE.toml annotations to new files and run reuse lint. Use when adding files or for license headers.
---

# Copyright and license information (REUSE)

CI runs `reuse lint` (`.github/workflows/reuse.yml`) on the whole
repository and fails if any file lacks a copyright notice or license. Only
new files need attention: **do not add or rewrite headers in files you only
edit.**

## New files that allow comments

Add this header at the top, below any shebang, in the file's own comment
syntax (for example `#` in `tito.props`, not `<!-- -->`):

```text
SPDX-FileCopyrightText: <year the file is created> SUSE LLC

SPDX-License-Identifier: <license>
```

- Copy the license from neighbouring files; it differs per component:

  ```bash
  grep -h -m1 SPDX-License-Identifier <dir>/* 2>/dev/null | sort | uniq -c
  ```

- The license must have a file in `LICENSES/`
  (`ls LICENSES/<license>.txt`).
- A classic copyright block with an `SPDX-License-Identifier` line is
  sufficient.

## New files that cannot hold a header

Files that cannot hold comments (JSON, binaries, fixtures, generated
files), files whose format requires specific first lines (for example
`SKILL.md` with its YAML front matter), and changelog fragments get no
header. Make sure a `[[annotations]]` glob in `REUSE.toml` covers them,
extending an existing entry with the same license and holder rather than
adding a new one. Do not add `.license` sidecar files.

Third-party files keep their upstream copyright and license, recorded in
`REUSE.toml`.

## Check

`reuse` is usually installed in the sandbox; check with
`command -v reuse`. If it is missing, report that the check was left to CI
instead of installing it. Check single files quickly:

```bash
reuse lint-file <file> [<file> ...]
```

No output and exit code 0 means compliant; otherwise it prints
`no license identifier` / `no copyright notice` per file. Before finishing,
run the full check as CI does (about 15 seconds):

```bash
reuse lint
```

Do not truncate the output with `head` or `tail`: the problem files are
listed first and the result last, and a pipe hides the exit code. Success
(exit code 0) ends with `Congratulations! Your project is compliant`. Failures
list the files under `MISSING COPYRIGHT AND LICENSING INFORMATION`.
