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

https://github.com/SUSE/spacewalk/wiki/Maintenance-Update-procedure

# ToDo some days before

Add more tasks if needed.

- [ ] For any new packages that will be added to the codestreams, fetch the groups what will maintain them (one per package)
- [ ] Ask the Doc Squad to submit an update of the doc package to the relevant `Devel:Galaxy:Manager:X.Y` project, and a MR for the [repository](https://gitlab.suse.de/susedoc/docserv-external-tree-suma) used for documentation.suse.com (they need to give us a URL for a MR)
- [ ] Ask @mcalmer (backup: @parlt91) to prepare the PR for the code translations. They will ask a brief period of branch freeze to prepare it. Ideally this should happen close to the branch freeze date.
- [ ] Confirm that the documentation is ready
- [ ] Ask the Ion Squad to promote all salt versions that have updates (not needed for MUs without salt/client tools)
- [ ] Quick review changelogs with `tito-wrapper`, and request changes if needed
- [ ] Check all the tests: everything should be green, or otherwise submission must be approved by QA

# ToDo during the submission window

Add more tasks if needed (for example, asking Maintenace to change the channel definitions).

- [ ] Check all the tests: everything should be green, or otherwise submission must be approved by QA
- [ ] Increase version number in web/conf/rhn_web.conf (x.y.z+1) in Manager-X.Y. branch
- [ ] Check if the schema directory exists with the correct versions (more at https://github.com/SUSE/spacewalk/wiki/Maintenance-Update-procedure)
- [ ] Check if schema migration directories exist between older and newer SUSE Manager version (more at https://github.com/SUSE/spacewalk/wiki/Maintenance-Update-procedure)
- [ ] Push changes to Manager-X.Y branch
- [ ] Tag everything with `tito`
- [ ] Create Manager-X.Y-MU.X.Y.Z branch
- [ ] Check manager-x.y-releng-2obs job has packaged latest changes from Manager-X.Y branch into ibs://Devel:Galaxy:Manager:X.Y
- [ ] Prepare the submissions with `patch-creator`
- [ ] Ping the Ion squad so they submit salt to `SLE15SP1`, `SLE15SP2` and `SLE15SP3` (not needed for MUs without salt/client tools)
- [ ] Check if `golang-github-prometheus-node_exporter` is included in the SLE12 client tools submission, and if so, ping @witekest so he can prepare a submission for `SUSE:SLE-15-SP1:Update` (other codestreams will inherit, `SUSE:SLE-15:Update` is not needed because it's LTSS and soon EoL, and this package comes from Basesystem)
- [ ] Check if `grafana` is included in the SLE15 client tools submission, and if so, ping @witekest so he can prepare a submission for `SUSE:SLE-15-SP2:Update` (other codestreams will inherit)
- [ ] Create the JIRA ticket with all the submissions and ping the Maintenace Team
- [ ] Send an email with the links for the patchinfos for the Server and the Proxy, and a link to this card to PO, so the release notes are generated and later submitted by the PO
- [ ] Submit the release notes
