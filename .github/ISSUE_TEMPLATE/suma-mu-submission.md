---
name: SUSE Manager MU submission
about: Use this template for SUSE Manager MU submissions
title: ''
labels: hexagon
assignees: 'juliogonzalez'

---

# Important changes, for release notes:

- 

# Deadline


# ToDo some days before

Add more tasks if needed.

- [ ] ask Joseph/Karl to submit a current version of the doc package to our Devel: project, and the changes to the repository used for documentation.suse.com (so later we can ping #doc.suse.com-request on release day)
- [ ] Confirm that doc is ready
- [ ] check salt package if all patches in testing are also available for the update, for both products and products:testing (not needed for MUs without client tools)
- [ ] Quick review changelogs with tito-wrapper and patch-creator, and request changes if needed
- [ ] check all testsuites - everything should be green

# ToDo during submission day:

Add more tasks if needed (for example, asking Maintenace to change the channel definitions).

- [ ] check all testsuites - everything should be green
- [ ] Tag and submit according to https://github.com/SUSE/spacewalk/wiki/Maintenance-Update-procedure
- [ ] Ping salt squad so they submit to SLE15SP1 and SLE15SP2 (not needed for MUs without client tools)
- [ ] Send patchinfo (link) to PO for release notes
- [ ] Submit release notes
