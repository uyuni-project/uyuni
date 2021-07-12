---
name: Documentation Release Checklist - SUMA
about: Use this issue template for documentation releases.
title: Documentation Release Checklist - SUMA X.YY
labels: docs-squad
assignees: Loquacity, jcayouette

---

# Documentation Release Checklist - SUMA

Use this issue template for SUMA documentation releases.
For Uyuni documentation releases, see https://github.com/uyuni-project/uyuni-docs.

## Before Packaging Day

- [ ] Check for previous SLES SP versions in the text, and update where necessary.
- [ ] Review the context sensitive help in the UI, and ensure all links are up to date.
- [ ] Check all outstanding pull requests, and ensure everything relevant is merged (and backported where required).
Check with the docs squad coordinator for confirmation.


## Packaging Day

- [ ] Update entities to the current versions.
- [ ] For SUMA, cut a new branch from the release branch using syntax `manager-x.y-MU-x.y.z` for MUs, or `manager-x.y-milestone` for alpha, beta, or RC releases.
- [ ] Locally build docs from the release branch and visually check output.
- [ ] Package documentation and push SR to OBS: https://github.com/uyuni-project/uyuni-docs/wiki/publishing-to-obs
- [ ] Notify Release Manager of SR.
- [ ] Release Manager accepts package.


## Release Day

- [ ] Build docs from release branch and visually check output.
- [ ] Publish to documentation.suse.com: https://github.com/uyuni-project/uyuni-docs/wiki/publishing-to-enterprise-endpoints
- [ ] When endpoints are live, visually check output.
