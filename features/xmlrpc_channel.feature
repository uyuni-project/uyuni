# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

@xmlrpc
Feature: Test XML-RPC "channel" namespace und sub-namespaces.

  Scenario: Create a custom software channel
    Given I am logged in via XML-RPC/channel as user "admin" and password "admin"
    When I create the following channels:
      | LABEL  | NAME   | SUMMARY | ARCH           | PARENT |
      | foobar | foobar | foobar  | channel-x86_64 |        |
    Then "foobar" should get listed with a call of listSoftwareChannels

  Scenario: Create a repo
    Given I am logged in via XML-RPC/channel as user "admin" and password "admin"
    When I create a repo with label "foobar" and url
    And I associate repo "foobar" with channel "foobar"
    Then channel "foobar" should have attribute "last_modified" from type "XMLRPC::DateTime"
    And channel "foobar" should not have attribute "yumrepo_last_sync"

  Scenario: Create a custom software channel as the child of another one
    Given I am logged in via XML-RPC/channel as user "admin" and password "admin"
    When I create the following channels:
      | LABEL        | NAME         | SUMMARY         | ARCH           | PARENT |
      | foobar-child | foobar-child | child of foobar | channel-x86_64 | foobar |
    Then "foobar-child" should get listed with a call of listSoftwareChannels
    And "foobar" should be the parent channel of "foobar-child"

  Scenario: List software channels
    Given I am logged in via XML-RPC/channel as user "admin" and password "admin"
    Then something should get listed with a call of listSoftwareChannels

  Scenario: Delete a child software channel
    Given I am logged in via XML-RPC/channel as user "admin" and password "admin"
    When I delete the software channel with label "foobar-child"
    Then "foobar-child" should not get listed with a call of listSoftwareChannels

  Scenario: Delete a software channel
    Given I am logged in via XML-RPC/channel as user "admin" and password "admin"
    When I delete the repo with label "foobar"
    And I delete the software channel with label "foobar"
    Then "foobar" should not get listed with a call of listSoftwareChannels

  Scenario: check yumrepo_last_sync of a synced channel
    Given I am logged in via XML-RPC/channel as user "admin" and password "admin"
    Then channel "sles11-sp3-updates-i586-channel" should have attribute "yumrepo_last_sync" from type "XMLRPC::DateTime"
