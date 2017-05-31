# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: CENTOS7 ssh feature.
   0) delete minion
   1) Bootstrap a new salt host via salt-ssh

  Scenario: Deletes centos minion
    Given no Salt packages are installed on remote "centos"
    When I am on the Systems overview page of this "ceos-minion"
    And I stop salt-minion on centos
    And I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"
    Then I should see a "has been deleted" text
    And I wait until salt-key "mincentos" is deleted

  Scenario: Bootstrap a system (centos salt-ssh managed)
    Given I am authorized
    And I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    And I check "manageWithSSH"
    And I enter the hostname of "ceos-minion" as hostname
    And I enter "linux" as "password"
    And I click on "Bootstrap"
    Then I wait until i see "Successfully bootstrapped host! " text
    And I wait for "60" seconds
  
   Scenario: Subscribe centos ssh-minion to a base-channel for testing
    Given I am on the Systems overview page of this "ceos-minion"
    When I follow "Software" in the content area
    Then I follow "Software Channels" in the content area
    And I select "Test Base Channel" from "new_base_channel_id"
    And I click on "Confirm"
    And I click on "Modify Base Software Channel"
    And I should see a "System's Base Channel has been updated." text

