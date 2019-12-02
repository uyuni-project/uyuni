# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: XML-RPC "activationkey" namespace

  Scenario: List all activation keys
    Given I am logged in via XML-RPC activationkey with the feature's user
    When I call listActivationKeys I should get some

  Scenario: Create activation key
    Given I am logged in via XML-RPC activationkey with the feature's user
    When I create an AK with id "testkey", description "Key for testing" and limit of 10
    Then I should get it listed with a call of listActivationKeys

  Scenario: Activation key details
    Given I am logged in via XML-RPC activationkey with the feature's user
    When I call activationkey.set_details() to the key
    Then I have to see them by calling activationkey.get_details()

  Scenario: Cleanup: delete activation key
    Then I should get key deleted
