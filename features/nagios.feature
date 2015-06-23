# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Check nagios plugins
  In order to check nagios probes for a host
  As the root user
  I want to check the number of pending patches
  I want to check the status of the last action

  Scenario: Check pending patches for host
    Given I perform a nagios check patches
    Then I should see WARNING: 1 patch pending

  Scenario: Check pending patches for non-existing host
    Given I perform an invalid nagios check patches
    Then I should see an unknown system message

  Scenario: Check status of last action
    Given I perform a nagios check last event
    Then I should see Completed: OpenSCAP xccdf scanning scheduled by testing
