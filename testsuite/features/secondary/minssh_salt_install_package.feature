# Copyright (c) 2019-2020 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Install a package on the SSH minion via Salt through the UI

@ssh_minion
  Scenario: Schedule errata refresh to reflect channel assignment on SSH minion
    Given I am authorized with the feature's user
    When I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

@ssh_minion
  Scenario: Install a package on the SSH minion
    Given I am on the Systems overview page of this "ssh_minion"
    And I follow "Software" in the content area
    And I follow "Install"
    And I check row with "hoag-dummy-1.1-1.1" and arch of "ssh_minion"
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled" text
    When I wait until event "Package Install/Upgrade scheduled" is completed
    Then "hoag-dummy-1.1-1.1" should be installed on "ssh_minion"

