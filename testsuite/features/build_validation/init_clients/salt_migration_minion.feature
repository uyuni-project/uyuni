# Copyright (c) 2024 SUSE LLC
# Licensed under the terms of the MIT license.

@salt_migration_minion
Feature: Bootstrap a SLES 15 SP5 minion without the Salt bundle

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap the minion without the Salt bundle
    When I wait at most 1000 seconds until Salt master sees "salt_migration_minion" as "unaccepted"
    And I follow the left menu "Salt > Keys"
    Then I wait at most 120 seconds until I see "salt-migration" text
    When I accept "salt_migration_minion" key
    And I list all Salt keys shown on the Salt master
    And I am on the Systems page
    And I wait until onboarding is completed for "salt_migration_minion"
