---
name: SUSE Manager EOL
about: Use this template when a product goes EOL in SUSE Manager
title: 'Epic: XXX EOL'
labels: ["vega-squad", "qe-squad", "docs-squad", "epics"]
assignees: ''
---

For more details see also: https://confluence.suse.com/display/SUSEMANAGER/How+to+request+and+EoL+SUSE+Manager+products

Product Name: 
EOL date: 
Links: 

# Tasks


- [ ] Orion: Set product in sumatoolbox EOL with official date
- [ ] Dev: Change repositories if the product move them to an archive place (ubuntu, debian, rocky and alma sometimes does it)
- [ ] QE: Remove the product from our [infrastructure](https://gitlab.suse.de/galaxy/infrastructure)
  - [ ] [DNS](https://gitlab.suse.de/galaxy/infrastructure/-/tree/master/srv/salt/bind-server?ref_type=heads) and [DHCP](https://gitlab.suse.de/galaxy/infrastructure/-/tree/master/srv/salt/dhcpd-server?ref_type=heads)
  - [ ] [Minima configuration files](https://gitlab.suse.de/galaxy/infrastructure/-/tree/master/srv/salt/minima?ref_type=heads)
- [ ] QE: Delete synchronized folders inside our Minima mirrors
- [ ] QE: [susemanager-ci](https://github.com/SUSE/susemanager-ci/)
  - [ ] Adjust [README](https://github.com/SUSE/susemanager-ci/blob/master/README.md)
  - [ ] Remove [Terraform files](https://github.com/SUSE/susemanager-ci/tree/master/terracumber_config/tf_files)
  - [ ] Remove [Jenkins Pipelines](https://github.com/SUSE/susemanager-ci/tree/master/jenkins_pipelines/environments)
- [ ] QE: Remove support from the testsuite
  - [ ] [Uyuni](https://github.com/uyuni-project/uyuni/tree/master/testsuite)
  - [ ] SUMA X.Y
- [ ] Naica: Remove support from [Sumaform](https://github.com/uyuni-project/sumaform)
- [ ] Doc: 
  - [ ] Remove references to the EOL version
  - [ ] Update support-matrix (Cl Config Guide and Install. Guide)
  - [ ] Update clients page
  - [ ] Update supported features
  - [ ] Check the navigation bar
  - [ ] Remove the EOL client tool from the guides
  - [ ] Set outdated Major versions as "unsupported" in our docserv XML config. These will then be listed on the [d.s.c unsupported page](https://documentation.suse.com/main-unsupported.html).
  - [ ] Remove outdated MU translations from Weblate.
- [ ] Releng: Remove the product from the [patch-creator configuration file](https://gitlab.suse.de/galaxy/patch-creator/-/blob/master/patchcreator.conf)
- [ ] Releng: For client tools, remove the product the CI installation checks (`manager-*-releng-SUSE_Manager_Client_Tools_Installation_Check`)
  - [ ] [Head](https://ci.suse.de/view/Manager/view/Manager-Head/job/manager-Head-releng-SUSE_Manager_Client_Tools_Installation_Check/)
  - [ ] Manager-X.Y
- [ ] Releng: For client tools, remove the product the CI build checks (`*-dev-at-obs`)
  - [ ] [Uyuni](https://ci.suse.de/view/Manager/view/Uyuni/job/uyuni-Master-dev-at-obs/)
  - [ ] [Head](https://ci.suse.de/view/Manager/view/Manager-Head/job/manager-Head-dev-at-obs/)
  - [ ] Manager-X.Y
- [ ] Releng: For Server/Proxy, remove the [Jenkins jobs and views](https://ci.suse.de/view/Manager/) for the relevant versión
- [ ] Releng: Disable builds at the relevant IBS/OBS projects
  - [ ] Uyuni
  - [ ] Head
  - [ ] Manager-X.y
- [ ] Releng: Contact Maintenance (via MSC ticket) and ask them to:
  - [ ] Move the channels to the [EOL project](https://build.suse.de/project/show/SUSE:Channels:EOL)
  - [ ] Remove the product from [SUSE:Maintenance](https://build.suse.de/projects/SUSE:Maintenance/meta)
  - [ ] Remove the maintained attribute from the codestream
- [ ] PO: Request to remove the supported flag from SCC
