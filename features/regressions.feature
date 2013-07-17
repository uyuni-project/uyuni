# Copyright (c) 2013 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Regression tests 
  In Order to validate the package summary of a package in the metadata
  As an authorized user
  I want to see the summary do not contain a \n at the end

  Background:
    Given I am testing channels

    #bug 821968
    Scenario: Check local metdata not contain \n at the end of the summary
    Given I am root
     When I refresh the metadata
     Then I should have 'summary.*</summary' in the metadata
    
    #bug 701082
    Scenario: Adding a base channel starting with rhn
      And I follow "Channels"
      And I follow "Manage Software Channels" in the left menu
      And I follow "create new channel"
     When I enter "rhn channel name" as "Channel Name"
      And I enter "rhn_channel_name" as "Channel Label"
      And I select "None" from "Parent Channel"
      And I select "x86_64" from "Architecture"
      And I enter "rhn channel name" as "Channel Summary"
      And I enter "rhn channel name" as "Channel Description"
      And I click on "Create channel"
     Then I should see "rhn channel name created." text

    #bug 728894
    Scenario: Deleting a software channel
      And I follow "Channels"
      And I follow "Manage software Channels" in the left menu
      And I follow "rhn channel name"
      And I click on "delete software channel"
      And I click on "Delete Channel"
     Then I should see "Channel rhn channel name has been deleted" text
