# Copyright (c) 2015-2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Install a patch on the contos minion via Salt through the UI

@centosminion
  Scenario: Pre-requisite: install virgo-dummy-1.0 packages
    Given I am on the Systems overview page of this "ceos-minion"
    And I run "sed -i 's/enabled=.*/enabled=1/' /etc/yum.repos.d/Devel_Galaxy_BuildRepo.repo" on "ceos-minion"
    And I run "yum erase -y andromeda-dummy" on "ceos-minion" without error control
    And I run "yum install -y virgo-dummy-1.0" on "ceos-minion" without error control
    When I follow "Software" in the content area
    And I follow "List / Remove" in the content area
    And I enter "virgo-dummy" in the css "input[placeholder='Filter by Package Name: ']"
    And I click on the css "button.spacewalk-button-filter" until page does contain "virgo-dummy" text
    And I follow "Admin"
    And I follow "Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

@centosminion
  Scenario: Install a patch on the minion
    Given I am on the Systems overview page of this "ceos-minion"
    And I follow "Software" in the content area
    And I follow "Patches" in the content area
    When I check "virgo-dummy-3456" in the list
    And I click on "Apply Patches"
    And I click on "Confirm"
    And I wait for "5" seconds
    Then I should see a "1 patch update has been scheduled for" text
    And I wait for "virgo-dummy-2.0-1.1" to be installed on this "ceos-minion"

@centosminion
  Scenario: Install a package on the minion
    Given I am on the Systems overview page of this "ceos-minion"
    And I follow "Software" in the content area
    And I follow "Install"
    And I check "andromeda-dummy" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled for" text
    When I wait until event "Package Install/Upgrade scheduled by admin" is completed
    Then "andromeda-dummy-2.0-1.1" should be installed on "ceos-minion"

@centosminion
  Scenario: Cleanup: remove virgo-dummy and andromeda-dummy packages from Centos minion
    Given I am authorized as "admin" with password "admin"
    And I run "yum erase -y andromeda-dummy" on "ceos-minion" without error control
    And I run "yum install -y andromeda-dummy-1.0" on "ceos-minion"
    And I run "yum erase -y virgo-dummy" on "ceos-minion"
    And I run "sed -i 's/enabled=.*/enabled=0/' /etc/yum.repos.d/Devel_Galaxy_BuildRepo.repo" on "ceos-minion"
