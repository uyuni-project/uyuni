---
name: SUSE Manager MU submission
about: Use this template for SUSE Manager MU submissions
title: 'X.Y Maintenance Update submission (X.Y.Z)'
labels: vega-squad
assignees: 'juliogonzalez'

---

# Important changes, for release notes:

- 

# Deadline

# Procedure

https://github.com/SUSE/spacewalk/wiki/Maintenance-Update-procedure

# ToDo some days before

Add more tasks if needed.

- [ ] For any new packages that will be added to the codestreams, fetch the groups what will maintain them (one per package)
- [ ] Ask Joseph/Karl to submit a current version of the doc package to our Devel: project, and the changes to the repository used for documentation.suse.com (they need to provide a URL for a MR)
- [ ] Confirm that doc is ready
- [ ] Check salt package if all patches in testing are also available for the update, for both products and products:testing (not needed for MUs without client tools)
- [ ] Quick review changelogs with tito-wrapper and patch-creator, and request changes if needed
- [ ] Check all testsuites - everything should be green, or otherwise submission must be approved by QA

# ToDo during submission day:

Add more tasks if needed (for example, asking Maintenace to change the channel definitions).

- [ ] Check all testsuites - everything should be green, or otherwise submission must be approved by QA
- [ ] Tag and submit
- [ ] Ping salt squad so they submit to SLE15SP1 and SLE15SP2 (not needed for MUs without client tools)
- [ ] Check if `golang-github-prometheus-node_exporter` is included in the SLE12 client tools submission, and if so, ping @witekest so he can prepare a submission for `SUSE:SLE-15-SP1:Update` (other codestreams will inherit, `SUSE:SLE-15:Update` is not needed because it's LTSS and soon EoL, and this package comes from Basesystem)
- [ ] Check if `grafana` is included in the SLE15 client tools submission, and if so, ping @witekest so he can prepare a submission for `SUSE:SLE-15:Update` (other codestreams will inherit)
- [ ] Send patchinfo (link) to PO for release notes
- [ ] Submit release notes
