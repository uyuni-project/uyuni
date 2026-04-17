# AGENTS.md

This file guides Copilot Agents working in the Uyuni repository.

## Product Code Guidelines

### Repository architecture

- Uyuni is a polyglot monorepo with major areas:
  - `java/`: main server-side Java stack (`core`, `webapp`, `doclets`). Houses legacy Struts (1.2.9) Web UI, XML-RPC APIs, and modern SparkJava HTTP APIs, communicating via Hibernate 7.x to PostgreSQL.
  - `python/`: backend and support libraries (`spacewalk`, `uyuni`, `rhn`) that govern Salt interactions, Cobbler integrations, and XML-RPC handlers. Tests rely on `pytest`.
  - `web/`: modern web UI (React + TypeScript + webpack) utilizing `Formik` for forms, `node-gettext` for i18n, and React Hooks. Replacing legacy JSP/JavaScript incrementally.
  - `schema/`: PostgreSQL database definitions and patching logic.
  - `containers/`, `microservices/`, `proxy/`: platform, deployment, and service-specific components utilizing Podman natively for local running.

### Technology expectations & constraints

- Frontend:
  - Node `22.x`, npm `>=10.x`
  - React + TypeScript, webpack, Jest
  - Strict Hooks usage; rely on `Formik` for form management and `node-gettext` for translations (no ad-hoc internationalization).
  - ESLint + Prettier enforced
- Java:
  - Maven multi-module build with strict Checkstyle rules
  - **Constraints:** Avoid introducing new Struts 1.x logic. Prefer modern SparkJava or external microservices for new endpoints.
  - Database interactions use Hibernate 7.1.6 and direct JDBC where necessary.
- Python:
  - `pytest` for tests
  - `black` + `pylint` (enforcing Google Python style with Uyuni overrides, line length 88)
- Schema:
  - PostgreSQL is the exclusive RDBMS. All migrations and tables belong in the `schema/` path.

### Coding conventions by stack

- Java (`java/buildconf/checkstyle.xml`):
  - 4-space indentation
  - max line length: 120
  - enforce import order, braces, naming, whitespace, and common correctness checks
  - avoid wildcard imports
- Python (`python/README.md`, `python/linting/pylintrc`):
  - 4-space indentation
  - black formatting (line length 88)
  - pylint naming/style rules derived from Google style with Uyuni-specific adjustments
  - new tests should use `pytest`
- Frontend (`web/eslint.config.js`, `web/.prettierrc`):
  - semicolons required
  - print width 120
  - import sorting via `simple-import-sort`
  - use configured path aliases (`components`, `core`, `manager`, `utils`)
  - do not edit generated outputs directly (for example, built dist assets)

### Change strategy

- Keep changes minimal and localized to the owning subsystem.
- Prefer existing abstractions/patterns over introducing new frameworks.
- Respect current file ownership and stack boundaries (see `.github/CODEOWNERS`).
- Do not mix legacy Struts logic with modern SparkJava/React endpoints unnecessarily. Keep boundaries clean.
- When touching cross-stack behavior (e.g., adding an API that React consumes and Python handles), call out contract assumptions explicitly.

## Testing Conventions

- Acceptance testing uses **Cucumber/Gherkin** paired with Ruby.
- All test implementation logic belongs in `testsuite/` (linted by RuboCop).
- Before writing any test implementation code, first outline the scenarios in Gherkin:
  - `Feature:`
  - `Scenario:`
  - `Given`
  - `When`
  - `Then`
- For product behavior changes, define/update Gherkin scenarios first, then implement step logic in `testsuite/`.

## Restrictions

- Do **not** modify `.github/` workflows unless the user explicitly requests it.
- Do **not** treat changes only in `testsuite/` or `.github/` as core product code work.
