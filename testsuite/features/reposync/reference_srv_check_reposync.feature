# Copyright (c) 2020-2023 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Reposync works as expected

@scc_credentials
@susemanager
  Scenario: Check reposync of SLES 15 SP4 channels being finished
    Then I wait until the channel "sle-module-basesystem15-sp4-updates-x86_64" has been synced
    And I wait until the channel "sle-manager-tools15-updates-x86_64-sp4" has been synced
    And I wait until the channel "sle-module-server-applications15-sp4-updates-x86_64" has been synced
    And I wait until the channel "sle-module-containers15-sp4-pool-x86_64" has been synced

@scc_credentials
@susemanager
  Scenario: Check reposync of Rocky Linux 8 channels being finished
    Then I wait until the channel "rockylinux-8-x86_64" has been synced
    And I wait until the channel "rockylinux-8-appstream-x86_64" has been synced
    And I wait until the channel "res8-manager-tools-updates-x86_64-rocky" has been synced

