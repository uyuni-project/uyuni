---
name: Add support for a new OS distribution version in the test framework
about: Use this template when adding/replacing/deleting a client in the test framework
title: 'Add support for <NEW_OPERATING_SYSTEM_NAME> in the Test Framework'
assignees: ''
labels: testsuite, test-framework
---

### Description

### TODOs

- [ ] [internal infrastructure](https://gitlab.suse.de/galaxy/infrastructure)
  - [ ] add DNS/DHCP entries (`srv/salt/bind-server/`, `srv/salt/dhcpd-server/`)
  - [ ] add repository entries to minima mirrors (`minima.yaml`, `minima-small.yaml`)
  - [ ] add ISO/qcow2 image to minima mirrors (`mirror-images.conf`)
  - [ ] apply high state to those mirrors on `manager.mgr.suse.de`
  - [ ] trigger a mirror sync to have the new images available
- [ ] [sumaform](https://github.com/uyuni-project/sumaform)
  - [ ] add client support for libvirt (`base/maint.f`, `host/user_data.yaml`)
  - [ ] add new image to image list (`null/base/variables.tf`, `cucumber_testsuite/variables.tf`)
  - [ ] add new client to controller (`salt/controller/bashrc`, `controller/variables.tf`, `controller/main.tf`)
- [ ] [susemanager-ci](https://github.com/SUSE/susemanager-ci)
  - [ ] modify the necessary terraform configuration files (`terracumber_config/tf_files/`)
- [ ] [test suite](https://github.com/uyuni-project/uyuni/tree/master/testsuite)
  - [ ] add new nodes to `twopence_init.rb`, `constants.rb`, `env.rb`
  - [ ] add/refactor tests
  - [ ] create new features inside `init_clients` to bootstrap as minion/SSH minion (build validation)
  - [ ] add product synchronization (add the client to the relevant features inside the `reposync` folder)
  - [ ] update YAML file(s) inside `run_sets`
