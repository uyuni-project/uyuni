# Copyright (c) 2014 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Check the SCC Migration UI
  In order to test the SCC Migration feature UI behavior
  As an admin user
  I want to go to the SUSE Manager Configuration page

  Scenario: SUSE Customer Center is not available
    Given I am authorized as "admin" with password "admin"
    When I save migration state
    Given the server is not yet migrated to SCC
    Given the SCC is not yet the default customer center
    When I follow "Admin"
    When I follow "SUSE Manager Configuration"
    When I follow "SUSE Customer Center" in the left menu
    Then I should see a "The SUSE Customer Center is not yet available." text
    Then I restore migration state

  Scenario: SUSE Manager is still connected to NCC
    Given I am authorized as "admin" with password "admin"
    When I save migration state
    Given the server is not yet migrated to SCC
    Given the SCC is the default customer center
    When I follow "Admin"
    When I follow "SUSE Manager Configuration"
    When I follow "SUSE Customer Center" in the left menu
    Then I should see a "SUSE Manager is still connected to the Novell Customer Center." text
    Then I should see a "Migration is an straightforward process but may take some minutes." text
    Then I should see a "Start Migration to SUSE Customer Center" text
    Then I restore migration state

  Scenario: SUSE Manager is already migrated to SCC
    Given I am authorized as "admin" with password "admin"
    When I save migration state
    Given the server is migrated to SCC
    Given the SCC is the default customer center
    When I follow "Admin"
    When I follow "SUSE Manager Configuration"
    When I follow "SUSE Customer Center" in the left menu
    Then I should see a "Congratulations! SUSE Manager is already migrated to the SUSE Customer Center." text
    Then I restore migration state

