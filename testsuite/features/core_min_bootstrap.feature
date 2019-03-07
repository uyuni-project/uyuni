# Copyright (c) 2016-2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Be able to bootstrap a Salt minion via the GUI

  Scenario: Create the bootstrap repository for a Salt client
     Given I am authorized
     And I create the "x86_64" bootstrap repository for "sle-minion" on the server

  Scenario: Bootstrap a SLES minion with wrong hostname
     Given I am authorized
     And I go to the bootstrapping page
     Then I should see a "Bootstrap Minions" text
     When I enter "not-existing-name" as "hostname"
     And I enter "22" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I click on "Bootstrap"
     And I wait until I see " Could not resolve hostname not-existing-name: Name or service not known" text
     Then I should not see a "GenericSaltError" text

  Scenario: Bootstrap a SLES minion with wrong SSH credentials
     Given I am authorized
     And I go to the bootstrapping page
     Then I should see a "Bootstrap Minions" text
     When I enter the hostname of "sle-minion" as "hostname"
     And I enter "22" as "port"
     And I enter "FRANZ" as "user"
     And I enter "KAFKA" as "password"
     And I click on "Bootstrap"
     And I wait until I see "Permission denied (publickey,keyboard-interactive)." text or "Password authentication failed" text
     Then I should not see a "GenericSaltError" text

  Scenario: Bootstrap a SLES minion with wrong SSH port number
     Given I am authorized
     And I go to the bootstrapping page
     Then I should see a "Bootstrap Minions" text
     When I enter the hostname of "sle-minion" as "hostname"
     And I enter "11" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I click on "Bootstrap"
     And I wait until I see "ssh: connect to host" text
     Then I should not see a "GenericSaltError" text
     And I should see a "port 11: Connection refused" text

  Scenario: Bootstrap a SLES minion
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

  Scenario: Check the new bootstrapped minion in System Overview page
     Given I am authorized
     And I go to the minion onboarding page
     Then I should see a "accepted" text
     And the Salt master can reach "sle-minion"
     When I navigate to "rhn/systems/Overview.do" page
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
    And I wait until I do not see "Loading..." text
    And I check radio button "Test-Channel-x86_64"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed

  # bsc#1085436 - Apache returns 403 Forbidden after a zypper refresh on minion
  Scenario: Check the new channel is working
    When I refresh the metadata for "sle-minion"

  Scenario: Detect latest Salt changes on the SLES minion
    When I query latest Salt changes on "sle-minion"

  Scenario: Run a remote command on normal SLES minion
    Given I am authorized as "testing" with password "testing"
    When I follow "Salt"
    And I follow "Remote Commands"
    Then I should see a "Remote Commands" text
    When I enter command "file /tmp"
    And I click on preview
    Then I should see "sle-minion" hostname
    And I wait until I do not see "pending" text
    When I click on run
    And I wait until I do not see "pending" text
    And I expand the results for "sle-minion"
    Then I should see "/tmp: sticky, directory" in the command output for "sle-minion"

  Scenario: Check spacecmd system ID of bootstrapped minion
    Given I am on the Systems overview page of this "sle-minion"
    Then I run spacecmd listevents for "sle-minion"

  Scenario: Bootstrap should fail when minion already exists
     Given I am authorized
     And I go to the bootstrapping page
     Then I should see a "Bootstrap Minions" text
     When I enter the hostname of "sle-minion" as "hostname"
     And I enter "22" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I click on "Bootstrap"
     And I wait until I see "A salt key for this host" text
     Then I should not see a "GenericSaltError" text
     And I should see a "seems to already exist, please check!" text

  Scenario: Turn the SLES minion into a container build host
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Details" in the content area
    And I follow "Properties" in the content area
    And I check "container_build_host"
    And I click on "Update Properties"
    Then I should see a "Container Build Host type has been applied." text
    And I should see a "Note: This action will not result in state application" text
    And I should see a "To apply the state, either use the states page or run `state.highstate` from the command line." text
    And I should see a "System properties changed" text

  Scenario: Apply the highstate to container build host
    Given I am on the Systems overview page of this "sle-minion"
    When I wait until no Salt job is running on "sle-minion"
    And I enable repositories before installing Docker
    And I apply highstate on "sle-minion"
    And I wait until "docker" service is active on "sle-minion"
    And I disable repositories after installing Docker

  Scenario: Check that the minion is now a build host
    Given I am on the Systems overview page of this "sle-minion"
    Then I should see a "[Container Build Host]" text

  Scenario: Check events history for failures on SLES minion
    Given I am on the Systems overview page of this "sle-minion"
    Then I check for failed events on history event page
