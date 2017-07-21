# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Explore the Minions page
  In Order to validate the minion onboarding page
  As an authorized user
  I want to verify all the minion key management features in the UI

  Scenario: Delete sles-minion system profile (Explore the Minions page)
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"
    Then I should see a "has been deleted" text
    And I restart salt-minion on "sle-minion"

  Scenario: Completeness of the Minions page
    Given I am authorized as "testing" with password "testing"
    And I go to the minion onboarding page
    Then I should see a "Keys" text in the content area
    And I should see a "Fingerprint" text

  Scenario: Minion is visible in the Pending section
    Given I am authorized as "testing" with password "testing"
    And "sle-minion" key is "unaccepted"
    And I go to the minion onboarding page
    Then I refresh page until see "sle-minion" hostname as text
    And I see "sle-minion" fingerprint
    And I should see a "pending" text

  Scenario: Reject and delete the pending "sle-minion" key
    Given I am authorized as "testing" with password "testing"
    And "sle-minion" key is "unaccepted"
    And I go to the minion onboarding page
    And I reject "sle-minion" from the Pending section
    And we wait till Salt master sees "sle-minion" as "rejected"
    Then I should see a "rejected" text
    And I delete "sle-minion" from the Rejected section
    Then I should not see "sle-minion" as a Minion anywhere

  Scenario: Accepted minion shows up as a registered system
    Given I am authorized as "testing" with password "testing"
    And "sle-minion" key is "unaccepted"
    And "sle-minion" is not registered in Spacewalk
    And I go to the minion onboarding page
    Then I should see a "pending" text
    When I accept "sle-minion" key
    And we wait till Salt master sees "sle-minion" as "accepted"
    # Registration takes a while
    And I wait until onboarding is completed for "sle-minion"

  Scenario: The minion communicates with the salt-master
    # It takes a while before we can get the grains and registration is done
    Given I am authorized as "testing" with password "testing"
    And the salt-master can reach "sle-minion"
    When I get OS information of "sle-minion" from the Master
    Then it should contain a "SLES" text
