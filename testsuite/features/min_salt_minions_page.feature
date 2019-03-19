# Copyright (c) 2015-2019 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Management of minion keys
  In Order to validate the minion onboarding page
  As an authorized user
  I want to verify all the minion key management features in the UI

  Scenario: Delete SLES minion system profile before exploring the onboarding page
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "sle-minion" should not be registered

  Scenario: Completeness of the onboarding page
    Given I am authorized as "testing" with password "testing"
    And I go to the minion onboarding page
    Then I should see a "Keys" text in the content area

  Scenario: Minion is visible in the Pending section
    Given I am authorized as "testing" with password "testing"
    And I restart salt-minion on "sle-minion"
    And I wait at most 10 seconds until Salt master sees "sle-minion" as "unaccepted"
    And I go to the minion onboarding page
    And I refresh page until I see "sle-minion" hostname as text
    Then I should see a "Fingerprint" text
    And I see "sle-minion" fingerprint
    And I should see a "pending" text

  Scenario: Reject and delete the pending key
    Given I am authorized as "testing" with password "testing"
    And I go to the minion onboarding page
    And I reject "sle-minion" from the Pending section
    And I wait at most 10 seconds until Salt master sees "sle-minion" as "rejected"
    Then I should see a "rejected" text
    # we stop the service so the minion does not resubmit its key spontaneously
    When I stop salt-minion on "sle-minion"
    And I delete "sle-minion" from the Rejected section
    And I refresh page until I do not see "sle-minion" hostname as text

  Scenario: Accepted minion shows up as a registered system
    Given I am authorized as "testing" with password "testing"
    When I start salt-minion on "sle-minion"
    And I wait at most 10 seconds until Salt master sees "sle-minion" as "unaccepted"
    Then "sle-minion" should not be registered
    When I go to the minion onboarding page
    Then I should see a "pending" text
    When I accept "sle-minion" key
    And I wait at most 10 seconds until Salt master sees "sle-minion" as "accepted"
    And I wait until onboarding is completed for "sle-minion"
    Then "sle-minion" should be registered

  Scenario: The minion communicates with the Salt master
    Given I am authorized as "testing" with password "testing"
    And the Salt master can reach "sle-minion"
    When I get OS information of "sle-minion" from the Master
    Then it should contain a "SLES" text

  Scenario: Delete profile of unreacheable minion
    Given I am on the Systems overview page of this "sle-minion"
    When I stop salt-minion on "sle-minion"
    And I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    Then I should see a "minion_unreachable" text
    When I click on "Delete Profile Without Cleanup"
    And I wait until I see "has been deleted" text
    Then "sle-minion" should not be registered

  Scenario: Cleanup: bootstrap again the minion
    Given I am authorized
    When I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle-minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select the hostname of the proxy from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host! " text
    And I wait until onboarding is completed for "sle-minion"

  Scenario: Cleanup: restore channels on the minion
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Software" in the content area
    Then I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test-Channel-x86_64"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed

  Scenario: Cleanup: turn the SLES minion into a container build host after new bootstrap
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Details" in the content area
    And I follow "Properties" in the content area
    And I check "container_build_host"
    And I click on "Update Properties"

  Scenario: Cleanup: apply the highstate to container build host after new bootstrap
    Given I am on the Systems overview page of this "sle-minion"
    When I wait until no Salt job is running on "sle-minion"
    And I enable repositories before installing Docker
    And I apply highstate on "sle-minion"
    And I wait until "docker" service is active on "sle-minion"
    And I disable repositories after installing Docker

  Scenario: Cleanup: check that the minion is now a build host after new bootstrap
    Given I am on the Systems overview page of this "sle-minion"
    Then I should see a "[Container Build Host]" text
