# Copyright (c) 2017-2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Install a package to the CentOS minion

@centosminion
  Scenario: Install a package to the CentOS minion
    Given I am on the Systems overview page of this "ceos-minion"
    # we don't know if the pkgs are installed before, just remove or don't fail
    When I run "yum erase -y andromeda-dummy" on "ceos-minion" without error control
    And I run "yum erase -y virgo-dummy" on "ceos-minion" without error control
    And I wait for "2" seconds
    And metadata generation finished for "test-channel-x86_64"
    And I follow "Software" in the content area
    And I follow "Install"
    And I check "virgo-dummy" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled for" text
    When I wait until event "Package Install/Upgrade scheduled by admin" is completed
    Then "virgo-dummy-2.0-1.1" should be installed on "ceos-minion"

@centosminion
  Scenario: Enable old packages for testing a patch install
   When I run "sed -i 's/enabled=.*/enabled=1/' /etc/yum.repos.d/Devel_Galaxy_BuildRepo.repo" on "ceos-minion"
    And I run "yum install -y andromeda-dummy-1.0-4.1" on "ceos-minion"
    And I wait for "2" seconds

@centosminion
  Scenario: Schedule errata refresh after reverting to old package
    Given I am authorized as "admin" with password "admin"
    When I follow "Admin"
    And I follow "Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

@centosminion
  Scenario: Install a patch in the CentOS minion
    Given I am on the Systems overview page of this "ceos-minion"
    When I follow "Software" in the content area
    And I follow "Patches" in the content area
    And I check "andromeda-dummy-6789" in the list
    And I click on "Apply Patches"
    And I click on "Confirm"
    Then I should see a "1 patch update has been scheduled for" text
    When I wait until event "Package Install/Upgrade scheduled by admin" is completed
    Then "andromeda-dummy-2.0-1.1" should be installed on "ceos-minion"
    And The metadata buildtime from package "andromeda-dummy" match the one in the rpm on "ceos-minion"

@centosminion
  Scenario: Cleanup: remove packages and restore non-update repo
    When I run "yum erase -y andromeda-dummy" on "ceos-minion"
    And I run "yum erase -y virgo-dummy" on "ceos-minion"
    And I run "sed -i 's/enabled=.*/enabled=0/' /etc/yum.repos.d/Devel_Galaxy_BuildRepo.repo" on "ceos-minion"
