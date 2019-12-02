# Copyright (c) 2015-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@skip_if_github_validation
@scope_res
@scope_salt
@rhlike_minion
Feature: Install a patch on the Red Hat-like minion via Salt through the UI

  Scenario: Pre-requisite: install virgo-dummy-1.0 and remove andromeda-dummy packages
    When I enable repository "test_repo_rpm_pool" on this "rhlike_minion"
    And I remove package "andromeda-dummy" from this "rhlike_minion"
    And I install package "virgo-dummy-1.0" on this "rhlike_minion"

  Scenario: Pre-requisite: refresh package list and check newly installed packages on Red Hat-like minion
    When I refresh packages list via spacecmd on "rhlike_minion"
    And I wait until refresh package list on "rhlike_minion" is finished
    Then spacecmd should show packages "virgo-dummy-1.0" installed on "rhlike_minion"

  Scenario: Log in as org admin user
    Given I am authorized for the "Admin" section

  Scenario: Pre-requisite: re-subscribe the Red Hat-like minion to a base channel
    Given I am on the Systems overview page of this "rhlike_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Fake-Base-Channel-RH-like"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled" is completed

  Scenario: Schedule errata refresh to reflect channel assignment on Red Hat-like minion
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

  Scenario: Install a patch on the Red Hat-like minion
    Given I am on the Systems overview page of this "rhlike_minion"
    When I follow "Software" in the content area
    And I follow "Patches" in the content area
    When I check "virgo-dummy-3456" in the list
    And I click on "Apply Patches"
    And I click on "Confirm"
    Then I should see a "1 patch update has been scheduled for" text
    And I wait for "virgo-dummy-2.0-1.1" to be installed on "rhlike_minion"

  Scenario: Install a package on the Red Hat-like minion
    When I follow "Software" in the content area
    And I follow "Install"
    And I check "andromeda-dummy" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled for" text
    And I wait until event "Package Install/Upgrade scheduled" is completed

  Scenario: Cleanup: remove virgo-dummy and andromeda-dummy packages from Red Hat-like minion
    When I follow "Software" in the content area
    And I follow "List / Remove"
    And I enter "andromeda" as the filtered package name
    And I click on the filter button
    And I check "andromeda-dummy" in the list
    And I enter "virgo-dummy" as the filtered package name
    And I click on the filter button
    And I check "virgo-dummy" in the list
    And I click on "Remove Packages"
    And I click on "Confirm"
    Then I should see a "2 package removals have been scheduled" text
    And I wait until event "Package Removal scheduled" is completed
    And I disable repository "test_repo_rpm_pool" on this "rhlike_minion"
