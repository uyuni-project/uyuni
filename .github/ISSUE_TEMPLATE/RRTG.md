---
name:  RRTG
about: Use this template for the RRTG role
title: 'RRTG Week '
labels: qa-squad, testsuite review, testsuite
assignees: ''

---

# Helpful tips for the RRTG

- [ ] Look at the previous RRTG card and link it here
- [ ] Update the topic in #team-susemanager with your name and the name of your helper
- [ ] Replace links with the links for the specific run you are debugging and edit the title of the card to include the week numbers
- [ ] Add below each of the links the failed tests
- [ ] Add who is working on it/ looking into it
- [ ] Add explanation of why it failed
- [ ] Go through the list of all branches every day to look for new failures
- [ ] Keep in mind that depending on the week, we need to be more focused on
      some branches due to MU submissions being prepared. Check the calendar and make
      sure that branch is more monitored
- [ ] See the [wiki entry](https://github.com/SUSE/spacewalk/wiki/The-Round-Robin-Testsuite-Geeko) for info on the role

## Links to the testsuites

- [Head](https://ci.suse.de/view/Manager/view/Manager-Head/job/manager-Head-dev-acceptance-tests-NUE/)
- [Uyuni](https://ci.suse.de/view/Manager/view/Uyuni/job/uyuni-master-dev-acceptance-tests-NUE/)
- [4.2](https://ci.suse.de/view/Manager/view/Manager-4.2/job/manager-4.2-dev-acceptance-tests-PRV/)
- [4.1](https://ci.suse.de/view/Manager/view/Manager-4.1/job/manager-4.1-dev-acceptance-tests-PRV/)
- [4.3 BV testsuite](https://ci.suse.de/view/Manager/view/Manager-qa/job/manager-4.3-qa-build-validation/)
- [4.2 BV testsuite](https://ci.suse.de/view/Manager/view/Manager-qa/job/manager-4.2-qa-build-validation/)
- [4.1 BV testsuite](https://ci.suse.de/view/Manager/view/Manager-qa/job/manager-4.1-qa-build-validation/)
- [openQA installation](https://ci.suse.de/view/Manager/view/Manager-qa/job/manager-4.2-qa-openqa-installation/)

## Template for commenting

```md
## Monday, week 1

### Head

Run[#]()

### Uyuni

Run[#]()

### Manager 4.2

Run[#]()

### Manager 4.1

Run[#]()
```