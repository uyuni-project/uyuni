---
name: New package for SUSE Manager/Uyuni
about: Use this template to request a new package to be added to SUSE Manager/Uyuni
title: 'New package: XXXXXX'
labels: ["vega-squad"]
projects: ["SUSE/35"]
assignees: ''
---
# Reason

Provide the reason why we need this new package. This would help all involved parties in ECO to understand the reason behind it and could help speed up the process.

# New package name

**Mandatory**

This is not about the RPM package, but about the OBS package, that should be the name as the `spec` file

- [ ] OBS name: 

# Maintainer

- [ ] suse-manager-maintainers
- [ ] monitoring (some monitoring packages only)
- [ ] Other (**please specify, only for excepcional situations**)

# Affected versions

**Mandatory**. You need to mark at least one checkbox. If you are not sure, check with the Product owner.

- [ ] Uyuni
- [ ] SUSE Manager Head
- [ ] SUSE Manager 4.3
- [ ] SLE modules


# Affected patterns, if any

**Optional**. If your new package is only a build dependency, or if it's a runtime dependency for another package, no need to mark any checkbox. 

- [ ] Server(**specify the RPMs to be added**)
- [ ] Proxy (**specify the RPMs to be added**)
- [ ] Retail Branch Server (**specify the RPMs to be added**)

This section is **optional**. 

# Affected channels/images, if any

**Optional**. If your new package is only a build dependency, no need to mark any checkbox.

- [ ] Server (**specify the RPMs to be added**)
- [ ] Proxy/Retail Branch Server (**specify the RPMs to be added**)
- [ ] Client Tools (**specify the list of client tools and the RPMs to be added**)
- [ ] SLE modules (**specify the SLE module names, the affected SLE versions , and the RPMs to be added**)

# ECO requirement

**Mandatory**.

If the new package:
- Is going to be part of the SLE12/SLE15 client tools for a released SUSE Manager version, or part of a SLE module, and
- Is either a runtime dependency, or a build dependency for an existing package

Then mark the following checkbox:
- [ ] ECO is **required**

Otherwise mark this other one:
- [ ] ECO is **NOT required**

# Related PRs

List here any related PRs for `SUSE/spacewalk` and/or `uyuni-project/uyuni`

-
-

# WARNING

If an ECO is required, do NOT merge any SRs/PRs related to the package until the ECO is approved!
