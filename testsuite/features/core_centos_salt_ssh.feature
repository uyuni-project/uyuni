# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  0) delete CentOS minion from previous non-SSH tests
#  1) bootstrap a new CentOS minion via salt-ssh
#  2) subscribe it to a base channel for testing

Feature: Be able to bootstrap a SSH-managed CentOS minion and do some basic operations on it

  Scenario: Delete the CentOS minion
    Given no Salt packages are installed on "ceos-minion"
    When I am on the Systems overview page of this "ceos-minion"
    And I stop salt-minion on "ceos-minion"
    And I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    Then I should see a "has been deleted" text
    And I wait until salt-key "mincentos" is deleted

  Scenario: Bootstrap a SSH-managed CentOS minion
    Given I am authorized
    When I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    When I check "manageWithSSH"
    And I enter the hostname of "ceos-minion" as hostname
    And I enter "linux" as "password"
    And I click on "Bootstrap"
    Then I wait until I see "Successfully bootstrapped host! " text
    And I navigate to "rhn/systems/Overview.do" page
    And I wait until I see the name of "ceos-minion", refreshing the page
    And I wait until onboarding is completed for "ceos-minion"

  Scenario: Subscribe the SSH-managed CentOS minion to a base channel for testing
    Given I am on the Systems overview page of this "ceos-minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I select "Test Base Channel" from "new_base_channel_id"
    And I click on "Confirm"
    And I click on "Modify Base Software Channel"
    Then I should see a "System's Base Channel has been updated." text
