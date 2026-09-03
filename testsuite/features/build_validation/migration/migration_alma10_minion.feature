# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@alma10_minion
Feature: Migrate a Alma 10 Salt minion to SUSE Liberty Linux 10 using Liberate Formula

  Scenario: Prerequisite: Create Liberate System Group for SLL 10
    Given I am authorized for the "Admin" section
    When I follow the left menu "Systems > System Groups"
    And I follow "Create Group"
    And I enter "sll10-migration" as "name"
    And I enter "Systems to be converted to SUSE Liberty Linux 10" as "description"
    And I click on "Create Group"
    Then I should see a "System group sll10-migration created." text
    When I follow "Formulas"
    And I check the "liberate" formula
    And I click on "Save"
    Then I wait until I see "Formula saved." text
    When I follow "Liberate" in the content area
    And I check "liberate#install_packages"
    And I check "liberate#install_logos"
    And I click on "Save Formula"
    Then I wait until I see "Formula saved. Apply the Highstate for the changes to take effect." text

  Scenario: Prerequisite: Prepare activation key for SLL 10
    Given I am authorized for the "Admin" section
    When I follow the left menu "Systems > Activation Keys"
    And I click on "Create Key"
    And I wait until I do not see "SUSE Multi-Linux Manager Default" text
    When I follow the left menu "Systems > Activation Keys"
    And I click on "Create Key"
    And I enter "sll10_migration_key" as "key"
    And I enter "sll10_migration_key" as "description"
    And I select "EL10-Pool for x86_64" from "selectedBaseChannel"
    And I include the recommended child channels
    And I check "SLL-10-Updates for x86_64"
    And I wait until "SLL-10-Updates for x86_64" has been checked
    And I wait until "SLL-AS-10-Updates for x86_64" has been checked
    And I wait until "SLL-CB-10-Updates for x86_64" has been checked
    And I click on "Create Activation Key"
    Then I wait until I see "Activation key sll10_migration_key has been created." text
    When I check "Deploy configuration files to systems on registration"
    And I wait until I see "SLL-10-Updates for x86_64" text
    And I click on "Update Activation Key"
    Then I wait until I see "Activation key sll10_migration_key has been modified." text
    When I follow "Groups" in the content area
    And I follow first "Join"
    And I check the row with the "sll10-migration" link
    And I click on "Join Groups"
    Then I wait until I see "1 system groups added." text

  Scenario: Prerequisite: Set environment variables for the bootstrap script on the Alma 10 minion
    When I set the "ACTIVATION_KEYS" environment variable to "1-sll10_migration_key" on this "alma10_minion"
    And I set a reactivation key as environment variable on this "alma10_minion"
    And I navigate to the Systems overview page of this "alma10_minion"
    And I follow "Details" in the content area
    And I follow "Reactivation" in the content area
    Then I should see a "Key:" text

  Scenario: Migrate the Alma 10 minion to SSL 10 with a bootstrap script
    When I follow the left menu "Admin > Manager Configuration > Bootstrap Script"
    And I click on "Update"
    Then I wait until I see "Bootstrap script successfully generated." text
    And I execute the "bootstrap.sh" bootstrap script on this "alma10_minion"
    When I delete "alma10_minion" key in the Salt master
    And I follow the left menu "Salt > Keys"
    And I should see a "Keys" text in the content area
    And I wait at most 60 seconds until Salt master sees "alma10_minion" as "unaccepted"
    And I accept "alma10_minion" key
    And I wait at most 10 seconds until Salt master sees "alma10_minion" as "accepted"

  Scenario: Check the migration is successful for this minion
    Given I am on the Systems overview page of this "alma10_minion"
    When I follow "Events"
    And I follow "History"
    And I wait at most 600 seconds until event "Apply highstate" is completed
    And I wait until event "Package List Refresh" is completed
    And I follow "Details" in the content area
    And I wait until I see "SUSE Liberty Linux 10 x86_64" text, refreshing the page
    Then file "/etc/sysconfig/liberated" should contain "LIBERATED_FROM=" on "alma10_minion"
