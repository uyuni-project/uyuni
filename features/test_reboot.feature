# Copyright (c) 2015-16 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test a reboot on a traditional-client
	 And test the webui for this feature.

  Scenario: Reboot a tradional client (sle)
    Given I am on the Systems overview page of this client
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    And I click on "Reboot system"
    And I run rhn_check on this client
    Then I wait and check that "sle-client" has rebooted

  Scenario: Check that Reboot required is not anymore on displayed on client section 
    Given I am authorized as "admin" with password "admin"
    And I follow "Systems"
    When I follow "Systems" in the left menu
    And I follow "Requiring Reboot" in the left menu
    Then I should not see the "sle-client" as text
