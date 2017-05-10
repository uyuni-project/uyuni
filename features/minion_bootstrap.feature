# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: register a salt-minion via bootstrap

  Scenario: Create bootstrap-repo for sle12sp2
     Given I am authorized
     And  I run "mgr-create-bootstrap-repo -c SLE-12-SP2-x86_64" on "server" without error control

  Scenario: bootstrap a sles minion
     Given I am authorized
     And I go to the minion onboarding page
     Then I should see a "Bootstrapping" text
     And I follow "Bootstrapping"
     Then I should see a "Bootstrap Minions" text
     And  I enter the hostname of "sle-minion" as hostname
     And I enter "22" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I click on "Bootstrap"
     Then I wait until i see "Successfully bootstrapped host! " text

  Scenario: check new bootstrapped minion in System Overview page
     Given I am authorized
     And I go to the minion onboarding page
     Then I should see a "accepted" text
     And the salt-master can reach "sle-minion"

  Scenario: Run a remote command sles-minion (salt-service)
    Given I am authorized as "testing" with password "testing"
    And I follow "Salt"
    And I follow "Remote Commands"
    And I should see a "Remote Commands" text
    Then I enter command "ls -lha /etc"
    And I click on preview
    Then I should see my hostname
    And I click on run
    Then I wait for "3" seconds
    And I expand the results
    Then I should see "SuSE-release" in the command output
 
  Scenario: Check spacecmd system ID of bootstrapped minion.
    Given I am on the Systems overview page of this "sle-minion"
    Then I run spacecmd listevents for sle-minion
 
  Scenario: Delete sles-minion system profile
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"
    Then I should see a "has been deleted" text
    And I cleanup minion: "sle-minion"

  Scenario: create minion activation key with Channel and package list
    Given I am on the Systems page
    And I follow "Activation Keys" in the left menu
    And I follow "Create Key"
    When I enter "Minion testing" as "description"
    And I enter "MINION-TEST" as "key"
    And I enter "20" as "usageLimit"
    And I select "Test-Channel-x86_64" from "selectedChannel"
    And I click on "Create Activation Key"
    And I follow "Packages"
    And I enter "orion-dummy perseus-dummy" as "packages"
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
     And I click on "Bootstrap"
     Then I wait until i see "Successfully bootstrapped host! " text
     And I wait for "100" seconds

  Scenario: verify minion bootstrapped with activation key, packages
     Given I am authorized
     And I go to the minion onboarding page
     Then I should see a "accepted" text
     And the salt-master can reach "sle-minion"
     And "orion-dummy" is installed on "minion"
     And "perseus-dummy" is installed on "minion"
     And I remove pkg "orion-dummy" on this "sle-minion"
     And I remove pkg "perseus-dummy" on this "sle-minion"

   Scenario: Check spacecmd system ID of second bootstrapped minion(after deletion of first)
    Given I am on the Systems overview page of this "sle-minion"
    Then I run spacecmd listevents for sle-minion

  Scenario: verify minion bootstrapped with activation key: activation key test
     Given I am on the Systems overview page of this "sle-minion"
     Then I should see a "Activation Key: 	1-MINION-TEST" text

  Scenario: verify minion bootstrapped with activation key: base channel test
     Given I am on the Systems page
     Then I should see a "Test-Channel-x86_64" text

  Scenario: bootstrap should fail: minion_id already existing
     Given I am authorized
     When I follow "Salt"
     Then I should see a "Bootstrapping" text
     And I follow "Bootstrapping"
     Then I should see a "Bootstrap Minions" text
     And  I enter the hostname of "sle-minion" as hostname
     And I enter "22" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I click on "Bootstrap"
     And I wait for "5" seconds
     And I should not see a "GenericSaltError({" text
     And I should see a "A salt key for this host" text
     And I should see a "seems to already exist, please check!" text

  Scenario: Delete sles-minion system profile (second-time)
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"
    Then I should see a "has been deleted" text
 # https://github.com/SUSE/spacewalk/pull/831
  Scenario: bootstrap a sles minion with wrong hostname
     Given I am authorized
     When I follow "Salt"
     Then I should see a "Bootstrapping" text
     And I follow "Bootstrapping"
     Then I should see a "Bootstrap Minions" text
     And  I enter "not-existing-name" as "hostname"
     And I enter "22" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I click on "Bootstrap"
     And I wait for "15" seconds
     And I should not see a "GenericSaltError({" text
     Then I should see a " Could not resolve hostname not-existing-name: Name or service not known" text

  Scenario: bootstrap a sles minion with wrong ssh-credentials
     Given I am authorized
     When I follow "Salt"
     Then I should see a "Bootstrapping" text
     And I follow "Bootstrapping"
     Then I should see a "Bootstrap Minions" text
     And I enter the hostname of "sle-minion" as hostname
     And I enter "22" as "port"
     And I enter "FRANZ" as "user"
     And I enter "KAFKA" as "password"
     And I click on "Bootstrap"
     And I wait for "15" seconds
     And I should not see a "GenericSaltError({" text
     Then I should see a "Permission denied (publickey,keyboard-interactive)." text

  Scenario: running command as user salt is forbidden.
   Given I am authorized
     When I follow "Salt"
     Then I should see a "Bootstrapping" text
     And I follow "Bootstrapping"
     Then I should see a "Bootstrap Minions" text
     And  I enter "`dmesg`" as "hostname"
     And I enter "22" as "port"
     And I enter "FRANZ" as "user"
     And I enter "KAFKA" as "password"
     And I click on "Bootstrap"
     And I wait for "15" seconds
     And I should not see a "dmesg: read kernel buffer failed:" text

  Scenario: bootstrap a sles minion with wrong ssh-port-number
     Given I am authorized
     When I follow "Salt"
     Then I should see a "Bootstrapping" text
     And I follow "Bootstrapping"
     Then I should see a "Bootstrap Minions" text
     And I enter the hostname of "sle-minion" as hostname
     And I enter "11" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I click on "Bootstrap"
     And I wait for "30" seconds
     And I should not see a "GenericSaltError({" text
     And I should see a "ssh: connect to host" text
     Then I should see a "port 11: Connection refused" text
