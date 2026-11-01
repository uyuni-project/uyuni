# Copyright (c) 2020 SUSE LLC
# SPDX-License-Identifier: MIT

Feature: Debug the clients after the testsuite has run

  Scenario: Extract the logs from all our clients
    When I extract the log files from all our active nodes
