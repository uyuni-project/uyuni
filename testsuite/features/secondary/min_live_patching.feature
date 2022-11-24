# Copyright (c) 2022 SUSE LLC
# Licensed under the terms of the MIT license.

@sle_minion
Feature: Live Patching on a SLE Minion
  In order to check if systems are patched against certain vulnerabilities
  As an authorized user
  I want to see the Salt Minions that need to be patched

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Pre-requisite: Delete SLES minion system profile
    Given I am authorized for the "Admin" section
    And I am on the Systems overview page of this "sle_minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "sle_minion" should not be registered

  # Maybe we can just reuse the regular key
  Scenario: Bootstrap SLES minion with an activation key containing Live Patching product
    Given I am logged in API as user "admin" and password "admin"
    When I create an activation key including Live Patching product via API
    When I call system.bootstrap() on host "sle_minion" with activation key "live_patch_key"
    And I logout from API

  Scenario: Check that kernel livepatch is installed on the minion
    # TODO


 
  Scenario: downgrade