---
name: Uyuni release
about: Use this template for Uyuni releases
title: 'XXXX.YY Uyuni release'
labels: vega-squad
assignees: 'juliogonzalez'

---

# Important changes, for the release notes

- 

# Procedure

https://github.com/uyuni-project/uyuni/wiki/Releasing-Uyuni-versions

# ToDo some days before

Add more tasks if needed.

- [ ] Ask the Doc Squad to submit an update of the doc package to [systemsmanagement:Uyuni:Master](https://build.opensuse.org/project/show/systemsmanagement:Uyuni:Master), a PR for the [documentation repository, gh-pages branch](https://github.com/uyuni-project/uyuni-docs/tree/gh-pages) and a PR for the [documentation API repository, gh-pages branch](https://github.com/uyuni-project/uyuni-docs-api/tree/gh-pages)
- [ ] Confirm that the documentation is ready
- [ ] Quick review changelogs with `tito-wrapper`, and request changes if needed
- [ ] Check all the tests: everything should be green, or otherwise submission must be approved by QA
- [ ] Prepare the release notes PR, send it to be reviewed
- [ ] Merge the release notes PR, submit the release notes to [systemsmanagement:Uyuni:Master](https://build.opensuse.org/project/show/systemsmanagement:Uyuni:Master)
- [ ] Prepare the PR for the [website repository, master branch](https://github.com/uyuni-project/uyuni-project.github.io), including announcement at the main page, updates to the stable page, news page, new doc folder (PDFs) with doc and release notes
- [ ] Prepare the email announcement and the twitter announcement, add them as comments to this card
- [ ] Prepare a new [snapshot](https://build.opensuse.org/project/show/systemsmanagement:Uyuni:Snapshots) as `systemsmanagement:Uyuni:Snapshots:YYYY.MM`. You can use [systemsmanagement:Uyuni:Snapshots:2021.09](https://build.opensuse.org/project/show/systemsmanagement:Uyuni:Snapshots:2021.09) as base, but make sure you adjust the meta configuration to match the version and the expect Leap version used as base OS.
- [ ] Modify the [meta configuration](https://build.opensuse.org/projects/systemsmanagement:Uyuni:Stable/meta) for `systemsmanagement:Uyuni:Stable`, so the promotion is done against the new Snapshot.

# ToDo during release day

Add more tasks if needed.

- [ ] Check all the tests: everything should be green, or otherwise submission must be approved by QA
- [ ] Tag everything with `tito`
- [ ] Make sure everything is still building
- [ ] Promote
- [ ] Merge the PRs for the site, the doc and the API doc
- [ ] Create a git tag
- [ ] Announce: Mailing lists, twitter and Gitter
