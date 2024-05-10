  ## IMPORTANT 📌


**SUSE Manager 4.3** is in maintenance mode, and we don't intend to implement significant changes unless absolutely necessary. If the pull request is substantial in size and targets **SUSE Manager 4.3**, and it's not a Level 3 fix, please reach out to @rjmateus or @admd before proceeding with the merge.

## What does this PR change?

**add description**

## GUI diff

No difference.

Before:

After:

- [ ] **DONE**

## Documentation
- No documentation needed: **add explanation. This can't be used if there is a GUI diff**
- No documentation needed: only internal and user invisible changes
- Documentation issue was created: [Link for SUSE Manager contributors](https://github.com/SUSE/spacewalk/issues/new?template=ISSUE_TEMPLATE_DOCUMENTATION.md&labels=documentation&projects=SUSE/spacewalk/31), [Link for community contributors](https://github.com/uyuni-project/uyuni-docs/issues/new).
- API documentation added: please review the Wiki page [Writing Documentation for the API](https://github.com/uyuni-project/uyuni/wiki/Writing-documentation-for-the-API) if you have any changes to API documentation.
- (OPTIONAL) [Documentation PR](https://github.com/uyuni-project/uyuni-docs/pulls)

- [ ] **DONE**

## Test coverage
- No tests: **add explanation**
- No tests: already covered
- Unit tests were added
- Cucumber tests were added

- [ ] **DONE**

## Links

Issue(s): #
Port(s): # **add upstream PR, if any**

- [ ] **DONE**

## Changelogs

Make sure the changelogs entries you are adding are compliant with https://github.com/uyuni-project/uyuni/wiki/Contributing#changelogs and https://github.com/uyuni-project/uyuni/wiki/Contributing#uyuni-projectuyuni-repository

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

# Before you merge

Check [How to branch and merge properly](https://github.com/uyuni-project/uyuni/wiki/How-to-branch-and-merge-properly)!
