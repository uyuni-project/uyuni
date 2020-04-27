# Copyright (c) 2020 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Be able to bootstrap a Salt minion via the GUI using SSH key

  Scenario: Delete SLES minion system profile before bootstrap test
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    And I cleanup minion "sle_minion"
    Then "sle_minion" should not be registered

  Scenario: Prepare the minion for SSH key authentication
    When I backup the SSH authorized_keys file of host "sle_minion"
    And I add pre-generated SSH public key to authorized_keys of host "sle_minion"
  
  Scenario: Bootstrap a SLES minion using SSH Key with wrong passphrase
    Given I am authorized
    When I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I check radio button "SSH Private Key"
    And I attach the file "ssh_keypair/id_rsa_bootstrap-passphrase_linux" to "privKeyFile"
    And I enter "you-shall-not-pass" as "privKeyPwd"
    And I click on "Bootstrap"
    And I wait until I see "Permission denied, no authentication information" text

  Scenario: Bootstrap a SLES minion using SSH Key
    Given I am authorized
    When I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I check radio button "SSH Private Key"
    And I attach the file "ssh_keypair/id_rsa_bootstrap-passphrase_linux" to "privKeyFile"
    And I enter "linux" as "privKeyPwd"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text

  Scenario: Check new minion bootstrapped with SSH Key in System Overview page
    Given I am authorized
    When I go to the minion onboarding page
    Then I should see a "accepted" text
    When I navigate to "rhn/systems/Overview.do" page
    And I wait until I see the name of "sle_minion", refreshing the page
    And I wait until onboarding is completed for "sle_minion"
    Then the Salt master can reach "sle_minion"

  Scenario: Also check contact method of this minion
    Given I am on the Systems overview page of this "sle_minion"
    Then I should see a "Default" text
  
  Scenario: Cleanup authorized keys
    When I restore the SSH authorized_keys file of host "sle_minion"

  Scenario: Cleanup: turn the SLES minion into a container build host after bootstrap with SSH Key
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Details" in the content area
    And I follow "Properties" in the content area
    And I check "container_build_host"
    And I click on "Update Properties"

  Scenario: Cleanup: turn the SLES minion into a OS image build host after bootstrap with SSH Key
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Details" in the content area
    And I follow "Properties" in the content area
    And I check "osimage_build_host"
    And I click on "Update Properties"
    Then I should see a "OS Image Build Host type has been applied." text
    And I should see a "Note: This action will not result in state application" text
    And I should see a "To apply the state, either use the states page or run state.highstate from the command line." text
    And I should see a "System properties changed" text

  Scenario: Cleanup: apply the highstate to build host after bootstrap with SSH Key
    Given I am on the Systems overview page of this "sle_minion"
    When I wait until no Salt job is running on "sle_minion"
    And I enable repositories before installing Docker
    And I apply highstate on "sle_minion"
    And I wait until "docker" service is active on "sle_minion"
    And I wait until file "/var/lib/Kiwi/repo/rhn-org-trusted-ssl-cert-osimage-1.0-1.noarch.rpm" exists on "sle_minion"
    And I disable repositories after installing Docker

  Scenario: Cleanup: check that the minion is now a build host after bootstrap with SSH Key
    Given I am on the Systems overview page of this "sle_minion"
    Then I should see a "[Container Build Host]" text
    Then I should see a "[OS Image Build Host]" text
