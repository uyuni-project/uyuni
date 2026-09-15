# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_hub
@hub_server_to_server
@peripheral1
@sles15sp7_minion
Feature: Smoke tests for sles15sp7_minion on peripheral1
  In order to manage sles15sp7_minion on peripheral1
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

  Scenario: Log in as admin on peripheral1 for SLES 15 SP7 smoke tests
    Given I am authorized for the "Admin" section on "peripheral1"

  Scenario: Check that Software package refresh works on a sles15sp7_minion
    Given I am on the Systems overview page of this "sles15sp7_minion" on peripheral1
    When I follow "Software" in the content area
    And I wait until I see "Upgrade Packages" text
    And I click on "Update Package List"
    And I wait until event "Package List Refresh scheduled by admin" is completed

  Scenario: Check that Hardware Refresh button works on a sles15sp7_minion
    Given I am on the Systems overview page of this "sles15sp7_minion" on peripheral1
    When I follow "Details" in the content area
    And I wait until I see "System Status" text
    And I follow "Hardware"
    And I wait until I see "Refresh Hardware List" text
    And I click on "Schedule Hardware Refresh"
    Then I should see a "You have successfully scheduled a hardware profile refresh" text
    And I wait until event "Hardware List Refresh scheduled by admin" is completed
    And I wait until there is no Salt job calling the module "hardware.profileupdate" on "sles15sp7_minion"

  Scenario: Client sles15sp7_minion grains are displayed correctly on the details page
    Given I am on the Systems overview page of this "sles15sp7_minion" on peripheral1
    Then the hostname for "sles15sp7_minion" should be correct
    And the kernel for "sles15sp7_minion" should be correct
    And the OS version for "sles15sp7_minion" should be correct
    And the IPv4 address for "sles15sp7_minion" should be correct
    And the IPv6 address for "sles15sp7_minion" should be correct
    And the system ID for "sles15sp7_minion" should be correct
    And the system name for "sles15sp7_minion" should be correct

  Scenario: Client sles15sp7_minion fields are displayed correctly on the details page
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

  Scenario: Install a patch on the sles15sp7_minion
    Given I am on the Systems overview page of this "sles15sp7_minion" on peripheral1
    When I follow "Software" in the content area
    And I wait until I see "Upgrade Packages" text
    And I follow "Patches" in the content area
    When I wait until I see "Relevant Patches" text
    When I check the first patch in the list, that does not require a reboot
    And I click on "Apply Patches"
    And I click on "Confirm"
    Then I should see a "1 patch update has been scheduled for" text
    And I wait until event "Patch Update:" is completed

  Scenario: Install a package on the sles15sp7_minion
    Given I am on the Systems overview page of this "sles15sp7_minion" on peripheral1
    When I follow "Software" in the content area
    And I wait until I see "Upgrade Packages" text
    And I follow "Install"
    And I wait until I see "Installable Packages" text
    And I enter the package for "sles15sp7_minion" as the filtered package name
    And I click on the filter button
    And I check the package last version for "sles15sp7_minion" in the list
    And I click on "Install Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled for" text
    And I wait until event "Package Install/Upgrade scheduled by admin" is completed

  Scenario: Remove package from sles15sp7_minion
    When I follow "Software" in the content area
    And I wait until I see "Upgrade Packages" text
    And I follow "List / Remove"
    And I enter the package for "sles15sp7_minion" as the filtered package name
    And I click on the filter button
    And I check the package for "sles15sp7_minion" in the list
    And I click on "Remove Packages"
    And I click on "Confirm"
    Then I should see a "1 package removal has been scheduled" text
    And I wait until event "Package Removal scheduled by admin" is completed

  Scenario: Run a remote command on sles15sp7_minion
    When I follow the left menu "Salt > Remote Commands"
    Then I should see a "Remote Commands" text in the content area
    When I enter command "echo 'My remote command output'"
    And I enter the hostname of "sles15sp7_minion" as "target"
    And I click on preview
    Then I should see a "Target systems (1)" text
    When I wait until I do not see "pending" text
    And I click on run
    And I wait until I see "show response" text
    And I expand the results for "sles15sp7_minion"
    Then I should see "My remote command output" in the command output for "sles15sp7_minion"

  Scenario: Subscribe sles15sp7_minion to the configuration channel
    Given I am on the Systems overview page of this "sles15sp7_minion" on peripheral1
    When I follow "Configuration" in the content area
    And I wait until I see "Configuration Overview" text
    And I follow "Manage Configuration Channels" in the content area
    And I wait until I see "centrally-managed configuration" text
    And I follow first "Subscribe to Channels" in the content area
    And I wait until I see "Select Channels for Subscription" text
    And I check "Mixed Channel" in the list
    And I click on "Continue"
    And I click on "Update Channel Rankings"
    Then I should see a "Channel Subscriptions successfully changed for" text

  Scenario: Reboot the sles15sp7_minion and wait until reboot is completed
    Given I am on the Systems overview page of this "sles15sp7_minion" on peripheral1
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I should see a "Reboot scheduled for system" text
    And I wait at most 600 seconds until event "System reboot scheduled by admin" is completed
    Then I should see a "This action's status is: Completed" text

  Scenario: Deploy the configuration file to sles15sp7_minion
    When I follow the left menu "Configuration > Channels"
    And I wait until I see "Centrally Managed Configuration Channel" text
    And I follow "Mixed Channel"
    And I wait until I see "Channel Properties" text
    And I follow "Deploy all configuration files to selected subscribed systems"
    And I wait until I see "Select Systems for Deployment" text
    And I enter the hostname of "sles15sp7_minion" as the filtered system name
    And I click on the filter button
    And I check the "sles15sp7_minion" client
    And I click on "Confirm & Deploy to Selected Systems"
    And I wait until I see "Revision 1" text
    Then I should see a "/etc/s-mgr/config" link
    When I click on "Deploy Files to Selected Systems"
    Then I should see a "1 revision-deploy is being scheduled." text
    And I should see a "0 revision-deploys overridden." text
    And I wait until file "/etc/s-mgr/config" exists on "sles15sp7_minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "sles15sp7_minion"

  Scenario: Install spacecmd from the client tools on the sles15sp7_minion
    Given I am on the Systems overview page of this "sles15sp7_minion" on peripheral1
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

  Scenario: Enable Prometheus Node exporter formula on the sles15sp7_minion
    Given I am on the Systems overview page of this "sles15sp7_minion" on peripheral1
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

  Scenario: Enable Prometheus Apache and Postgres exporter formula on the sles15sp7_minion
    Given I am on the Systems overview page of this "sles15sp7_minion" on peripheral1
    When I follow "Formulas" in the content area
    When I follow "Prometheus Exporters" in the content area
    And I click on "Expand All Sections"
    And I should see a "Enable and configure Prometheus exporters for managed systems." text
    And I check "apache" exporter
    And I check "postgres" exporter
    And I click on "Save"
    Then I should see a "Formula saved" text

  Scenario: Apply highstate for the Prometheus exporters on the sles15sp7_minion
    Given I am on the Systems overview page of this "sles15sp7_minion" on peripheral1
    When I follow "States" in the content area
    And I wait until I see "Apply Highstate" text
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    And I wait until event "Apply highstate scheduled by admin" is completed

  Scenario: Visit Node monitoring endpoint on the sles15sp7_minion
    When I wait until "node" exporter service is active on "sles15sp7_minion"
    And I visit "Prometheus node exporter" endpoint of this "sles15sp7_minion"

  Scenario: Visit Apache and Postgres monitoring endpoint on the sles15sp7_minion
    When I wait until "apache" exporter service is active on "sles15sp7_minion"
    And I visit "Prometheus apache exporter" endpoint of this "sles15sp7_minion"
    And I wait until "postgres" exporter service is active on "sles15sp7_minion"
    And I visit "Prometheus postgres exporter" endpoint of this "sles15sp7_minion"

  Scenario: Check events history for failures on sles15sp7_minion after smoke tests
    Given I am on the Systems overview page of this "sles15sp7_minion" on peripheral1
    Then I check for failed events on history event page
