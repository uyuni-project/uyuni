---
name: Uyuni release
about: Use this template for Uyuni releases
title: 'XXXX.YY Uyuni release'
labels: ["vega-squad", "uyuni"]
projects: ["SUSE/35"]
---

# Important changes, for the release notes

- 

# Procedure

https://github.com/uyuni-project/uyuni/wiki/Releasing-Uyuni-versions

# ToDo some days before

Add more tasks if needed.

- [ ] Check all the [tests](https://ci.suse.de/view/Manager/view/Uyuni/): everything should be green, in particular the acceptance testsuite or otherwise submission must be approved by QA.
- [ ] Ask the Doc Squad to submit an update of the doc package to [systemsmanagement:Uyuni:Master](https://build.opensuse.org/project/show/systemsmanagement:Uyuni:Master), a PR for the main documention and ReportDB documentation at the [documentation repository, gh-pages branch](https://github.com/uyuni-project/uyuni-docs/tree/gh-pages) and a PR for the [documentation API repository, gh-pages branch](https://github.com/uyuni-project/uyuni-docs-api/tree/gh-pages).
- [ ] Confirm that the documentation is ready.
- [ ] [Lock](https://github.com/uyuni-project/uyuni/settings/branch_protection_rules/2243617) the `master` branch (check the checkbox for `Restrict who can push to matching branches`)
- [ ] [Lock](https://github.com/uyuni-project/uyuni-tools/settings/branch_protection_rules/37702039) the `main` branch for the uyuni-tools as well (check the checkbox for `Restrict who can push to matching branches`)
- [ ] Send an email to galaxy-devel@suse.de informing that the branch `master` is locked, and adjust the topic on the slack [#team-susemanager](https://app.slack.com/client/T02863RC2AC/C02D78LLS04) channel
- [ ] After the branch freeze, ask [Orion](https://suse.slack.com/archives/C02DDMY6R0R) to prepare the PR for the code translations from `master-weblate` to `master`. If nobody from Orion is available, ask @parlt91. They will add us as reviewers.
- [ ] Merge the PR for the translations with the option `Merge pull request`
- [ ] Quick review changelogs with `tito-wrapper`, and request changes if needed
- [ ] Adjust the version number in `web/conf/rhn_web.conf` (`web.version.uyuni`)
- [ ] Check if the migration paths exist for both the main database and report database, if they are needed (more at https://github.com/uyuni-project/uyuni/wiki/Releasing-Uyuni-versions)
- [ ] Check if schema migration directories exist last SUSE Manager versions and Uyuni (more at https://github.com/uyuni-project/uyuni/wiki/Releasing-Uyuni-versions)
- [ ] Tag everything in uyuni-project/uyuni with `tito` and push
- [ ] Additionally, tag everything in uyuni-project/uyuni-tools with `tito` and push. Please notice `tito-wrapper` will not work here, but so far it is only a single package to tag.
- [ ] Check that the job [uyuni-Master-releng-2obs](https://ci.suse.de/view/Manager/view/Uyuni/job/uyuni-Master-releng-2obs/) job has packaged the whole tagging and submitted the changes from `master` branch into https://build.opensuse.org/project/show/systemsmanagement:Uyuni:Master (changes will propagate to the client tools for linked packages)
- [ ] Run `rel-eng/uyuni-check-version` at the Git repository, to check what other packages need changes and adjust them.
- [ ] Prepare the release notes PR, send it to be reviewed.
- [ ] Merge the release notes PR, submit the release notes to [systemsmanagement:Uyuni:Master](https://build.opensuse.org/project/show/systemsmanagement:Uyuni:Master)
- [ ] Prepare the PR for the [website repository, master branch](https://github.com/uyuni-project/uyuni-project.github.io), including announcement at the main page, updates to the stable page, news page, new doc folder (PDFs) with doc and release notes.
- [ ] Prepare the email announcement and the twitter announcement, add them as comments to this card.
- [ ] Prepare a new [snapshot](https://build.opensuse.org/project/show/systemsmanagement:Uyuni:Snapshots) as `systemsmanagement:Uyuni:Snapshots:YYYY.MM`. You can use [systemsmanagement:Uyuni:Snapshots:2023.12](https://build.opensuse.org/project/show/systemsmanagement:Uyuni:Snapshots:2023.12) as base (even better if you use the snapshot for the previous release), but make sure you adjust the meta configuration to match the version and the expect Leap version used as base OS.
- [ ] Modify the [meta configuration](https://build.opensuse.org/projects/systemsmanagement:Uyuni:Stable/meta) for `systemsmanagement:Uyuni:Stable`, so the promotion is done against the new Snapshot.

# ToDo during release day

Add more tasks if needed.

- [ ] Check all the [tests](https://ci.suse.de/view/Manager/view/Uyuni/): everything should be green, or otherwise submission must be approved by QA.
- [ ] Specifically, make sure everything is still [building](https://ci.suse.de/view/Manager/view/Uyuni/job/uyuni-Master-dev-at-obs/) and [server and proxy are installable](https://ci.suse.de/view/Manager/view/Uyuni/job/Uyuni-Master-releng-Media-Install-Test/).
- [ ] Run `rel-eng/uyuni-check-version` at the Git repository, to make sure nothing is missing
- [ ] Promote.
- [ ] Merge the PRs for the site, the main doc and ReportDB, and the API doc.
- [ ] Review that the [site](https://github.com/uyuni-project/uyuni-project.github.io/actions/workflows/pages/pages-build-deployment), [main doc and ReportDB](https://github.com/uyuni-project/uyuni-docs/actions/workflows/pages/pages-build-deployment), and the [API doc](https://github.com/uyuni-project/uyuni-docs-api/actions/workflows/pages/pages-build-deployment) were published
- [ ] Create a git tag or a branch (in both cases `Uyuni-XXXX.YY`), and push it
- [ ] Announce: mailing lists, twitter and Gitter (update the topic for the `users` channel)
- [ ] [Unlock](https://github.com/uyuni-project/uyuni/settings/branch_protection_rules/2243617) the `master` branch (uncheck the checkbox for `Restrict who can push to matching branches`)
- [ ] [Unlock](https://github.com/uyuni-project/uyuni-tools/settings/branch_protection_rules/37702039) the `main` branch (uncheck the checkbox for `Restrict who can push to matching branches`)
- [ ] Send an email to galaxy-devel@suse.de informing that the branch `master` is unlocked, and adjust the topic on the slack [#team-susemanager](https://app.slack.com/client/T02863RC2AC/C02D78LLS04) channel

## 24h after the release
- [ ] (Task to be done by QE, most likely Jordi): Update the [test container images](https://github.com/uyuni-project/uyuni/wiki/Build-test-container-images)
> :information_source: We wait 24h release to give time to the opensuse mirrors to be in sync.
