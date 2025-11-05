# Copyright (c) 2021-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_api
Feature: API "activationkey" namespace

  Scenario: List all activation keys
    Then I should get some activation keys

  Scenario: Create activation key
    When I create an activation key with id "testkey", description "Key for testing", limit of 10 and contact method "default"
    Then I should get the new activation key "1-testkey"

  Scenario: Activation key details
    When I set the description of the activation key "1-testkey" to "Key description"
    Then I get the description "Key description" for the activation key "1-testkey"

  Scenario: Cleanup: delete activation key
    When I delete the activation key "1-testkey"
