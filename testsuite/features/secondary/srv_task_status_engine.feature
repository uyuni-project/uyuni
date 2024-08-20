# Copyright (c) 2022-2024 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Task Engine Status

  Scenario: Login as admin
    Given I am authorized for the "Admin" section

  @susemanager
  Scenario: Check if the Task Engine Status page exists
    When I follow the left menu "Admin > Task Engine Status > Last Execution Times"
    Then I should see a "Task Engine Status" text
    And I should see a "The following is a status report for the various tasks run by the SUSE Manager task engine:" text
    And I should see a "Runtime Status" text
    And I should see a "Last Execution Times" link in the left menu
    And I should see a "Runtime Status" link in the left menu

  @uyuni
  Scenario: Check if the Task Engine Status page exists
    When I follow the left menu "Admin > Task Engine Status > Last Execution Times"
    Then I should see a "Task Engine Status" text
    And I should see a "The following is a status report for the various tasks run by the Uyuni task engine:" text
    And I should see a "Runtime Status" text
    And I should see a "Last Execution Times" link in the left menu
    And I should see a "Runtime Status" link in the left menu

  Scenario: Check if the Runtime Status Page exists
    When I follow the left menu "Admin > Task Engine Status > Runtime Status"
    Then I should see a "Task Engine Status" text
    And I should see a "Last Execution Times" text
    And I should see a "The server is running or has finished executing the following tasks during the latest 5 minutes." text
    And I should see a "Last Execution Times" link in the left menu
    And I should see a "Runtime Status" link in the left menu

  Scenario: Run a remote command on the server to check if it shows up on Last Execution Times page
    When I follow the left menu "Admin > Task Engine Status > Last Execution Times"
    And I run "cobbler sync" on "server"
    And I refresh the page
    Then I should see a "Cobbler Sync:" text
    And I should see the correct timestamp for task "Cobbler Sync:"
    And I should see a "FINISHED" text

@scc_credentials
@susemanager
  Scenario: Resync a product to trigger a new task and check if it is visible on the Runtime Status page
    When I follow the left menu "Admin > Task Engine Status > Runtime Status"
    And I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Linux Enterprise Server 15 SP4 x86_64" as "product-description-filter"
    And I wait until I see "SUSE Linux Enterprise Server 15 SP4 x86_64" text
    Then I should see the "SUSE Linux Enterprise Server 15 SP4 x86_64" selected
    When I click on "Schedule channels product resync"
    And I follow the left menu "Admin > Task Engine Status > Runtime Status"
    And I wait until I see "repo-sync" text
    Then I should see the correct timestamp for task "repo-sync"
    And I should see a "running" text in the content area
    And I wait until I see "finished" text
    And I should see the correct timestamp for task "repo-sync"
