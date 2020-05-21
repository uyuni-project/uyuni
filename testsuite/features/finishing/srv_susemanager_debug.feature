# Copyright (c) 2015-2019 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Debug SUSE Manager after the testsuite has run

  Scenario: Call spacewalk-debug on server
    Then I execute spacewalk-debug on the server

  Scenario: Check spacewalk upd2date logs on client
    Then the up2date logs on client should contain no Traceback error

  Scenario: Check the tomcat logs on server
    Then I check the tomcat logs for errors

  Scenario: Check salt event log for failures on server
    Then the salt event log on server should contain no failures

@sle_client
  Scenario: Get client logs
    Then I get logfiles from "sle_client"

@sle_minion
  Scenario: Get client logs
    Then I get logfiles from "sle_minion"

@centos_minion
  Scenario: Get client logs
    Then I get logfiles from "ceos_minion"

@ubuntu_minion
  Scenario: Get client logs
    Then I get logfiles from "ubuntu_minion"

@ssh_minion
  Scenario: Get client logs
    Then I get logfiles from "ssh_minion"

@proxy
  Scenario: Get client logs
    Then I get logfiles from "proxy"
