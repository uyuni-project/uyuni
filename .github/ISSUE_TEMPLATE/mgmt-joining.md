---
name: New joiner checklist
about: Use this template when someone joins the SUSE Manager team
title: 'New joiner <name>'
labels: team
assignees: ''
---

General template for new joiners of the SUSE Manager team.

- [ ] Go through the general [onboarding guide](https://geekos.io/onboarding) for new employees.
- [ ] GitHub account with SUSE email address
  - [ ] [Add to SUSE org](https://confluence.suse.com/pages/viewpage.action?spaceKey=IAM&title=Github+account+and+access)
  - [ ] Add to [SUSE Manager Team](https://github.com/orgs/SUSE/teams/suse-manager-team/) on GitHub
  - [ ] Add to [Uyuni organization and teams](https://github.com/orgs/uyuni-project/people) (@juliogonzalez or [another owner](https://github.com/orgs/uyuni-project/people?query=role%3Aowner))
- [ ] Add SUMA account on [manager.mgr.suse.de](https://manager.mgr.suse.de)
- [ ] [Trello account](https://confluence.suse.com/display/IAM/Trello+account+and+access)
- [ ] Confluence
  - [ ] Add to [SUMA squads](https://confluence.suse.com/display/SUSEMANAGER/Squads%2C+People+and+Topics) page
  - [ ] Add to [Confluence overview page](https://confluence.suse.com/display/SUSEMANAGER/SUSE+Manager) of the team
- [ ] Subscribe the user to the following mailing lists:
  - [ ] [galaxy-devel](https://mailman.suse.de/mailman/admin/galaxy-devel/members/add)
  - [ ] [galaxy-team](https://mailman.suse.de/mailman/admin/galaxy-team/members/add)
  - [ ] [suse-manager](https://mailman.suse.de/mailman/admin/suse-manager/members/add)
  - According to the role and squad, others could be needed:
    - [ ] [galaxy-alerts](https://mailman.suse.de/mailman/admin/galaxy-alerts/members/add)
    - [ ] [galaxy-bugs](https://mailman.suse.de/mailman/admin/galaxy-bugs/members/add)
    - [ ] [galaxy-ci](https://mailman.suse.de/mailman/admin/galaxy-ci/members/add)
    - [ ] [galaxy-docs](https://mailman.suse.de/mailman/admin/galaxy-docs/members/add)
    - [ ] [galaxy-infra](https://mailman.suse.de/mailman/admin/galaxy-infra/members/add)
    - [ ] [galaxy-noise](https://mailman.suse.de/mailman/admin/galaxy-noise/members/add)
    - [ ] [galaxy-releng](https://mailman.suse.de/mailman/admin/galaxy-releng/members/add)
    - [ ] [salt](https://mailman.suse.de/mailman/admin/salt/members/add)
    - [ ] [salt-maintainers](https://mailman.suse.de/mailman/admin/salt-maintainers/members/add)
    - [ ] [tomcat-maintainers](https://mailman.suse.de/mailman/admin/tomcat-maintainers/members/add)
    - [ ] [uyuni-leader](https://mailman.suse.de/mailman/admin/uyuni-leader/members/add)
- [ ] Tell the user to subscribe to the following lists:
  - [ ] [Uyuni announce](https://lists.opensuse.org/archives/list/announce@lists.uyuni-project.org/) (mandatory)
  - [ ] [SUSE](https//mailman.suse.de), at the very least [devel](https://mailman.suse.de/mailman/listinfo/devel), [users](https://mailman.suse.de/mailman/listinfo/users), recommended subscribing to [research](https://mailman.suse.de/mailman/listinfo/research) and [results](https://mailman.suse.de/mailman/listinfo/results)
  - [ ] [openSUSE](https://lists.opensuse.org) (whatever is interesting for the user)
- [ ] Slack
  - [ ] Update workflows (daily standup, meetings, etc.)
  - [ ] Add to user groups (`susemanager-engineers` and squad specific groups, open a ticket on https://sd.suse.com)
- [ ] Add the user to [SUSE Manager Engineering Team](https://outlook.office.com/people/group/mysuse.onmicrosoft.com/suma-all) and squad groups (if available), to get access to the calendar (PHP, Retrospective, and all other events), and to emails send to the mailing list for the group
- [ ] Add SSH public key to [SUMA infrastructure](https://gitlab.suse.de/galaxy/infrastructure/-/blob/master/srv/salt/ssh/init.sls)
- [ ] Provide default passwords if applicable (testsuite VMs, maybe https://gitlab.suse.de/galaxy/credentials)
- [ ] Add user to SCC organization [SUSE Manager Team Playground](https://scc.suse.com/organizations/432530/users) for SCC mirror credentials
- [ ] Add to the relevant IBS/OBS groups:
  - [ ] Add to https://build.suse.de/groups/monitoring (if applicable)
  - [ ] Add to https://build.suse.de/groups/salt-maintainers (if applicable)
  - [ ] Add to https://build.suse.de/groups/scap-security-guide-maintainers (if applicable)
  - [ ] Add to https://build.suse.de/groups/suse-manager-maintainers (if applicable)
  - [ ] Add to https://build.suse.de/groups/suse-manager-developers (if applicable)
  - [ ] Add to https://build.suse.de/groups/susemanager-releng (if applicable)
  - [ ] Add to https://build.suse.de/groups/qam-manager (if applicable)
  - [ ] Add to https://build.opensuse.org/groups/uyuni-maintainers (if applicable)
- [ ] Add the new employee to our [GitLab group](https://gitlab.suse.de/groups/galaxy/-/group_members)
- [ ] Schedule new joiner meetups with at least one [member from each squad](https://confluence.suse.com/x/OIGAOQ), with the PO and with the architect
- [ ] Update the [finglonger config](https://gitlab.suse.de/galaxy/infrastructure/-/blob/master/srv/salt/bugguy-finglonger/galaxy.edn) and [ION squad finglonger config](https://gitlab.suse.de/galaxy/infrastructure/-/blob/master/srv/salt/bugguy-finglonger/salt.edn)
