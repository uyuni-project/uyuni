---
name: QE - Public Cloud images
about: QE - Use this template for testing public cloud images
title: "PAYG/BYOS "
labels: ["qe-squad", "manual tests", "suma payg", "public cloud", "manual tests"]
projects: ["SUSE/32"]
assignees: ''

---

# Description

## AWS

:warning: For AWS the instance needs to be pointed to the update server in the test region in order to register it!

```bash
registercloudguest --clean
registercloudguest --smt-ip 18.156.40.199 --smt-fqdn smt-ec2.susecloud.net --smt-fp A0:8A:3E:90:1A:FE:DF:A4:DB:A4:B0:61:A4:86:DE:48:8A:B9:D8:75
```

## Azure

:warning: Be aware that the network configuration still needs to be adjusted before SUMA is set up! Check the [docs](https://documentation.suse.com/suma/5.0/en/suse-manager/specialized-guides/public-cloud-guide/payg/azure/payg-azure-server-setup.html).

## What to test?

| Provider | PAYG | BYOS |
| -------- | ---- | ---- |
| AWS      | [ ]  | [ ]  |
| Azure    | [ ]  | [ ]  |
| GCP      | [ ]  | [ ]  |

## Goal

Test and verify that our public cloud images work as expected.

# Tasks

## SETUP

### SUMA 5.x

- [ ] instance creation through the guided procedure
- [ ] storage setup with **mgr-storage-server**
- [ ] PAYG instance registration
- [ ] SUMA 5.x server setup with **mgradm install podman**

### SUMA 4.x

- [ ] instance creation through the guided procedure
- [ ] PAYG instance registration
- [ ] SUMA 4.x server setup

## PAYG

## BYOS

## No SCC credentials tests

- [ ] default products are available
  - Expected: free Products like openSUSE, Rocky Linux, AlmaLinux, CentOS
  - Expected: SUSE Manager Proxy
  - Expected: SUSE Manager Server
  - NOT POSSIBLE: Ubuntu and Debian (We cannot offer them as RMT cannot sync and provide the client tools channels).
  - These products should not be visible in the Product page.
- [ ] add SLES 15 PAYG instance to SUMA without SCC Credential
- [ ] add SAP PAYG instance to SUMA without SCC Credential
- [ ] Test that registering a BYOS or DC instance fails
- [ ] Bootstrap SLES 15 SP6 PAYG instance
- [ ]  CLM SLES for SAP creating different environments should work
- [ ] CLM Pure SLES and clone HA channels from a SLES for SAP under SLES must not work.
  - [ ] After adding SCC credentials this works
- [ ]  Check that in SUMA PAYG you cannot clone channels between different product families
  - SAP module can't be clone to SLES 15 SPx
  - HA module can't be clone to SLES 15 SPx
  - Proxy module can't be clone to SLES 15 SPx
  - Server module can't be clone to SLES 15 SPx
  - Other modules can be cloned between SLES 15 SPx and SLES 15 SP(-|+)1
- [ ]  Product Migration just switching the Service Pack should work (e.g. SLES SP5 to SLES SP6)
- [ ] Product Migration when changing the product type should not work (openSUSE to SLES migration) (or SLES to SLES-for-SAP - not working with SUMA anyway)

## With SCC credentials tests

- [ ] configure SCC credentials
- [ ] check that it is possible to register a BYOS instance
- [ ] Test to disable forward registration - should result in a message that this is not possible and data should still be send
- [ ] Check that PAYG systems are not registered at SCC
- [ ] remove the SCC credentials and verify it is no longer possible to manager the BYOS instance

## Other tests

- [ ] register a RHEL 9 PAYG instance + onboard + some smoke tests
- [ ] Product migration from RHEL8/9 to Liberty 8/9
- [ ] smoke tests on a PAYG instance
- [ ] smoke tests on a BYOS instance

## Acceptance Criteria

- All tests were conducted
- Found issues/bugs are reported and blockers were communicated

## Links

- [Testing public cloud images](https://confluence.suse.com/x/AgFJUw)

## Found issues/bugs
