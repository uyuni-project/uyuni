# Copyright (c) 2024 SUSE LLC
# Licensed under the terms of the MIT license.

@skip_if_github_validation
@sle_minion
@scope_salt
Feature: Salt Master integration with Git Pillar

  Scenario: Preparing Git Pillar configuration for Salt Master
    When I setup a git_pillar environment on the Salt Master
    And I wait for "30" seconds
    Then file "/etc/salt/master.d/zz-testing-gitpillar.conf" should exist on server

  Scenario: Check for the expected pillar data after enabling Git Pillar
    When I refresh the pillar data
    Then the pillar data for "git_pillar_foobar" should be "12345" on "sle_minion"
    And the pillar data for "org_id" should be "1" on "sle_minion"
    And the pillar data for "git_pillar_foobar" should be "12345" on the Salt Master

  Scenario: Cleanup: Removing Git Pillar configuration for Salt Master
    When I clean up the git_pillar environment on the Salt Master
    And I wait for "30" seconds
    Then file "/etc/salt/master.d/zz-testing-gitpillar.conf" should not exist on server

  Scenario: Check for the expected pillar data after disabling Git Pillar
    When I refresh the pillar data
    Then the pillar data for "git_pillar_foobar" should be empty on "sle_minion"
    And the pillar data for "org_id" should be "1" on "sle_minion"
    And the pillar data for "git_pillar_foobar" should be empty on the Salt Master
