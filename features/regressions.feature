# Copyright (c) 2013 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Test bug 821968
  In Order to validate the package summary of a package in the metadata
  As an authorized user
  I want to see the summary do not contain a \n at the end

  Background:
    Given I am testing channels

    Scenario: Check local metdata not contain \n at the end of the summary
    Given I am root
     When I refresh the metadata
     Then I should have 'summary.*</summary' in the metadata

@wip
Feature: Test bug 701082 and 701082
    In Order distribute software to the clients
    As an authorized user
    I want to add channels

    Background:
      Given I am testing channels

    Scenario: Adding a base channel starting with rhn
      And I follow "Channels"
      And I follow "Manage Software Channels" in the left menu
      And I follow "create new channel"
     When I enter "rhn channel name" as "Channel Name"
      and I enter "rhn_channel_name" as "Channel Label"
      and I select "None" from "Parent Channel"
      and I select "x86_64" from "Architecture"
      and I enter "rhn channel name" as "Channel Summary"
      and I enter "rhn channel name" as "Channel Description"
      And I click on "Create channel"
     Then I should see "rhn channel name created." text

    Scenario: Deleting a software channel
      And I follow "Channels"
      And I follow "Manage software Channels" in the left menu
      And I follow "rhn channel name"
      And I click on "delete software channel"
      And I click on "Delete Channel"
     Then I should see "Channel rhn channel name has been deleted" text

