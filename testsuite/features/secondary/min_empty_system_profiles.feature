# Copyright (c) 2018-2024 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_onboarding
Feature: Empty minion profile operations

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: Create an empty minion profile with HW address via API
    When I call system.create_system_profile() with name "empty-profile" and HW address "00:11:22:33:44:55"

  Scenario: Create an empty minion profile with hostname via API
    When I call system.create_system_profile() with name "empty-profile-hostname" and hostname "min-retail.mgr.suse.de"

  Scenario: Check the created empty minion profiles in Unprovisioned Systems page
    And I follow the left menu "System > System List > Unprovisioned Systems"
    And I wait until I see "empty-profile" text, refreshing the page
    And I wait until I see "00:11:22:33:44:55" text
    And I wait until I see "empty-profile-hostname" text

  Scenario: Check the empty profiles has the hostname set
    And I follow the left menu "System > System List > Unprovisioned Systems"
    And I follow "empty-profile-hostname"
    Then I wait until I see "min-retail.mgr.suse.de" text, refreshing the page

  Scenario: Check the empty minion profiles visible via API
    When I call system.list_empty_system_profiles()
    Then "empty-profile" should be present in the result
    And "empty-profile-hostname" should be present in the result

  Scenario: Cleanup: Delete first empty minion profile
    When I follow the left menu "Systems > System List"
    And I wait until I see the "empty-profile" system, refreshing the page
    And I follow "empty-profile"
    And I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text

  Scenario: Cleanup: Delete second empty minion profiles
    When I follow the left menu "Systems > System List"
    And I wait until I see the "empty-profile-hostname" system, refreshing the page
    And I follow "empty-profile-hostname"
    And I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
