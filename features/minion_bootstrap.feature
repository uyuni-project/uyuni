# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: register a salt-minion via bootstrap

  Scenario: bootstrap a sles minion
     Given I am authorized
     When I follow "Salt"
     Then I should see a "Bootstrapping" text
     And I follow "Bootstrapping"
     Then I should see a "Bootstrap Minions" text
     # sle-minion = sles, rh_minion = redhat
     And  I enter the hostname of "sle-minion" as hostname
     And I enter "22" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I click on "Bootstrap it"
     And I wait for "150" seconds
     Then I should see a "Successfully bootstrapped host! Your system should appear in System Overview shortly." text
  # testing command line
  Scenario: check new bootstrapped minion in System Overview page
     Given I am authorized
     When I follow "Salt"
     Then I should see a "accepted" text
     # sle-minion = sles, rh_minion = redhat
     And the salt-master can reach "sle-minion"
  # testing GUI
  Scenario: Run a remote command
    And I am authorized as "testing" with password "testing"
    Given I follow "Salt"
    And I follow "Remote Commands"
    And I should see a "Remote Commands" text
    Then I enter command "ls -lha /etc"
    And I click on preview
    Then I should see my hostname
    And I click on run
    Then I wait for "3" seconds
    And I expand the results
    Then I should see "SuSE-release" in the command output
  # delete for retest with activation key
  Scenario: Delete minion system profile
    Given I am on the Systems overview page of this minion
    When I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"
    Then I should see a "has been deleted" text

  Scenario: create minion activation key with Channel and package list
    Given I am on the Systems page
    And I follow "Activation Keys" in the left menu
    And I follow "Create Key"
    When I enter "Minion testing" as "description"
    And I enter "MINION-TEST" as "key"
    And I enter "20" as "usageLimit"
    And I select "SLES12-SP1-Pool for x86_64" from "selectedChannel"
    And I click on "Create Activation Key"
    And I follow "Packages"
    And I enter "man emacs" as "packages"
    And I click on "Update Key"
    Then I should see a "Activation key Minion testing has been modified" text

  Scenario: bootstrap a sles minion with an activation-key
     Given I am authorized
     When I follow "Salt"
     Then I should see a "Bootstrapping" text
     And I follow "Bootstrapping"
     Then I should see a "Bootstrap Minions" text
     # sle-minion = sles, rh_minion = redhat
     And  I enter the hostname of "sle-minion" as hostname
     And I enter "22" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I select "1-MINION-TEST" from "activationKeys"
     And I click on "Bootstrap it"
     And I wait for "150" seconds
     Then I should see a "Successfully bootstrapped host! Your system should appear in System Overview shortly." text

  # testing command line
  Scenario: verify minion bootstrapped with activation key, packages
     Given I am authorized
     When I follow "Salt"
     Then I should see a "accepted" text
     # sle-minion = sles, rh_minion = redhat
     And the salt-master can reach "sle-minion"
     # the "man" package is part of the channel, and the minion doesn't have this installed
     And "man" is installed on "minion"
     And "emacs" is installed on "minion"
   
  Scenario: verify minion bootstrapped with activation key: activation key test
     Given I am on the Systems overview page of this minion
     Then I should see a "Activation Key: 	1-MINION-TEST" text

  Scenario: verify minion bootstrapped with activation key: base channel test
     Given I am on the Systems page
     Then I should see a "SLES12-SP1-Pool for x86_64" text

  Scenario: Delete minion system profile
    Given I am on the Systems overview page of this minion
    When I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"
    Then I should see a "has been deleted" text
