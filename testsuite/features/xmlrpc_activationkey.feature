# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test XML-RPC "api" namespace.

  Scenario: List all Activation Keys
    Given I am logged in via XML-RPC/activationkey as user "admin" and password "admin"
    When I call listActivationKeys I should get some.

  Scenario: Activation key
    Given I am logged in via XML-RPC/activationkey as user "admin" and password "admin"
    When I create an AK with id "testkey", description "Key for testing" and limit of 10
    Then I should get it listed with a call of listActivationKeys.

  Scenario: Details
    Given I am logged in via XML-RPC/activationkey as user "admin" and password "admin"
    When I call activationkey.setDetails() to the key
    Then I have to see them by calling activationkey.getDetails()

  Scenario: Cleanup
    Then I should get key deleted.
