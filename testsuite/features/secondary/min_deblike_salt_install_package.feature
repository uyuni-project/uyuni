# Copyright (c) 2019-2024 SUSE LLC
# Licensed under the terms of the MIT license.

@skip_if_github_validation
@scope_deblike
@deblike_minion
Feature: Install and upgrade package on the Debian-like minion via Salt through the UI

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: Pre-requisite: install virgo-dummy-1.0 package on Debian-like minion
    When I enable repository "test_repo_deb_pool" on this "deblike_minion"
    And I run "apt update" on "deblike_minion" with logging
    And I remove package "andromeda-dummy" from this "deblike_minion"
    And I install old package "virgo-dummy=1.0" on this "deblike_minion"
    And I am on the Systems overview page of this "deblike_minion"
    And I follow "Software" in the content area
    And I click on "Update Package List"
    And I follow "Events" in the content area
    And I wait until I do not see "Package List Refresh scheduled" text, refreshing the page
    And I wait until package "virgo-dummy" is installed on "deblike_minion" via spacecmd
    And I wait until package "andromeda-dummy" is removed from "deblike_minion" via spacecmd

  Scenario: Install a package on the Debian-like minion
    And I follow "Software" in the content area
    And I follow "Install"
    And I check "andromeda-dummy" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    And I should see a "1 package install has been scheduled for" text
    When I wait until event "Package Install/Upgrade scheduled" is completed
    Then Deb package "andromeda-dummy" with version "2.0" should be installed on "deblike_minion"

  Scenario: Update a package on the Debian-like minion
    And I follow "Software" in the content area
    And I follow "Upgrade" in the content area
    And I wait until I see "virgo-dummy" text, refreshing the page
    And I check "virgo-dummy-2.0-X" in the list
    And I click on "Upgrade Packages"
    And I click on "Confirm"
    And I should see a "1 package upgrade has been scheduled for" text
    When I wait until event "Package Install/Upgrade scheduled" is completed
    Then Deb package "virgo-dummy" with version "2.0" should be installed on "deblike_minion"

  Scenario: Cleanup: remove virgo-dummy and andromeda-dummy packages from Debian-like minion
    And I remove package "andromeda-dummy" from this "deblike_minion"
    And I remove package "virgo-dummy" from this "deblike_minion"
    And I disable repository "test_repo_deb_pool" on this "deblike_minion"
    And I run "apt update" on "deblike_minion" with logging
