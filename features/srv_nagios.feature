# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Check nagios plugins
  In order to check nagios probes for a host
  I want to check the number of pending patches
  I want to check the status of the last action

  Scenario: Check pending patches for non-existing host
    Given I perform an invalid nagios check patches
    Then I should see an unknown system message
