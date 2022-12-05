# Copyright (c) 2021-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@ssh_minion
@scope_salt_ssh
@proxy
Feature: Move a ssh minion from a proxy to direct connection

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section
    And I am logged in API as user "admin" and password "admin"

  Scenario: Delete minion system profile before bootstrap
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "ssh_minion" should not be registered

  Scenario: Bootstrap a minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I check "manageWithSSH"
    And I enter the hostname of "ssh_minion" as "hostname"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-SUSE-SSH-KEY-x86_64" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I wait until onboarding is completed for "ssh_minion"

  Scenario: Check initial connection from minion to proxy
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

  Scenario: Check initial registration on proxy of minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ssh_minion" hostname

  Scenario: Change from proxy to direct connection
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    And I follow "Change" in the content area
    And I select "None" from "proxies"
    And I click on "Change Proxy"
    And I wait until I see "scheduled" text
    And I follow "Events"
    And I follow "History"
    And I wait until I see the event "Apply states [channels] scheduled by admin" completed during last minute, refreshing the page

  Scenario: Check direct connection
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see a "This system connects directly and not through a Proxy" text

  Scenario: Change connection to a proxy
    # be sure that the old events are older than 1 minute
    Given I wait for "120" seconds
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    And I follow "Change" in the content area
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Change Proxy"
    And I wait until I see "scheduled" text
    And I follow "Events"
    And I follow "History"
    And I wait until I see the event "Apply states [channels] scheduled by admin" completed during last minute, refreshing the page

  Scenario: Check registration on proxy of minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ssh_minion" hostname

  Scenario: Check connection from minion to proxy
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

  Scenario: Check events history for failures on the minion
    Given I am on the Systems overview page of this "ssh_minion"
    Then I check for failed events on history event page

  Scenario: Cleanup: Logout from API
    When I logout from API
