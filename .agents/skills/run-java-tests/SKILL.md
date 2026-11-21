---
name: run-java-tests
description: Run Java JUnit unit tests (java/core) in this repo from the sandbox, including the one-time Maven setup and database check. Use when asked to run or verify Java tests.
---

# Running Java unit tests

JUnit tests in `java/core` need a PostgreSQL test database. Run all commands
from the repository root.

## 1. Check the database

The database runs in a container. In a sandbox (for example a podman-based
sandbox), containers usually cannot be started and `localhost` is not the
host, so the user must start it on the host (ask them; do not try to start
containers):

```bash
cd java && make -f Makefile.docker confighost podmanrun
```

`confighost` writes the git-ignored `java/buildconf/test/rhn.conf` with
`db_host` and `report_db_host` set to `host.containers.internal`. If the
database is already running but the config is missing or points to
localhost, regenerate only the config:

```bash
make -C java -f Makefile.docker confighost
```

Outside a sandbox, `cd java && make -f Makefile.docker` pulls the image,
writes `rhn.conf` for localhost and runs the database in the foreground on
localhost:5432; keep it running in a separate terminal while tests run, and
use `localhost` instead of `host.containers.internal` below.

Verify config and reachability:

```bash
grep db_host java/buildconf/test/rhn.conf        # host.containers.internal
timeout 20 bash -c '</dev/tcp/host.containers.internal/5432' && echo db-ok
```

## 2. One-time Maven setup

Needed once per checkout (or when the branding/parent version changes);
otherwise `core` fails with `Could not find artifact ...branding:jar:<version>`.

```bash
mvn -q -f microservices/uyuni-java-parent --non-recursive install
mvn -q -f branding install
mvn -q -f java --non-recursive initialize
```

Network access to Maven Central works in the sandbox. Do not use `-o`
(offline): plugins such as `maven-source-plugin` are not cached.

## 3. Run the tests

```bash
mvn -f java/core test -DskipTests=false -Dtest=<TestClass>
# single method:
mvn -f java/core test -DskipTests=false -Dtest=<TestClass>#<method>
```

The run is slow, so start it in the background and filter the noisy
resolver warnings:

```bash
set -o pipefail
mvn -f java/core test -DskipTests=false -Dtest=<TestClass> 2>&1 | grep -v -e WARNING -e Progress -e Download | tail -40
```

`pipefail` keeps Maven's exit code; without it the pipeline returns
`tail`'s status and a failed build looks successful.

Look for `Tests run: N, Failures: F, Errors: E` and `BUILD SUCCESS`.
Per-test reports are in `java/core/target/surefire-reports/`.

## Notes

- Run the narrowest test class first; widen only if the change crosses modules.
- Report honestly if tests could not run (DB unreachable, build failure).
- Whether to prepare commits is up to the user. Only provide commit commands
  if the user asks for them; then follow the commit instructions from the
  global configuration.
