# Copyright (c) 2021-2022 SUSE LLC.
# Licensed under the terms of the MIT license.

Feature: Configuration state channels via API

  Scenario: Create a state channel via API
    Given I am logged in API as user "admin" and password "admin"
    When I create state channel "statechannel1" via API
    And I call configchannel.get_file_revision() with file "/init.sls", revision "1" and channel "statechannel1" via API
    Then I should get file contents ""

  Scenario: Create a state channel with contents via API
    When I create state channel "statechannel2" containing "touch /root/foobar:\n  cmd.run:\n    - creates: /root/foobar" via API
    And I call configchannel.get_file_revision() with file "/init.sls", revision "1" and channel "statechannel2" via API
    Then I should get file contents "touch /root/foobar:\n  cmd.run:\n    - creates: /root/foobar"

  Scenario: Cleanup: remove state channels via API
    Then I delete channel "statechannel1" via API without error control
    And I delete channel "statechannel2" via API without error control
    And I logout from API
