---
name: Leavers checklist
about: Use this template when someone leaves the SUSE Manager team
title: 'Leaver <name>'
labels: team
assignees: ''
---

General template for leavers of the SUSE Manager team.

# Leaving date

**Official:** YYYY-MM-DD
**Last work day:** YYYY-MM-DD

# Some days before the member's last work day

- [ ] **General SUSE Procedure:** Team Lead to review the [team members leaving the company](https://intra.suse.net/company/company-news/department/hr/team-leader-hr-quick-links/), in particular the `SUSE Termination Information Guide`? Those documents contain information about everything that needs to be returned, and things to be done by the team lead and the team member (not just hardware!)
- [ ] **Exit interview:** Conduct an [exit interview](https://en.wikipedia.org/wiki/Exit_interview) and store the notes at [Confluence](https://confluence.suse.com/display/SUSEMANAGER/Exit+Interviews)
- [ ] **Hardware:** 
  - [ ] Does this person have access to something special which also needs to get transferred to another person? (SSH access to a server, permissions to special pages on wikis, Confluence, resources in the cloud, etc.)
  - [ ] Did IT create a ticket and sent an email to the leaver, explaining how to return their personal hardware?
- [ ] **Bugzilla:** Remind the person to remove all ["watches"](https://bugzilla.suse.com/userprefs.cgi?tab=email) in Bugzilla before leaving
- [ ] **Bugzilla:** Review the [responsibles for the bugzilla team accounts](https://confluence.suse.com/display/IAM/5.+UCS+Role+Model+and+Entitlements+Management#id-5.UCSRoleModelandEntitlementsManagement-Bot/Serviceentitlements) and nominate a new one if needed
- [ ] **Packages:** Run `OUSER=<LEAVER>; osc search -i ${OUSER}; osc -A https://api.suse.de maintainer -U ${OUSER}` (replace `<LEAVER>` with the leaver's username), and if the person is a bugowner of any package, look for a new bugowner (ideally a group, not a single person, home projects can be ignored)
- [ ] **Code:** Consider any code stored in personal accounts and create forks accordingly (e.g. in GitLab)
- [ ] **Credentials:** Consider any credentials known only to that person (e.g. for mailing lists) and share
- [ ] **Mailing lists:** Transfer mailing list administration to someone else
- [ ] **Content:** Consider any content in e.g. https://w3.suse.de or Google Drive and maybe share it permanently (e.g. recordings, slides)

# The day after the member leaves the team

- [ ] GitHub
  - [ ] Remove from [SUSE Manager Team](https://github.com/orgs/SUSE/teams/suse-manager-team/members) on GitHub
  - [ ] Remove from [SUSE Manager Administrators](https://github.com/orgs/SUSE/teams/suse-manager-administrators/members) on GitHub
  - [ ] Remove from [SUSE Manager CI Administrators](https://github.com/orgs/SUSE/teams/suse-manager-ci-administrators/members) on GitHub
  - [ ] Remove from [Uyuni organization](https://github.com/orgs/uyuni-project/people) and [teams](https://github.com/orgs/uyuni-project/teams). It can remain a member (but not owner), if it will keep contributing.
- [ ] Trello access
  - [ ] Tell the person to leave all SUSE Trello boards, otherwise, they might become paid guests
  - The account will be removed from the SUSE organization 30 days after termination date, when the OKTA account is removed.
- [ ] Confluence
  - [ ] Remove from [Confluence overview page](https://confluence.suse.com/display/SUSEMANAGER/SUSE+Manager) of the team
  - [ ] Remove from [SUMA squads](https://confluence.suse.com/display/SUSEMANAGER/Squads%2C+People+and+Topics) page
  - [ ] Clean up the [Ownership of topics and features](https://confluence.suse.com/display/SUSEMANAGER/Squads%2C+People+and+Topics) if applicable
- [ ] Mailing lists
  - [ ] [galaxy-alerts](https://mailman.suse.de/mailman/admin/galaxy-alerts/members/remove)
  - [ ] [galaxy-bugs](https://mailman.suse.de/mailman/admin/galaxy-bugs/members/remove)
  - [ ] [galaxy-ci](https://mailman.suse.de/mailman/admin/galaxy-ci/members/remove)
  - [ ] [galaxy-devel](https://mailman.suse.de/mailman/admin/galaxy-devel/members/remove)
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
  - [ ] Remove from groups (left menu -> `...` -> `People` -> `User Groups`)
    - [ ] `susemanager-engineers`
    - [ ] `susemanager-infra`, if leaver is part of the [Infra squad](https://confluence.suse.com/display/SUSEMANAGER/Squads%2C+People+and+Topics)
    - [ ] `susemanager-coordinators`, if leaver is a [squad coordinator](https://confluence.suse.com/display/SUSEMANAGER/Squads%2C+People+and+Topics)
    - [ ] Squad specific group
- [ ] Remove from Google [SUSE Manager Engineering Team](https://groups.google.com/a/suse.com/g/suma-all/members) and squads groups (if available)
- [ ] Hardware
  - [ ] Collect remaining equipment, as explained via ticket created ahead of the termination date.
  - [ ] Are there somewhere machines (e.g. RPI) or VMs around managed by this person? Stop them or transfer the management to other team members
- [ ] Cloud resources (accounts for each cloud at the [Landing Zones](https://confluence.suse.com/display/CCOE/Cloud+Landing+Zone+Access#CloudLandingZoneAccess-OktaGroupNaming/PermissionScheme#Roles) confluence page).
  - [ ] AWS
  - [ ] Azure
  - [ ] Google Cloud
- [ ] Remove from the relevant IBS/OBS groups:
  - [ ] Remove from https://build.suse.de/groups/monitoring (if applicable)
  - [ ] Remove from https://build.suse.de/groups/salt-maintainers (if applicable)
  - [ ] Remove from https://build.suse.de/groups/scap-security-guide-maintainers (if applicable)
  - [ ] Remove from https://build.suse.de/groups/suse-manager-maintainers (if applicable)
  - [ ] Remove from https://build.suse.de/groups/suse-manager-developers (if applicable)
  - [ ] Remove from https://build.suse.de/groups/susemanager-releng (if applicable)
  - [ ] Remove from https://build.opensuse.org/groups/uyuni-maintainers (if applicable)
  - [ ] Remove from https://build.opensuse.org/groups/salt-maintainers (if applicable)
- [ ] Internal
  - [ ] Remove SSH public key from [SUMA infrastructure](https://gitlab.suse.de/galaxy/infrastructure/-/blob/master/srv/salt/ssh/init.sls)
  - [ ] Remove user from SCC organization [SUSE Manager Team Playground](https://scc.suse.com/organizations/432530/users) for SCC mirror credentials
  - [ ] Remove user from SCC organization [SUSE Manager Stable (Infra/CI/MU validation)](https://scc.suse.com/organizations/784242/users) for SCC mirror credentials
  - [ ] Remove SUMA account on [manager.mgr.suse.de](https://manager.mgr.suse.de)
  - [ ] Buildservice - Bugowner: is the person the bug owner of some packages? Find a new one and remove the mail address for maintainers and bug owners. Also projects where this person is the only maintainer needs to be transferred.
  - [ ] Remove the person from our [GitLab group](https://gitlab.suse.de/groups/galaxy/-/group_members)
  - [ ] Update the [finglonger config](https://gitlab.suse.de/galaxy/infrastructure/-/blob/master/srv/salt/bugguy-finglonger/galaxy.edn)
- [ ] QE specific
  - [ ] IBS/OBS: Remove from the https://build.suse.de/groups/qam-manager group
  - [ ] Remove QE account on [manager.mgr.suse.de](https://manager.mgr.suse.de) for the QE organization
  - [ ] Remove user entries for private hypervisor from [SUMA infrastructure](https://gitlab.suse.de/galaxy/infrastructure/-/blob/master/srv/salt/qa/users/init.sls)
  - [ ] Remove user entries for DNS from [SUMA infrastructure](https://gitlab.suse.de/galaxy/infrastructure/-/tree/master/srv/salt/bind-server)
  - [ ] Remove user entries for DHCP from [SUMA infrastructure](https://gitlab.suse.de/galaxy/infrastructure/-/tree/master/srv/salt/dhcpd-server)
  - [ ] Remove from [galaxy-qa](https://mailman.suse.de/mailman/admin/galaxy-qa/members/remove) mailing list
  - [ ] Remove from [SUSE Manager QE Team](https://github.com/orgs/SUSE/teams/suse-manager-qe/members) on GitHub
  - [ ] Remove from [Uyuni QE developer Team](https://github.com/orgs/uyuni-project/teams/qe) on GitHub
  - [ ] Remove from [QE Retrospective project](https://github.com/orgs/SUSE/projects/54) on GitHub
  - [ ] Remove from [QE project board member list](https://github.com/orgs/SUSE/projects/32/views/1?pane=info) on GitHub
  - [ ] Remove from the Google [SUMA QE Squad](https://groups.google.com/a/suse.com/g/suma-qe/members) group
