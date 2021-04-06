## Maintenance Update status

Check http://ramrod.mgr.suse.de/pub/mu-badges/ and if any of the versions has an active Maintenance Update, please consider if this PR should be ported to the Maintenance Update branch.

## What does this PR change?

Port of # **remove rest of file if this is a port**

## GUI diff

No difference.

Before:

After:

- [ ] **DONE**

## Documentation
- No documentation needed: only internal and user invisible changes
- No documentation needed: **add explanation. This can't be used if there is a GUI diff**
- [Create documentation issue](https://github.com/SUSE/spacewalk/issues/new?template=ISSUE_TEMPLATE_DOCUMENTATION.md&labels=documentation&projects=SUSE/spacewalk/31)
- (OPTIONAL) [Documentation PR](https://github.com/uyuni-project/uyuni-docs/pulls)

- [ ] **DONE**

## Test coverage
- No tests: **add explanation**
- No tests: already covered
- Unit tests were added
- Cucumber tests were added

- [ ] **DONE**

## Links

Fixes #

Relevant branches (GitHub automatic links expected below):
 - Manager-4.0
 - Manager-4.1
 - Uyuni

- [ ] **DONE**

## Changelogs

If you don't need a changelog check, please mark this checkbox:

- [ ] No changelog needed

If you uncheck the checkbox after the PR is created, you will need to re-run `changelog_test` (see below)


## Re-run a test

If you need to re-run a test, please mark the related checkbox, it will be unchecked automatically once it has re-run:

- [ ] Re-run test "changelog_test"
- [ ] Re-run test "backend_unittests_pgsql"
- [ ] Re-run test "java_lint_checkstyle"
- [ ] Re-run test "java_pgsql_tests"
- [ ] Re-run test "ruby_rubocop"
- [ ] Re-run test "schema_migration_test_pgsql"
- [ ] Re-run test "susemanager_unittests"
- [ ] Re-run test "javascript_lint"
- [ ] Re-run test "spacecmd_unittests"
