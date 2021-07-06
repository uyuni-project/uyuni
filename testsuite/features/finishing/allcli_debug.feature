# Copyright (c) 2020 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Debug the clients after the testsuite has run

  Scenario: Extract the logs from all our clients
    When I extract the log files from all our active nodes

@client
  Scenario: Check spacewalk upd2date logs on client
    When the up2date logs on client should contain no Traceback error
