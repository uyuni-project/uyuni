# Copyright (c) 2015-2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Management of minion keys
  In Order to validate the minion onboarding page
  As an authorized user
  I want to verify all the minion key management features in the UI

  Scenario: Delete SLES minion system profile before exploring the onboarding page
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"
    Then I wait until I see "has been deleted" text

  Scenario: Completeness of the onboarding page
    Given I am authorized as "testing" with password "testing"
    And I go to the minion onboarding page
    Then I should see a "Keys" text in the content area

  Scenario: Minion is visible in the Pending section
    Given I am authorized as "testing" with password "testing"
    And I restart salt-minion on "sle-minion"
    And I wait until Salt master sees "sle-minion" as "unaccepted"
    And I go to the minion onboarding page
    And I refresh page until I see "sle-minion" hostname as text
    Then I should see a "Fingerprint" text
    And I see "sle-minion" fingerprint
    And I should see a "pending" text

  Scenario: Reject and delete the pending key
    Given I am authorized as "testing" with password "testing"
    And I go to the minion onboarding page
    And I reject "sle-minion" from the Pending section
    And I wait until Salt master sees "sle-minion" as "rejected"
    Then I should see a "rejected" text
    # we stop the service so the minion does not resubmit its key spontaneously
    When I stop salt-minion on "sle-minion"
    And I delete "sle-minion" from the Rejected section
    And I refresh page until I do not see "sle-minion" hostname as text

  Scenario: Accepted minion shows up as a registered system
    Given I am authorized as "testing" with password "testing"
    When I start salt-minion on "sle-minion"
    And I wait until Salt master sees "sle-minion" as "unaccepted"
    Then "sle-minion" should not be registered
    When I go to the minion onboarding page
    Then I should see a "pending" text
    When I accept "sle-minion" key
    And I wait until Salt master sees "sle-minion" as "accepted"
    And I wait until onboarding is completed for "sle-minion"
    Then "sle-minion" should be registered

  Scenario: The minion communicates with the Salt master
    Given I am authorized as "testing" with password "testing"
    And the Salt master can reach "sle-minion"
    When I get OS information of "sle-minion" from the Master
    Then it should contain a "SLES" text

  Scenario: Cleanup: restore channels on the minion
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Software" in the content area
    Then I follow "Software Channels" in the content area
    And I check radio button "Test-Channel-x86_64"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed
