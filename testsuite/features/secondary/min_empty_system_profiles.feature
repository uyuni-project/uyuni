# Copyright (c) 2018-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_onboarding
Feature: Empty minion profile operations

  Scenario: Create an empty minion profile with HW address via XML-RPC API
    Given I am logged in via XML-RPC system as user "admin" and password "admin"
    When I call system.create_system_profile() with name "empty-profile" and HW address "00:11:22:33:44:55"
    And I logout from XML-RPC system namespace

  Scenario: Create an empty minion profile with hostname via XML-RPC API
    Given I am logged in via XML-RPC system as user "admin" and password "admin"
    When I call system.create_system_profile() with name "empty-profile-hostname" and hostname "min-retail.mgr.suse.de"
    And I logout from XML-RPC system namespace

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Check the created empty minion profiles in Unprovisioned Systems page
    And I follow the left menu "System > System List > Unprovisioned Systems"
    And I wait until I see "empty-profile" text, refreshing the page
    And I wait until I see "00:11:22:33:44:55" text
    And I wait until I see "empty-profile-hostname" text

  Scenario: Check the empty profiles has the hostname set
    And I follow the left menu "System > System List > Unprovisioned Systems"
    And I follow "empty-profile-hostname"
    Then I wait until I see "min-retail.mgr.suse.de" text, refreshing the page

  Scenario: Check the empty minion profiles visible via XML-RPC
    Given I am logged in via XML-RPC system as user "admin" and password "admin"
    When I call system.list_empty_system_profiles()
    Then "empty-profile" should be present in the result
    And "empty-profile-hostname" should be present in the result

  Scenario: Cleanup: Delete first empty minion profile
    When I follow the left menu "Systems > System List"
    And I follow "empty-profile"
    And I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text

  Scenario: Cleanup: Delete second empty minion profiles
    When I follow the left menu "Systems > System List"
    And I follow "empty-profile-hostname"
    And I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text

