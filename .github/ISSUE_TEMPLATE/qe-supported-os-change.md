---
name: QE - Add support for a new OS distribution version in the test framework
about: Use this template when adding/replacing/deleting a client in the test framework
title: 'Add support for <NEW_OPERATING_SYSTEM_NAME> in the Test Framework'
labels: ["testsuite", "test-framework"]
projects: ["SUSE/32"]
assignees: ''

---

### Description

### TODOs

- [ ] [internal infrastructure](https://gitlab.suse.de/galaxy/infrastructure)
  - [ ] add DNS/DHCP entries (`srv/salt/bind-server/`, `srv/salt/dhcpd-server/`)
  - [ ] add repository entries to the minima mirror configuration (`srv/salt/minima/ci-bv/`)
  - [ ] add ISO/qcow2 image to the minima mirror (`srv/salt/minima/ci-bv/mirror-images.conf`)
  - [ ] apply high state to the mirrors on `manager.mgr.suse.de`
  - [ ] trigger a mirror sync to have the new changes available
- [ ] [sumaform](https://github.com/uyuni-project/sumaform)
  - [ ] add client support for libvirt (`backend_modules/libvirt/base/maint.f`, `backend_modules/libvirt/host/user_data.yaml`)
  - [ ] add new client to controller (`salt/controller/bashrc`, `modules/controller/variables.tf`, `modules/controller/main.tf`)
  - [ ] update mirror configuration (`salt/mirror/etc/minima.yaml`)
- [ ] [susemanager-ci](https://github.com/SUSE/susemanager-ci)
  - [ ] modify the necessary terraform configuration files (`terracumber_config/tf_files/`)
  - [ ] add entries to the `minionList` variable in the pipeline configuration files (`jenkins_pipelines/environments/`)
  - [ ] add new product to the JSON creation script (`jenkins_pipelines/scripts/maintenance_json_generator.py`)
- [ ] [test suite](https://github.com/uyuni-project/uyuni/tree/master/testsuite)
  - [ ] add new nodes to `constants.rb` and `env.rb`
  - [ ] add/refactor tests
  - [ ] create new features inside `init_clients` to bootstrap as minion/SSH minion (build validation)
  - [ ] add product synchronization (add the client to the relevant features inside the `reposync` folder)
  - [ ] add to sanity check (`core/allcli_sanity.feature`)
  - [ ] update YAML file(s) inside `run_sets`
