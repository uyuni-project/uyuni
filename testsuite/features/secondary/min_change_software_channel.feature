# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_changing_software_channels
@sle_minion
Feature: Assign child channel to a system

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Check the system is still subscribed to old channels before channel change completes
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    Then radio button "Test-Channel-x86_64" is checked
    And I wait until I do not see "Loading..." text
    And I should see "Test-Channel-x86_64 Child Channel" as unchecked

  Scenario: Check old channels are still enabled on the system before channel change completes
    When I refresh the metadata for "sle_minion"
    Then "1" channels should be enabled on "sle_minion"
    And channel "Test-Channel-x86_64" should be enabled on "sle_minion"

  Scenario: Assign a child channel to the system
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    Then radio button "Test-Channel-x86_64" is checked
    And I wait until I do not see "Loading..." text
    And I check "Test-Channel-x86_64 Child Channel"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    And I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page
    Then channel "Test-Channel-x86_64 Child Channel" should be enabled on "sle_minion"

  Scenario: Check channel change has completed for the system
    Given I am on the Systems overview page of this "sle_minion"
    When I wait until event "Subscribe channels scheduled by admin" is completed
    Then I should see a "The client completed this action on" text

  Scenario: Check the system is subscribed to the new channels
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    Then radio button "Test-Channel-x86_64" is checked
    And I wait until I do not see "Loading..." text
    And I should see "Test-Channel-x86_64 Child Channel" as checked

  Scenario: Check the new channels are enabled on the system
    When I refresh the metadata for "sle_minion"
    Then "2" channels should be enabled on "sle_minion"
    And channel "Test-Channel-x86_64" should be enabled on "sle_minion"
    And channel "Test-Channel-x86_64 Child Channel" should be enabled on "sle_minion"

  Scenario: Cleanup: subscribe the system back to previous channels
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    Then radio button "Test-Channel-x86_64" is checked
    And I wait until I do not see "Loading..." text
    And I wait until I see "Test-Channel-x86_64 Child Channel" text
    And I uncheck "Test-Channel-x86_64 Child Channel"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page
    Then channel "Test-Channel-x86_64 Child Channel" should not be enabled on "sle_minion"
