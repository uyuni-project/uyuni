---
name: SUSE Manager Milestone submission
about: Use this template for SUSE Manager Milestone submissions
title: 'X.Y <MILESTONE_NAME> Milestone submission'
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

https://github.com/SUSE/spacewalk/wiki/Submission-of-new-major-versions-%28Alpha%2C-Beta%2C-RC%2C-GMC%29

# ToDo some days before

Add more tasks if needed.

- [ ] For any new packages that will be added to the codestreams, fetch the groups what will maintain them (one per package)
- [ ] Ask the Doc Squad to submit an update of the doc package to `Devel:Galaxy:Manager:Head` project, and a MR for the [repository](https://gitlab.suse.de/susedoc/docserv-external-tree-suma) used for documentation.suse.com (they need to give us a URL for a MR)
- [ ] Ask @mcalmer (backup: @parlt91) to prepare the PR for the code translations. They will ask a brief period of branch freeze to prepare it. Ideally this should happen close to the branch freeze date.
- [ ] Confirm that the documentation is ready
- [ ] Ask the Ion Squad to promote all salt versions that have updates (including bundle)
- [ ] Quick review changelogs with `tito-wrapper`, and request changes if needed
- [ ] Check all the tests: everything should be green, or otherwise submission must be approved by QA

# ToDo during the submission window

Add more tasks if needed (for example, asking Maintenace to change the channel definitions).

- [ ] Check all the tests: everything should be green, or otherwise submission must be approved by QA
- [ ] Increase version number in web/conf/rhn_web.conf (x.y.z+1) in `master`
- [ ] Check if the schema directory exists with the correct versions (more at https://github.com/SUSE/spacewalk/wiki/Maintenance-Update-procedure)
- [ ] Check if schema migration directories exist between older and newer SUSE Manager version (more at https://github.com/SUSE/spacewalk/wiki/Maintenance-Update-procedure)
- [ ] Push changes to `master`
- [ ] Tag everything with `tito`
- [ ] For all the packages with changes at `Devel:Galaxy:Manager:Head`, `Devel:Galaxy:Manager:Head:Other` and `Devel:Galaxy:Manager:Head:Kit` submit SRs to the GA codestream, and ping autobuild for review 
- [ ] Prepare the submissions for client tools, salt and salt bundle with `patch-creator`
- [ ] Create the JIRA ticket with all the submissions for client tools, salt and salt bundle and ping the Maintenace Team
- [ ] Ask release notes from the PO. For milestones there are no patchinfos
- [ ] Submit the release notes
- [ ] Prepare a draft for the email announcement during the release date
