# Copyright (c) 2020-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_salt_ssh
Feature: Register a salt system to be managed via SSH tunnel

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Delete the salt minion for SSH tunnel bootstrap
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "sle_minion" should not be registered

  Scenario: Register this minion for push via SSH tunnel
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    And I enter the hostname of "sle_ssh_tunnel_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-SUSE-SSH-TUNNEL-KEY-x86_64" from "activationKeys"
    And I select the hostname of "proxy" from "proxies"
    And I check "manageWithSSH"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I wait until onboarding is completed for "sle_ssh_tunnel_minion"

  Scenario: The contact method is SSH tunnel on this minion
    Given I am on the Systems overview page of this "sle_ssh_tunnel_minion"
    Then I should see a "Push via SSH tunnel" text

  Scenario: Install a package from this SSH tunnel minion
    Given I am on the Systems overview page of this "sle_ssh_tunnel_minion"
    When I follow "Software" in the content area
    And I follow "Install"
    And I enter "milkyway-dummy" as the filtered package name
    And I click on the filter button
    And I check "milkyway-dummy" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled for" text
    When I force picking pending events on "sle_ssh_tunnel_minion" if necessary
    Then I wait until event "Package Install/Upgrade scheduled by admin" is completed

  Scenario: Remove a package from this SSH tunnel minion
    Given I am on the Systems overview page of this "sle_ssh_tunnel_minion"
    And I follow "Software" in the content area
    And I follow "List / Remove"
    And I enter "milkyway-dummy" as the filtered package name
    And I click on the filter button
    And I check "milkyway-dummy" in the list
    And I click on "Remove Packages"
    And I click on "Confirm"
    Then I should see a "1 package removal has been scheduled" text
    When I force picking pending events on "sle_ssh_tunnel_minion" if necessary
    Then I wait until event "Package Removal scheduled by admin" is completed

  Scenario: Run a remote command on this SSH tunnel minion
    When I follow the left menu "Salt > Remote Commands"
    Then I should see a "Remote Commands" text in the content area
    When I enter command "echo 'My remote command output'"
    And I enter the hostname of "sle_ssh_tunnel_minion" as "target"
    And I click on preview
    Then I should see a "Target systems (1)" text
    When I wait until I do not see "pending" text
    And I click on run
    And I wait until I see "show response" text
    And I expand the results for "sle_ssh_tunnel_minion"
    Then I should see "My remote command output" in the command output for "sle_ssh_tunnel_minion"

  Scenario: Cleanup: delete the SSH tunnel minion
    Given I am on the Systems overview page of this "sle_ssh_tunnel_minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "sle_ssh_tunnel_minion" should not be registered

  Scenario: Cleanup: register a salt minion after SSH tunnel tests
    When I bootstrap minion client "sle_minion" using bootstrap script with activation key "1-SUSE-KEY-x86_64" from the proxy
    And I wait at most 10 seconds until Salt master sees "sle_minion" as "unaccepted"
    And I accept "sle_minion" key in the Salt master
    Then I should see "sle_minion" via spacecmd
