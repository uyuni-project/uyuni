# Copyright (c) 2016-2019 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  0) delete Ubuntu minion from previous non-SSH tests
#  1) bootstrap a new Ubuntu minion via salt-ssh
#  2) subscribe it to a base channel for testing

Feature: Bootstrap a SSH-managed Ubuntu minion and do some basic operations on it

@ubuntu_minion
  Scenario: Bootstrap a SSH-managed Ubuntu minion
    Given I am authorized
    When I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    When I check "manageWithSSH"
    And I enter the hostname of "ubuntu-minion" as "hostname"
    And I enter "linux" as "password"
    And I click on "Bootstrap"
    Then I wait until I see "Successfully bootstrapped host! " text
    And I navigate to "rhn/systems/Overview.do" page
    And I wait until I see the name of "ubuntu-minion", refreshing the page
    And I wait until onboarding is completed for "ubuntu-minion"

@ubuntu_minion
  Scenario: Subscribe the SSH-managed Ubuntu minion to a base channel
    Given I am on the Systems overview page of this "ubuntu-minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test Base Channel"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed

@ubuntu_minion
  Scenario: Delete the SSH-managed Ubuntu minion
    When I am on the Systems overview page of this "ubuntu-minion"
    And I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    Then I should see a "has been deleted" text
    And "ubuntu-minion" should not be registered