# Copyright (c) 2020-2023 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Reposync works as expected

@scc_credentials
@susemanager
  Scenario: Check reposync of Rocky Linux 8 channels being finished
    Then I wait until the channel "rockylinux8-x86_64" has been synced
    And I wait until the channel "rockylinux8-appstream-x86_64" has been synced

@uyuni
  Scenario: Check reposync of Rocky Linux 8 channels being finished
    Then I wait until the channel "rockylinux8-x86_64" has been synced
    And I wait until the channel "rockylinux8-x86_64-appstream" has been synced

@scc_credentials
@susemanager
  Scenario: Check reposync of Ubuntu 22.04 channels being finished
    Then I wait until the channel "ubuntu-2204-amd64-main-amd64" has been synced
    And I wait until the channel "ubuntu-2204-amd64-main-updates-amd64" has been synced
    And I wait until the channel "ubuntu-2204-amd64-main-security-amd64" has been synced

@uyuni
  Scenario: Check reposync of Ubuntu 22.04 channels being finished
    Then I wait until the channel "ubuntu-2204-pool-amd64-uyuni" has been synced
    And I wait until the channel "ubuntu-2204-amd64-main-uyuni" has been synced
    And I wait until the channel "ubuntu-2204-amd64-main-updates-uyuni" has been synced
    And I wait until the channel "ubuntu-2204-amd64-main-security-uyuni" has been synced

@scc_credentials
@susemanager
  Scenario: Check reposync of SLES 15 SP4 channels being finished
    Then I wait until the channel "sle-module-basesystem15-sp4-updates-x86_64" has been synced
    And I wait until the channel "sle-module-server-applications15-sp4-updates-x86_64" has been synced
    And I wait until the channel "sle-module-desktop-applications15-sp4-updates-x86_64" has been synced
    And I wait until the channel "sle-module-devtools15-sp4-updates-x86_64" has been synced
    And I wait until the channel "sle-module-containers15-sp4-pool-x86_64" has been synced

@uyuni
  Scenario: Check reposync of openSUSE Leap 15.5 channels being finished
    Then I wait until the channel "opensuse_leap15_5-x86_64" has been synced
    And I wait until the channel "opensuse_leap15_5-non-oss-x86_64" has been synced
    And I wait until the channel "opensuse_leap15_5-non-oss-updates-x86_64" has been synced
    And I wait until the channel "opensuse_leap15_5-updates-x86_64" has been synced
    And I wait until the channel "opensuse_leap15_5-backports-updates-x86_64" has been synced
    And I wait until the channel "opensuse_leap15_5-sle-updates-x86_64" has been synced
    And I wait until the channel "opensuse_leap15_5-uyuni-client-devel-x86_64" has been synced
    And I wait until the channel "uyuni-proxy-devel-leap-x86_64" has been synced

@scc_credentials
@susemanager
  Scenario: Check reposync of Client Tools being finished
    Then I wait until the channel "sle-manager-tools15-pool-x86_64-sp4" has been synced
    And I wait until the channel "sle-manager-tools15-updates-x86_64-sp4" has been synced
    And I wait until the channel "sle-manager-tools15-beta-pool-x86_64-sp4" has been synced
    And I wait until the channel "sle-manager-tools15-beta-updates-x86_64-sp4" has been synced
    And I wait until the channel "res8-manager-tools-updates-x86_64-rocky" has been synced
    And I wait until the channel "res8-manager-tools-pool-x86_64-rocky" has been synced
    And I wait until the channel "ubuntu-2204-suse-manager-tools-amd64" has been synced

@uyuni
  Scenario: Check reposync of Client Tools being finished
    And I wait until the channel "opensuse_leap15_5-uyuni-client-x86_64" has been synced
    And I wait until the channel "rockylinux8-uyuni-client-x86_64" has been synced
    And I wait until the channel "ubuntu-2204-amd64-uyuni-client" has been synced
