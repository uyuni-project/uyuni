---
name: SUSE Manager MU submission
about: Use this template for SUSE Manager MU submissions
title: 'X.Y.Z Maintenance Update submission'
labels: vega-squad
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

- [ ] Lock the `Manager-X.Y` branch
- [ ] Send an email to galaxy-devel@suse.de informing that the branch `Manager-X.Y` is locked, and adjust the topic on the slack [#team-susemanager](https://app.slack.com/client/T02863RC2AC/C02D78LLS04) channel
- [ ] Ask the Doc Squad to submit an update of the doc package to the relevant `Devel:Galaxy:Manager:X.Y` project, and ask them to warn the translators so they can start their work. A Merge Request for the [documentation.suse.com repository](https://gitlab.suse.de/susedoc/docserv-external-tree-suma) should get created a few days before the release
- [ ] After the branch freeze, ask [Orion](https://suse.slack.com/archives/C02DDMY6R0R) to prepare the PR for the code translations. If nobody from Orion is available, ask @parlt91.
- [ ] Confirm that the SR for the documentation is merged, and translators warned
- [ ] Confirm that the translations are merged
- [ ] Ask the Ion Squad to promote all salt versions that have updates, including bundle (not needed for MUs without salt/client tools)
- [ ] Quick review changelogs with `tito-wrapper`, and request changes if needed
- [ ] Check all the [tests for the relevant version](https://ci.suse.de/view/Manager/): everything should be green, or otherwise submission must be approved by the RRTG

# TODO during the submission window

Add more tasks if needed (for example, asking Maintenace to change the channel definitions).

- [ ] Check all the [tests for the relevant version](https://ci.suse.de/view/Manager/): everything should be green, or otherwise submission must be approved by the RRTG
- [ ] Increase `web.version` in `web/conf/rhn_web.conf` (`x.y.z+1`) in `Manager-X.Y` branch
- [ ] Check if the migration paths exist for both the main database and report database, if they are needed (more at https://confluence.suse.com/display/SUSEMANAGER/Maintenance+Update+procedure)
- [ ] Check if schema migration directories exist between older and newer SUSE Manager version (more at https://confluence.suse.com/display/SUSEMANAGER/Maintenance+Update+procedure)
- [ ] Push changes to `Manager-X.Y`branch
- [ ] Tag everything with `tito`
- [ ] Check that the relevant `manager-x.y-releng-2obs` job has packaged latest and submitted the changes from `Manager-X.Y` branch into the relevant https://build.suse.de/project/subprojects/Devel:Galaxy:Manager:X.Y project.
- [ ] Create the JIRA ticket, with placeholders for the IDs
- [ ] Prepare the submissions with `mu-massive-task` or `patch-creator`. For any new packages that will be added to the codestreams, fetch the groups what will maintain them (one per package) and document this at the release card.
- [ ] Ping the Ion squad so they submit salt to `SLE15SP1`, `SLE15SP2`, `SLE15SP3`, `SLE15SP4` and `SLE15SP5` (not needed for MUs without salt/client tools)
- [ ] Add the IDs (and notes, if any), to the JIRA ticket, and ping the Maintenace Team at [#discuss-susemamanager-maintenance](https://app.slack.com/client/T02863RC2AC/C02DEF2U0E5)
- [ ] Once autobuild approves all MRs, create the `Manager-X.Y-MU.X.Y.Z` branch, push it and unlock `Manager-X.Y`
- [ ] Send an email to galaxy-devel@suse.de informing that the branch `Manager-X.Y` is unlocked , and adjust the topic on the slack [#team-susemanager](https://app.slack.com/client/T02863RC2AC/C02D78LLS04) channel
- [ ] Add links to the patchinfos on top of this issue, and send for the PO with a link to this issue, and the deadline for the SR for the release notes.
- [ ] Submit the release notes
