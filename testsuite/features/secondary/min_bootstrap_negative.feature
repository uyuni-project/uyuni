# Copyright (c) 2021-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@sle_minion
@scope_onboarding
Feature: Negative tests for bootstrapping normal minions
  In order to register only valid minions
  As an authorized user
  I want to avoid registration with invalid input parameters

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap should fail when minion already exists
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I click on "Bootstrap"
    And I wait until I see "A salt key for this host" text
    Then I should not see a "GenericSaltError" text
    And I should see a "seems to already exist, please check!" text

  Scenario: Delete SLES minion system profile before bootstrap negative tests
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "sle_minion" should not be registered

  Scenario: Bootstrap a SLES minion with wrong hostname
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter "not-existing-name" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I click on "Bootstrap"
    And I wait until I see "An error has occurred during salt execution: unable to establish ssh connection." text
    Then I should see a "Details" button
    When I click on "Details"
    And I wait at most 10 seconds until I see modal containing "Error Details" text
    Then I should see a "Standard Error" text
    And I should see "Could not resolve hostname not-existing-name: Name or service not known" in the textarea
    When I close the modal dialog

  Scenario: Bootstrap a SLES minion with wrong SSH credentials
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "FRANZ" as "user"
    And I enter "KAFKA" as "password"
    And I click on "Bootstrap"
    And I wait until I see "An error has occurred during salt execution: unable to establish ssh connection." text
    Then I should see a "Details" button
    When I click on "Details"
    And I wait at most 10 seconds until I see modal containing "Error Details" text
    Then I should see a "Standard Error" text
    And I should see "Permission denied (publickey,keyboard-interactive)." or "Password authentication failed" in the textarea
    When I close the modal dialog

  Scenario: Bootstrap a SLES minion with wrong SSH port number
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle_minion" as "hostname"
    And I enter "11" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I click on "Bootstrap"
    And I wait until I see "An error has occurred during salt execution: unable to establish ssh connection." text
    Then I should see a "Details" button
    When I click on "Details"
    And I wait at most 10 seconds until I see modal containing "Error Details" text
    Then I should see a "Standard Error" text
    And I should see "port 11: Connection refused" or "port 11: Invalid argument" in the textarea
    When I close the modal dialog

  Scenario: Cleanup: bootstrap a SLES minion after negative tests
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I follow the left menu "Systems > Overview"
    And I wait until I see the name of "sle_minion", refreshing the page

  Scenario: Cleanup: subscribe again to base channel after negative tests
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test-Channel-x86_64"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed
