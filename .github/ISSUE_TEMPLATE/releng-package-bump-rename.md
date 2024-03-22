---
name: Package version bump/rename for SUSE Manager/Uyuni
about: Use this template to notify RE about a package version bump/rename for SUSE Manager/Uyuni
title: 'Package version bump/rename for SUSE Manager/Uyuni: XXXXXXX'
labels: ["vega-squad"]
projects: ["SUSE/35"]
assignees: ''
---
# Reason

Provide the reason why we need to rename/bump the version. This would help all the involved parties in ECO to understand the reason behind it and could help speed up the process.


# Affected package

**Mandatory**

This is not about the RPM package, but about the OBS package, that should be the name as the `spec` file

- [ ] OBS name: (**if this is a package rename, specify the new name**)

# Maintainer

Run `iosc bugowner <package_name>` and pick one:

- [ ] suse-manager-maintainers
- [ ] monitoring (some monitoring packages only)
- [ ] Other (**please specify, only for excepcional situations**)

# Actions

**Mandatory**

- [ ] Version bump: (**specify the old version and the new version**)
- [ ] Rename: (**specify the old OBS name and the new OBS name**)

**NOTE:** Avoid renames unless they are strictly needed. They may requires changes on the product definitions, channel definitions, patterns, code for the server, the client tools, the doc, the testsuite and other places.

# Affected versions

**Mandatory**. You need to mark at least one checkbox. If you are not sure, check with the Product owner.

- [ ] Uyuni
- [ ] SUSE Manager Head
- [ ] SUSE Manager 4.3

# Affected codestreams and product/modules

**Mandatory**

Use https://maintenance.suse.de/maintained/ to find the codestreams and product/modules where the package is, and list them here. 

If you are doing a package rename, use the old OBS package name.

-
-
-

**NOTE**: The REs will tell you the right devel codestreams for the submissions. Do not use those from this list.

# ECO requirement

**Mandatory**.

If the new package is part of any codestream matching: `SUSE:SLE-12:Update`, `SUSE:SLE-12-SP[X]:Update`, `SUSE:SLE-15:Update`, or `SUSE:SLE-15-SP[X]:Update`, or part of SLE modules.

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
