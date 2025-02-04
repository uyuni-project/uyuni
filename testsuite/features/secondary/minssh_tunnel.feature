# Copyright (c) 2020-2024 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature can cause failures in the following features:
# - features/secondary/min_bootstrap_activation_key.feature
# If the minion fails to bootstrap again.

@skip_if_github_validation
@scope_salt_ssh
@ssh_minion
Feature: Register a Salt system to be managed via SSH tunnel

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: Pre-requisite: remove package before ssh tunnel test
    When I remove package "milkyway-dummy" from this "ssh_minion" without error control

  Scenario: Delete the Salt minion for SSH tunnel bootstrap
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "ssh_minion" should not be registered

  Scenario: Register this minion for push via SSH tunnel
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    And I enter the hostname of "ssh_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-SUSE-SSH-TUNNEL-KEY-x86_64" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I check "manageWithSSH"
    And I click on "Bootstrap"
    # workaround for bsc#1222108
    And I wait at most 480 seconds until I see "Bootstrap process initiated." text
    And I wait until onboarding is completed for "ssh_minion"

  Scenario: The contact method is SSH tunnel on this minion
    Given I am on the Systems overview page of this "ssh_minion"
    Then I should see a "Push via SSH tunnel" text

  Scenario: Install a package from this SSH tunnel minion
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Software" in the content area
    And I follow "Install"
    And I enter "milkyway-dummy" as the filtered package name
    And I click on the filter button
    And I check row with "milkyway-dummy" and arch of "ssh_minion"
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled for" text
    Then I wait until event "Package Install/Upgrade scheduled" is completed

  Scenario: Remove a package from this SSH tunnel minion
    Given I am on the Systems overview page of this "ssh_minion"
    And I follow "Software" in the content area
    And I follow "List / Remove"
    And I enter "milkyway-dummy" as the filtered package name
    And I click on the filter button
    And I wait until I see "milkyway-dummy" text
    And I check "milkyway-dummy" in the list
    And I click on "Remove Packages"
    And I click on "Confirm"
    Then I should see a "1 package removal has been scheduled" text
    Then I wait until event "Package Removal scheduled" is completed

  Scenario: Run a remote command on this SSH tunnel minion
    When I follow the left menu "Salt > Remote Commands"
    Then I should see a "Remote Commands" text in the content area
    When I enter command "echo 'My remote command output'"
    And I enter the hostname of "ssh_minion" as "target"
    And I click on preview
    Then I should see a "Target systems (1)" text
    When I wait until I do not see "pending" text
    And I click on run
    And I wait until I see "show response" text
    And I expand the results for "ssh_minion"
    Then I should see "My remote command output" in the command output for "ssh_minion"

  Scenario: Cleanup: delete the SSH tunnel minion
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "ssh_minion" should not be registered

  Scenario: Cleanup: register a SSH minion after SSH tunnel tests
    When I call system.bootstrap() on host "ssh_minion" and salt-ssh "enabled"
    And I follow the left menu "Systems > System List > All"
    And I wait until I see the name of "ssh_minion", refreshing the page
    And I wait until onboarding is completed for "ssh_minion"
