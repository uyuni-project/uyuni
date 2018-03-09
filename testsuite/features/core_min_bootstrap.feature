# Copyright (c) 2016-2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Be able to bootstrap a Salt minion via the GUI

  Scenario: Create the bootstrap repository for a Salt client
     Given I am authorized
     And I create the "x86_64" bootstrap-repo for "sle-minion" on the server

  Scenario: Bootstrap a SLES minion that will be deleted after
     Given I am authorized
     When I go to the bootstrapping page
     Then I should see a "Bootstrap Minions" text
     When I enter the hostname of "sle-minion" as hostname
     And I enter "22" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I select the hostname of the proxy from "proxies"
     And I click on "Bootstrap"
     Then I wait until I see "Successfully bootstrapped host! " text

  Scenario: Check the new bootstrapped minion in System Overview page
     Given I am authorized
     When I go to the minion onboarding page
     Then I should see a "accepted" text
     And the Salt master can reach "sle-minion"
     When I navigate to "rhn/systems/Overview.do" page
     And I wait until I see the name of "sle-minion", refreshing the page
     And I wait until onboarding is completed for "sle-minion"

  Scenario: Run a remote command on normal SLES minion
    Given I am authorized as "testing" with password "testing"
    When I follow "Salt"
    And I follow "Remote Commands"
    Then I should see a "Remote Commands" text
    When I enter command "ls -lha /etc"
    And I click on preview
    Then I should see "sle-minion" hostname
    When I click on run
    And I wait for "3" seconds
    And I expand the results for "sle-minion"
    Then I should see "SuSE-release" in the command output for "sle-minion"
 
  Scenario: Check spacecmd system ID of bootstrapped minion
    Given I am on the Systems overview page of this "sle-minion"
    Then I run spacecmd listevents for "sle-minion"
 
  Scenario: Delete SLES minion system profile
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"
    Then I wait until I see "has been deleted" text
    And I cleanup minion "sle-minion"

  Scenario: Create minion activation key with channel and package list
    Given I am on the Systems page
    When I follow "Activation Keys" in the left menu
    And I follow "Create Key"
    And I enter "Minion testing" as "description"
    And I enter "MINION-TEST" as "key"
    And I enter "20" as "usageLimit"
    And I select "Test-Channel-x86_64" from "selectedChannel"
    And I click on "Create Activation Key"
    And I follow "Packages"
    And I enter "orion-dummy perseus-dummy" as "packages"
    And I click on "Update Activation Key"
    Then I should see a "Activation key Minion testing has been modified" text

  Scenario: Bootstrap a SLES minion with an activation key
     Given I am authorized
     When I go to the bootstrapping page
     Then I should see a "Bootstrap Minions" text
     When I enter the hostname of "sle-minion" as hostname
     And I enter "22" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I select "1-MINION-TEST" from "activationKeys"
     And I select the hostname of the proxy from "proxies"
     And I click on "Bootstrap"
     Then I wait until I see "Successfully bootstrapped host! " text
     And I navigate to "rhn/systems/Overview.do" page
     And I wait until I see the name of "sle-minion", refreshing the page
     And I wait until onboarding is completed for "sle-minion"

  Scenario: Verify that minion bootstrapped with Salt key and packages
     Given I am authorized
     When I go to the minion onboarding page
     Then I should see a "accepted" text
     And the Salt master can reach "sle-minion"
     And I wait for "orion-dummy" to be installed on this "sle-minion"
     And I wait for "perseus-dummy" to be installed on this "sle-minion"
     And I remove package "orion-dummy" from this "sle-minion"
     And I remove package "perseus-dummy" from this "sle-minion"

  Scenario: Check system ID of second bootstrapped minion
    Given I am on the Systems overview page of this "sle-minion"
    Then I run spacecmd listevents for "sle-minion"

  Scenario: Verify that minion bootstrapped with activation key
     Given I am on the Systems overview page of this "sle-minion"
     Then I should see a "Activation Key: 	1-MINION-TEST" text

  Scenario: Verify that minion bootstrapped with base channel
     Given I am on the Systems page
     Then I should see a "Test-Channel-x86_64" text

  Scenario: Bootstrap should fail when minion already exists
     Given I am authorized
     And I go to the bootstrapping page
     Then I should see a "Bootstrap Minions" text
     And  I enter the hostname of "sle-minion" as hostname
     And I enter "22" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I click on "Bootstrap"
     And I wait until I see "A salt key for this host" text
     And I should not see a "GenericSaltError({" text
     And I should see a "seems to already exist, please check!" text

  Scenario: Delete SLES minion system profile for the second time
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"
    Then I wait until I see "has been deleted" text
      
  Scenario: Bootstrap a SLES minion with wrong hostname
     Given I am authorized
     And I go to the bootstrapping page
     Then I should see a "Bootstrap Minions" text
     And  I enter "not-existing-name" as "hostname"
     And I enter "22" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I click on "Bootstrap"
     Then I wait until I see " Could not resolve hostname not-existing-name: Name or service not known" text
     And I should not see a "GenericSaltError({" text

  Scenario: Bootstrap a SLES minion with wrong SSH credentials
     Given I am authorized
     And I go to the bootstrapping page
     Then I should see a "Bootstrap Minions" text
     And I enter the hostname of "sle-minion" as hostname
     And I enter "22" as "port"
     And I enter "FRANZ" as "user"
     And I enter "KAFKA" as "password"
     And I click on "Bootstrap"
     Then I wait until I see "Permission denied (publickey,keyboard-interactive)." text
     And I should not see a "GenericSaltError({" text
      
  Scenario: Bootstrap a SLES minion with wrong SSH port number
     Given I am authorized
     And I go to the bootstrapping page
     Then I should see a "Bootstrap Minions" text
     And I enter the hostname of "sle-minion" as hostname
     And I enter "11" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I click on "Bootstrap"
     And I wait until I see "ssh: connect to host" text
     And I should not see a "GenericSaltError({" text
     Then I should see a "port 11: Connection refused" text

  Scenario: Bootstrap a SLES minion permanently
     Given I am authorized
     And I go to the bootstrapping page
     Then I should see a "Bootstrap Minions" text
     And  I enter the hostname of "sle-minion" as hostname
     And I enter "22" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I select the hostname of the proxy from "proxies"
     And I click on "Bootstrap"
     Then I wait until I see "Successfully bootstrapped host! " text

  Scenario: Wait for minion to finish bootstrap
     Given I am authorized
     And I go to the minion onboarding page
     Then I should see a "accepted" text
     And the Salt master can reach "sle-minion"
     And I navigate to "rhn/systems/Overview.do" page
     And I wait until I see the name of "sle-minion", refreshing the page
     And I wait until onboarding is completed for "sle-minion"

@proxy
  Scenario: Check connection from minion to proxy
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" hostname

@proxy
  Scenario: Check registration on proxy of minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sle-minion" hostname

  Scenario: Subscribe the SLES minion to a base channel
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I check radio button "Test-Channel-x86_64"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed

  Scenario: Detect latest Salt changes on the SLES minion
    When I query latest Salt changes on "sle-minion"
