# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Create a configuration channel
  In Configuration
  As the admin user
  I want to create a configuration channel

  Scenario: Successfully create configuration channel
    Given I am testing configuration
    When I follow "Create a New Configuration Channel"
     And I enter "New Test Channel" as "cofName"
     And I enter "newtestchannel" as "cofLabel"
     And I enter "This is a test channel" as "cofDescription"
     And I click on "Create Config Channel"
    Then I should see a "Test Channel" text
     And I should see a "Add Files" link
     And I should see a "Systems" link
     And I should see a "Edit Properties" link
     And I should see a "Configuration Actions" text
     And I should see a "Add/Create Files" text
     And I should see a "Create new configuration file or directory" link
     And I should see a "Upload configuration files" link
     And I should see a "Import a file from another channel or system" link
     And I should see a "delete channel" link

  Scenario: Add a config file to newtestchannel
    Given I am testing configuration
    When I follow "Configuration Channels" in the left menu
     And I follow "New Test Channel"
     And I follow "Create new configuration file or directory"
     And I enter "/etc/mgr-test-file.cnf" as "cffPath"
     And I enter "MGR_PROXY='yes'" as "contents"
     And I click on "Create Configuration File"
    Then I should see a "Revision 1 of /etc/mgr-test-file.cnf from channel New Test Channel" text
     And I should see a "Update Configuration File" button

  Scenario: Subscribe system to channel
   Given I am on the Systems overview page of this client
    When I follow "Configuration" in class "content-nav"
     And I follow "Manage Configuration Channels" in class "contentnav-row2"
     And I follow "Subscribe to Channels" in class "content-nav"
     And I check "New Test Channel" in the list
     And I click on "Continue"
     And I click on "Update Channel Rankings"
    Then I should see a "Channel Subscriptions successfully changed for" text

  Scenario: Check Centrally Managed Files
    Given I am testing configuration
    When I follow "Configuration Files" in the left menu
    Then I should see a table line with "/etc/mgr-test-file.cnf", "New Test Channel", "1 system"



