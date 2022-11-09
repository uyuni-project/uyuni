# Copyright (c) 2022 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Task Engine Status

  Scenario: Login as admin
    Given I am authorized for the "Admin" section
    
  Scenario: Check if the Last Execution Times page exsists
    When I follow the left menu "Admin > Task Engine Status"
    Then I should see a "Task Engine Status" text
    And I should see a "The following is a status report for the various tasks run by the Uyuni task engine:" text
    # And I should see a "Scheduling Service is: ON" text
    And I should see a "Runtime Status" text
    And I should see a "Last Execution Times" link in the left menu
    And I should see a "Runtime Status" link in the left menu

  Scenario: Resync a product to trigger a new tast and check if it is visible on the Task Status Engine Page
    When I follow the left menu "Admin > Task Engine Status > Last Execution Times"
    And I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server 15 SP4 x86_64" as "product-description-filter"
    And I wait until I see "SUSE Linux Enterprise Server 15 SP4 x86_64" text
    Then I should see the "SUSE Linux Enterprise Server 15 SP4 x86_64" selected
    When I click on "Schedule channels product resync"
    And I follow the left menu "Admin > Task Engine Status > Runtime Status"
    Then I wait until I see "repo-sync" text
    And I should see the current time as starting time
    And I should see a "running" text in the content area
    And I wait until I see "finished" text
