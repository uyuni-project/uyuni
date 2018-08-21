# Copyright (c) 2017-2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: XML-RPC image namespace for containers

  Scenario: Test image.store namespace
    Given I am authorized as "admin" with password "admin"
    Then I run image.store tests via XML-RPC

  Scenario: Test image.profiles namespace
    Given I am authorized as "admin" with password "admin"
    Then I run image.profiles tests via XML-RPC

  Scenario: Cleanup: remove custom system info key
    Given I am authorized as "admin" with password "admin"
    When I follow "Systems"
    And I follow "Custom System Info"
    And I follow "arancio"
    And I follow "Delete Key"
    And I click on "Delete Key"
