# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Nagios plugins

  Scenario: Check pending patches for non-existing host
    Given I perform an invalid nagios check patches
    Then I should see an unknown system message
