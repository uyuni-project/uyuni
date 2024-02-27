---
name:  RRTG
about: Use this template for the RRTG role
title: "RRTG Week "
labels: ["qe-squad", "testsuite review", "testsuite"]
projects: ["SUSE/32"]
assignees: ''

---

### 💥 Blockers

*Blockers found in a CI of a branch that impedes to submit that branch*

**Version reviewed to be submitted:** 4.x / Uyuni

- Keep in mind that depending on the week, we need to be more focused on some branches due to MU submissions being prepared.
- Please, check the [calendar](https://confluence.suse.com/display/SUSEMANAGER/Release+calendar) and edit the field above.

**Lists of blockers:**

- [ ] 🛑 Short description
  - Bugzilla link:
  - Related card:
- [ ] 🛑 Short description
  - Bugzilla link:
  - Related card:
- [ ] 🛑 Short description
  - Bugzilla link:
  - Related card:

**Notes:**

- Add additional blockers to the list following the same format
- Duplicate this section if you review multiple branches whom must be submitted during your card.
- As soon as a blocker is resolved, mark it on the list. This list must be updated ASAP, release engineers are monitoring it. Furthermore, we need to track all the issues that could delay a submission
- Add additional information to a blocker in a new comment below and keep that list clean and simple

---

### ℹ️ Useful information

- **[SUMA Test suite status board](https://github.com/orgs/SUSE/projects/23/views/3)**

- **Reminders**:
  - Update the topic in #team-susemanager with your name
  - Edit the title of the card to include the week numbers
  - Review the test report and compare failures within the issues in the [test suite board](https://github.com/orgs/SUSE/projects/23/views/3)
  - Create new cards for each new issue following this title format: `Feature <title> | Scenario <name>`
  - Label the cards with the version the issue was found for: "xxx_ci", i.e. `4.3_ci`
  - You can also compare the new failures with older versions from the [Grafana Test Report timeline (Features/Version)](http://grafana.mgr.suse.de/d/GreziyMMk/testsuites-wip-time-perspective?orgId=1&from=now-3d&to=now)
  - See the [RRTG introduction](https://confluence.suse.com/display/SUSEMANAGER/The+Round+Robin+Testsuite+Geeko) for more information on the role

- **Links to the test suites**:
  - [Head](https://ci.suse.de/view/Manager/view/Manager-Head/job/manager-Head-dev-acceptance-tests-NUE/)
  - [Uyuni](https://ci.suse.de/view/Manager/view/Uyuni/job/uyuni-master-dev-acceptance-tests-NUE/)
  - [4.3 NUE](https://ci.suse.de/view/Manager/view/Manager-4.3/job/manager-4.3-dev-acceptance-tests-NUE/), [4.3 PRV](https://ci.suse.de/view/Manager/view/Manager-4.3/job/manager-4.3-dev-acceptance-tests-PRV/)
  - [Uyuni Podman](https://ci.suse.de/view/Manager/view/Uyuni/job/uyuni-master-dev-acceptance-tests-podman/)
  - [Uyuni K3s](https://ci.suse.de/view/Manager/view/Uyuni/job/uyuni-master-dev-acceptance-tests-K3S/)
  - [openQA offline installation (4.3)](https://ci.suse.de/view/Manager/view/Manager-qe/job/manager-4.3-qe-openqa-offline-installation/)
  - [openQA online installation (4.3)](https://ci.suse.de/view/Manager/view/Manager-qe/job/manager-4.3-qe-openqa-installation-online/)
