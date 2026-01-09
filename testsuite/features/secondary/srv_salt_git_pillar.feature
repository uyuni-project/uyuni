# Copyright (c) 2025 SUSE LLC
# Licensed under the terms of the MIT license.

@skip_if_github_validation
@sle_minion
@scope_salt
Feature: Salt master integration with Git pillar

  # Workaround: Enabling repositories for installing git-core
  Scenario: Pre-requisite: Enabling repositories for installing git-core
    When I add repository "SLE-Module-Basesystem15-SP7-Updates" with url "http://minima-mirror-ci-bv.mgr.suse.de/SUSE/Updates/SLE-Module-Basesystem/15-SP7/x86_64/update/" on "server" without error control
    And I add repository "SLE-Module-Basesystem15-SP7-Pool" with url "http://minima-mirror-ci-bv.mgr.suse.de/SUSE/Products/SLE-Module-Basesystem/15-SP7/x86_64/product/" on "server" without error control

  Scenario: Preparing Git pillar configuration for Salt master
    When I setup a git_pillar environment on the Salt master
    And I wait until Salt master can reach "sle_minion"
    Then file "/etc/salt/master.d/zz-testing-gitpillar.conf" should exist on server

  Scenario: Check for the expected pillar data after enabling Git pillar
    When I refresh the pillar data
    And I wait until there is no pillar refresh salt job active
    Then the pillar data for "org_id" should be "1" on "sle_minion"
    And the pillar data for "git_pillar_foobar" should be "12345" on "sle_minion"
    And the pillar data for "git_pillar_foobar" should be "12345" on the Salt master

  Scenario: Cleanup: Remove Git pillar configuration for Salt master
    When I clean up the git_pillar environment on the Salt master
    And I wait until Salt master can reach "sle_minion"
    Then file "/etc/salt/master.d/zz-testing-gitpillar.conf" should not exist on server

Scenario: Cleanup: Check for the expected pillar data after disabling Git pillar
    When I refresh the pillar data
    Then the pillar data for "git_pillar_foobar" should be empty on "sle_minion"
    And the pillar data for "org_id" should be "1" on "sle_minion"
    And the pillar data for "git_pillar_foobar" should be empty on the Salt master

Scenario: Pre-Cleanup: Disabling repositories for uninstalling git-core
  When I remove repository "SLE-Module-Basesystem15-SP7-Updates" on "server" without error control
  And I remove repository "SLE-Module-Basesystem15-SP7-Pool" on "server" without error control
