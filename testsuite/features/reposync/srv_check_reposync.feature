# Copyright (c) 2020 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Reposync works as expected

  Scenario: Check reposync of custom channel
    Then "orion-dummy-1.1-1.1.x86_64.rpm" package should have been stored
    And solver file for "test-channel-x86_64" should reference "orion-dummy-1.1-1.1.x86_64.rpm"
