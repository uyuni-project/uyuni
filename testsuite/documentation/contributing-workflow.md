# Contribution workflow (Uyuni and Multi-Linux Manager)

This document describes the basic workflow for working with the
[Uyuni](https://github.com/uyuni-project/uyuni) and
[Multi-Linux Manager (spacewalk)](https://github.com/SUSE/spacewalk) repositories when
creating new tests, fixing existing ones and opening pull requests (PRs).

For coding style and conventions, see the [guidelines](guidelines.md) and the
[Cucumber steps documentation](cucumber-steps.md).

## General rules

- **Upstream first:** Uyuni is the upstream project. Make your changes there first, then
  back-port them to Multi-Linux Manager (spacewalk).
- **Release branch policy:** back-port new tests/features to supported Multi-Linux Manager
  versions when needed. If a branch is locked, coordinate with the team and keep changes
  focused on what is required for that branch.
- Check whether a branch is locked before starting new work. Branch locking is
  coordinated within the team.

## Uyuni (upstream)

1. Make your changes in a local branch of your fork.
2. Test your changes locally / on your test server with
   [sumaform](https://github.com/uyuni-project/sumaform), or run a complete test suite
   run in CI.
3. Open a draft PR with all your changes.
   - Leave the PR template intact and only remove the lines that do not apply.
   - Tick the checkboxes, except the one under the _links_ section — you tick that later,
     once the spacewalk back-port PR exists (or right away if no back-port is needed).
4. The automatic PR tests must pass before the PR can be merged.
5. Mark the draft PR as ready for review and wait for a review from the team
   (maintainers / reviewers).
6. Before merging, squash your commits into one (see the project's
   "how to branch and merge" guidance).
7. If the branch is locked, coordinate with the maintainers / release engineers to merge.

## Multi-Linux Manager / spacewalk (back-port)

After your upstream Uyuni PR is merged, back-port the changes to Multi-Linux Manager:

1. Cherry-pick the changes with `git` into a local branch of your spacewalk fork.
2. Open a PR against each supported Multi-Linux Manager version.
3. Go back to the Uyuni PR and add a link to the spacewalk PR under the _links_ section.
4. If the branch is locked, add the `merge-candidate` label and coordinate with the
   maintainers / release engineers to merge.
5. If the Uyuni `master` branch is locked and your change is small, you may merge the
   spacewalk PRs before the Uyuni PR is merged — as long as the Uyuni PR has already been
   approved.

## Changelog

If your change needs a changelog entry, follow the changelog guidance in the project
documentation.
