# Copyright (c) 2011 SUSE Linux Products GmbH
# Licensed under the terms of the MIT license.

Feature: Test XML-RPC "channel" namespace und sub-namespaces.

  Scenario: Create a software channel
    Given I am logged in via XML-RPC/channel as user "admin" and password "admin"
    When I create a channel with label "foobar", name "foobar", summary "foobar", arch "channel-x86_64" and parent ""
    Then "foobar" should get listed with a call of listSoftwareChannels

  Scenario: List software channels
    Given I am logged in via XML-RPC/channel as user "admin" and password "admin"
    Then something should get listed with a call of listSoftwareChannels

  Scenario: Delete a software channel
    Given I am logged in via XML-RPC/channel as user "admin" and password "admin"
    When I delete the software channel with label "foobar"
    Then "foobar" should not get listed with a call of listSoftwareChannels

