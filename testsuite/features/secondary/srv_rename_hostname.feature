# Copyright (c) 2022-2023 SUSE LLC
# Licensed under the terms of the MIT License.
#
# This feature can cause failures in the following features:
# All following features
# If the server fails to reboot properly
# or the cleanup fails and renders the server unreachable.

@skip_if_github_validation
@skip_if_cloud
@skip_if_container_server
Feature: Reconfigure the server's hostname
  As admin user
  In order to change the server's hostname
  I want to use the tool spacewalk-hostname-rename.

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Change hostname and reboot server
    When I change the server's short hostname from hosts and hostname files
    And I run spacewalk-hostname-rename command on the server

@proxy
  Scenario: Copy the new server keys and configure the proxy
    When I copy server's keys to the proxy
    And I configure the proxy
    Then I should see "proxy" via spacecmd
    When I restart the "venv-salt-minion" service on "proxy"
    Then service "venv-salt-minion" is active on "proxy"
    When I restart the "salt-broker" service on "proxy"
    Then service "salt-broker" is active on "proxy"

@proxy
  Scenario: Apply high state on the proxy to populate new server CA
    When I apply highstate on "proxy"

@sle_minion
  Scenario: Apply high state on the SUSE Minion to populate new server CA
    When I apply highstate on "sle_minion"

@ssh_minion
  Scenario: Apply high state on the SUSE SSH Minion to populate new server CA
    When I apply highstate on "ssh_minion"

@rhlike_minion
  Scenario: Apply high state on the Red Hat-like Minion to populate new server CA
    When I apply highstate on "rhlike_minion"

@deblike_minion
  Scenario: Apply high state on the Debian-like Minion to populate new server CA
    When I apply highstate on "deblike_minion"

@buildhost
  Scenario: Apply high state on the build host to populate new server CA
    When I apply highstate on "build_host"

@virthost_kvm
  # WORKAROUND: Use the webUI instead of Salt like with the other minions above
  # The Salt call always failed for unknown reasons.
  # WORKAROUND: Use the webUI steps instead of the API call,
  # as it fails due to an SSL error, even if we established a new connection.
  Scenario: Apply high state on the virthost to populate new server CA
    Given I navigate to the Systems overview page of this "kvm_server"
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    And I wait until event "Apply highstate scheduled by admin" is completed

  Scenario: Check all new server certificates on the minions
    When I check all certificates after renaming the server hostname

  Scenario: Do some minimal smoke test on the renamed server
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Details" in the content area
    And I follow "Remote Command" in the content area
    And I enter as remote command this script in
      """
      #!/bin/bash
      date
      """
    And I click on "Schedule"
    And I follow "Events" in the content area
    And I follow "Pending" in the content area
    And I wait at most 180 seconds until I do not see "Remote Command on" text, refreshing the page
    And I follow "History" in the content area
    And I wait until I see the event "Remote Command on" completed during last minute, refreshing the page

  Scenario: Change hostname back and reboot server
    When I change back the server's hostname
    And I run spacewalk-hostname-rename command on the server

@proxy
  Scenario: Copy the new server keys and configure the proxy
    When I copy server's keys to the proxy
    And I configure the proxy
    Then I should see "proxy" via spacecmd
    When I restart the "venv-salt-minion" service on "proxy"
    Then service "venv-salt-minion" is active on "proxy"
    When I restart the "salt-broker" service on "proxy"
    Then service "salt-broker" is active on "proxy"

@proxy
  Scenario: Apply high state on the proxy to populate new server CA
    When I apply highstate on "proxy"

@sle_minion
  Scenario: Apply high state on the SUSE Minion to populate new server CA
    When I apply highstate on "sle_minion"

@ssh_minion
  Scenario: Apply high state on the SUSE SSH Minion to populate new server CA
    When I apply highstate on "ssh_minion"

@rhlike_minion
  Scenario: Apply high state on the Red Hat-like Minion to populate new server CA
    When I apply highstate on "rhlike_minion"

@deblike_minion
  Scenario: Apply high state on the Debian-like Minion to populate new server CA
    When I apply highstate on "deblike_minion"

@buildhost
  Scenario: Apply high state on the build host to populate new server CA
    When I apply highstate on "build_host"

@virthost_kvm
  # WORKAROUND: Use the webUI instead of Salt like with the other minions above
  # The Salt call always failed for unknown reasons
  Scenario: Apply high state on the virthost to populate new server CA
    Given I am on the Systems overview page of this "kvm_server"
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    And I wait until event "Apply highstate scheduled by admin" is completed

  Scenario: Check all new server certificates on the minions
    When I check all certificates after renaming the server hostname
