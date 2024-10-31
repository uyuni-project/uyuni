---
name: QE - Build Validation
about: QE - Use this template for a new build validation
title: "BV "
labels: ["qe-squad", "build validation", "high-priority"]
projects: ["SUSE/32"]
assignees: ''

---

## Information

- Planned start date:
- Real start date:
- Planned deadline:
- Real end date:

---

- Submission card:
- Jira card:
- Release notes draft:

---

- Jenkins pipeline:
- Server URL:

### Links

- [Step-by-step guide](https://confluence.suse.com/display/SUSEMANAGER/QE+Build+Validation)
- [Automated tests](https://confluence.suse.com/display/SUSEMANAGER/Automated+tests)
- [Pipeline parameters](https://confluence.suse.com/display/SUSEMANAGER/The+new+BV+pipeline)

## Manual tests and tasks

- [ ] Clean up the old environment
- [ ] Update the mirror/check if it is up to date

### Long running server

- [ ] Upgrade the long running [server](https://documentation.suse.com/external-tree/en-us/suma/4.3/en/suse-manager/installation-and-upgrade/upgrade-intro.html)
- [ ] And the [proxy](https://documentation.suse.com/external-tree/en-us/suma/4.3/en/suse-manager/installation-and-upgrade/proxy-y-z.html#_update_the_proxy_z)
- [ ] Then bootstrap a clean Salt Minion
- [ ] And perform some basic tests on it

### Release notes and new features

- [ ] Update and verify the release notes for server and proxy
- [ ] ...

### Verification of Bug fixes included in this MU

These bugs were fixed during the development cycle and were delivered together with the submissions.
We must verify them before approving the MU.

- [ ] ...

## Automated tests and tasks

- [ ] JSON creation
- [ ] All the supported systems were bootstrapped and passed smoke tests
- [ ] Product and Salt migration tests
- [ ] Containerized proxy passed using the [aggregate namespace](https://confluence.suse.com/display/SUSEMANAGER/MI+process+for+Containerized+components#MIprocessforContainerizedcomponents-WhathappenswhenwehaveanewMaintenanceUpdateinSUSEManager?).
  - Edit `/etc/sysconfig/uyuni-proxy-systemd-services` inside the Proxy VM and assure the namespace is correct:
    ```bash
    NAMESPACE=registry.suse.de/devel/galaxy/manager/mutesting/4.3/containers/suse/manager/4.3
    ```
- [ ] Retail
  - [ ] SLES12 SP5
  - [ ] SLES15 SP4

## Approval

- [ ] Ping our release engineers in Slack
- [ ] Approve all related MUs in the [IBS](https://smelt.suse.de/overview/?7=qam-manager#testing) or via the [command line](https://confluence.suse.com/display/SUSEMANAGER/QE+Build+Validation)

## Test suite fixes

- [ ] ...

## Reported and found Bugs

### Non-blocker

- [ ] ...

### Blocker

- [ ] ...

