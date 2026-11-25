---
name: run-frontend-checks
description: Run the web/ build, Jest, ESLint and TypeScript checks with Node 22 as CI does. Use when building, testing or linting the frontend, or before finishing a web/ change.
---

# Running frontend checks

The frontend lives in `web/`. The root `package.json` scripts delegate to
`web/`, so all commands below run from the repository root. Use npm, not
Yarn.

## 1. Select Node 22

`web/package.json` requires Node 22.x and `.npmrc` sets
`engine-strict = true`, so `npm ci` fails with `EBADENGINE` on another
version. Check with `node -v` first; the default can be a newer version.
If it is not 22.x and `/usr/bin/node` is the openSUSE `nodejs-common`
wrapper (check with `command -v node22`), select the version with
`NODE_VERSION`; npm scripts inherit it:

```bash
export NODE_VERSION=22
node -v    # v22.x
```

Otherwise use Node 22 from the local version manager. Do not bypass the
check with `--engine-strict=false`. Shell state does not persist between
tool calls, so repeat the export in every call, for example
`export NODE_VERSION=22 && npm run tsc`.

## 2. Install dependencies

Same flags as CI (about 30 seconds; `web/node_modules` is git-ignored):

```bash
npm --prefix web ci --ignore-scripts --save=false
```

Network access to the npm registry works in the sandbox. Do not use
`npm install`, which can rewrite `web/package-lock.json`.

## 3. Run the checks

Run the ones relevant to the change; together they are what the
`frontend-*` workflows run on pull requests touching `web/html/src/`.
Scripts without a wrapper in the root `package.json` run with
`npm --prefix web run`.

| Check | Command | CI workflow |
|---|---|---|
| Jest tests (focused) | `npm run test -- --testPathPattern <pattern>` | `frontend-unit-tests` |
| Jest tests (all) | `npm run test -- --no-cache` | `frontend-unit-tests` |
| TypeScript | `npm run tsc` (about 15 s) | `frontend-typescript-compilation` |
| Storybook TypeScript | `npm --prefix web run tsc:storybook` | `frontend-typescript-compilation` |
| `package.json` order | `npm run sort-package -- --check` | `frontend-lint` |
| ESLint, no fixes (sorts `web/package.json` first) | `npm run lint:production` (about 20 s) | `frontend-lint` |
| Production build | `npm run build` (about 50 s) | `frontend-build` |

- **Focused tests need `--testPathPattern`.** The test script ends in
  `jest --projects html/src/jest.config.js`, so a bare pattern
  (`npm run test -- foo`) is read as a second project path and fails with
  `Can't find a root directory while resolving a config file path`. If
  `jest` in `web/package.json` is 30 or later, the option is
  `--testPathPatterns`. Use `-t <name>` to select tests by name.
- **`npm run lint` rewrites files**: it runs ESLint with `--fix` and sorts
  `web/package.json`. Use it to apply fixes, then review the result with
  `git diff`. `lint:production` reports ESLint findings without fixing,
  but first sorts `web/package.json` itself, so run
  `sort-package -- --check` **before** it (as CI does); afterwards the
  check always passes.
- `npm run build` writes the git-ignored `web/html/src/dist/`; never commit
  it. It prints many Node deprecation stack traces; only the exit code and
  the final lines matter.
- `frontend-build` installs with `--omit=dev` (about 700 instead of 1550
  packages), so a build can pass locally only because a development
  dependency is present. When dependencies or build-time imports change,
  reproduce it exactly, then restore the full install for the other
  checks:

  ```bash
  npm --prefix web ci --ignore-scripts --save=false --omit=dev
  npm run build
  npm --prefix web ci --ignore-scripts --save=false
  ```
- The Storybook build (`npm --prefix web run build-storybook`, also in
  `frontend-typescript-compilation`) is slow; run it only when changing
  stories or `web/.storybook/`.

## Interactive tools

These start long-running servers and are for developers, not routine
validation; do not run them unless asked:

- `npm run proxy -- https://<server-fqdn>`: development proxy against a
  running server.
- `npm --prefix web run storybook`: Storybook on port 6006.

## Notes

- Report honestly if a check could not run (wrong Node version, install
  failure) instead of skipping it silently.
- Local results normally match CI, which uses Node 22 and the same install
  flags; CI's result is authoritative.
