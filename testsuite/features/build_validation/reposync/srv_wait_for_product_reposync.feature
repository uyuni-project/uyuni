# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Wait for reposync activity to finish after adding products

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Delete scheduled reposyncs
    When I follow the left menu "Admin > Task Schedules"
    And I follow "mgr-sync-refresh-default"
    And I choose "disabled"
    And I click on "Update Schedule"
    And I click on "Delete Schedule"

  Scenario: Wait for running reposyncs to finish after adding products
    When I wait until all spacewalk-repo-sync finished

  Scenario: Verify the reposync went fine after adding products
    Then the reposync logs should not report errors
