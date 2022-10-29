# Copyright (c) 2020-2022 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Reposync works as expected

  Scenario: Check reposync of custom channel
    Then "orion-dummy-1.1-1.1.x86_64.rpm" package should have been stored
    And solver file for "fake-rpm-sles15sp4-channel" should reference "orion-dummy-1.1-1.1.x86_64.rpm"
