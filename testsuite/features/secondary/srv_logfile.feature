# Copyright (c) 2022 SUSE LLC
# SPDX-License-Identifier: MIT


Feature: Sanity check that all logfiles exist
  In order to be secure
  As an authorized user
  I want to watch different logfiles

  Scenario: All logfiles should be available
    Then I wait until file "/var/log/rhn/rhn_web_ui.log" exists on server
    And I wait until file "/var/log/rhn/rhn_web_api.log" exists on server
    And I wait until file "/var/log/rhn/rhn_web_frontend.log" exists on server
    And I wait until file "/var/log/rhn/rhn_taskomatic_daemon.log" exists on server
    And I wait until file "/var/log/rhn/search/rhn_search_daemon.log" exists on server
    And I wait until file "/var/log/rhn/search/rhn_search.log" exists on server
    And I wait until file "/var/log/rhn/reposync.log" exists on server
    And I wait until file "/var/log/salt/master" exists on server
    And I wait until file "/var/log/salt/api" exists on server
