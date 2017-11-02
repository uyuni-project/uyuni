# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: call spacewalk-debug and copy the server logs
  In Order to get the server logfiles
  As user root
  I want to call spacewalk-debug on the server
  And I want to copy the logs to the client

  Scenario: call spacewalk-debug
    Given I am root
    Then I execute spacewalk-debug on the server

  Scenario: copy the archive
    When I copy "/tmp/spacewalk-debug.tar.bz2"
