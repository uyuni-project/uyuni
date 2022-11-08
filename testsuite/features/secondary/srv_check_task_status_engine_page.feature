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

  Scenario: Resync the product channels to trigger a new task
    When I follow the left menu "Admin > Task Engine Status > Last Execution Times"
    And I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Desktop 15 SP4 x86_64" as "product-description-filter"
    And I wait until I see "SUSE Linux Enterprise Desktop 15 SP4 x86_64" text
    And I select "SUSE Linux Enterprise Desktop 15 SP4 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Desktop 15 SP4 x86_64" selected
    When I click on "Add products"
    And I follow the left menu "Admin > Task Engine Status > Runtime Status"
    Then I should see a "repo-sync" text
    And I should see a "running" text
    