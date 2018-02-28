# Copyright (c) 2017-2018 SUSE LLC.
# Licensed under the terms of the MIT license.

Feature: Register a salt-ssh system via XML-RPC

@sshminion
  Scenario: Setup XML-RPC bootstrap: delete SSH minion system profile
    Given I am on the Systems overview page of this "ssh-minion"
    When I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    And I cleanup minion "ssh-minion"

@sshminion
  Scenario: Bootstrap a SLES SSH minion via XML-RPC
    Given I am logged in via XML-RPC system as user "admin" and password "admin"
    When I call system.bootstrap() on host "ssh-minion" and salt-ssh "enabled"
    And I logout from XML-RPC system namespace

@sshminion
  Scenario: Check new XML-RPC bootstrapped salt-ssh system in System Overview page
     Given I am authorized
     And I navigate to "rhn/systems/Overview.do" page
     And I wait until I see the name of "ssh-minion", refreshing the page
     And I wait until onboarding is completed for "ssh-minion"

@sshminion
  Scenario: Check contact method of this Salt SSH system
    Given I am on the Systems overview page of this "ssh-minion"
    Then I should see a "Push via SSH" text

@proxy
@sshminion
  Scenario: Check registration on proxy of SSH minion bootstrapped via XML-RPC
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ssh-minion" hostname

@sshminion
  Scenario: Check spacecmd system ID of SSH minion bootstrapped via XML-RPC
    Given I am on the Systems overview page of this "ssh-minion"
    Then I run spacecmd listevents for "ssh-minion"

@sshminion
  Scenario: Cleanup: subscribe SSH minion to base channel
    Given I am on the Systems overview page of this "ssh-minion"
    When I follow "Software" in the content area
    Then I follow "Software Channels" in the content area
    And I check radio button "Test-Channel-x86_64"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed
