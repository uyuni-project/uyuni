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
