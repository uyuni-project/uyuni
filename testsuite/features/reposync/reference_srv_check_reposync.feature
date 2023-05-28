# Copyright (c) 2020-2023 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Reposync works as expected

@scc_credentials
@susemanager
  Scenario: Check reposync of Rocky Linux 8 channels being finished
    Then I wait until the channel "rockylinux-8-x86_64" has been synced with packages
    And I wait until the channel "rockylinux-8-appstream-x86_64" has been synced with packages
@uyuni
  Scenario: Check reposync of Rocky Linux 8 channels being finished
    Then I wait until the channel "rockylinux8-x86_64" has been synced with packages
    And I wait until the channel "rockylinux8-x86_64-appstream" has been synced with packages

@scc_credentials
@susemanager
  Scenario: Check reposync of Ubuntu 22.04 channels being finished
    Then I wait until the channel "ubuntu-2204-amd64-main-amd64" has been synced with packages
    And I wait until the channel "ubuntu-2204-amd64-main-updates-amd64" has been synced with packages
    And I wait until the channel "ubuntu-2204-amd64-main-security-amd64" has been synced with packages

@uyuni
  Scenario: Check reposync of Ubuntu 22.04 channels being finished
    Then I wait until the channel "ubuntu-22.04-pool-amd64-uyuni" has been synced
    And I wait until the channel "ubuntu-2204-amd64-main-uyuni" has been synced with packages
    And I wait until the channel "ubuntu-2204-amd64-main-updates-uyuni" has been synced with packages
    And I wait until the channel "ubuntu-2204-amd64-main-security-uyuni" has been synced with packages

@scc_credentials
  Scenario: Check reposync of SLES 15 SP4 channels being finished
    Then I wait until the channel "sle-module-basesystem15-sp4-updates-x86_64" has been synced with packages
    And I wait until the channel "sle-module-server-applications15-sp4-updates-x86_64" has been synced with packages
    And I wait until the channel "sle-module-desktop-applications15-sp4-updates-x86_64" has been synced with packages
    And I wait until the channel "sle-module-devtools15-sp4-updates-x86_64" has been synced with packages
    And I wait until the channel "sle-module-containers15-sp4-pool-x86_64" has been synced with packages

@scc_credentials
@susemanager
  Scenario: Check reposync of SLES 15 Client Tools being finished
    Then I wait until the channel "sle-manager-tools15-pool-x86_64-sp4" has been synced
    And I wait until the channel "sle-manager-tools15-updates-x86_64-sp4" has been synced with packages
    And I wait until the channel "res8-manager-tools-updates-x86_64-rocky" has been synced with packages
    And I wait until the channel "res8-manager-tools-pool-x86_64-rocky" has been synced
    And I wait until the channel "ubuntu-22.04-suse-manager-tools-amd64" has been synced with packages

@uyuni
  Scenario: Check reposync of SLES 15 Client Tools being finished
    Then I wait until the channel "sles15-sp4-uyuni-client-x86_64" has been synced with packages
    And I wait until the channel "rockylinux8-uyuni-client-x86_64" has been synced with packages
    And I wait until the channel "ubuntu-2204-amd64-uyuni-client" has been synced with packages
