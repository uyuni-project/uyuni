# Copyright (c) 2017-2020 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_onboarding
Feature: Register a Salt minion via XML-RPC API

  Scenario: Delete SLES minion system profile before XML-RPC bootstrap test
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "sle_minion" should not be registered

  Scenario: Bootstrap a SLES minion via XML-RPC
    Given I am logged in via XML-RPC system as user "admin" and password "admin"
    When I call system.bootstrap() on host "sle_minion" and salt-ssh "disabled"
    And I logout from XML-RPC system namespace

  Scenario: Check new minion bootstrapped via XML-RPC in System Overview page
    Given I am authorized
    When I follow the left menu "Salt > Keys"
    Then I should see a "accepted" text
    When I am on the System Overview page
    And I wait until I see the name of "sle_minion", refreshing the page
    And I wait until onboarding is completed for "sle_minion"
    Then the Salt master can reach "sle_minion"

  Scenario: Check contact method of this minion
    Given I am on the Systems overview page of this "sle_minion"
    Then I should see a "Default" text

@proxy
  Scenario: Check registration on proxy of minion bootstrapped via XML-RPC
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sle_minion" hostname

  Scenario: Check spacecmd system ID of minion bootstrapped via XML-RPC
    Given I am on the Systems overview page of this "sle_minion"
    Then I run spacecmd listevents for "sle_minion"

  Scenario: XML-RPC bootstrap: subscribe to base channel
    Given I am on the Systems overview page of this "sle_minion"
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
    Given I am on the Systems overview page of this "sle_minion"
    Then I check for failed events on history event page

  Scenario: Bootstrap via XML-RPC a non-existing system
    Given I am logged in via XML-RPC system as user "admin" and password "admin"
    When I call system.bootstrap() on unknown host, I should get an XML-RPC fault with code -1
    And I logout from XML-RPC system namespace

  Scenario: Bootstrap a salt-ssh system with activation key and default contact method
    Given I am logged in via XML-RPC system as user "admin" and password "admin"
    When I call system.bootstrap() on a salt minion with saltSSH = true, but with activation key with Default contact method, I should get an XML-RPC fault with code -1
    And I logout from XML-RPC system namespace
