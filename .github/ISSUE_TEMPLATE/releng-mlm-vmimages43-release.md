---
name: SUSE Manager 4.3 VM Images release
about: Use this template for SUSE Manager 4.3 VM Images releases (announcements)
title: 'SUSE Manager 4.3 VM Images X.Y.Z'
labels: ["vega-squad", "4.3"]
projects: ["SUSE/35"]
assignees: ''
---

Related: #

# Procedure

https://confluence.suse.com/display/SUSEMANAGER/How+to+release+new+VM+images+for+SUSE+Manager+4.3


# Release blockers

Add any additional bugzilla report, PR, or EPIC that blocks this release for having an overview of still pending changes.

# A couple of days before the release deadline:
- [ ] In case of documentation changes, ensure that the documentation team integrated the most recent updates into the master branch and the appropriate version directory at the [documentation.suse.com repository](https://gitlab.suse.de/susedoc/docserv-external-tree-suma). This process should encompass all translations, except in cases where an exemption has been explicitly granted.

# Release deadline:

**IMPORTANT: In case of changes to the release notes or documentation, coordinate the release with both autobuild and maintenance teams.**

- [ ] After the ok from QE, in case of changes for the release notes and documentation, approve all RRs and ping the Maintenance Team, so they can prepare to release coordinating the task with autobuild.
- [ ] Report a new issue for autobuild team with the details of the needed projects at the [release-support board](https://gitlab.suse.de/buildops/release-support/-/issues/new)
  - [ ] ask them to release the images from [SUSE:SLE-15-SP4:Update:Products:Manager43:Update:CR:ToTest](https://build.suse.de/project/show/SUSE:SLE-15-SP4:Update:Products:Manager43:Update:CR:ToTest). 
  - [ ] specify which images they should release and for which architecture. **In case of coordinated release with maintenance, ping them too.**
- [ ] In case of changes to the documentation, create a new request (`/request` command) at [#proj-doc-suse-com-request](https://suse.slack.com/archives/C02CUMY276J), so they can accept the Merge request and sync the repository to the staging server. Once the `/request` command is run, in the menu, remember to fill in the "comment" field with `Please publish to the staging server then ping @susemanager-docs so that they can review the content.`
- [ ] Check with autobuild (and eventually maintenance) and ask for confirmation that the release is done
- [ ] Ping the PO and ask to update the [download page](https://www.suse.com/download/suse-manager/) via https://gitlab.suse.de/scc/schotty
- [ ] Wait a couple of hours and announce to the `multi-linux-manager@suse.de` mailing list. 
- [ ] In case of new images, create requests for maintainership for the new packages with `osc -A https://api.suse.de bugowner -S group:<GROUP> -m "Add maintainer" <CODESTREAM>/<PACKAGE>` (this must be done **AFTER** the release)
