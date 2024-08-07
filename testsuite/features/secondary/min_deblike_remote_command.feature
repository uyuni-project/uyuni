# Copyright (c) 2021-2024 SUSE LLC
# Licensed under the terms of the MIT license.

# Skip if container. This test is broken
# This needs to be fixed

@skip_if_github_validation
@scope_deblike
@deblike_minion
Feature: Remote command on Debian-like Salt minion
  In order to manage a Debian-like Salt minion
  As an authorized user
  I want to run a remote command on it

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: Run a remote command on the Debian-like minion
    When I follow the left menu "Salt > Remote Commands"
    Then I should see a "Remote Commands" text in the content area
    When I enter command "cat /etc/os-release"
    And I enter target "deblike_minion"
    And I click on preview
    And I click on run
    Then I should see "deblike_minion" hostname
    When I wait until I see "show response" text
    And I expand the results for "deblike_minion"
    Then I should see a "ID=ubuntu" text
