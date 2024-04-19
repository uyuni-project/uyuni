# Copyright (c) 2021-2023 SUSE LLC
# Licensed under the terms of the MIT license.
#
#
# This feature can cause failures in the following features:
# - features/secondary/min_bootstrap_script.feature
# If the minion fails to bootstrap again.

@skip_if_github_validation
@scope_onboarding
Feature: Bootstrap a Salt minion via the GUI using SSH key

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Delete SLES minion system profile before bootstrap with SSH key test
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    And I wait until Salt client is inactive on "sle_minion"
    Then "sle_minion" should not be registered

  Scenario: Prepare the minion for SSH key authentication
    When I backup the SSH authorized_keys file of host "sle_minion"
    And I add pre-generated SSH public key to authorized_keys of host "sle_minion"

  Scenario: Bootstrap a SLES minion using SSH key with wrong passphrase
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I check radio button "SSH Private Key"
    And I attach the file "ssh_keypair/id_rsa_bootstrap-passphrase_linux" to "privKeyFile"
    And I enter "you-shall-not-pass" as "privKeyPwd"
    And I click on "Bootstrap"
    And I wait until I see "An error has occurred during salt execution: unable to establish ssh connection." text
    Then I should see a "Details" button
    When I click on "Details"
    And I wait at most 10 seconds until I see modal containing "Error Details" text
    Then I should see a "Standard Error" text
    And I should see "Permission denied, no authentication information" or "Permission denied (publickey,password,keyboard-interactive)" in the stderr textarea
    When I close the modal dialog

  Scenario: Bootstrap a SLES minion using SSH key
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I check radio button "SSH Private Key"
    And I attach the file "ssh_keypair/id_rsa_bootstrap-passphrase_linux" to "privKeyFile"
    And I enter "linux" as "privKeyPwd"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    # workaround for bsc#1222108
    And I wait at most 480 seconds until I see "Bootstrap process initiated." text

  Scenario: Check new minion bootstrapped with SSH key in System Overview page
    When I follow the left menu "Salt > Keys"
    Then I should see a "accepted" text
    When I am on the Systems page
    And I wait until I see the name of "sle_minion", refreshing the page
    And I wait until onboarding is completed for "sle_minion"
    Then the Salt master can reach "sle_minion"

  Scenario: Also check contact method of this minion
    Given I am on the Systems overview page of this "sle_minion"
    Then I should see a "Default" text

  Scenario: Cleanup: restore authorized keys
    When I restore the SSH authorized_keys file of host "sle_minion"
