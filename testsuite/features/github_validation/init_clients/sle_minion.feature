# Copyright (c) 2023 SUSE LLC
# Licensed under the terms of the MIT license.

@sle_minion
Feature: Bootstrap a Salt minion via the GUI

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Check the new bootstrapped minion in System List page
    When I follow the left menu "Salt > Keys"
    And I accept "sle_minion" key in the Salt master
    And I wait until I do not see "Loading..." text
    Then I should see a "accepted" text
    When I follow the left menu "Systems > System List > All"
    And I wait until I see the name of "sle_minion", refreshing the page
    Then the Salt master can reach "sle_minion"

  Scenario: Subscribe the SLE minion to a base channel
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Fake Base Channel"
    And I wait until I do not see "Loading..." text
    And I check "Fake-RPM-SUSE-Channel"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled" is completed
