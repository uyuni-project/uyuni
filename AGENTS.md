# AI Agent Guide for Uyuni/Spacewalk

This guide helps AI coding agents efficiently navigate and contribute to the Uyuni systems management project (upstream Spacewalk).

## Project Overview

**Uyuni** is a comprehensive open-source systems management solution with:
- **Backend**: Python (xml-rpc APIs, database handlers), Java (business logic), Perl (setup/utilities)
- **Frontend**: Modern TypeScript/React (web/html/src) + legacy JSP/Perl templates
- **Database**: PostgreSQL (primary) or Oracle, with complex schema and migration infrastructure
- **Architecture**: Monolithic with emerging microservices (coco-attestation, java-common)

## Multi-Language Stack & Build System

The codebase spans multiple languages with separate build systems:

| Component | Language | Build | Key Location |
|-----------|----------|-------|--------------|
| **Java Backend** | Java 11+ | Maven (pom.xml) | `java/` → modules: `core`, `webapp`, `doclets` |
| **Frontend UI** | TypeScript/React | npm/webpack | `web/package.json` delegates to `web/html/src/` |
| **Python Backend** | Python 3 | pytest/Docker | `python/` → namespace: `spacewalk`, `uyuni`, `rhn` |
| **Perl Utilities** | Perl 5 | tito/RPM | `spacewalk/`, `susemanager/`, web modules |
| **Database Schema** | SQL (PL/pgSQL) | Make | `schema/spacewalk/postgres/` & `schema/spacewalk/oracle/` |
| **Tests** | Gherkin/Ruby + pytest | Cucumber + pytest | `testsuite/` (integration), `test/` (unit) |

**Critical Convention**: Each language has independent PYTHONPATH/CLASSPATH setup. See `susemanager-utils/testing/docker/scripts/` for container build patterns.

## Key Architectural Components

### 1. **Backend Service Layers** (Python)
Located in `backend/server/`:
- **rhnSQL/**: Database connection abstraction (multi-database: PostgreSQL/Oracle)
- **rhnServer/**: Core business handlers (XML-RPC API implementations)
- **handlers/**: Request routing (HTTP → business logic)
- **importlib/**: ISS disk import machinery (batch XML processing with validation pipeline)

**Pattern**: Data flows through `Import` → `preprocess()` → `fix()` → `submit()` → `commit()`. See `backend/server/importlib/archImport.py` for template.

### 2. **Java Web Layer** (Spring-based)
Located in `java/core/`:
- REST/XML-RPC endpoints via `com.suse.manager.*` packages
- Reactor messaging system (salt-event processing)
- Database DAOs and ORM patterns

**Build**: Maven with parent POM at `microservices/uyuni-java-parent/`. Deploy mode configurable: `<deploy.mode>` in `java/pom.xml` (local, remote-container, or remote).

### 3. **Frontend Architecture** (React)
Located in `web/html/src/`:
- **components/**: Reusable React components with `.example.ts` files for Storybook documentation
- **manager/**: Application pages (routing via URL paths)
- **core/**: Shared utilities and hooks
- **build/**: Webpack config + custom ESLint rules in `web/eslint-local-rules/`

**Development**: `npm run proxy https://server.tf.local` proxies frontend to backend; hot reload enabled.

### 4. **Database & Schema**
- **PostgreSQL Primary**: `schema/spacewalk/postgres/` (PL/pgSQL functions, tables)
- **Oracle Support**: `schema/spacewalk/oracle/` (SQL functions, synonyms for compatibility)
- **Migration Testing**: `susemanager-utils/testing/docker/scripts/schema_idempotency_test_pgsql.py` — validates schema idempotency across versions

**Important**: Schema includes `rhn_*` and `logging` schemas with strict compatibility layer for Oracle→PostgreSQL migrations (see `utils/spacewalk-dump-schema`).

### 5. **Microservices** (Emerging)
- **coco-attestation**: Java Spring Boot service for TPM attestation (`microservices/coco-attestation/`)
- **uyuni-java-common**: Shared Java utilities and base classes

## Development Workflows

### Running the Full Stack Build

```bash
# Frontend (delegates to web/)
npm run all              # Full pipeline: install, build, lint, test, tsc
npm run lint             # ESLint + prettier autofixer
npm run test             # Jest tests in web/html/src/jest.config.js

# Java backend
cd java && mvn clean package  # Builds core, webapp, doclets

# Python backend unit tests (no database needed)
cd python && python3 -m pytest test/unit/

# Database integration tests (requires container)
cd susemanager-utils/testing/automation && sh backend-unittest-pgsql.sh

# Integration test suite (Cucumber-based)
cd testsuite && # Use sumaform orchestration (see testsuite/README.md)
```

### Container Development
Build and deployment Docker images defined in `containers/server-image/` and microservices.
Use `containers/BUILDING.md` for OBS (Open Build Service) package push workflow.

## Code Conventions & Patterns

### Python
- **Namespaces**: `spacewalk.*` (backend), `uyuni.*` (uyuni-specific), `rhn.*` (RHN lib)
- **Testing**: pytest fixtures in `python/test/conftest.py`; import tests with `python3 -m pytest path/to/test/`
- **Linting**: pylint with Google ruleset + custom 4-space indent (see `reporting/pylintrc`)
- **DB Access**: Always use `spacewalk.server.rhnSQL` wrapper, never raw connections

**Example Import Data Flow**:
```python
# In backend/server/importlib/archImport.py:
class TypedArchImport(Import):
    def preprocess(self): # Extract arch types
    def fix(self):        # Validate and lookup IDs
    def submit(self):     # Call backend.processXxxArches(batch)
```

### Java
- **Package Structure**: `com.suse.manager.{reactor,webui,api,...}`
- **Spring Config**: Via POM parent chain → buildconf/ properties
- **Testing**: JUnit; tests run separately (skipTests=true by default, requires DB)
- **Deployment**: Maven plugin configures Tomcat/container deployment targets

### TypeScript/React
- **Import Sorting**: Enforced by eslint-plugin-simple-import-sort (groups: side-effects, packages, internal, relative)
- **Type Safety**: Strict mode enabled (`tsconfig.json`); no-any mostly off for legacy
- **Custom ESLint Rules**: `web/eslint-local-rules/` (e.g., `no-raw-date` for i18n dates)
- **Component Patterns**: Functional components + React Hooks; Storybook examples in same directory as component

### Perl
- **Setup Tools**: `spacewalk/setup/lib/Spacewalk/Setup.pm` (database/Apache initialization)
- **Web Modules**: `web/modules/*/` (PXT framework — legacy templating)
- **RuboCop Config**: `.rubocop.yml` targets Ruby 2.5, excludes `.spec` files

## Integration Points & Cross-Component Communication

### XML-RPC API Boundary
- **Python Handlers** in `backend/server/handlers/` → Java Reactor → Frontend calls
- **API Contract**: Documented in legacy wiki; XML-RPC namespace routing via URL path (e.g., `/rhn/manager/do/*`)

### Database Schema Compatibility
- Oracle and PostgreSQL must have bit-for-bit compatible schema dumps
- Migration tool: `utils/spacewalk-dump-schema` → validates via diff tests

### Frontend ↔ Backend
- REST endpoints in Java layer; frontend proxied during dev
- Storybook at `/rhn/manager/storybook` for component review

## Testing Strategy & CI

### By Layer
- **Unit**: pytest (Python), Jest (TypeScript), JUnit (Java)
- **Integration**: Container-based with PostgreSQL (`backend-unittest-pgsql.sh`)
- **End-to-End**: Gherkin scenarios in `testsuite/features/` (Cucumber)

### Running Tests
```bash
# Python unit tests (fast, no database)
python3 -m pytest python/test/unit/ -v

# Frontend tests
npm run test              # Jest with watch mode: npm run test:watch

# Integration test groups defined in testsuite/run_sets/
# Executed sequentially: sanity_check → core → reposync → secondary → finishing
```

### Linting & Static Analysis
- **SonarQube**: Configuration in `sonar-project.properties` (runs on PRs)
- **Pre-commit**: No formal hooks; relies on CI automation
- **Production Lint**: `npm run lint:production` forces errors (not warnings)

## File Organization Rules

### Python
- Modules under namespace directories: `python/spacewalk/server/`, `python/uyuni/`
- Tests mirror source: `python/test/unit/spacewalk/server/`
- Handlers in `backend/server/handlers/sat/` (auth, cert, etc.)

### Java
- Core logic: `java/core/src/com/suse/manager/`
- Web layer: `java/webapp/src/com/suse/manager/webui/`
- Tests: `java/*/src/test/java/com/suse/manager/`

### Frontend
- Components: `web/html/src/components/`
- Pages: `web/html/src/manager/` (routable views)
- Utilities: `web/html/src/utils/` (helpers, formatters)
- Styles: Imported via webpack; SCSS in components or `web/html/src/branding/`

## Debugging & Troubleshooting

### Database Issues
- Check schema version: `SELECT VERSION, RELEASE FROM rhnServerInfo;`
- Idempotency test: Rerun schema upgrade scripts, verify no errors
- Migration validation: `schema_idempotency_test_pgsql.py --schema-path /etc/sysconfig/rhn/schema-upgrade`

### Build Failures
- Java: Check `java/pom.xml` parent chain and buildconf/ properties
- Frontend: Run `npm run clean && npm install` (clears node_modules)
- Python: Verify PYTHONPATH includes `python/` and backend/ via `PYTHONPATH` env var

### Container Issues
- Use scripts in `susemanager-utils/testing/docker/` for reproducible environments
- Logs: Check container output in `containers/server-image/Dockerfile`

## External Dependencies & APIs

### Package Repositories
- OBS (Open Build Service) for RPM builds: `systemsmanagement:Uyuni:Master`
- Test packages: `systemsmanagement:Uyuni:Test-Packages:Pool` & `:Updates`

### Third-Party Libraries
- **Frontend**: Full-Calendar, NoVNC, Spice HTML5 (desktop access)
- **Java**: Spring, Apache, Tomcat (see `java/pom.xml`)
- **Python**: urllib, yum/dnf libraries (package management)

## References & Key Files

- **Contributing**: `CONTRIBUTING.md` (PR expectations, commit message style)
- **Testsuite Guidelines**: `testsuite/documentation/guidelines.md`, `cucumber-steps.md`
- **Security**: `SECURITY.md`
- **Project Home**: https://www.uyuni-project.org/
- **Gitter Chat**: Community support channel
- **Branches**: master (dev), Manager-5.1/5.0/4.3 (release)

