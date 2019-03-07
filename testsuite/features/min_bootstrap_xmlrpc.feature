# Copyright (c) 2017-2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Register a Salt minion via XML-RPC API

  Scenario: Delete SLES minion system profile before XML-RPC bootstrap test
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"
    Then I wait until I see "has been deleted" text
    And I cleanup minion "sle-minion"

  Scenario: Bootstrap a SLES minion via XML-RPC
    Given I am logged in via XML-RPC system as user "admin" and password "admin"
    When I call system.bootstrap() on host "sle-minion" and salt-ssh "disabled"
    And I logout from XML-RPC system namespace

  Scenario: Check new minion bootstrapped via XML-RPC in System Overview page
     Given I am authorized
     And I go to the minion onboarding page
     Then I should see a "accepted" text
     And the Salt master can reach "sle-minion"
     And I navigate to "rhn/systems/Overview.do" page
     And I wait until I see the name of "sle-minion", refreshing the page
     And I wait until onboarding is completed for "sle-minion"

  Scenario: Check contact method of this minion
    Given I am on the Systems overview page of this "sle-minion"
    Then I should see a "Default" text

@proxy
  Scenario: Check registration on proxy of minion bootstrapped via XML-RPC
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sle-minion" hostname

  Scenario: Check spacecmd system ID of minion bootstrapped via XML-RPC
    Given I am on the Systems overview page of this "sle-minion"
    Then I run spacecmd listevents for "sle-minion"

  Scenario: XML-RPC bootstrap: subscribe to base channel
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

  Scenario: Check events history for failures on SLES minion after XML-RPC bootstrap
    Given I am on the Systems overview page of this "sle-minion"
    Then I check for failed events on history event page

  Scenario: Bootstrap via XML-RPC a non-existing system
    Given I am logged in via XML-RPC system as user "admin" and password "admin"
    When I call system.bootstrap() on unknown host, I should get an XML-RPC fault with code -1
    And I logout from XML-RPC system namespace

  Scenario: Bootstrap a salt-ssh system with activation key and default contact method
    Given I am logged in via XML-RPC system as user "admin" and password "admin"
    When I call system.bootstrap() on a salt minion with saltSSH = true, but with activation key with Default contact method, I should get an XML-RPC fault with code -1
    And I logout from XML-RPC system namespace

  Scenario: Cleanup: turn the SLES minion into a container build host after XML bootstrap
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Details" in the content area
    And I follow "Properties" in the content area
    And I check "container_build_host"
    And I click on "Update Properties"

  Scenario: Cleanup: apply the highstate to container build host after XML bootstrap
    Given I am on the Systems overview page of this "sle-minion"
    When I wait until no Salt job is running on "sle-minion"
    And I enable repositories before installing Docker
    And I apply highstate on "sle-minion"
    And I wait until "docker" service is active on "sle-minion"
    And I disable repositories after installing Docker

  Scenario: Cleanup: check that the minion is now a build host after XML bootstrap
    Given I am on the Systems overview page of this "sle-minion"
    Then I should see a "[Container Build Host]" text
