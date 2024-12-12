# Copyright (c) 2021-2024 SUSE LLC
# Licensed under the terms of the MIT license.

@skip_if_github_validation
@sle_minion
@scope_onboarding
Feature: Bootstrapping with reactivation key
  In order to re-register valid minions
  As an authorized user
  I want to avoid re-registration with invalid input parameters

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: Generate a re-activation key
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Reactivation"
    And I click on "Generate New Key"
    Then I should see a "Key:" text

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

  Scenario: Bootstrap should fail when system already exists in the server
    Given I delete "sle_minion" key in the Salt master
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I click on "Bootstrap"
    And I wait until I see "seems to already exist, please check!" text
    Then I should not see a "GenericSaltError" text
    And I should see a "with minion id" text

  Scenario: Bootstrap a SLES minion with reactivation key
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I enter the reactivation key of "sle_minion"
    And I select "1-SUSE-KEY-x86_64" from "activationKeys"
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text
    And I follow the left menu "Systems > System List > All"
    And I wait until I see the name of "sle_minion", refreshing the page
    And I wait until onboarding is completed for "sle_minion"

  Scenario: Check the events history for the reactivation
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Events" in the content area
    And I follow "History" in the content area
    And I wait until I see "Server reactivated as Salt minion" text, refreshing the page
    And I wait until event "Apply states [certs, channels, packages, services.salt-minion] scheduled" is completed

  Scenario: Cleanup: delete SLES minion after reactivation tests
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    And I wait until Salt client is inactive on "sle_minion"
    Then "sle_minion" should not be registered

  Scenario: Cleanup: bootstrap a SLES minion after reactivation tests
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-SUSE-KEY-x86_64" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text
    And I follow the left menu "Systems > System List > All"
    And I wait until I see the name of "sle_minion", refreshing the page
    And I wait until onboarding is completed for "sle_minion"
