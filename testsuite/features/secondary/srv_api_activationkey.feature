# Copyright (c) 2021-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_api
Feature: API "activationkey" namespace

  Scenario: List all activation keys
    When I am logged in API as user "admin" and password "admin"
    And I call listActivationKeys I should get some

  Scenario: Create activation key
    When I create an activation key with id "testkey", description "Key for testing" and limit of 10
    Then I should get it listed with a call of listActivationKeys

  Scenario: Activation key details
    When I call activationkey.set_details() to the key setting as description "Key description"
    Then I have to see them by calling activationkey.get_details() having as description "Key description"

  Scenario: Cleanup: delete activation key
    When I delete the activation key
    And I logout from API
