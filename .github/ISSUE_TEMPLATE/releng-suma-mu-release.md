---
name: SUSE Manager MU release
about: Use this template for SUSE Manager MU releases (announcements)
title: 'X.Y.Z Maintenance Update release'
labels: ["vega-squad"]
projects: ["SUSE/35"]
assignees: ''

---

Related: #

A couple of days before the release deadline:
- [ ] Ensure that the documentation team integrates the most recent updates into the master branch and the appropriate version directory at the [documentation.suse.com repository](https://gitlab.suse.de/susedoc/docserv-external-tree-suma). This process should encompass all translations, except in cases where an exemption has been explicitly granted.

Release deadline:

- [ ] Approve all RRs and ping the Maintenance Team, so they can release
- [ ] Create a new request (`/request` command) at `#proj-doc-suse-com-request` (Slack), so they can accept the Merge request and sync the repository to the staging server. Once the `/request` command is run, in the menu, remember to fill in the "comment" field with "Please publish to the staging server then ping @susemanager-docs so that they can review the content."
- [ ] Run `osc -A https://api.suse.de pr SUSE:Containers:SUSE-Manager:X.Y` (replace `X.Y` to see if the containers are published)
- [ ] Wait a couple of hours and announce to the `suse-manager@suse.de` mailing list. See example email https://mailman.suse.de/mlarch/SuSE/suse-manager/2021/suse-manager.2021.12/msg00028.html
- [ ] Create requests for maintainership for the new packages with `osc -A https://api.suse.de bugowner -S group:<GROUP> -m "Add maintainer" <CODESTREAM>/<PACKAGE>` (this must be done **AFTER** the release)
