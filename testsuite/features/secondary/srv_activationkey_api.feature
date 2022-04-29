# Copyright (c) 2021-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_api
Feature: API "activationkey" namespace

  Scenario: List all activation keys
    When I am logged in API as user "admin" and password "admin"
    Then I should get some activation keys

  Scenario: Create activation key
    When I create an activation key with id "testkey", description "Key for testing" and limit of 10
    Then I should get the new activation key

  Scenario: Activation key details
    When I set the description of activation key to "Key description"
    Then I get the description "Key description" for the activation key

  Scenario: Cleanup: delete activation key
    When I delete the activation key
    And I logout from API
