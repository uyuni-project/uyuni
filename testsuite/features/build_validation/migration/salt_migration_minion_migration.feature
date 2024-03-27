# Copyright (c) 2023-2024 SUSE LLC
# Licensed under the terms of the MIT license.

# This feature depends on
# - srv_sync_all_products.feature which syncs SLES 15 SP5 channels and the client tools.
# We also test 'Bootstrapping using the command line' in this feature

@salt_migration_minion
Feature: Migrate Salt to bundled Salt on a SLES 15 SP5 minion

  Scenario: Log in as admin user in the Salt migration context
    Given I am authorized for the "Admin" section

  Scenario: Do some basic testing on the minion without Salt bundle
    When I follow the left menu "Salt > Remote Commands"
    Then I should see a "Remote Commands" text in the content area
    When I enter command "file /etc/salt"
    And I enter the hostname of "salt_migration_minion" as "target"
    And I click on preview
    Then I should see a "Target systems (1)" text
    And I should see a "Stop waiting" text
    And I click on stop waiting
    And I click on run
    And I wait until I do not see "pending" text
    And I expand the results for "salt_migration_minion"
    Then I should see "/etc/salt: directory" in the command output for "salt_migration_minion"

  @susemanager
  Scenario: Subscribe the minion to the SLES 15 SP5 channel and enable client tools in the Salt migration context
    Given I am on the Systems overview page of this "salt_migration_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "SLE-Product-SLES15-SP5-Pool for x86_64"
    And I wait until I see "SLE-Module-Basesystem15-SP5-Pool for x86_64" text
    And I check "SLE-Manager-Tools15-Pool for x86_64 SP4"
    And I check "SLE-Manager-Tools15-Updates for x86_64 SP4"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page

  @uyuni
  Scenario: Subscribe the minion to the SLES 15 SP5 channel and enable client tools in the Salt migration context
    Given I am on the Systems overview page of this "salt_migration_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "SLE-Product-SLES15-SP5-Pool for x86_64"
    And I wait until I see "SLE-Module-Basesystem15-SP5-Pool for x86_64" text
    And I check "Uyuni Client Tools for SLES15 SP5 x86_64"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page

  Scenario: Migrate the minion to the Salt bundle
    When I apply highstate on "salt_migration_minion"
    Then "venv-salt-minion" should be installed on "salt_migration_minion"
    And I wait until "venv-salt-minion" service is active on "salt_migration_minion"

  Scenario: Purge the minion from the old salt-minion leftovers
    When I purge salt-minion on "salt_migration_minion" after a migration

  Scenario: Check if the Salt bundle migration was successful
    When I follow the left menu "Salt > Remote Commands"
    Then I should see a "Remote Commands" text in the content area
    When I enter command "file /etc/salt"
    And I click on preview
    Then I should see "salt_migration_minion" hostname
    And I wait until I do not see "pending" text
    When I click on run
    And I wait until I do not see "pending" text
    And I expand the results for "salt_migration_minion"
    Then I should see "/etc/salt: cannot open `/etc/salt' (No such file or directory)" in the command output for "salt_migration_minion"
    When I enter command "file /etc/venv-salt-minion"
    And I click on preview
    Then I should see "salt_migration_minion" hostname
    And I wait until I do not see "pending" text
    When I click on run
    And I wait until I do not see "pending" text
    And I expand the results for "salt_migration_minion"
    Then I should see "/etc/venv-salt-minion: directory" in the command output for "salt_migration_minion"

  Scenario: Do some basic testing on the minion with the Salt bundle in the Salt migration context
    Given I am on the Systems overview page of this "salt_migration_minion"
    And I follow "Software" in the content area
    And I follow "Install"
    And I enter "adobe-sourcecodepro-fonts" as the filtered package name
    And I click on the filter button
    And I wait until I see "adobe-sourcecodepro-fonts" text
    And I check "adobe-sourcecodepro-fonts" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    When I wait until event "Package Install/Upgrade scheduled by admin" is completed
    Then "adobe-sourcecodepro-fonts" should be installed on "salt_migration_minion"
    And I follow "Software" in the content area
    And I follow "Install"
    When I follow "List / Remove"
    And I enter "adobe-sourcecodepro-fonts" as the filtered package name
    And I click on the filter button
    And I wait until I see "adobe-sourcecodepro-fonts" text
    And I check "adobe-sourcecodepro-fonts" in the list
    And I click on "Remove Packages"
    And I click on "Confirm"
    When I wait until event "Package Removal scheduled by admin" is completed
    Then "adobe-sourcecodepro-fonts" should not be installed on "salt_migration_minion"

  Scenario: Cleanup: remove the minion in the Salt migration context
    Given I am on the Systems overview page of this "salt_migration_minion"
    And I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "salt_migration_minion" should not be registered
