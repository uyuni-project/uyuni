# Copyright (c) 2021-2024 SUSE LLC
# Licensed under the terms of the MIT license.

@sle_minion
@proxy
Feature: Move a minion from a proxy to direct connection

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: Delete minion system profile before bootstrap
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    And I wait until Salt client is inactive on "sle_minion"
    Then "sle_minion" should not be registered

  Scenario: Bootstrap a minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-SUSE-KEY-x86_64" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text
    And I wait until onboarding is completed for "sle_minion"

  Scenario: Check the new bootstrapped minion in System Overview page
    When I follow the left menu "Salt > Keys"
    Then I should see a "accepted" text
    And the Salt master can reach "sle_minion"

  Scenario: Check initial connection from minion to proxy
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

  Scenario: Check initial registration on proxy of minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sle_minion" hostname

  Scenario: Change from proxy to direct connection
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    And I follow "Change" in the content area
    And I select "None" from "proxies"
    And I click on "Change Proxy"
    And I wait until I see "scheduled" text
    And I follow "Events"
    And I follow "History"
    And I wait until I see the event "Apply states [bootstrap.set_proxy] scheduled" completed during last minute, refreshing the page
    And I wait until I see the event "Apply states [channels] scheduled" completed during last minute, refreshing the page

  Scenario: Check direct connection
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see a "This system connects directly and not through a Proxy" text

  Scenario: Change connection back to a proxy via SSM
    # be sure that the old events are older than 1 minute
    Given I wait for "120" seconds
    When I follow the left menu "Systems > System List > All"
    And I click on the clear SSM button
    And I check the "sle_minion" client
    And I should see "1" systems selected for SSM
    And I follow the left menu "Systems > System Set Manager > Overview"
    And I follow "proxy server" in the content area
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Change Proxy"
    And I wait until I see "scheduled" text

  Scenario: Check events on the minion
    Given I am on the Systems overview page of this "sle_minion"
    And I follow "Events"
    And I follow "History"
    And I wait until I see the event "Apply states [bootstrap.set_proxy] scheduled" completed during last minute, refreshing the page
    And I wait until I see the event "Apply states [channels] scheduled" completed during last minute, refreshing the page

  Scenario: Check registration on proxy of minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sle_minion" hostname

  Scenario: Check connection from minion to proxy
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

  Scenario: Check events history for failures on the minion
    Given I am on the Systems overview page of this "sle_minion"
    Then I check for failed events on history event page
