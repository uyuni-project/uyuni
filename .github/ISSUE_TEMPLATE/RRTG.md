---
name:  RRTG
about: Use this template for RRTG role
title: 'RRTG Week '
labels: qa-squad, testsuite review, testsuite
assignees: ''

---

# Helpful tips for RRTG:
- [ ] Look at the previous RRTG card and link it here
- [ ] Update the topic in #galaxy-devel with your name and the [name of your helper](https://trello.com/c/PyagRara)
- [ ] Replace links with the links for the specific run you are debugging and edit the title of the card to include the week numbers
- [ ] Add below each of the links the failed tests
- [ ] Add who is working on it/ looking into it
- [ ] Add explanation of why it failed
- [ ] Go through the list of all branches every day to look for new failures
- [ ] Keep in mind that depending on the week, we need to be more focused on
some branches due to MU submissions being prepared. Check the calendar and make
sure that branch is more monitored
- [ ] See the [wiki entry](https://github.com/SUSE/spacewalk/wiki/The-Round-Robin-Testsuite-Geeko) for info on the role

# Links to testsuites:

[Head](https://ci.suse.de/view/Manager/view/Manager-Head/job/manager-Head-cucumber-NUE/)


[Uyuni](https://ci.suse.de/view/Manager/view/Uyuni/job/uyuni-master-cucumber-NUE/)


[4.1](https://ci.suse.de/view/Manager/view/Manager-4.1/job/manager-4.1-cucumber-PRV/)


[4.0](https://ci.suse.de/view/Manager/view/Manager-4.0/job/manager-4.0-cucumber-PRV/)


[3.2](https://ci.suse.de/view/Manager/view/Manager-3.2/job/manager-3.2-cucumber-NUE/)
Lower priority atm

[QAM setup](https://ci.suse.de/view/Manager/view/Manager-4.1/job/manager-4.1-qam-setup-cucumber/)
Has to be run before the next one

[QAM testsuite](https://ci.suse.de/view/Manager/view/Manager-4.1/job/manager-4.1-qam-cucumber/)
Only run after the setup above has finished

[openQA installation](https://ci.suse.de/view/Manager/view/Manager-4.1/job/manager-4.1-openqa-installation/)
