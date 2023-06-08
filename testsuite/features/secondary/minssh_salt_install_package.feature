# Copyright (c) 2019-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@skip_if_github_validation
@scope_salt_ssh
@scope_onboarding
@ssh_minion
Feature: Install a package on the SSH minion via Salt through the UI

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Schedule errata refresh to reflect channel assignment on SSH minion
    When I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Install a package on the SSH minion
    When I am on the Systems overview page of this "ssh_minion"
    And I follow "Software" in the content area
    And I follow "Install"
    And I enter "hoag-dummy-1.1-1.1" as the filtered package name
    And I click on the filter button
    And I check row with "hoag-dummy-1.1-1.1" and arch of "ssh_minion"
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled" text
    When I wait until event "Package Install/Upgrade scheduled by admin" is completed
    Then "hoag-dummy-1.1-1.1" should be installed on "ssh_minion"

  Scenario: Cleanup: remove the package from the SSH minion
   When I remove package "hoag-dummy-1.1-1.1" from this "ssh_minion"
   And "hoag-dummy-1.1-1.1" should not be installed on "ssh_minion"
   And I refresh packages list via spacecmd on "ssh_minion"
   And I wait until refresh package list on "ssh_minion" is finished
