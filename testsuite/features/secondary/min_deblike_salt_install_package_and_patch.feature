# Copyright (c) 2019-2025 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_deblike
@scope_salt
@deblike_minion
Feature: Install and upgrade package on the Debian-like minion via Salt through the UI

  Scenario: Pre-requisite: enable test_repo_deb_pool repository on the Debian-like minion
    When I enable repository "test_repo_deb_pool" on this "deblike_minion"

  Scenario: Pre-requisite: install virgo-dummy-1.0 package on Debian-like minion
    When I run "apt update" on "deblike_minion" with logging
    And I install old package "virgo-dummy=1.0" on this "deblike_minion"

  Scenario: Pre-requisite: remove andromeda-dummy package from Debian-like minion
    When I remove package "andromeda-dummy" from this "deblike_minion"

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: Pre-requisite: refresh package list and check newly installed packages on Debian-like minion
    Given I am on the Systems overview page of this "deblike_minion"
    When I follow "Software" in the content area
    And I click on "Update Package List"
    And I follow "Events" in the content area
    And I wait until I do not see "Package List Refresh scheduled" text, refreshing the page
    And I wait until package "virgo-dummy" is installed on "deblike_minion" via spacecmd
    And I wait until package "andromeda-dummy" is removed from "deblike_minion" via spacecmd

  Scenario: Install a patch on the Debian-like minion
    When I follow "Software" in the content area
    And I follow "Patches" in the content area
    When I check "3456-1" in the list
    And I click on "Apply Patches"
    And I click on "Confirm"
    Then I should see a "1 patch update has been scheduled for" text
    And I wait for "virgo-dummy-2.0" to be installed on "deblike_minion"

  @susemanager
  Scenario: Install a package on the Debian-like minion
    When I follow "Software" in the content area
    And I follow "Install"
    And I check "andromeda-dummy-2.0" in the list
    And I click on "Install Packages"
    And I click on "Confirm"
    And I should see a "1 package install has been scheduled for" text
    When I wait until event "Package Install/Upgrade scheduled" is completed
    Then Deb package "andromeda-dummy" with version "2.0" should be installed on "deblike_minion"

  @uyuni
  Scenario: Install a package on the Debian-like minion
    When I follow "Software" in the content area
    And I follow "Install"
    And I check "andromeda-dummy-2.0" in the list
    And I click on "Install Packages"
    And I click on "Confirm"
    And I should see a "1 package install has been scheduled for" text
    When I wait until event "Package Install/Upgrade scheduled" is completed
    Then Deb package "andromeda-dummy" with version "2.0" should be installed on "deblike_minion"

  Scenario: Cleanup: disable test_repo_deb_pool on Debian-like minion
    When I disable repository "test_repo_deb_pool" on this "deblike_minion"

  Scenario: Cleanup: remove virgo-dummy package from Debian-like minion
    When I remove package "virgo-dummy" from this "deblike_minion"

  Scenario: Cleanup: remove andromeda-dummy package from Debian-like minion
    When I remove package "andromeda-dummy" from this "deblike_minion"
    And I run "apt update" on "deblike_minion" with logging
