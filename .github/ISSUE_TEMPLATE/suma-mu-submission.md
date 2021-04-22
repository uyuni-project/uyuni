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


# ToDo some days before

Add more tasks if needed.

- [ ] ask Joseph/Karl to submit a current version of the doc package to our Devel: project, and the changes to the repository used for documentation.suse.com (Doc and API, so later we can ping #doc.suse.com-request on release day)
- [ ] Confirm that doc is ready
- [ ] check salt package if all patches in testing are also available for the update, for both products and products:testing (not needed for MUs without client tools)
- [ ] Quick review changelogs with tito-wrapper and patch-creator, and request changes if needed
- [ ] check all testsuites - everything should be green

# ToDo during submission day:

Add more tasks if needed (for example, asking Maintenace to change the channel definitions).

- [ ] check all testsuites - everything should be green
- [ ] Tag and submit according to https://github.com/SUSE/spacewalk/wiki/Maintenance-Update-procedure
- [ ] Ping salt squad so they submit to SLE15SP1 and SLE15SP2 (not needed for MUs without client tools)
- [ ] Check if `golang-github-prometheus-node_exporter` is included in the SLE12 client tools submission, and if so, ping @witekest so he can prepare a submission for `SUSE:SLE-15-SP1:Update` (other codestreams will inherit, `SUSE:SLE-15:Update` is not needed because it's LTSS and soon EoL, and this package comes from Basesystem)
- [ ] Check if `grafana` is included in the SLE15 client tools submission, and if so, ping @witekest so he can prepare a submission for `SUSE:SLE-15:Update` (other codestreams will inherit)
- [ ] Send patchinfo (link) to PO for release notes
- [ ] Submit release notes
