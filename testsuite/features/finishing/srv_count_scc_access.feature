# Copyright (c) 2024 SUSE LLC
# Licensed under the terms of the MIT license.

@srv_scc_access_logging
Feature: Test for excessive SCC accesses

  Scenario: Count UI SCC accesses in rhn_web_ui.log
    Then the word "scc.suse.com" does not occur more than 100 times in "/var/log/rhn/rhn_web_ui.log" on "server"

  Scenario: Count Taskomatic SCC accesses in rhn_taskomatic_daemon.log
    Then the word "scc.suse.com" does not occur more than 50 times in "/var/log/rhn/rhn_taskomatic_daemon.log" on "server"
