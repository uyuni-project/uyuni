---
name: Update to new Salt version
about: Use this template to prepare an update to a new Salt version
title: 'Update to new Salt version: XXXX.X'
labels: ["ion-squad", "salt", "packaging"]
projects: ["SUSE/29"]
assignees: ''
---
## New Salt version: XXXX.X

### List of OBS projects to upgrade:

(Remove the ones that do not apply)

- [Salt (testing)](https://build.opensuse.org/project/show/systemsmanagement:saltstack:products:testing)
- [Salt (next)](https://build.opensuse.org/project/show/systemsmanagement:saltstack:products:next)
- [Salt (openSUSE)](https://build.opensuse.org/project/show/systemsmanagement:saltstack)
- [Salt Bundle (testing)](https://build.opensuse.org/project/show/systemsmanagement:saltstack:bundle:testing)
- [Salt Bundle (next)](https://build.opensuse.org/project/show/systemsmanagement:saltstack:bundle:next)

### TODO

- [ ] Prepare branch for new version at "openSUSE/salt" repo, rebasing the Salt packages according to [documentation](https://github.com/openSUSE/salt/wiki/Workflow#upgrade-to-a-new-upstream-release)
- [ ] Prepare branch for new version at "openSUSE/salt-packaging", with previous `release/YYYY.Y` branch as parent.
- [ ] Adjust the spec file on "openSUSE/salt-packaging" with the new version + dependencies, and adjust patches.
- [ ] Make sure the new Salt package is successfully building in your branch and package is usable.
- [ ] Prepare the SRs to the different Salt projects at OBS.
- [ ] Make sure new dependencies are included in the Salt projects in OBS, and also inside Salt Bundle projects (bundle:next / bundle:testing).
- [ ] Make sure SR to Factory is created, together with any new dependency for Salt.
- [ ] Make sure SUMA and Uyuni RelEngs are aware of the new dependencies so they can be added to IBS and also to the corresponding channel definitions.
- [ ] Salt Bundle: Make sure the new "saltbundlepy-xxxx" package is added to "include-rpm" and "include-deb" files for the "venv-salt-minion" package.
- [ ] Classic Salt: Make sure DEB packages (in debian subprojects) are also building fine using the new version.
- [ ] Classic Salt: Make sure the new dependencies are included in the bootstrap repository definition for the different OS (if applies): https://github.com/uyuni-project/uyuni/blob/master/susemanager/src/mgr_bootstrap_data.py
