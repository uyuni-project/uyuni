---
name: SUSE Manager Milestone submission
about: Use this template for SUSE Manager Milestone submissions
title: 'X.Y <MILESTONE_NAME> Milestone submission'
labels: ["vega-squad"]
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

https://confluence.suse.com/pages/viewpage.action?pageId=1082228986

# TODO some days before

Add more tasks if needed.

- [ ] Lock the `master` branch
- [ ] Send an email to galaxy-devel@suse.de informing that branch `master` is locked, and adjust the topic on the slack [#team-susemanager](https://app.slack.com/client/T02863RC2AC/C02D78LLS04) channel
- [ ] Ask the Doc Squad to submit an update of the doc package to the `Devel:Galaxy:Manager:Head` project, and ask them to warn the translators so they can start their work. A Merge Request for the [documentation.suse.com repository](https://gitlab.suse.de/susedoc/docserv-external-tree-suma) should get created a few days before the release
- [ ] After the branch freeze, ask [Orion](https://suse.slack.com/archives/C02DDMY6R0R) to prepare the PR for the code translations. If nobody from Orion is available, ask @parlt91. They will add us as reviewers.
- [ ] Confirm that the SR for the documentation is merged, and translators warned
- [ ] Merge the PR for the translations with the option `Merge pull request`
- [ ] Ask the Ion Squad to promote all salt versions that have updates, including bundle
- [ ] Quick review changelogs with `tito-wrapper`, and request changes if needed
- [ ] Check all the [tests](https://ci.suse.de/view/Manager/view/Manager-Head/): everything should be green, or otherwise submission must be approved by QA

# TODO during the submission window

Add more tasks if needed (for example, asking Maintenace to change the channel definitions).

- [ ] Check all the [tests](https://ci.suse.de/view/Manager/view/Manager-Head/): everything should be green, or otherwise submission must be approved by QA
- [ ] Adjust `web.version` in `web/conf/rhn_web.conf` for `master` to match the Milestone (for example `4.3.0 Alpha1`)
- [ ] Check if the schema directory exists with the correct versions (more at https://github.com/SUSE/spacewalk/wiki/Maintenance-Update-procedure)
- [ ] Check if schema migration directories exist between older and newer SUSE Manager version (more at https://github.com/SUSE/spacewalk/wiki/Maintenance-Update-procedure)
- [ ] Push changes to `master`
- [ ] Tag everything with `tito`
- [ ] For all the packages with changes at `Devel:Galaxy:Manager:Head`, `Devel:Galaxy:Manager:Head:Other` and `Devel:Galaxy:Manager:Head:Kit` submit SRs to the GA codestream, and ping autobuild for review
- [ ] Create the JIRA ticket, with placeholders for the IDs
- [ ] Prepare the submissions with `mu-massive-task` or `patch-creator` for the client tools, salt and salt bundle. For any new packages that will be added to the codestreams, fetch the groups which will maintain them (one per package) and document this at the release card.
- [ ] Add the IDs (and notes, if any), to the JIRA ticket, and ping the Maintenace Team at [#discuss-susemamanager-maintenance](https://app.slack.com/client/T02863RC2AC/C02DEF2U0E5)
- [ ] Once autobuild approves all MRs, create the `Manager-X.Y-MILESTONE` branch (for example `Manager-4.3-Alpha1`), push it, and unlock `master`
- [ ] Consider PRs for merge that became ready during the branch freeze looking for the ["merge-candidate" label](https://github.com/uyuni-project/uyuni/pulls?q=is%3Apr+is%3Aopen+label%3Amerge-candidate) or ping reviewers to take care of it
- [ ] Send an email to galaxy-devel@suse.de informing that the branch `Manager-X.Y` is unlocked , and adjust the topic on the slack [#team-susemanager](https://app.slack.com/client/T02863RC2AC/C02D78LLS04) channel
- [ ] Ask release notes from the PO. For milestones there are no patchinfos
- [ ] Submit the release notes
- [ ] Prepare a draft for the email announcement during the release date
