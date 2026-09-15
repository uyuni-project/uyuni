# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_hub
@hub_server_to_server
@peripheral2
@rocky10_minion
Feature: Smoke tests for rocky10_minion on peripheral2
  In order to manage rocky10_minion on peripheral2
  As an authorized user
  I want to:
  - View the details of the system
  - Install a package via Web UI
  - Install a patch via Web UI
  - Remove a package via Web UI
  - Execute a remote command via Web UI
  - Apply a configuration file via Web UI
  - Schedule Software package refresh
  - Schedule Hardware refresh
  - Reboot the client via Web UI
  - Enable Prometheus and Prometheus Exporter

  Scenario: Log in as admin on peripheral2 for Rocky Linux 10 smoke tests
    Given I am authorized for the "Admin" section on "peripheral2"

  Scenario: Check that Software package refresh works on a rocky10_minion
    Given I am on the Systems overview page of this "rocky10_minion" on peripheral2
    When I follow "Software" in the content area
    And I wait until I see "Upgrade Packages" text
    And I click on "Update Package List"
    And I wait until event "Package List Refresh scheduled by admin" is completed

  Scenario: Check that Hardware Refresh button works on a rocky10_minion
    Given I am on the Systems overview page of this "rocky10_minion" on peripheral2
    When I follow "Details" in the content area
    And I wait until I see "System Status" text
    And I follow "Hardware"
    And I wait until I see "Refresh Hardware List" text
    And I click on "Schedule Hardware Refresh"
    Then I should see a "You have successfully scheduled a hardware profile refresh" text
    And I wait until event "Hardware List Refresh scheduled by admin" is completed
    And I wait until there is no Salt job calling the module "hardware.profileupdate" on "rocky10_minion"

  Scenario: Client rocky10_minion grains are displayed correctly on the details page
    Given I am on the Systems overview page of this "rocky10_minion" on peripheral2
    Then the hostname for "rocky10_minion" should be correct
    And the kernel for "rocky10_minion" should be correct
    And the OS version for "rocky10_minion" should be correct
    And the IPv4 address for "rocky10_minion" should be correct
    And the IPv6 address for "rocky10_minion" should be correct
    And the system name for "rocky10_minion" should be correct

  Scenario: Client rocky10_minion fields are displayed correctly on the details page
    And I should see a "Virtualization" text
    And I should see a "Installed Products" text
    And I should see a "Checked In" text
    And I should see a "Registered" text
    And I should see a "Contact Method" text
    And I should see a "Auto Patch Update" text
    And I should see a "Maintenance Schedule" text
    And I should see a "Description" text
    And I should see a "Location" text
    And I should see a "UUID" text

  Scenario: Install a patch on the rocky10_minion
    Given I am on the Systems overview page of this "rocky10_minion" on peripheral2
    When I follow "Software" in the content area
    And I wait until I see "Upgrade Packages" text
    And I follow "Patches" in the content area
    When I wait until I see "Relevant Patches" text
    When I check the first patch in the list, that does not require a reboot
    And I click on "Apply Patches"
    And I click on "Confirm"
    Then I should see a "1 patch update has been scheduled for" text
    And I wait until event "Patch Update:" is completed

  Scenario: Install a package on the rocky10_minion
    Given I am on the Systems overview page of this "rocky10_minion" on peripheral2
    When I follow "Software" in the content area
    And I wait until I see "Upgrade Packages" text
    And I follow "Install"
    And I wait until I see "Installable Packages" text
    And I enter the package for "rocky10_minion" as the filtered package name
    And I click on the filter button
    And I check the package last version for "rocky10_minion" in the list
    And I click on "Install Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled for" text
    And I wait until event "Package Install/Upgrade scheduled by admin" is completed

  Scenario: Remove package from rocky10_minion
    When I follow "Software" in the content area
    And I wait until I see "Upgrade Packages" text
    And I follow "List / Remove"
    And I enter the package for "rocky10_minion" as the filtered package name
    And I click on the filter button
    And I check the package for "rocky10_minion" in the list
    And I click on "Remove Packages"
    And I click on "Confirm"
    Then I should see a "1 package removal has been scheduled" text
    And I wait until event "Package Removal scheduled by admin" is completed

  Scenario: Run a remote command on rocky10_minion
    When I follow the left menu "Salt > Remote Commands"
    Then I should see a "Remote Commands" text in the content area
    When I enter command "echo 'My remote command output'"
    And I enter the hostname of "rocky10_minion" as "target"
    And I click on preview
    Then I should see a "Target systems (1)" text
    When I wait until I do not see "pending" text
    And I click on run
    And I wait until I see "show response" text
    And I expand the results for "rocky10_minion"
    Then I should see "My remote command output" in the command output for "rocky10_minion"

  Scenario: Reboot the rocky10_minion and wait until reboot is completed
    Given I am on the Systems overview page of this "rocky10_minion" on peripheral2
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I should see a "Reboot scheduled for system" text
    And I wait at most 600 seconds until event "System reboot scheduled by admin" is completed
    Then I should see a "This action's status is: Completed" text

  Scenario: Install spacecmd from the client tools on the rocky10_minion
    Given I am on the Systems overview page of this "rocky10_minion" on peripheral2
    When I follow "Software" in the content area
    And I wait until I see "Upgrade Packages" text
    And I follow "Install"
    And I wait until I see "Installable Packages" text
    And I enter "spacecmd" as the filtered package name
    And I click on the filter button
    And I check "spacecmd" last version in the list
    And I click on "Install Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled for" text
    And I wait until event "Package Install/Upgrade scheduled by admin" is completed

  Scenario: Enable Prometheus Node exporter formula on the rocky10_minion
    Given I am on the Systems overview page of this "rocky10_minion" on peripheral2
    When I follow "Formulas" in the content area
    And I wait until I see "Salt Formulas" text
    And I check the "prometheus-exporters" formula
    And I click on "Save"
    Then I wait until I see "Formula saved" text
    When I follow "Prometheus Exporters" in the content area
    And I wait until I see "configure Prometheus exporters" text
    And I click on "Expand All Sections"
    And I should see a "Enable and configure Prometheus exporters for managed systems." text
    And I check "node" exporter
    And I click on "Save"
    Then I should see a "Formula saved" text

  Scenario: Apply highstate for the Prometheus exporters on the rocky10_minion
    Given I am on the Systems overview page of this "rocky10_minion" on peripheral2
    When I follow "States" in the content area
    And I wait until I see "Apply Highstate" text
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    And I wait until event "Apply highstate scheduled by admin" is completed

  Scenario: Visit Node monitoring endpoint on the rocky10_minion
    When I wait until "node" exporter service is active on "rocky10_minion"
    And I visit "Prometheus node exporter" endpoint of this "rocky10_minion"

  Scenario: Check events history for failures on rocky10_minion after smoke tests
    Given I am on the Systems overview page of this "rocky10_minion" on peripheral2
    Then I check for failed events on history event page
