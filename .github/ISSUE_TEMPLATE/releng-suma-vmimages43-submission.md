---
name: SUSE Manager VM Images submission
about: Use this template for submitting SUSE Manager VM Images
title: 'SUSE Manager VM Images X.Y.Z release'
labels: ["vega-squad", "4.3"]
projects: ["SUSE/35"]
assignees: ''
---

# Deadlines

Check the [Release Calendar](https://confluence.suse.com/display/SUSEMANAGER/Release+calendar)
Submission: 
Release: 

# Procedure

https://confluence.suse.com/display/SUSEMANAGER/How+to+release+new+VM+images+for+SUSE+Manager+4.3

# Submission blockers


# TODO a few days before

Add more tasks if needed.

- [ ] Ensure that the `<version>` tag in the `config.kiwi` file is set to the last released maintenance update version for SUSE Manager (for example) `<version>4.3.11</version>`. If the version is older, prepare a SR for fixing it.
- [ ] Check that the images are built in the Devel project
  - [ ] https://build.suse.de/package/show/Devel:Galaxy:Manager:4.3/SLES15-SP4-Manager-4-3-Server
- [ ] Check that the images are built in the submission project
  - [ ] https://build.suse.de/package/show/Devel:Galaxy:Manager:VMImages:4.3/SLES15-SP4-Manager-4-3-Server
- [ ] If there are more images to be provided or if there's the need to release them for a different set of architectures, send an email to autobuild and provide them all the details.
- [ ] Check with PO if there's the need to update the release notes or the documentation. 
- [ ] In case of documentation changes, make sure that the documentation team is aware of the needed changes and that they are ready to provide them. Ask the documentation squad to submit an update of the doc package to the `Devel:Galaxy:Manager:4.3` project, and ask them to warn the translators so they can start their work. A Merge Request for the [documentation.suse.com repository](https://gitlab.suse.de/susedoc/docserv-external-tree-suma) should get created a few days before the release.
- [ ] If needed (see step before), create the JIRA ticket, with placeholders for the IDs for the release notes and/or the documentation.
- [ ] Prepare the draft of the release announcement to be sent to the `suse-manager@suse.de` mailing list.

# TODO during the submission window

Add more tasks if needed.

- [ ] Run the CI job [manager-4.3-releng-2obs](https://ci.suse.de/view/Manager/view/Manager-4.3/job/manager-4.3-releng-2obs/) with parameters, using as `spacewalk_branch` the last released MU branch (for example `Manager-4.3-MU-4.3.11`) and check that the job has packaged and submitted the changes to [Devel:Galaxy:Manager:4.3](https://build.suse.de/project/show/Devel:Galaxy:Manager:4.3) project.
- [ ] Check that the images are built in the Devel project
  - [ ] https://build.suse.de/package/show/Devel:Galaxy:Manager:4.3/SLES15-SP4-Manager-4-3-Server
- [ ] Check that the images are built in the submission project
  - [ ] https://build.suse.de/package/show/Devel:Galaxy:Manager:VMImages:4.3/SLES15-SP4-Manager-4-3-Server
- [ ] For the submission message specify the ijsc ID if any and the MU version. For example `Refreshed VM images for SUSE Manager 4.3 based on SUSE Manager 4.3.11. ijsc#MSQA-737`
Prepare the submission with `osc -A https://api.suse.de -m "Refreshed VM images for SUSE Manager 4.3 based on SUSE Manager 4.3.11. ijsc#MSQA-737" Devel:Galaxy:Manager:VMImages:4.3 SLES15-SP4-Manager-4-3-Server SUSE:SLE-15-SP4:Update:Products:Manager43:Update:CR`
- [ ] Send an e-mail to autobuild and specify the ID of the submit request created against the CR project. They will have to accept it.
- [ ] After autobuild has accepted the submit request, run the CI job [manager-4.3-releng-2obs](https://ci.suse.de/view/Manager/view/Manager-4.3/job/manager-4.3-releng-2obs/) with parameters, using as `spacewalk_branch` the branch `Manager-4.3`.
- [ ] For any **new image** that will be added to the codestreams, fetch the group that will maintain them (one per package) and document this at the release card.
- [ ] If needed, submit the release notes (prepare the submission with `patch-creator`)
- [ ] If needed, submit the documentation (prepare the submission with `patch-creator`, using the `-l` option for specifying the `susemanager-docs_en` source to be submitted. In this case, don't forget to adjust the patchinfos and don't forget to review the `Maintenancerequest.sh`, making sure you are not submitting additional sources.
- [ ] If needed, add to the jira card the link of the maintenance requests for release notes and/or the documentation. Ping the Maintenance Team at [#discuss-susemamanager-maintenance](https://app.slack.com/client/T02863RC2AC/C02DEF2U0E5)
- [ ] When the sources finish to build and everything is published in the [CR project](https://build.suse.de/project/show/SUSE:SLE-15-SP4:Update:Products:Manager43:Update:CR), send a new e-mail to autobuild and ask them to release the images from [SUSE:SLE-15-SP4:Update:Products:Manager43:Update:CR](https://build.suse.de/project/show/SUSE:SLE-15-SP4:Update:Products:Manager43:Update:CR) to [SUSE:SLE-15-SP4:Update:Products:Manager43:Update:CR:ToTest](https://build.suse.de/project/show/SUSE:SLE-15-SP4:Update:Products:Manager43:Update:CR:ToTest)
- [ ] Ping susemanager-qa and tell them that the images are ready to be tested from the repo [SUSE:/SLE-15-SP4:/Update:/Products:/Manager43:/Update:/CR:/ToTest/images/](https://download.suse.de/ibs/SUSE:/SLE-15-SP4:/Update:/Products:/Manager43:/Update:/CR:/ToTest/images/)
- [ ] Ping the PO and inform him that the images are ready for testing
