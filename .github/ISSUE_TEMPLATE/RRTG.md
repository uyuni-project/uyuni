---
name:  RRTG
about: Use this template for the RRTG role
title: 'RRTG Week '
labels: qe-squad, testsuite review, testsuite
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

- Add aditional blockers to the list following the same format
- Duplicate this section if you review multiple branches whom must be submitted during your card.
- As soon as a blocker is resolved, mark it on the list. This list must be updated ASAP, release engineers are monitoring it, also we need to track all the issues that could delay a submission
- Add additional information to a blocker in a new comment on the card, keep that list clean and simple.

---

### ℹ️ Useful information

- **[SUMA Test suite status board](https://github.com/orgs/SUSE/projects/23/views/3)**

- **Reminders**:
  - Update the topic in #team-susemanager with your name
  - Edit the title of the card to include the week numbers
  - Review the test report and compare issues within the issues in the [board](https://github.com/orgs/SUSE/projects/23/views/3)
  - Create new cards for each new issue following this title format: Feature title (and if necessary add also Scenario title) 
  - Label the cards with the version the issue was found: "xxx_ci", i.e. `4.3_ci`
  - You can also compare your reds with other versions from the [Grafana Test Report timeline (Features/Version)](http://grafana.mgr.suse.de/d/GreziyMMk/testsuites-wip-time-perspective?orgId=1&from=now-3d&to=now)
  - See the [RRTG introduction](https://confluence.suse.com/display/SUSEMANAGER/The+Round+Robin+Testsuite+Geeko) for info on the role

- **Links to the test suites**:
  - [Head](https://ci.suse.de/view/Manager/view/Manager-Head/job/manager-Head-dev-acceptance-tests-NUE/)
  - [Uyuni](https://ci.suse.de/view/Manager/view/Uyuni/job/uyuni-master-dev-acceptance-tests-NUE/)
  - [4.3](https://ci.suse.de/view/Manager/view/Manager-4.3/job/manager-4.3-dev-acceptance-tests-PRV/)
  - [4.2](https://ci.suse.de/view/Manager/view/Manager-4.2/job/manager-4.2-dev-acceptance-tests-PRV/)
  - [openQA installation](https://ci.suse.de/view/Manager/view/Manager-qa/job/manager-4.2-qa-openqa-installation/)
