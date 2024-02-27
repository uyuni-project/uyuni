---
name:  Build Validation
about: Use this template for a new build validation
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

## Tasks

### Main Manual tests

- [ ] Upgrade the long running server and proxy (and bootstrap a clean Salt Minion) [following the upgrade procedure for the server](https://documentation.suse.com/external-tree/en-us/suma/4.3/en/suse-manager/installation-and-upgrade/upgrade-intro.html) and the [proxy](https://documentation.suse.com/external-tree/en-us/suma/4.3/en/suse-manager/installation-and-upgrade/proxy-y-z.html#_update_the_proxy_z)

### Bug fixes included in this MU

These bugs were fixed during the development cycle, they are P1 or P2.
They are delivered together with submissions and we must verify them before approving the MU.

- [ ]

### Automated BV

- [ ] JSON creation
- [ ] All the supported systems were bootstrapped and passed smoke tests
  - [ ] alma9_minion
  - [ ] alma9_ssh_minion
  - [ ] centos7_client
  - [ ] centos7_minion
  - [ ] centos7_ssh_minion
  - [ ] debian10_minion
  - [ ] debian10_ssh_minion
  - [ ] debian11_minion
  - [ ] debian11_ssh_minion
  - [ ] debian12_minion
  - [ ] debian12_ssh_minion
  - [ ] liberty9_minion
  - [ ] liberty9_ssh_minion
  - [ ] oracle9_minion
  - [ ] oracle9_ssh_minion
  - [ ] rocky8_minion
  - [ ] rocky8_ssh_minion
  - [ ] rocky9_minion
  - [ ] rocky9_ssh_minion
  - [ ] sle12sp5_client
  - [ ] sle12sp5_minion
  - [ ] sle12sp5_ssh_minion
  - [ ] sle15sp1_client
  - [ ] sle15sp1_minion
  - [ ] sle15sp1_ssh_minion
  - [ ] sle15sp2_client
  - [ ] sle15sp2_minion
  - [ ] sle15sp2_ssh_minion
  - [ ] sle15sp3_client
  - [ ] sle15sp3_minion
  - [ ] sle15sp3_ssh_minion
  - [ ] sle15sp4_client
  - [ ] sle15sp4_minion
  - [ ] sle15sp4_ssh_minion
  - [ ] sle15sp5_client
  - [ ] sle15sp5_minion
  - [ ] sle15sp5_ssh_minion
  - [ ] slemicro51_minion
  - [ ] slemicro51_ssh_minion
  - [ ] slemicro52_minion
  - [ ] slemicro52_ssh_minion
  - [ ] slemicro53_minion
  - [ ] slemicro53_ssh_minion
  - [ ] slemicro54_minion
  - [ ] slemicro54_ssh_minion
  - [ ] slemicro55_minion
  - [ ] slemicro55_ssh_minion
  - [ ] ubuntu2004_minion
  - [ ] ubuntu2004_ssh_minion
  - [ ] ubuntu2204_minion
  - [ ] ubuntu2204_ssh_minion
  - [ ] opensuse154arm_minion
  - [ ] opensuse154arm_ssh_minion
  - [ ] opensuse155arm_minion
  - [ ] opensuse155arm_ssh_minion
  - [ ] sles15sp5s390_minion
  - [ ] sles15sp5s390_ssh_minion
- [ ] Containerized proxy passed using the aggregate namespace [please set it up](https://confluence.suse.com/display/SUSEMANAGER/MI+process+for+Containerized+components#MIprocessforContainerizedcomponents-Finalcurrentsolution)
  Make sure to read the info and ask maint-coord to create the release requests

```bash
Edit `/etc/sysconfig/uyuni-proxy-systemd-services` inside the Pod Proxy VM
In the NAMESPACE, use this link exactly:
http://registry.suse.de/devel/galaxy/manager/mutesting/4.3/containers/suse/manager/4.3/
```

- [ ] Retail
  - [ ] SLES12 SP5
  - [ ] SLES15 SP4
- [ ] Release Notes

### New features announced in the [release notes](https://gitlab.suse.de/documentation/release-notes-suse-manager/-/merge_requests/)

- ...

### Blocker bugs found in this version

- [ ]

### Non-blocker bugs found in this version

- [ ]
