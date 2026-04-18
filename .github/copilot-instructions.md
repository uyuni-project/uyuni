# Copilot Instructions for Uyuni

These instructions orient GitHub Copilot (coding agent and code review) to the Uyuni
repository. Trust the guidance here first; only fall back to broad exploration if
something you need is missing or proves inaccurate.

Companion file: [`AGENTS.md`](../AGENTS.md) in the repo root contains the same
architecture and stack conventions and must be kept consistent with this file.

## 1. Repository overview

Uyuni is an open source systems management platform (upstream of SUSE Multi-Linux
Manager, forked from Spacewalk). It is a large polyglot monorepo (~several GB, tens
of thousands of files) with these major top-level areas:

| Path | Stack | Purpose |
| --- | --- | --- |
| `java/` | Java 17, Ant + Ivy, Maven (for some modules), Hibernate 7.x, Struts 1.2.9 (legacy), SparkJava (modern) | Main server: `core/`, `webapp/`, `doclets/`. XML-RPC APIs, legacy JSP/Struts UI, modern HTTP APIs. |
| `web/` | Node 22.x, npm ≥10, React + TypeScript, webpack, Jest, Formik, `node-gettext` | Modern web UI under `web/html/src/`. Incrementally replacing legacy JSP/JS. |
| `python/` | Python 3, pytest, black, pylint | Backend libs: `spacewalk/`, `uyuni/`, `rhn/`, `uyuni-cobbler-helper/`. Salt, Cobbler, XML-RPC handlers. |
| `schema/` | PostgreSQL SQL | DB schema and migration logic. PostgreSQL is the only supported RDBMS. |
| `microservices/` | Java 17, Maven | Modern standalone services (e.g. `coco-attestation`, `uyuni-java-common`, `uyuni-java-parent`). |
| `search-server/` | Java | Search microservice. |
| `containers/`, `proxy/`, `susemanager/` | Dockerfiles, Helm charts, shell | Packaging, containers, proxy, server platform bits (Podman-native locally). |
| `testsuite/` | Ruby 3.3, Cucumber, RuboCop | Acceptance/e2e suite. **Not product code.** |
| `spacecmd/`, `spacewalk/`, `client/`, `utils/`, `reporting/`, `projects/`, `susemanager-utils/`, `susemanager-sync-data/`, `susemanager-branding-oss/`, `branding/` | Mixed | Supporting packages, clients, tooling, translations, branding. |
| `rel-eng/`, `tito.props` | Release engineering | RPM/tito release tooling. Owned by release engineering. |
| `documentation/` | Asciidoc-adjacent | Developer docs. User docs live in the separate `uyuni-docs` repo. |
| `.devcontainer/` | JSON, shell | Devcontainer configs (`dev`, `test`). |
| `.github/` | YAML, Markdown | Workflows, templates, CODEOWNERS, this file. |

Key root files: `README.md`, `CONTRIBUTING.md`, `AGENTS.md`, `SECURITY.md`,
`package.json` (root is minimal; the real frontend package lives in `web/`),
`sonar-project.properties`, `.rubocop.yml`.

## 2. Ground rules (must-follow)

1. **Never modify `.github/workflows/**` or other `.github/` workflow/config files
   unless the user explicitly asks.** CI is owned by the project.
2. **Do not treat changes limited to `testsuite/` or `.github/` as "product code"
   work.** Product code lives elsewhere.
3. **Respect stack boundaries.** Do not mix legacy Struts logic with modern
   SparkJava/React endpoints. Avoid introducing new Struts 1.x controllers/actions;
   prefer SparkJava or a microservice for new server endpoints.
4. **Do not introduce new frameworks or major dependencies** when an existing
   abstraction already covers the use case. Check `web/package.json`,
   `java/buildconf/ivy/`, `microservices/*/pom.xml`, and Python `requirements*`
   first.
5. **Keep changes minimal and localized** to the owning subsystem (see
   `.github/CODEOWNERS`). Touching many unrelated files is a red flag.
6. **No fixup commits** in PRs — the `git-checks` workflow blocks fixup-commit
   merges. Use proper commit messages (see `CONTRIBUTING.md`).
7. **Changelogs are required** for user-visible product changes in most top-level
   directories. See §6 below. Do not edit `*.changes` master files directly.
8. **PostgreSQL only.** Never add code paths, drivers, or SQL for other RDBMSes.

## 3. Build, lint, test — what to run and when

The project is too large to build end-to-end in the agent environment. Match your
local validation to the files you change. Each CI workflow in
`.github/workflows/` defines the authoritative command; the table below summarizes
the most common ones.

### 3.1 Frontend (`web/`)

All npm scripts assume `--prefix web` from the repo root, or run from inside `web/`.

- **Install (first time):** `npm --prefix web ci --ignore-scripts --save=false`
  (this is exactly what CI does; `--ignore-scripts` avoids heavyweight postinstall
  steps). Use a cached `~/.npm` if available.
- **Lint:** `npm --prefix web run lint:production`
  - Auto-fix locally with `npm --prefix web run lint` (adds fixes + re-sorts
    `package.json`).
  - Also validates `package.json` sort order. If the lint failure is about sort
    order, run `npm --prefix web run sort-package`.
- **TypeScript type-check:** `npm --prefix web run tsc`
- **Unit tests (Jest):** `BABEL_ENV=test npm --prefix web run test -- --no-cache`
- **Build (webpack):** `npm --prefix web run build` (only needed when verifying
  build breakage; not required for lint/test validation).
- Conventions (`web/eslint.config.js`, `web/.prettierrc`): semicolons required,
  print width 120, `simple-import-sort` for imports, use the path aliases
  `components`, `core`, `manager`, `utils`. Do not edit generated `dist/` assets.
- Strict React Hooks rules. Use `Formik` for forms and `node-gettext` (`t(...)`)
  for all user-facing strings — no ad-hoc i18n.

### 3.2 Java main server (`java/`)

The Java stack uses **Ant + Ivy**, not Maven. It expects a build container
(`registry.opensuse.org/systemsmanagement/uyuni/master/docker/containers/uyuni-master-pgsql:latest`),
which is **not** available in the cloud agent environment. Full Java compilation
and unit tests are therefore generally **not runnable locally in the agent**;
rely on CI for those.

- CI-equivalent commands (run inside the build container):
  - Resolve deps: `ant -f java/manager-build.xml ivy`
  - Checkstyle: `ant -f java/manager-build.xml checkstyle`
  - Tests: see targets in `java/manager-build.xml` (e.g. `test`, `test-pgsql`).
- If you can only reason statically, follow the checkstyle config at
  `java/buildconf/checkstyle.xml` (4-space indent, max line length 120, enforced
  import order, no wildcard imports, brace/whitespace/naming rules).
- DB access uses Hibernate 7.1.6 and, where needed, direct JDBC. Do not add a new
  ORM or query framework.

### 3.3 Java microservices (`microservices/`)

These **do** use Maven and Java 17 and can be built locally without the Uyuni
container:

```
mvn -B --no-transfer-progress --file microservices/uyuni-java-parent/pom.xml --non-recursive install checkstyle:check
mvn -B --no-transfer-progress --file microservices/uyuni-java-common/pom.xml install checkstyle:check
mvn -B --no-transfer-progress --file microservices/coco-attestation/pom.xml install checkstyle:check
```

Install `uyuni-java-parent` first (it is the parent POM for the others). The same
Checkstyle rules apply.

### 3.4 Python (`python/`)

- Run new unit tests with pytest from the repo root:
  `python3 -m pytest python/test/unit/`
  Tests under `python/test/unit/` must stay runnable without a container.
  Integration tests require PostgreSQL and the Uyuni Python container
  (`registry.opensuse.org/systemsmanagement/uyuni/master/docker/containers_tw/uyuni-master-python:latest`).
- Lint: black + pylint. CI uses:
  - `black --check --diff -t py36 <changed .py files>`
  - `pylint --rcfile=/root/.pylintrc <changed .py files>` (the rcfile lives inside
    the container; locally use `python/linting/pylintrc`).
  Convenience script: `python/linting/lint.sh <file> ...` or `-a` for
  added/modified files.
- Conventions: 4-space indent, black line length 88, Google pylintrc with Uyuni
  overrides. New tests use `pytest`. The lint filter excludes `mgr_bootstrap_data.py`
  and the entire `testsuite/` tree.

### 3.5 Schema (`schema/`)

PostgreSQL only. CI workflow
`.github/workflows/schema-migration-reportdb-test-pgsql.yml` covers migrations.
Place new tables/migrations under `schema/`.

### 3.6 Ruby acceptance tests (`testsuite/`)

- Ruby 3.3 (`.ruby-version`).
- Install and lint:
  ```
  cd testsuite
  bundle install
  bundle exec rubocop features/*
  ```
- Style enforced by `testsuite/.rubocop.yml` (with `.rubocop_todo.yml` tracking
  historical debt). Use Gherkin + Ruby step definitions; run sets live in
  `testsuite/run_sets/`, features in `testsuite/features/`.

### 3.7 Other relevant checks

- `.github/workflows/changelogs.yml` — validates changelog entries (see §6).
- `.github/workflows/git-checks.yml` — blocks merging PRs that contain `fixup!`
  commits.
- `.github/workflows/check_translations.yml`, `.github/workflows/update_translations.yml` —
  i18n automation. Do not hand-edit generated translation output.
- `.github/workflows/sonarcloud.yml` — static analysis triggered from the
  `java-checkstyle` workflow.

## 4. When validation cannot be run locally

Several validations depend on openSUSE Uyuni build containers that are not
reachable from the agent environment (Java stack, Python integration tests, schema
migration). When that is the case:

1. Make changes that are obviously correct by inspection and consistent with
   existing patterns in neighboring files.
2. Run every lint/test command that **is** locally runnable for the stacks you
   touched (frontend, microservices Maven, Python unit tests, RuboCop).
3. State clearly in the PR description which checks you ran locally and which
   ones you are deferring to CI. Do not disable or relax a CI check to make your
   PR green.

## 5. Coding conventions (quick reference)

- **Java (`java/buildconf/checkstyle.xml`):** 4-space indent; max line 120; no
  wildcard imports; enforced import order, braces, naming, whitespace.
- **Python (`python/linting/pylintrc`):** 4-space indent; black line length 88;
  Google pylint rules with Uyuni overrides; pytest for new tests.
- **Frontend (`web/eslint.config.js`, `web/.prettierrc`):** semicolons; print
  width 120; `simple-import-sort`; path aliases `components`, `core`, `manager`,
  `utils`; do not edit `dist/`.
- **Ruby (`testsuite/.rubocop.yml`):** RuboCop as configured; prefer extending
  existing step definitions over new ones.
- **SQL/schema:** PostgreSQL dialect only; migrations in `schema/`.

## 6. PR expectations

The PR template (`.github/PULL_REQUEST_TEMPLATE.md`) is checked by reviewers and
several workflows. Always fill in:

- **What does this PR change?** — a reviewer-friendly description.
- **GUI diff** — screenshots before/after for user-visible UI changes, or mark
  "No difference".
- **Documentation** — check the appropriate option (internal change / docs issue
  opened / API docs updated).
- **Test coverage** — state which tests were added (unit/Cucumber) or why none are
  needed.
- **Links** — reference the issue number(s) and any downstream port PR(s).
- **Changelogs** — either add entries under the touched subsystem following
  [the wiki guide](https://github.com/uyuni-project/uyuni/wiki/Contributing#changelogs),
  or tick "No changelog needed" in the template. `changelogs.yml` runs on most
  top-level product directories (see that workflow for the exact path filter).
  **Do not modify the generated master `*.changes` files directly.**
- Use meaningful commit messages; no `fixup!` merges.

### For product code changes: Gherkin scenarios are required

When proposing or reviewing product code changes, include Cucumber scenarios in
**strict Gherkin format only**:

- `Feature:`, `Scenario:`, `Given`, `When`, `Then`.
- Do **not** output step-definition or implementation code.
- Do **not** output pseudocode or test framework code.
- Keep scenarios behavior-focused and black-box.
- Cover happy path, negative path, and regression path when relevant.

Implementation of step definitions (if any) belongs in `testsuite/` and must pass
RuboCop.

## 7. Review focus (for Copilot code review)

Prioritize:

- Product code quality, architecture fit, correctness, maintainability, potential
  regressions.
- Behavioral regressions and edge cases.
- Integration risks across Java, Python, frontend, schema, and packaging layers
  (cross-stack contract changes should be called out explicitly).
- Missing acceptance coverage for user-visible behavior — and propose Gherkin
  scenarios as described in §6.

Keep review comments concise, actionable, and tied to changed code paths.

## 8. Trust this document

The information above is current and reflects the `.github/workflows/` CI
definitions and the stack `README`/config files at the time of writing. Search
the repository only when:

- a file you need is not described here,
- CI fails in a way these instructions do not explain, or
- you have concrete evidence that something here is out of date.

In those cases, read the relevant workflow under `.github/workflows/` and the
nearest `README` to the code you are changing, then prefer the workflow's
commands as the source of truth.
