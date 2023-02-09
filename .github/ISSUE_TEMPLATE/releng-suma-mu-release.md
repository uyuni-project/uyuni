---
name: SUSE Manager MU release
about: Use this template for SUSE Manager MU releases (announcements)
title: 'X.Y.Z Maintenance Update release'
labels: vega-squad
assignees: ''

---

Related: #

A couple of days before the release deadline:
- [ ] Make sure the documentation team created a Merge Request for the [documentation.suse.com repository](https://gitlab.suse.de/susedoc/docserv-external-tree-suma). It must include the translations.

Release deadline:

- [ ] Approve all RRs and ping the Maintenance Team, so they can release
- [ ] Create a new request (`/request` command) at `#proj-doc-suse-com-request` (Slack), so they can accept the Merge request and sync the repository to documentation.suse.com (MR: <MR_URL>)
- [ ] Run `osc -A https://api.suse.de pr SUSE:Containers:SUSE-Manager:X.Y` (replace `X.Y` to see if the containers are published)
- [ ] Wait a couple of hours and announce to the `suse-manager@suse.de` mailing list. See example email https://mailman.suse.de/mlarch/SuSE/suse-manager/2021/suse-manager.2021.12/msg00028.html
- [ ] Create requests for maintainership for the new packages with `osc -A https://api.suse.de bugowner -S group:<GROUP> -m "Add maintainer" <CODESTREAM>/<PACKAGE>` (this must be done **AFTER** the release)
