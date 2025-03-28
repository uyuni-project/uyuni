---
name: New joiner checklist
about: Use this template when someone joins the SUSE Multi-Linux team
title: 'New joiner <name>'
labels: team
assignees: ''
---

General template for new joiners of the SUSE Multi-Linux team.

- [ ] Go through the general [onboarding guide](https://geekos.io/onboarding) for new employees.
- [ ] GitHub account with SUSE email address
  - [ ] [Add to SUSE org](https://confluence.suse.com/pages/viewpage.action?spaceKey=IAM&title=Github+account+and+access)
  - [ ] Add to [Multi-Linux Manager Team](https://github.com/orgs/SUSE/teams/multi-linux-manager-team/members)
  - [ ] Add to [Uyuni organization and teams](https://github.com/orgs/uyuni-project/people) (@juliogonzalez or [another owner](https://github.com/orgs/uyuni-project/people?query=role%3Aowner))
- [ ] Add SUMA account on [manager.mgr.suse.de](https://manager.mgr.suse.de)
- [ ] [Trello account](https://confluence.suse.com/display/IAM/Trello+account+and+access)
- [ ] [IBS access](https://confluence.suse.com/display/devops/How+to+request+IBS+access) 
- [ ] Confluence
  - [ ] Add to [squads page](https://confluence.suse.com/display/SUSEMANAGER/Squads%2C+People+and+Topics)
  - [ ] Add to [team overview page](https://confluence.suse.com/display/SUSEMANAGER/)
- [ ] Subscribe the user to the following mailing lists:
  - [ ] [galaxy-devel](https://mailman.suse.de/mailman/admin/galaxy-devel/members/add)
  - [ ] [galaxy-team](https://mailman.suse.de/mailman/admin/galaxy-team/members/add)
  - [ ] [multi-linux-manager](https://mailman.suse.de/mailman/admin/multi-linux-manager/members/add)
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
- [ ] Tell the user to have a look at the recommended lists, and subscribe if interested:
  - [Linux Info](https://lists.suse.com/mailman/listinfo/linux) (strongly recommended for Release Engineers)
  - Lists at [openSUSE](https://lists.opensuse.org) (whatever is interesting for the user, such as for example `buildservice`, `users`, `packaging` or `factory`)
  - Lists at [suse.com](https://lists.suse.com/mailman/listinfo)  (whatever is interesting for the user, such as for example `sle-security-updates`, `sle-container-updates`, or `suma-updates`)
- [ ] Slack
  - [ ] Update workflows (daily standup, meetings, etc.)
  - [ ] Add to groups (left menu -> `...` -> `People` -> `User Groups`)
    - [ ] `multi-linux-manager-engineers`
    - [ ] Squad specific group
- [ ] Add the user to the Google [SUSE Multi-Linux Team](https://groups.google.com/a/suse.com/g/multi-linux-all/members) and squad groups (if available), to get access to the calendar (PHP, Retrospective, and all other events), and to emails send to the mailing list for the group
- [ ] Add SSH public key to the [galaxy/infrastructure repository](https://gitlab.suse.de/galaxy/infrastructure/-/blob/master/srv/salt/ssh/init.sls)
- [ ] Provide default passwords if applicable (testsuite VMs, maybe https://gitlab.suse.de/galaxy/credentials)
- [ ] Add user to SCC organization [SUSE Multi-Linux Manager Team Playground](https://scc.suse.com/organizations/432530/users) for SCC mirror credentials
- [ ] Add to the relevant IBS/OBS groups:
  - [ ] Add to https://build.suse.de/groups/monitoring (if applicable)
  - [ ] Add to https://build.suse.de/groups/salt-maintainers (if applicable)
  - [ ] Add to https://build.suse.de/groups/scap-security-guide-maintainers (if applicable)
  - [ ] Add to https://build.suse.de/groups/suse-manager-maintainers (if applicable)
  - [ ] Add to https://build.suse.de/groups/suse-manager-developers (if applicable)
  - [ ] Add to https://build.suse.de/groups/susemanager-releng (if applicable)
  - [ ] Add to https://build.opensuse.org/groups/uyuni-maintainers (if applicable)
  - [ ] Add to https://build.opensuse.org/groups/salt-maintainers (if applicable)
- [ ] Add the new employee to our [GitLab group](https://gitlab.suse.de/groups/galaxy/-/group_members)
- [ ] Schedule new joiner meetups with at least one [member from each squad](https://confluence.suse.com/x/OIGAOQ), with the PO and with the architect
- [ ] Update the [finglonger config](https://gitlab.suse.de/galaxy/infrastructure/-/blob/master/srv/salt/bugguy-finglonger/galaxy.edn)
- [ ] QE specific
  - [ ] IBS/OBS: Add to the https://build.suse.de/groups/qam-manager group
  - [ ] Add QE account on [manager.mgr.suse.de](https://manager.mgr.suse.de) for the QE organization
  - [ ] Add user entries for private hypervisor to [SUMA infrastructure](https://gitlab.suse.de/galaxy/infrastructure/-/blob/master/srv/salt/qa/users/init.sls)
  - [ ] Add user entries for DNS to the [galaxy/infrastructure repository](https://gitlab.suse.de/galaxy/infrastructure/-/tree/master/srv/salt/bind-server)
  - [ ] Add user entries for DHCP to the [galaxy/infrastructure repository](https://gitlab.suse.de/galaxy/infrastructure/-/tree/master/srv/salt/dhcpd-server)
  - [ ] Add to [galaxy-qa](https://mailman.suse.de/mailman/admin/galaxy-qa/members/add) mailing list
  - [ ] Add to [Multi-Linux Manager QE Team](https://github.com/orgs/SUSE/teams/multi-linux-manager-qe/members) on GitHub
  - [ ] Add to [Uyuni QE developer Team](https://github.com/orgs/uyuni-project/teams/qe) on GitHub
  - [ ] Add to [QE Retrospective project](https://github.com/orgs/SUSE/projects/54) on GitHub
  - [ ] Add to [QE project board member list](https://github.com/orgs/SUSE/projects/32/views/1?pane=info) on GitHub
  - [ ] Add to the Google [Multi Linux Manager QE Squad](https://groups.google.com/a/suse.com/g/multi-linux-manager-qe/members) group
