---
name: SUSE Manager 5.0 MU submission
about: Use this template for SUSE Manager 5.0 MU submissions
title: 'X.Y.Z Maintenance Update submission'
labels: ["vega-squad", "5.0"]
projects: ["SUSE/35"]
assignees: ''
---

# Important changes, for the release notes

- 

# Deadlines

Check the [Release Calendar](https://confluence.suse.com/display/SUSEMANAGER/Release+calendar)
Freeze: 
Submission: 
Release: 

# Procedure

https://confluence.suse.com/display/SUSEMANAGER/Maintenance+Update+procedure

# TODO some days before

Add more tasks if needed.

- [ ] Lock the `Manager-5.0` branches on `SUSE/spacewalk` and `SUSE/uyuni-tools`
- [ ] Send an email to galaxy-devel@suse.de informing that the branches `SUSE/spacewalk:Manager-5.0` and `SUSE/uyuni-tools:Manager-5.0` are locked, and adjust the topic on the slack [#team-susemanager](https://app.slack.com/client/T02863RC2AC/C02D78LLS04) channel
- [ ] Ask the Doc Squad to submit an update of the doc package to `Devel:Galaxy:Manager:5.0` project, and ask them to warn the translators so they can start their work. A Merge Request for the [documentation.suse.com repository](https://gitlab.suse.de/susedoc/docserv-external-tree-suma) should get created a few days before the release
- [ ] After the branch freeze, ask [Orion](https://suse.slack.com/archives/C02DDMY6R0R) to prepare the PR for the code translations. If nobody from Orion is available, ask @parlt91. They will add us as reviewers.
- [ ] Increase `web.version` in `web/conf/rhn_web.conf` (`x.y.z+1`) in `Manager-5.0` branch
  - [ ] Check that the sed that replaces the default tag for SUSE Multi-Linux Manager in the [push.sh](https://github.com/SUSE/uyuni-tools/blob/Manager-5.0/push.sh) script in [uyuni-tools](https://github.com/SUSE/uyuni-tools) specifies the same version you added in `web.version`. If the value is outdated, update it with a PR for [uyuni-tools](https://github.com/SUSE/uyuni-tools). For example, see the [PR done](https://github.com/SUSE/uyuni-tools/pull/43) for fixing the default pull tag after the release of SUSE Manager 5.0.2.
- [ ] Check if the migration paths exist for both the main database and report database, if they are needed (more at https://confluence.suse.com/display/SUSEMANAGER/Maintenance+Update+procedure)
- [ ] Check if schema migration directories exist between older and newer SUSE Manager version (more at https://confluence.suse.com/display/SUSEMANAGER/Maintenance+Update+procedure)
- [ ] Push changes to `Manager-5.0` branch
- [ ] Confirm that the SR for the documentation is merged, and translators warned
- [ ] Merge the PR for the translations with the option `Merge pull request`
- [ ] Ask the Ion Squad to promote all salt versions that have updates, including bundle (not needed for MUs without salt/client tools)
- [ ] Quick review changelogs with `tito-wrapper`, and request changes if needed
- [ ] Check all the [tests for the relevant version](https://ci.suse.de/view/Manager/): everything should be green, or otherwise submission must be approved by the RRTG
- [ ] Create the JIRA ticket, with placeholders for the IDs

# TODO during the submission window

Add more tasks if needed (for example, asking Maintenace to change the channel definitions).

- [ ] Check all the [tests for the relevant version](https://ci.suse.de/view/Manager/): everything should be green, or otherwise submission must be approved by the RRTG
- [ ] Tag everything with `tito tag --use-release=0`
- [ ] Check that the job `manager-5.0-releng-2obs` and `manager-5.0-uyunitools-2obs` have packaged and submitted the changes from `Manager-5.0` branch into the relevant https://build.suse.de/project/subprojects/Devel:Galaxy:Manager:5.0 project.
- [ ] Prepare the submissions with `mu-massive-task` or `patch-creator`. For any new packages that will be added to the codestreams, fetch the groups what will maintain them (one per package) and document this at the release card.
- [ ] For submitting to **SL Micro 6.0** the client tools, salt and salt bundle, remember that you can't use `patch-creator`; instead use the script `submit-slmicro6_clienttools` that will explain step by step what to submit and how.
- [ ] Ping the Ion squad so they submit salt to `SLE15SP2`, `SLE15SP3`, `SLE15SP4`, `SLE15SP5`, `SUSE:ALP:Source:Standard:1.0`, `SUSE:SLFO:1.1` (not needed for MUs without salt/client tools). Tell them to add to the submit message the ID of the Jira ticket in the form `ijsc#MSQA-$ID`, for example, `ijsc#MSQA-808`
- [ ] Add the IDs (and notes, if any), to the JIRA ticket, and ping the Maintenance Team at [#discuss-susemamanager-maintenance](https://app.slack.com/client/T02863RC2AC/C02DEF2U0E5)
- [ ] Once autobuild approves all MRs, create the `Manager-5.0-MU-X.Y.Z` branch, push it.
- [ ] Before unlocking the branches `SUSE/spacewalk:Manager-5.0` and `SUSE/uyuni-tools:Manager-5.0`, consider PRs for merge that became ready during the branch freeze looking for the ["merge-candidate" label](https://github.com/SUSE/spacewalk/pulls?q=is%3Apr+is%3Aopen+label%3Amerge-candidate) or ["merge-candidate" label for uyuni-tools](https://github.com/SUSE/uyuni-tools/pulls?q=is%3Apr+is%3Aopen+label%3Amerge-candidate) or ping reviewers to take care of it.
- [ ] Send an email to galaxy-devel@suse.de informing that the branches `SUSE/spacewalk:Manager-5.0` and `SUSE/uyuni-tools:Manager-5.0` are unlocked , and adjust the topic on the slack [#team-susemanager](https://app.slack.com/client/T02863RC2AC/C02D78LLS04) channel
- [ ] Add links to the patchinfos on top of this issue, and send for the PO with a link to this issue, and the deadline for the SR for the release notes.
- [ ] Submit the release notes
