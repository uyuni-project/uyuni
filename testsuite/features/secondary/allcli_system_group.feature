# Copyright (c) 2017-2025 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_visualization
Feature: Manage a group of systems

  Scenario: Log in as org admin user
    Given I am authorized for the "Admin" section
    When I install the GPG key of the test packages repository on:"sle_minion" minion

  Scenario: Pre-requisite: enable dummy packages to fake an installation
    When I enable repository "test_repo_rpm_pool" on this "sle_minion"
    And I refresh the metadata for "sle_minion"
    And I install old package "andromeda-dummy-1.0" on this "sle_minion"
    And I install old package "virgo-dummy-1.0" on this "sle_minion"

  Scenario: Pre-requisite: ensure that errata are computed
    When I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Pre-requisite: ensure that fake channels were created
    When I follow the left menu "Patches > Patch List > Relevant"
    Then I should see an update in the list
    When I wait until I see "andromeda-dummy" text, refreshing the page
    Then I should see a "andromeda-dummy-6789" link
    When I enter "virgo-dummy" as the filtered synopsis
    And I click on the filter button
    And I wait until I see "virgo-dummy" text
    Then I should see a "virgo-dummy-3456" link

  Scenario: Fail to create a group with only its name
    When I follow the left menu "Systems > System Groups"
    And I follow "Create Group"
    And I enter "new-systems-group" as "name"
    And I click on "Create Group"
    Then I should see a "Both name and description are required for System Groups." text

  Scenario: Fail to create a group with only its description
    When I follow the left menu "Systems > System Groups"
    And I follow "Create Group"
    And I enter "My new group" as "description"
    And I click on "Create Group"
    Then I should see a "Both name and description are required for System Groups." text

  Scenario: Create a group
    When I follow the left menu "Systems > System Groups"
    And I follow "Create Group"
    And I enter "new-systems-group" as "name"
    And I enter "My new group" as "description"
    And I click on "Create Group"
    Then I should see a "System group new-systems-group created." text

  Scenario: Add the SLE minion system to the group
    When I follow the left menu "Systems > System Groups"
    And I follow "new-systems-group"
    And I follow "Target Systems"
    And I check the "sle_minion" client
    And I click on "Add Systems"
    Then I should see a "1 systems were added to new-systems-group server group." text
  
  Scenario: Add the SLE minion system to SSM
    When I follow the left menu "Systems > System Groups"
    And I follow "new-systems-group"
    And I follow " Systems"
    And I check the "sle_minion" client
    And I follow "Add Selected to SSM"

  Scenario: The SLE minion is part of the new group
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Groups" in the content area
    Then I should see a "new-systems-group" text

  Scenario: Apply a patch
    When I follow the left menu "Systems > System Groups"
    And I follow "new-systems-group"
    And I follow first "Patches"
    When I enter "virgo-dummy" as the filtered synopsis
    And I click on the filter button
    When I wait until I see "virgo-dummy-3456" text, refreshing the page
    Then I should see a "virgo-dummy-3456" link
    When I follow "virgo-dummy-3456"
    And I follow first "Affected Systems"
    And I check the "sle_minion" client
    And I click on "Apply Patches"
    And I click on "Confirm"
    Then I should see a "Patch virgo-dummy-3456 has been scheduled for 1 system" text

  Scenario: Apply a patch in the SSM
    When I follow the left menu "Systems > System Set Manager > Overview"
    And I follow first "Patches"
    When I enter "virgo-dummy" as the filtered synopsis
    And I click on the filter button
    When I wait until I see "virgo-dummy-3456" text, refreshing the page
    Then I should see a "virgo-dummy-3456" link
    When I follow "virgo-dummy-3456"
    And I follow first "Affected Systems"
    And I check the "sle_minion" client
    And I click on "Apply Patches"
    And I click on "Confirm"
    Then I should see a "Patch virgo-dummy-3456 has been scheduled for 1 system" text

  Scenario: Add the fake RPM repository to the SUSE fake child channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-RPM-SUSE-Channel"
    And I enter "file:///etc/pki/rpm-gpg/uyuni-tools-gpg-pubkey-0d20833e.key" as "GPG key URL"
    And I click on "Update Channel"
    Then I should see a "Channel Fake-RPM-SUSE-Channel updated" text
    When I follow "Repositories" in the content area
    And I select the "fake-rpm-repo" repo
    And I click on "Save Repositories"
    Then I should see a "Fake-RPM-SUSE-Channel repository information was successfully updated" text

  Scenario: Add the fake RPM repository to the Test child channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Test-Child-Channel-x86_64"
    And I enter "file:///etc/pki/rpm-gpg/uyuni-tools-gpg-pubkey-0d20833e.key" as "GPG key URL"
    And I click on "Update Channel"
    Then I should see a "Channel Test-Child-Channel-x86_64 updated" text
    When I follow "Repositories" in the content area
    And I select the "fake-rpm-repo" repo
    And I click on "Save Repositories"
    Then I should see a "Test-Child-Channel-x86_64 repository information was successfully updated" text

  Scenario: Install a package
    When I follow the left menu "Systems > System Set Manager > Overview"
    And I follow "Packages"
    And I follow "Install"
    Then I should see a "Fake-RPM-SUSE-Channel" text
    When I follow "Fake-RPM-SUSE-Channel"
    Then I should see a "andromeda-dummy-2.0-1.1" text
    And I enter "andromeda-dummy" as the filtered package name
    And I click on the filter button
    When I check "andromeda-dummy-2.0-1.1" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "Package installations are being scheduled, it may take several minutes for this to complete." text

  Scenario: Delete a package
    When I follow the left menu "Systems > System Set Manager > Overview"
    And I follow "Packages"
    And I follow "Remove"
    And I enter "virgo-dummy" as the filtered package name
    And I click on the filter button
    And I check "virgo-dummy-1.0-1.2" in the list
    And I click on "Remove Selected Packages"
    And I click on "Confirm"
    Then I should see a "Package removals are being scheduled, it may take several minutes for this to complete." text

@rhlike_minion
  Scenario: Add the Red Hat-like minion to the group in a different way
    When I follow the left menu "Systems > System Groups"
    Then I should see a "System Groups" text
    When I follow "new-systems-group"
    And I follow "Target Systems"
    Then I should see a "The following are systems that may be added to this group." text
    When I check the "rhlike_minion" client
    And I click on "Add Systems"
    Then I should see a "1 systems were added to new-systems-group server group" text

  Scenario: Add the new group to SSM
    When I follow the left menu "Systems > System Groups"
    And I click on "Use in SSM" in row "new-systems-group"
    Then I should see a "Selected Systems List" text
    And I should see "rhlike_minion" as link
    And I should see "sle_minion" as link

@skip_if_containerized_server
  # container already has locale formula installed
  Scenario: Install the locale formula package on the server
    When I manually install the "locale" formula on the server

  Scenario: I synchronize all Salt dynamic modules on "sle_minion"
    When I synchronize all Salt dynamic modules on "sle_minion"

  Scenario: New formula page is rendered for the system group
    When I follow the left menu "Systems > System Groups"
    And I follow "new-systems-group"
    And I follow "Formulas"
    Then I should see a "Choose formulas:" text
    And I should see a "General System Configuration" text
    And the "locale" formula should be unchecked

@rhlike_minion
  Scenario: Apply the highstate to the group
    When I follow the left menu "Systems > System Groups"
    Then I should see a "System Groups" text
    When I follow "new-systems-group"
    And I follow "States"
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    When I follow "scheduled"
    Then I should see a "Apply states (highstate)" text
    And I should see a "Action Details" text
    And I wait until I see "2 systems successfully completed this action." text, refreshing the page

  Scenario: Remove SLE minion from new group
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Groups"
    And I check "new-systems-group" in the list
    And I click on "Leave Selected Groups"
    Then I should see a "1 system groups removed." text

  # Red Hat-like minion is intentionally not removed from group

@skip_if_containerized_server
  Scenario: Cleanup: uninstall formula from the server
    When I manually uninstall the "locale" formula from the server

  Scenario: Cleanup: remove the new group
    When I follow the left menu "Systems > System Groups"
    When I follow "new-systems-group" in the content area
    And I follow "Delete Group" in the content area
    And I click on "Confirm Deletion"
    Then I should see a "System group" text
    And I should see a "new-systems-group" text
    And I should see a "deleted" text

  Scenario: Cleanup: regenerate search index for later tests
    When I clean the search index on the server

  Scenario: Cleanup: remove dummy packages
    When I disable repository "test_repo_rpm_pool" on this "sle_minion" without error control
    And I refresh the metadata for "sle_minion"
    And I remove package "andromeda-dummy" from this "sle_minion" without error control
    And I remove package "virgo-dummy" from this "sle_minion" without error control
