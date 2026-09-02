# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@alma9_sshminion
Feature: Migrate a Alma 9 Salt SSH minion to SUSE Liberty Linux 9 using Liberate Formula

  Scenario: Prerequisite: Create Liberate System Group for SLL 9
    Given I am authorized for the "Admin" section
    When I follow the left menu "Systems > System Groups"
    And I follow "Create Group"
    And I enter "sll9-ssh-migration" as "name"
    And I enter "Systems to be converted to SUSE Liberty Linux 9" as "description"
    And I click on "Create Group"
    Then I should see a "System group sll9-ssh-migration created." text
    When I follow "Formulas"
    And I check the "liberate" formula
    And I click on "Save"
    Then I wait until I see "Formula saved." text
    When I follow "Liberate" in the content area
    And I check "liberate#reinstall_packages"
    And I check "liberate#install_logos"
    And I click on "Save Formula"
    Then I wait until I see "Formula saved. Apply the Highstate for the changes to take effect." text

  Scenario: Prerequisite: Prepare activation key for SLL 9
    When I follow the left menu "Systems > Activation Keys"
    And I click on "Create Key"
    And I enter "sll9_ssh_migration_key" as "key"
    And I enter "sll9_ssh_migration_key" as "description"
    And I select "EL9-Pool for x86_64" from "selectedBaseChannel"
    And I include the recommended child channels
    And I check "SLL-9-Updates for x86_64"
    And I wait until "SLL-9-Updates for x86_64" has been checked
    And I wait until "SLL-AS-9-Updates for x86_64" has been checked
    And I wait until "SLL-CB-9-Updates for x86_64" has been checked
    And I select "Push via SSH" from "contact-method"
    And I click on "Create Activation Key"
    Then I wait until I see "Activation key sll9_ssh_migration_key has been created." text
    When I check "Deploy configuration files to systems on registration"
    And I wait until I see "SLL-9-Updates for x86_64" text
    And I click on "Update Activation Key"
    Then I wait until I see "Activation key sll9_ssh_migration_key has been modified." text
    When I follow "Groups" in the content area
    And I follow first "Join"
    And I check the row with the "sll9-ssh-migration" link
    And I click on "Join Groups"
    Then I wait until I see "1 system groups added." text

  Scenario: Prerequisite: Set environment variables for the bootstrap script on the Alma 9 minion
    When I set the "ACTIVATION_KEYS" environment variable to "1-sll9_ssh_migration_key" on this "alma9_sshminion"
    And I set a reactivation key as environment variable on this "alma9_sshminion"
    And I navigate to the Systems overview page of this "alma9_sshminion"
    And I follow "Details" in the content area
    And I follow "Reactivation" in the content area
    Then I should see a "Key:" text

  Scenario: Migrate the Alma 9 SSH minion to SSL 9 with a bootstrap script
    When I follow the left menu "Admin > Manager Configuration > Bootstrap Script"
    And I click on "Update"
    Then I wait until I see "Bootstrap script successfully generated." text
    And I execute the "bootstrap.sh" bootstrap script on this "alma9_sshminion"
    When I delete "alma9_sshminion" key in the Salt master
    And I follow the left menu "Salt > Keys"
    And I should see a "Keys" text in the content area
    And I wait at most 60 seconds until Salt master sees "alma9_sshminion" as "unaccepted"
    And I accept "alma9_sshminion" key
    And I wait at most 9 seconds until Salt master sees "alma9_sshminion" as "accepted"

  Scenario: Check the migration is successful for this minion
    Given I am on the Systems overview page of this "alma9_sshminion"
    When I follow "Events"
    And I follow "History"
    And I wait at most 600 seconds until event "Apply highstate" is completed
    And I wait until event "Package List Refresh" is completed
    And I follow "Details" in the content area
    And I wait until I see "SUSE Liberty Linux 9 x86_64" text, refreshing the page
    Then file "/etc/sysconfig/liberated" should contain "LIBERATED_FROM=" on "alma9_sshminion"
