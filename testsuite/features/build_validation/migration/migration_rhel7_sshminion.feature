# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.
#
# RHEL 7 is the only generation where the conversion changes the base product, from
# RHEL 7 to SUSE Liberty Linux LTSS 7, so it is driven through Product Migration. The
# distupgrade state applies the Liberate formula as part of the migration.

@susemanager
@long_running
@rhel7_sshminion
Feature: Migrate a RHEL 7 Salt SSH minion to SUSE Liberty Linux LTSS 7

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Enable the Liberate formula on the RHEL 7 SSH minion
    Given I am on the Systems overview page of this "rhel7_sshminion"
    When I follow "Formulas" in the content area
    Then I should see a "Choose formulas:" text
    When I check the "liberate" formula
    And I click on "Save"
    Then I should see a "Formula saved" text

  Scenario: Configure the Liberate formula on the RHEL 7 SSH minion
    When I follow "Formulas" in the content area
    And I follow "Liberate" in the content area
    And I check "liberate#reinstall_packages"
    And I click on "Save"
    Then I should see a "Formula saved" text

  Scenario: Migrate this SSH minion to SUSE Liberty Linux LTSS 7
    Given I am on the Systems overview page of this "rhel7_sshminion"
    When I follow "Software" in the content area
    And I follow "Product Migration" in the content area
    And I wait until I see "Target Products:" text, refreshing the page
    And I wait until I see "SUSE Liberty Linux LTSS 7 x86_64" text
    And I click on "Select Channels"
    And I click on "Schedule Migration"
    Then I should see a "Product Migration - Confirm" text
    When I click on "Confirm"
    Then I should see a "This system is scheduled to be migrated to" text

  Scenario: Check the migration is successful for this SSH minion
    Given I am on the Systems overview page of this "rhel7_sshminion"
    When I follow "Events"
    And I follow "History"
    And I wait at most 3600 seconds until event "Product Migration" is completed
    And I wait until event "Package List Refresh" is completed

  Scenario: Check the Liberate formula converted this SSH minion
    Then "sles_es-release-server" should be installed on "rhel7_sshminion"
    And file "/etc/sysconfig/liberated" should exist on "rhel7_sshminion"
    And file "/etc/sysconfig/liberated" should contain "LIBERATED=.True." on "rhel7_sshminion"
    And file "/var/log/yum_sles_es_migration.log" should exist on "rhel7_sshminion"

  Scenario: Check that Software package refresh works on the liberated RHEL 7 SSH minion
    Given I am on the Systems overview page of this "rhel7_sshminion"
    When I follow "Software" in the content area
    And I wait until I see "Upgrade Packages" text
    And I click on "Update Package List"
    And I wait until event "Package List Refresh" is completed

  Scenario: Check that Hardware refresh works on the liberated RHEL 7 SSH minion
    Given I am on the Systems overview page of this "rhel7_sshminion"
    When I follow "Details" in the content area
    And I wait until I see "System Status" text
    And I follow "Hardware"
    And I wait until I see "Refresh Hardware List" text
    And I click on "Schedule Hardware Refresh"
    Then I should see a "You have successfully scheduled a hardware profile refresh" text
    And I wait until event "Hardware List Refresh" is completed
    And I wait until there is no Salt job calling the module "hardware.profileupdate" on "rhel7_sshminion"

  Scenario: Check the grains of the liberated RHEL 7 SSH minion on the details page
    Given I am on the Systems overview page of this "rhel7_sshminion"
    Then the hostname for "rhel7_sshminion" should be correct
    And the kernel for "rhel7_sshminion" should be correct
    And the OS version for "rhel7_sshminion" should be correct
    And the IPv4 address for "rhel7_sshminion" should be correct
    And the system ID for "rhel7_sshminion" should be correct
    And the system name for "rhel7_sshminion" should be correct

  Scenario: Install a package on the liberated RHEL 7 SSH minion
    Given I am on the Systems overview page of this "rhel7_sshminion"
    When I follow "Software" in the content area
    And I wait until I see "Upgrade Packages" text
    And I follow "Install"
    And I wait until I see "Installable Packages" text
    And I enter the package for "rhel7_sshminion" as the filtered package name
    And I click on the filter button
    And I check the package last version for "rhel7_sshminion" in the list
    And I click on "Install Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled for" text
    And I wait until event "Package Install/Upgrade" is completed

  Scenario: Remove the package from the liberated RHEL 7 SSH minion
    Given I am on the Systems overview page of this "rhel7_sshminion"
    When I follow "Software" in the content area
    And I wait until I see "Upgrade Packages" text
    And I follow "List / Remove"
    And I enter the package for "rhel7_sshminion" as the filtered package name
    And I click on the filter button
    And I check the package for "rhel7_sshminion" in the list
    And I click on "Remove Packages"
    And I click on "Confirm"
    Then I should see a "1 package removal has been scheduled" text
    And I wait until event "Package Removal" is completed

  Scenario: Run a remote command on the liberated RHEL 7 SSH minion
    When I follow the left menu "Salt > Remote Commands"
    Then I should see a "Remote Commands" text in the content area
    When I enter command "echo 'My remote command output'"
    And I enter the hostname of "rhel7_sshminion" as "target"
    And I click on preview
    Then I should see a "Target systems (1)" text
    When I wait until I do not see "pending" text
    And I click on run
    And I wait until I see "show response" text
    And I expand the results for "rhel7_sshminion"
    Then I should see "My remote command output" in the command output for "rhel7_sshminion"

  Scenario: Check events history for failures on the RHEL 7 SSH minion
    Given I am on the Systems overview page of this "rhel7_sshminion"
    Then I check for failed events on history event page
