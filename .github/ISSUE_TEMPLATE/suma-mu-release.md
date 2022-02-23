---
name: SUSE Manager MU release
about: Use this template for SUSE Manager MU releases (announcements)
title: 'X.Y.Z Maintenance Update release'
labels: vega-squad
assignees: ''

---

Related: #

Release deadline:

- [ ] Approve all RRs and ping the Maintenance Team, so they can release
- [ ] Create a new request (`/request` command) at `#proj-doc-suse-com-request` (Slack), so they accept the MR and sync the repository to documentation.suse.com (MR: <MR_URL>)
- [ ] Check if you can find salt MIs for Leap using `osc sm salt`. If any of them is missing, ping salt squad. (remove this task for MUs that do not include salt/client tools)
- [ ] Wait a couple of hours and announce to the suse-manager@suse.de mailing list. See example email https://mailman.suse.de/mlarch/SuSE/suse-manager/2021/suse-manager.2021.12/msg00028.html
- [ ] Create requests for maintainership for the new packages with `iosc bugowner -S group:<GROUP> -m "Add maintainer" <CODESTREAM>/<PACKAGE>` (this must be done **AFTER** the release)
