# Copyright (c) 2020-2025 SUSE LLC
# SPDX-License-Identifier: MIT

Feature: Synchronize product channels

@scc_credentials
@susemanager
  Scenario: Reposync of Rocky Linux 8 channels has finished
    When I wait until the channel "rockylinux8-x86_64" has been synced
    And I wait until the channel "rockylinux8-appstream-x86_64" has been synced

@uyuni
  Scenario: Reposync of Rocky Linux 8 channels has finished
    When I wait until the channel "rockylinux8-x86_64" has been synced
    And I wait until the channel "rockylinux8-x86_64-appstream" has been synced

@scc_credentials
@susemanager
  Scenario: Reposync of Ubuntu 24.04 channels has finished
    When I wait until the channel "ubuntu-2404-amd64-main-amd64" has been synced
    And I wait until the channel "ubuntu-2404-amd64-main-updates-amd64" has been synced
    And I wait until the channel "ubuntu-2404-amd64-main-security-amd64" has been synced

@uyuni
  Scenario: Reposync of Ubuntu 24.04 channels has finished
    When I wait until the channel "ubuntu-2404-pool-amd64-uyuni" has been synced
    And I wait until the channel "ubuntu-2404-amd64-main-uyuni" has been synced
    And I wait until the channel "ubuntu-2404-amd64-main-updates-uyuni" has been synced
    And I wait until the channel "ubuntu-2404-amd64-main-security-uyuni" has been synced

@scc_credentials
@susemanager
  Scenario: Reposync of SLES 15 SP4 channels has finished
    When I wait until the channel "sle-module-basesystem15-sp4-updates-x86_64" has been synced
    And I wait until the channel "sle-module-server-applications15-sp4-updates-x86_64" has been synced
    And I wait until the channel "sle-module-desktop-applications15-sp4-updates-x86_64" has been synced
    And I wait until the channel "sle-module-devtools15-sp4-updates-x86_64" has been synced
    And I wait until the channel "sle-module-containers15-sp4-pool-x86_64" has been synced

@uyuni
  Scenario: Reposync of openSUSE Tumbleweed channels has finished
    When I wait until the channel "opensuse_tumbleweed-x86_64" has been synced
    And I wait until the channel "opensuse_tumbleweed-uyuni-client-devel-x86_64" has been synced
    And I wait until the channel "uyuni-proxy-devel-tumbleweed-x86_64" has been synced

@scc_credentials
@susemanager
  Scenario: Reposync of client tools has finished
    When I wait until the channel "sle-manager-tools15-pool-x86_64-sp4" has been synced
    And I wait until the channel "sle-manager-tools15-updates-x86_64-sp4" has been synced
    And I wait until the channel "res8-manager-tools-updates-x86_64-rocky" has been synced
    And I wait until the channel "res8-manager-tools-pool-x86_64-rocky" has been synced
    And I wait until the channel "ubuntu-2404-suse-manager-tools-amd64" has been synced

@beta
@scc_credentials
@susemanager
  Scenario: Reposync of beta client tools has finished
    When I wait until the channel "sle-manager-tools15-beta-pool-x86_64-sp4" has been synced
    And I wait until the channel "sle-manager-tools15-beta-updates-x86_64-sp4" has been synced

@uyuni
  Scenario: Reposync of Uyuni client tools has finished
    When I wait until the channel "opensuse_tumbleweed-uyuni-client-devel-x86_64" has been synced
    And I wait until the channel "rockylinux8-uyuni-client-x86_64" has been synced
    And I wait until the channel "ubuntu-2404-amd64-uyuni-client" has been synced
