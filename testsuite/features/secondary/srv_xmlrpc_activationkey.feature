# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_xmlrpc
Feature: XML-RPC "activationkey" namespace

  Background:
    Given I am logged in via XML-RPC activationkey as user "admin" and password "admin"

  Scenario: List all activation keys
    When I call listActivationKeys I should get some

  Scenario: Create activation key
    When I create an AK with id "testkey", description "Key for testing" and limit of 10
    Then I should get it listed with a call of listActivationKeys

  Scenario: Activation key details
    When I call activationkey.set_details() to the key setting as description "Key description"
    Then I have to see them by calling activationkey.get_details() having as description "Key description"

  Scenario: Cleanup: delete activation key
    Then I should get key deleted
