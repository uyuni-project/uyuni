---
name: Leavers checklist
about: Use this template when someone leaves the SUSE Manager team
title: 'Leaver <name>'
labels: team
assignees: ''
---

General template for leavers of the SUSE Manager team.

- [ ] Conduct an [exit interview](https://en.wikipedia.org/wiki/Exit_interview)
- [ ] GitHub
  - [ ] Remove from [SUSE Manager Team](https://github.com/orgs/SUSE/teams/suse-manager-team/members) on GitHub
  - [ ] Remove from [Uyuni organization](https://github.com/orgs/uyuni-project/people) and [teams](https://github.com/orgs/uyuni-project/teams). It can remain a member (but not owner), if it will keep contributing.
- [ ] Trello access
  - [ ] Reach out to trello-owners@suse.de for removal from SUSE organization in Trello
  - [ ] Tell the person to leave all SUSE Trello boards, otherwise, they might become paid guests
- [ ] Confluence
  - [ ] Remove from [Confluence overview page](https://confluence.suse.com/display/SUSEMANAGER/SUSE+Manager) of the team
  - [ ] Remove from [SUMA squads](https://confluence.suse.com/display/SUSEMANAGER/Squads%2C+People+and+Topics) page
  - [ ] Clean up the [Ownership of topics and features](https://confluence.suse.com/display/SUSEMANAGER/Squads%2C+People+and+Topics) if applicable
- [ ] Mailing lists
  - [ ] [galaxy-alerts](https://mailman.suse.de/mailman/admin/galaxy-alerts/members/remove)
  - [ ] [galaxy-bugs](https://mailman.suse.de/mailman/admin/galaxy-bugs/members/remove)
  - [ ] [galaxy-ci](https://mailman.suse.de/mailman/admin/galaxy-ci/members/remove)
  - [ ] [galaxy-devel](https://mailman.suse.de/mailman/admin/galaxy-devel/members/remove)
  - [ ] [galaxy-docs](https://mailman.suse.de/mailman/admin/galaxy-docs/members/remove)
  - [ ] [galaxy-infra](https://mailman.suse.de/mailman/admin/galaxy-infra/members/remove)
  - [ ] [galaxy-noise](https://mailman.suse.de/mailman/admin/galaxy-noise/members/remove)
  - [ ] [galaxy-releng](https://mailman.suse.de/mailman/admin/galaxy-releng/members/remove)
  - [ ] [galaxy-team](https://mailman.suse.de/mailman/admin/galaxy-team/members/remove)
  - [ ] [salt](https://mailman.suse.de/mailman/admin/salt/members/remove)
  - [ ] [salt-maintainers](https://mailman.suse.de/mailman/admin/salt-maintainers/members/remove)
  - [ ] [suse-manager](https://mailman.suse.de/mailman/admin/suse-manager/members/remove)
  - [ ] [tomcat-maintainers](https://mailman.suse.de/mailman/admin/tomcat-maintainers/members/remove)
  - [ ] [uyuni-leader](https://mailman.suse.de/mailman/admin/uyuni-leader/members/remove)
  - [ ] uyuni announce as [user](https://lists.opensuse.org/manage/lists/announce.lists.uyuni-project.org/members/member/) (to prevent bounces later), and maybe [moderator](https://lists.opensuse.org/manage/lists/announce.lists.uyuni-project.org/members/moderator/) and [owner](https://lists.opensuse.org/manage/lists/announce.lists.uyuni-project.org/members/owner/)
- [ ] Remove from meeting invitations/calendars (in case the leaver got the meeting with a forward)
  - [ ] SUSE Manager Team Review
  - [ ] Priorities, Help and Planning (PHP)
  - [ ] SUSE Manager Team Retrospective
- [ ] Slack
  - [ ] Update workflows (daily standup, meetings, etc.)
  - [ ] Remove from user groups (`susemanager-engineers` and squad specific groups, open a ticket on https://sd.suse.com)
- [ ] Remove from outlook groups of the [SUSE Manager Engineering Team](https://outlook.office.com/people/group/mysuse.onmicrosoft.com/suma-all) and squads (if available)
- [ ] Remove from outlook group of the [DCM department](https://outlook.office.com/people/group/mysuse.onmicrosoft.com/dcm)
- [ ] Hardware
  - [ ] Collect remaining equipment via ticket
  - [ ] Are there somewhere machines (e.g. RPI) or VMs around managed by this person? Stop them or transfer the management to other team members
- [ ] AWS/Azure/Google Cloud access
  - [ ] Remove the person from the IAM of the service and remove any leftover resource
- [ ] Remove from the relevant IBS/OBS groups:
  - [ ] Remove from https://build.suse.de/groups/monitoring (if applicable)
  - [ ] Remove from https://build.suse.de/groups/salt-maintainers (if applicable)
  - [ ] Remove from https://build.suse.de/groups/scap-security-guide-maintainers (if applicable)
  - [ ] Remove from https://build.suse.de/groups/suse-manager-maintainers (if applicable)
  - [ ] Remove from https://build.suse.de/groups/suse-manager-developers (if applicable)
  - [ ] Remove from https://build.suse.de/groups/susemanager-releng (if applicable)
  - [ ] Remove from https://build.suse.de/groups/qam-manager (if applicable)
  - [ ] Remove from https://build.opensuse.org/groups/uyuni-maintainers (if applicable)
- [ ] Internal
  - [ ] Remove SSH public key from [SUMA infrastructure](https://gitlab.suse.de/galaxy/infrastructure/-/blob/master/srv/salt/ssh/init.sls)
  - [ ] Remove user from SCC organization [SUSE Manager Team Playground](https://scc.suse.com/organizations/432530/users) for SCC mirror credentials
  - [ ] Remove SUMA account on [manager.mgr.suse.de](https://manager.mgr.suse.de)
  - [ ] Buildservice - Bugowner: is the person the bug owner of some packages? Find a new one and remove the mail address for maintainers and bug owners. Also projects where this person is the only maintainer needs to be transferred.
  - [ ] Does this person have access to something special which also needs to get transferred to another person? (ssh access to a server, permissions to special pages on wikis, confluence, resources in the cloud, etc.)
  - [ ] Bugzilla: remind the person to remove all "watches" in Bugzilla before leaving
  - [ ] Consider any code stored in personal accounts and create forks accordingly (e.g. in GitLab)
  - [ ] Consider any credentials known only to that person (e.g. for mailing lists) and share
  - [ ] Transfer mailing list administration to someone else
  - [ ] Consider any content in e.g. w3.suse.de or o365 and maybe share it permanently (e.g. recordings, slides)
  - [ ] Remove the person from our [GitLab group](https://gitlab.suse.de/groups/galaxy/-/group_members)
  - [ ] Update the [finglonger config](https://gitlab.suse.de/galaxy/infrastructure/-/blob/master/srv/salt/bugguy-finglonger/galaxy.edn) and [ION squad finglonger config](https://gitlab.suse.de/galaxy/infrastructure/-/blob/master/srv/salt/bugguy-finglonger/salt.edn)