<!--
SPDX-FileCopyrightText: 2026 SUSE LLC

SPDX-License-Identifier: GPL-2.0-only
-->

# Storybook validation

From the repository root:

```sh
npm --prefix web run tsc:storybook
npm --prefix web run build-storybook
```

`tsc:storybook` checks Storybook configuration and story files, which the application TypeScript configuration excludes. The Storybook build transpiles TypeScript and produces `web/storybook-static`; it does not replace the type check.
