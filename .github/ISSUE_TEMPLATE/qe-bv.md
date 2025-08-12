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

---

### Links

- [Step-by-step guide](https://confluence.suse.com/display/SUSEMANAGER/QE+Build+Validation)
- [Automated tests](https://confluence.suse.com/display/SUSEMANAGER/Automated+tests)
- [Pipeline parameters](https://confluence.suse.com/display/SUSEMANAGER/The+new+BV+pipeline)

---

## Legend
- Selected checkbox means, we tested it
- :white_check_mark: : Test/verification was successful
- :x: : Test/verification was not successful
- :test_tube: : Test failed due to test suite issue but succeed manually
- If multiple emotes: task was run several times
  - Example: :x: :white_check_mark: = first run failed, second run passed (resubmission)

---

## Manual tests and tasks

- [ ] Clean up the old environment
- [ ] Update the mirror / check if it is up to date

---

## Automated tests and tasks

### Preparation

- [ ] JSON creation

### Proxy and Monitoring

- [ ] Bootstrap proxy setup
- [ ] Monitoring server setup

### Client Bootstrap and Smoke Tests

- [ ] All supported systems were bootstrapped and passed smoke tests
  - [ ] Client bootstrap stage
  - [ ] Client smoke tests

### Migration Tests

- [ ] Product and Salt migration tests
  - [ ] SLES15 SP6 minion → SP7
  - [ ] SLES15 SP6 SSH minion → SP7
  - [ ] SLE Micro 5.4 → 5.5
  - [ ] salt → salt bundle

### Retail

- [ ] SLES12 SP5
- [ ] SLES15 SP4

---

## Release notes and new features

- [ ] Update and verify the release notes for server and proxy
- [ ] ...

---

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
