# Copyright (c) 2020-2023 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Reposync works as expected

  Scenario: Check reposync of custom channel
    When I wait until the channel "fake-rpm-suse-channel" has been synced
    Then "orion-dummy-1.1-1.1.x86_64.rpm" package should have been stored
    And solver file for "fake-rpm-suse-channel" should reference "orion-dummy-1.1-1.1.x86_64.rpm"
