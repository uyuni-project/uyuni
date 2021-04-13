# Copyright (c) 2018-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_onboarding
Feature: Bootstrap a Salt minion via the GUI with an activation key

  Scenario: Delete SLES minion system profile
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "sle_minion" should not be registered

  Scenario: Create a configuration channel for the activation key
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Configuration > Channels"
    And I follow "Create Config Channel"
    And I enter "Key Channel" as "cofName"
    And I enter "keychannel" as "cofLabel"
    And I enter "This is a configuration channel for the activation key" as "cofDescription"
    And I click on "Create Config Channel"
    Then I should see a "Key Channel" text

  Scenario: Add a configuration file to the key configuration channel
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Configuration > Channels"
    And I follow "Key Channel"
    And I follow "Create Configuration File or Directory"
    And I enter "/etc/euler.conf" as "cffPath"
    And I enter "e^i.pi=-1" in the editor
    And I click on "Create Configuration File"

  Scenario: Create a complete minion activation key
    Given I am on the Systems page
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I enter "Minion testing" as "description"
    And I enter "MINION-TEST" as "key"
    And I enter "20" as "usageLimit"
    And I select "Test-Channel-x86_64" from "selectedBaseChannel"
    And I click on "Create Activation Key"
    And I follow "Configuration" in the content area
    And I follow first "Subscribe to Channels" in the content area
    And I check "Key Channel" in the list
    And I click on "Continue"
    And I follow "Packages"
    And I enter "orion-dummy perseus-dummy" as "packages"
    And I click on "Update Activation Key"
    Then I should see a "Activation key Minion testing has been modified" text

  Scenario: Bootstrap a SLES minion with an activation key
    Given I am authorized
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-MINION-TEST" from "activationKeys"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    When I am on the System Overview page
    And I wait until I see the name of "sle_minion", refreshing the page
    And I wait until onboarding is completed for "sle_minion"

  Scenario: Verify that minion bootstrapped with Salt key and packages
    Given I am authorized
    When I follow the left menu "Salt > Keys"
    Then I should see a "accepted" text
    And the Salt master can reach "sle_minion"
    When I wait for "orion-dummy" to be installed on "sle_minion"
    And I wait for "perseus-dummy" to be installed on "sle_minion"

  Scenario: Check system ID of bootstrapped minion
    Given I am on the Systems overview page of this "sle_minion"
    Then I run spacecmd listevents for "sle_minion"

  Scenario: Verify that minion bootstrapped with activation key
    Given I am on the Systems overview page of this "sle_minion"
    Then I should see a "Activation Key: 	1-MINION-TEST" text
    And the "activation_key" on "sle_minion" grains does not exist

  Scenario: Verify that minion bootstrapped with base channel
    Given I am on the Systems page
    Then I should see a "Test-Channel-x86_64" text

  # bsc#1080807 - Assigning configuration channel in activation key doesn't work
  Scenario: Verify that minion bootstrapped with configuration channel
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Configuration" in the content area
    Then I should see a "1 configuration channel" text
    When I follow "View Files" in the content area
    Then I should see a "/etc/euler.conf" text
    And I should see a "Key Channel" text

  Scenario: Cleanup: remove the package states
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "States" in the content area
    And I follow "Packages"
    Then I should see a "Package States" text
    When I change the state of "orion-dummy" to "Unmanaged" and ""
    And I change the state of "perseus-dummy" to "Unmanaged" and ""
    Then I should see a "2 Changes" text
    When I click save
    Then I wait until I see "Package states have been saved." text
    And I click apply
    And I remove package "orion-dummy" from this "sle_minion"
    And I remove package "perseus-dummy" from this "sle_minion"

  Scenario: Cleanup: remove the key configuration channel
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Configuration > Channels"
    And I follow "Key Channel"
    And I follow "Delete Channel"
    And I click on "Delete Config Channel"

  Scenario: Cleanup: delete the activation key
    Given I am on the Systems page
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Minion testing" in the content area
    And I follow "Delete Key"
    And I click on "Delete Activation Key"
    Then I should see a "Activation key Minion testing has been deleted." text

  Scenario: Check events history for failures on SLES minion with activation key
    Given I am on the Systems overview page of this "sle_minion"
    Then I check for failed events on history event page
