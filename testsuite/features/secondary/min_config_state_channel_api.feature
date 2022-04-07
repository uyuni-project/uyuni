# Copyright (c) 2021 SUSE LLC.
# Licensed under the terms of the MIT license.

Feature: Configuration state channels via XML-RPC API

  Scenario: Create a state channel via XML-RPC
    Given I am logged in via XML-RPC configchannel as user "admin" and password "admin"
    When I create state channel "statechannel1" via XML-RPC
    And I call configchannel.get_file_revision with file "/init.sls", revision "1" and channel "statechannel1" via XML-RPC
    Then I should get file contents ""

  Scenario: Create a state channel with contents via XML-RPC
    Given I am logged in via XML-RPC configchannel as user "admin" and password "admin"
    When I create state channel "statechannel2" containing "touch /root/foobar:\n  cmd.run:\n    - creates: /root/foobar" via XML-RPC
    And I call configchannel.get_file_revision with file "/init.sls", revision "1" and channel "statechannel2" via XML-RPC
    Then I should get file contents "touch /root/foobar:\n  cmd.run:\n    - creates: /root/foobar"

  Scenario: Cleanup: remove state channels via XML-RPC
    Given I am logged in via XML-RPC configchannel as user "admin" and password "admin"
    Then I delete channel "statechannel1" via XML-RPC without error control
    And I delete channel "statechannel2" via XML-RPC without error control
    And I logout from XML-RPC configchannel namespace
