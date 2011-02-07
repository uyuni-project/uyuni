# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

# feature/monitoring.feature

Feature: Cleanup after testing monitoring
  As the root user
  I want to check the scout config push has not expired
  I want to delete a probe suite
  I want to de-activate the monitoring scout

  Scenario: Check result of scout config push
    Given I am on the Monitoring page
     And I follow "Scout Config Push"
    Then I should not see a "Expired" text

  Scenario: Delete probe suite "Test"
    Given I am on the Monitoring page
     And I follow "Probe Suites"
     And I check "Test" in the list
     And I click on "Delete Probe Suites"
     And I click on "Delete Probe Suites"
    Then I should see a "Probe Suite(s) deleted" text

  Scenario: De-activate monitoring scout
    Given I am on the Admin page
     When I follow "SUSE Manager Configuration"
       And I follow "Monitoring" in class "content-nav"
       And I check "Enable Monitoring Scout"
       And I click on "Update Config"
    Then I should see a "Configuration updated, Monitoring services restarted." text
