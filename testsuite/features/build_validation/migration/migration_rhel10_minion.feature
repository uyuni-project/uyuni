# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.
#
# RHEL 10 clients are registered against the same base product the SUSE Liberty Linux
# channels hang from, so there is no product migration to schedule. The conversion is
# done by subscribing to the SUSE Liberty Linux child channels and applying the Liberate
# formula, as described in the SUSE Liberty Linux client documentation.

@susemanager
@long_running
@rhel10_minion
Feature: Liberate a RHEL 10 Salt minion into SUSE Liberty Linux 10

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Subscribe the RHEL 10 minion to the SUSE Liberty Linux 10 channels
    Given I am on the Systems overview page of this "rhel10_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check "SLL-10-Updates for x86_64"
    And I check "SLL-AS-10-Updates for x86_64"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page

  Scenario: Enable the Liberate formula on the RHEL 10 minion
    Given I am on the Systems overview page of this "rhel10_minion"
    When I follow "Formulas" in the content area
    Then I should see a "Choose formulas:" text
    When I check the "liberate" formula
    And I click on "Save"
    Then I should see a "Formula saved" text

  Scenario: Configure the Liberate formula on the RHEL 10 minion
    When I follow "Formulas" in the content area
    And I follow "Liberate" in the content area
    And I check "liberate#reinstall_packages"
    And I click on "Save"
    Then I should see a "Formula saved" text

  Scenario: Liberate the RHEL 10 minion by applying the highstate
    Given I am on the Systems overview page of this "rhel10_minion"
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    And I wait at most 5400 seconds until event "Apply highstate scheduled" is completed

  Scenario: Check the Liberate formula converted the RHEL 10 minion
    Then "sll-release" should be installed on "rhel10_minion"
    And file "/etc/sysconfig/liberated" should exist on "rhel10_minion"
    And file "/etc/sysconfig/liberated" should contain "LIBERATED=.True." on "rhel10_minion"
    And file "/var/log/dnf_sll_migration.log" should exist on "rhel10_minion"

  Scenario: Detect latest Salt changes on the RHEL 10 minion
    When I query latest Salt changes on "rhel10_minion"

  Scenario: Check that Software package refresh works on the liberated RHEL 10 minion
    Given I am on the Systems overview page of this "rhel10_minion"
    When I follow "Software" in the content area
    And I wait until I see "Upgrade Packages" text
    And I click on "Update Package List"
    And I wait until event "Package List Refresh" is completed

  Scenario: Check that Hardware refresh works on the liberated RHEL 10 minion
    Given I am on the Systems overview page of this "rhel10_minion"
    When I follow "Details" in the content area
    And I wait until I see "System Status" text
    And I follow "Hardware"
    And I wait until I see "Refresh Hardware List" text
    And I click on "Schedule Hardware Refresh"
    Then I should see a "You have successfully scheduled a hardware profile refresh" text
    And I wait until event "Hardware List Refresh" is completed
    And I wait until there is no Salt job calling the module "hardware.profileupdate" on "rhel10_minion"

  Scenario: Check the grains of the liberated RHEL 10 minion on the details page
    Given I am on the Systems overview page of this "rhel10_minion"
    Then the hostname for "rhel10_minion" should be correct
    And the kernel for "rhel10_minion" should be correct
    And the OS version for "rhel10_minion" should be correct
    And the IPv4 address for "rhel10_minion" should be correct
    And the system ID for "rhel10_minion" should be correct
    And the system name for "rhel10_minion" should be correct

  Scenario: Install a package on the liberated RHEL 10 minion
    Given I am on the Systems overview page of this "rhel10_minion"
    When I follow "Software" in the content area
    And I wait until I see "Upgrade Packages" text
    And I follow "Install"
    And I wait until I see "Installable Packages" text
    And I enter the package for "rhel10_minion" as the filtered package name
    And I click on the filter button
    And I check the package last version for "rhel10_minion" in the list
    And I click on "Install Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled for" text
    And I wait until event "Package Install/Upgrade" is completed

  Scenario: Remove the package from the liberated RHEL 10 minion
    Given I am on the Systems overview page of this "rhel10_minion"
    When I follow "Software" in the content area
    And I wait until I see "Upgrade Packages" text
    And I follow "List / Remove"
    And I enter the package for "rhel10_minion" as the filtered package name
    And I click on the filter button
    And I check the package for "rhel10_minion" in the list
    And I click on "Remove Packages"
    And I click on "Confirm"
    Then I should see a "1 package removal has been scheduled" text
    And I wait until event "Package Removal" is completed

  Scenario: Run a remote command on the liberated RHEL 10 minion
    When I follow the left menu "Salt > Remote Commands"
    Then I should see a "Remote Commands" text in the content area
    When I enter command "echo 'My remote command output'"
    And I enter the hostname of "rhel10_minion" as "target"
    And I click on preview
    Then I should see a "Target systems (1)" text
    When I wait until I do not see "pending" text
    And I click on run
    And I wait until I see "show response" text
    And I expand the results for "rhel10_minion"
    Then I should see "My remote command output" in the command output for "rhel10_minion"

  Scenario: Check events history for failures on the RHEL 10 minion
    Given I am on the Systems overview page of this "rhel10_minion"
    Then I check for failed events on history event page
