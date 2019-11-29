# Copyright (c) 2015-2019 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Install a patch on the CentOS SSH minion via Salt through the UI

@centos_minion
  Scenario: Pre-requisite: install virgo-dummy-1.0 and remove andromeda-dummy packages
    When I enable repository "test_repo_rpm_pool" on this "ceos-ssh-minion"
    And I remove package "andromeda-dummy" from this "ceos-ssh-minion"
    And I install package "virgo-dummy-1.0" on this "ceos-ssh-minion"

@centos_minion
  Scenario: Pre-requisite: refresh package list and check newly installed packages on Centos SSH minion
    When I refresh packages list via spacecmd on "ceos-ssh-minion"
    And I wait until refresh package list on "ceos-ssh-minion" is finished
    Then spacecmd should show packages "virgo-dummy-1.0" installed on "ceos-ssh-minion"

@centos_minion
  Scenario: Pre-requisite: re-subscribe the SSH-managed CentOS minion to a base channel
    Given I am on the Systems overview page of this "ceos-ssh-minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test-Channel-x86_64"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed

@centos_minion
  Scenario: Schedule errata refresh to reflect channel assignment on Centos SSH minion
    Given I am on the Systems overview page of this "ceos-ssh-minion"
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

@centos_minion
  Scenario: Install a patch on the Centos SSH minion
    Given I am on the Systems overview page of this "ceos-ssh-minion"
    When I follow "Software" in the content area
    And I follow "Patches" in the content area
    When I check "virgo-dummy-3456" in the list
    And I click on "Apply Patches"
    And I click on "Confirm"
    And I wait for "5" seconds
    Then I should see a "1 patch update has been scheduled for" text
    And I wait for "virgo-dummy-2.0-1.1" to be installed on this "ceos-ssh-minion"

@centos_minion
  Scenario: Install a package on the Centos SSH minion
    Given I am on the Systems overview page of this "ceos-ssh-minion"
    When I follow "Software" in the content area
    And I follow "Install"
    And I check "andromeda-dummy" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled for" text
    And I wait until event "Package Install/Upgrade scheduled by admin" is completed

@centos_minion
  Scenario: Cleanup: remove virgo-dummy and andromeda-dummy packages from Centos SSH minion
    Given I am on the Systems overview page of this "ceos-ssh-minion"
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
    And I wait until event "Package Removal scheduled by admin" is completed
    And I disable repository "test_repo_rpm_pool" on this "ceos-ssh-minion"
