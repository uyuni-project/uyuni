# Copyright (c) 2020 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Assign child channel to system

@sle_minion
  Scenario: Check SLES minion is still subscribed to old channels before channel change completes
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    Then radio button "Test-Channel-x86_64" is checked
    And I wait until I do not see "Loading..." text
    And I should see "Test-Channel-x86_64 Child Channel" as unchecked


@sle_minion
  Scenario: Check old channels are still enabled on SLES minion before channel change completes
    When I refresh the metadata for "sle_minion"
    Then "1" channels should be enabled on "sle_minion"
    And channel "Test-Channel-x86_64" should be enabled on "sle_minion"

@sle_minion
  Scenario: Assign a child channel to system
    Given I am on the Systems overview page of this "sle_minion"
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

@sle_minion
  Scenario: Check channel change has completed for the SLES minion
    Given I am on the Systems overview page of this "sle_minion"
    When I wait until event "Subscribe channels scheduled by admin" is completed
    Then I should see a "The client completed this action on" text

@sle_minion
  Scenario: Check the SLES minion is subscribed to the new channels
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    Then radio button "Test-Channel-x86_64" is checked
    And I wait until I do not see "Loading..." text
    And I should see "Test-Channel-x86_64 Child Channel" as checked

@sle_minion
  Scenario: Check the new channels are enabled on the SLES minion
    When I refresh the metadata for "sle_minion"
    Then "2" channels should be enabled on "sle_minion"
    And channel "Test-Channel-x86_64" should be enabled on "sle_minion"
    And channel "Test-Channel-x86_64 Child Channel" should be enabled on "sle_minion"

@sle_minion
  Scenario: Cleanup: subscribe the SLES minion back to previous channels
    Given I am on the Systems overview page of this "sle_minion"
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
