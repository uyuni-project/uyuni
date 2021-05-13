# Copyright (c) 2019-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_ubuntu
@ubuntu_minion
Feature: Install and upgrade package on the Ubuntu minion via Salt through the UI

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Pre-requisite: install virgo-dummy-1.0 package on Ubuntu minion
    When I enable repository "test_repo_deb_pool" on this "ubuntu_minion"
    And I run "apt update" on "ubuntu_minion" with logging
    And I remove package "andromeda-dummy" from this "ubuntu_minion"
    And I install old package "virgo-dummy=1.0" on this "ubuntu_minion"
    And I am on the Systems overview page of this "ubuntu_minion"
    And I follow "Software" in the content area
    And I click on "Update Package List"
    And I follow "Events" in the content area
    And I wait until I do not see "Package List Refresh scheduled by admin" text, refreshing the page
    And I wait until package "virgo-dummy" is installed on "ubuntu_minion" via spacecmd
    And I wait until package "andromeda-dummy" is removed from "ubuntu_minion" via spacecmd

  Scenario: Install a package on the Ubuntu minion
    And I follow "Software" in the content area
    And I follow "Install"
    And I check "andromeda-dummy" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    And I should see a "1 package install has been scheduled for" text
    When I wait until event "Package Install/Upgrade scheduled by admin" is completed
    Then Deb package "andromeda-dummy" with version "2.0" should be installed on "ubuntu_minion"

  Scenario: Update a package on the Ubuntu minion
    And I follow "Software" in the content area
    And I follow "Upgrade" in the content area
    And I check "virgo-dummy-2.0-X" in the list
    And I click on "Upgrade Packages"
    And I click on "Confirm"
    And I should see a "1 package upgrade has been scheduled for" text
    When I wait until event "Package Install/Upgrade scheduled by admin" is completed
    Then Deb package "virgo-dummy" with version "2.0" should be installed on "ubuntu_minion"

  Scenario: Cleanup: remove virgo-dummy and andromeda-dummy packages from Ubuntu minion
    And I remove package "andromeda-dummy" from this "ubuntu_minion"
    And I remove package "virgo-dummy" from this "ubuntu_minion"
    And I disable repository "test_repo_deb_pool" on this "ubuntu_minion"
    And I run "apt update" on "ubuntu_minion" with logging
