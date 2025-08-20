---
name: SUSE Multi-Linux Manager Milestone release
about: Use this template for SUSE Multi-Linux Manager Milestone releases (announcements)
title: 'X.Y <MILESTONE_NAME> release'
labels: ["vega-squad"]
projects: ["SUSE/35"]
assignees: ''

---

Related: #

Release deadline:

# Some days before the release date (public milestones only!)

- [ ] The PO must provide the release notes for the Beta announcement. Release engineers can provide support as needed, to improve the announcement.

# Release date
- [ ] Ask autobuild to publish to the CDN opening a new issue in the [release-support board](https://gitlab.suse.de/buildops/release-support/-/issues).
 - [ ] Mention the list of projects to be released (client tools, extensions etc)
 - [ ] Mention any new container image to be released and included in their release automation
 - [ ] Mention any new vm and bare metal image to be included
 - [ ] For any milestone to be preserved, make sure to mention this info in the GitLab issue. This is an [example from Multi-Linux Manager 5.1 FCS](https://gitlab.suse.de/buildops/release-support/-/issues/115)
- [ ] Approve all RRs for client tools/bundle and ping the Maintenance Team, so they can release
- [ ] Create a new request (`/request` command) at `#proj-doc-suse-com-request` (Slack), so they accept the MR and sync the repository to documentation.suse.com (MR: <MR_URL>)
- [ ] Run `osc -A https://api.suse.de pr SUSE:Containers:SUSE-MultiLinuxManager:X.Y` to see if the containers are published)
- [ ] Check also [How to check the status of the released containers in the registry](https://confluence.suse.com/display/SUSEMANAGER/How+to+check+the+status+of+the+released+containers+in+the+registry)
- [ ] Wait a couple of hours, ask to release the announcement, and announce to the `multi-linux-manager@suse.de` and `galaxy-devel@suse.de` mailing lists.
- [ ] Create requests for maintainership for the new packages with `osc -A https://api.suse.de bugowner -S group:<GROUP> -m "Add maintainer" <CODESTREAM>/<PACKAGE>` (this must be done **AFTER** the release of a maintenance incident. For packages submitted via "GA workflow", the maintainership request happens during the submission.)
