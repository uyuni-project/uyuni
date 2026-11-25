---
name: run-java-tests
description: Run java/core JUnit tests, including database check and one-time Maven setup. Use when running or verifying Java tests.
---

# Running Java unit tests

JUnit tests in `java/core` need a PostgreSQL test database. Run all commands
from the repository root.

## 1. Check the database

The database runs in a container. First check whether the test config
exists and the database is reachable:

```bash
grep db_host java/buildconf/test/rhn.conf
timeout 20 bash -c '</dev/tcp/<db_host>/5432' && echo db-ok
```

`<db_host>` is the value from `rhn.conf`: `host.containers.internal` in a
sandbox, `localhost` otherwise. If both commands succeed, continue with
step 2.

Otherwise the database has to be started. In a sandbox (for example a
podman-based sandbox), containers usually cannot be started and `localhost`
is not the host, so the user must start it on the host (ask them; do not
try to start containers):

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
localhost:5432; keep it running in a separate terminal while tests run.

Repeat the check above afterwards.

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

The run takes minutes. Run it detached (in Claude Code with the Bash
tool's `run_in_background`, otherwise in a separate terminal or with `&`)
and write the output to a log file in the git-ignored `target/` directory
instead of piping it through `tail`, which would hide the failure details:

```bash
mkdir -p java/core/target
mvn -f java/core test -DskipTests=false -Dtest=<TestClass> > java/core/target/test-run.log 2>&1
echo "exit code: $?"
```

Then check the result:

```bash
grep -E 'Tests run:|FAIL|ERROR|BUILD' java/core/target/test-run.log
```

Look for `Tests run: N, Failures: F, Errors: E` and `BUILD SUCCESS`. For
failing tests, read the per-class reports in
`java/core/target/surefire-reports/` (`<package>.<TestClass>.txt` has the
assertion messages and stack traces).

## Notes

- Run the narrowest test class first; widen only if the change crosses modules.
- Report honestly if tests could not run (DB unreachable, build failure).
