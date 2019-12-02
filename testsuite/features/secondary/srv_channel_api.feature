# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: XML-RPC "channel" namespace and sub-namespaces

  Scenario: Create a custom software channel
    Given I am logged in via XML-RPC channel with the feature's user
    When I create the following channels:
      | LABEL  | NAME   | SUMMARY | ARCH           | PARENT |
      | foobar | foobar | foobar  | channel-x86_64 |        |
    Then "foobar" should get listed with a call of listSoftwareChannels

  Scenario: Create a repository
    Given I am logged in via XML-RPC channel with the feature's user
    When I create a repo with label "foobar" and url
    And I associate repo "foobar" with channel "foobar"
    Then channel "foobar" should have attribute "last_modified" from type "XMLRPC::DateTime"
    And channel "foobar" should not have attribute "yumrepo_last_sync"

  Scenario: Create a custom software channel as the child of another one
    Given I am logged in via XML-RPC channel with the feature's user
    When I create the following channels:
      | LABEL        | NAME         | SUMMARY         | ARCH           | PARENT |
      | foobar-child | foobar-child | child of foobar | channel-x86_64 | foobar |
    Then "foobar-child" should get listed with a call of listSoftwareChannels
    And "foobar" should be the parent channel of "foobar-child"

  Scenario: List software channels
    Given I am logged in via XML-RPC channel with the feature's user
    Then something should get listed with a call of listSoftwareChannels

  Scenario: Delete a child software channel
    Given I am logged in via XML-RPC channel with the feature's user
    When I delete the software channel with label "foobar-child"
    Then "foobar-child" should not get listed with a call of listSoftwareChannels

  Scenario: Delete a software channel
    Given I am logged in via XML-RPC channel with the feature's user
    When I delete the repo with label "foobar"
    And I delete the software channel with label "foobar"
    Then "foobar" should not get listed with a call of listSoftwareChannels

  Scenario: Check last synchronization of a synced channel
    Given I am logged in via XML-RPC channel with the feature's user
    Then channel "test-channel-i586" should have attribute "yumrepo_last_sync" from type "XMLRPC::DateTime"
