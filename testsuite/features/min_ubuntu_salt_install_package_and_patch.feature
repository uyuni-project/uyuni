# Copyright (c) 2019 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Install and upgrade package on the Ubuntu minion via Salt through the UI

@ubuntu_minion
  Scenario: Pre-requisite: install virgo-dummy-1.0 packages
    When I enable repository "Devel_Uyuni_BuildRepo" on this "ubuntu-minion"
    And I run "apt update" on "ubuntu-minion"
    And I remove package "andromeda-dummy" from this "ubuntu-minion"
    And I install package "virgo-dummy=1.0" on this "ubuntu-minion"
    And I am on the Systems overview page of this "ubuntu-minion"
    And I follow "Software" in the content area
    And I click on "Update Package List"
    And I follow "Events" in the content area
    And I wait until I do not see "Package List Refresh scheduled by admin" text, refreshing the page

@ubuntu_minion
  Scenario: Install a package on the minion
    Given I am on the Systems overview page of this "ubuntu-minion"
    And I follow "Software" in the content area
    And I follow "Install"
    And I check "andromeda-dummy" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    And I should see a "1 package install has been scheduled for" text
    When I wait until event "Package Install/Upgrade scheduled by admin" is completed
    Then Deb package "andromeda-dummy" with version "2.0" should be installed on "ubuntu-minion"

@ubuntu_minion
  Scenario: Update a package on the minion
    Given I am on the Systems overview page of this "ubuntu-minion"
    And I follow "Software" in the content area
    And I follow "Upgrade" in the content area
    And I check "virgo-dummy-2.0-X" in the list
    And I click on "Upgrade Packages"
    And I click on "Confirm"
    And I should see a "1 package upgrade has been scheduled for" text
    When I wait until event "Package Install/Upgrade scheduled by admin" is completed
    Then Deb package "virgo-dummy" with version "2.0" should be installed on "ubuntu-minion"

@ubuntu_minion
  Scenario: Cleanup: remove virgo-dummy and andromeda-dummy packages from Ubuntu minion
    Given I am authorized as "admin" with password "admin"
    And I remove package "andromeda-dummy" from this "ubuntu-minion"
    And I install package "andromeda-dummy=1.0" on this "ubuntu-minion"
    And I remove package "virgo-dummy" from this "ubuntu-minion"
    And I disable repository "Devel_Uyuni_BuildRepo" on this "ubuntu-minion"
    And I run "apt update" on "ubuntu-minion"

