---
name: run-java-checkstyle
description: Run Checkstyle on Java code (java/core, java/webapp, java/doclets, microservices/*) with the repository configuration. Use when asked to check Java code style or before finishing a Java change.
---

# Running Checkstyle on Java code

Run from the repository root. No database is needed.

## Prerequisite

The Maven parent must be installed once (see the `run-java-tests` skill,
step 2), because the plugin and its configuration come from
`microservices/uyuni-java-parent/pom.xml`:

```bash
mvn -q -f microservices/uyuni-java-parent --non-recursive install
```

## Run

Pick the module that contains the changed files:

| Changed files under                | `<module>`                          |
|------------------------------------|-------------------------------------|
| `java/core/`                       | `java/core`                         |
| `java/webapp/`                     | `java/webapp`                       |
| `java/doclets/`                    | `java/doclets`                      |
| `microservices/uyuni-java-common/` | `microservices/uyuni-java-common`   |
| `microservices/coco-attestation/`  | `microservices/coco-attestation`    |

```bash
mvn -f <module>/pom.xml checkstyle:check 2>&1 | grep -v -e WARNING -e Progress -e Download | tail -30
```

Success ends with `You have 0 Checkstyle violations.` and `BUILD SUCCESS`.
Violations are listed as `[ERROR] <file>:[line,col] ... [CheckName]`.

On a fresh checkout the first run downloads the plugin and takes a while;
later runs take 5 to 40 seconds depending on the module. Network access to Maven Central works in the sandbox; do not use
`-o`. For microservices that depend on `uyuni-java-common`, install it first
(`mvn -q -f microservices/uyuni-java-common/pom.xml install`) if resolution
fails.

## Details

- Plugin: `maven-checkstyle-plugin` 3.3.1 with Checkstyle 10.12.7, pinned in
  the parent POM. All modules share one config.
- Config: `java/buildconf/checkstyle.xml`, suppressions in
  `java/buildconf/checkstyle-suppressions.xml`, license header in
  `java/buildconf/LICENSE.txt`.
- Test sources are checked too (`includeTestSourceDirectory`).
- Javadoc is enforced on public methods; lines are limited to 120
  characters.
- The check covers the whole module, not only changed files, so a failure
  may come from untouched code; compare with `git diff --stat` before fixing.
- CI: the "Java checkstyle" workflow for `java/` runs
  `ant -f java/manager-build.xml checkstyle`. The Ant target is compatible
  with the Maven run; prefer Maven (`mvn ... checkstyle:check`).
