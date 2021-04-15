# Copyright (c) 2015-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_salt
Feature: Install a patch on the client via Salt through the UI

  Scenario: Pre-requisite: install virgo-dummy-1.0 package on SLE minion
    When I enable repository "test_repo_rpm_pool" on this "sle_minion"
    And I run "zypper -n ref" on "sle_minion"
    And I install old package "virgo-dummy-1.0" on this "sle_minion" without error control

  Scenario: Pre-requisite: refresh package list and check old packages installed on SLE minion
    When I refresh packages list via spacecmd on "sle_minion"
    And I wait until refresh package list on "sle_minion" is finished
    Then spacecmd should show packages "virgo-dummy-1.0" installed on "sle_minion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Pre-requisite: ensure the errata cache is computed before patching Salt minion
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "virgo-dummy" as the filtered package name
    And I click on the filter button until page does contain "virgo-dummy" text
    When I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Install a patch on the minion
    Given I am on the Systems overview page of this "sle_minion"
    And I follow "Software" in the content area
    And I follow "Patches" in the content area
    When I check "virgo-dummy-3456" in the list
    And I check "allowVendorChange"
    And I click on "Apply Patches"
    And I click on "Confirm"
    Then I should see a "1 patch update has been scheduled for" text
    And I wait for "virgo-dummy-2.0-1.1" to be installed on "sle_minion"
    And vendor change should be enabled for package actions on "sle_minion"

  Scenario: Cleanup: remove virgo-dummy package from SLE minion
    When I disable repository "test_repo_rpm_pool" on this "sle_minion"
    And I remove package "virgo-dummy" from this "sle_minion" without error control
    And I run "zypper -n ref" on "sle_minion"
