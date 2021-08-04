---
name: SUSE Manager MU release
about: Use this template for SUSE Manager MU releases (announcements)
title: 'X.Y Maintenance Update release (X.Y.Z)'
labels: vega-squad
assignees: 'juliogonzalez'

---

Related: #

Deadline:

- [ ] Approve all RRs and ping Maintenance so they can release
- [ ] Ping #doc.suse.com-request (Rocket.Chat, usually Stefan Knorr), so they sync the repository to get documentation.suse.com (MR: <MR_URL>)
- [ ] Check if you can find salt MIs for Leap using `osc sm salt`. If any of them is missing, ping salt squad. (remove this task for MUs that do not include client tools)
- [ ] Wait a couple of hours and announce to the suse-manager@suse.de mailing list.
- [ ] Create requests for maintainership for the new packages with `iosc bugowner -S group:<GROUP> -m "Add maintainer" <CODESTREAM>/<PACKAGE>` (this must be done **AFTER** the release)
