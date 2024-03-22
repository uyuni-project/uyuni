---
name: SUSE Manager Milestone release
about: Use this template for SUSE Manager Milestone releases (announcements)
title: 'X.Y <MILESTONE_NAME> release'
labels: ["vega-squad"]
projects: ["SUSE/35"]
assignees: ''

---

Related: #

Release deadline:

# Some days before the release date (public milestones only!)

- [ ] The PO must provide the release notes for the Beta announcement to Vincent Moutoussamy. REs can provide support as needed, to improve the announcement.

# Release date
- [ ] Ask autobuild to publish to the CDN
- [ ] Approve all RRs for client tools/bundle and ping the Maintenance Team, so they can release
- [ ] Create a new request (`/request` command) at `#proj-doc-suse-com-request` (Slack), so they accept the MR and sync the repository to documentation.suse.com (MR: <MR_URL>)
- [ ] Run `osc -A https://api.suse.de pr SUSE:Containers:SUSE-Manager:X.Y` to see if the containers are published)
- [ ] Wait a couple of hours, ask Vincent Moutoussamy to release the announcement, and announce to the suse-manager@suse.de and galaxy-devel@suse.de mailing lists.
- [ ] Create requests for maintainership for the new packages with `osc -A https://api.suse.de bugowner -S group:<GROUP> -m "Add maintainer" <CODESTREAM>/<PACKAGE>` (this must be done **AFTER** the release)
