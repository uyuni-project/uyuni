# Copyright (c) 2015 SUSE LLC
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
    And I should see a "Systems" link in the content area
    And I should see a "Edit Properties" link
    And I should see a "Configuration Actions" text
    And I should see a "Add/Create Files" text
    And I should see a "Create configuration file or directory" link
    And I should see a "Upload configuration files" link
    And I should see a "Import a file from another channel or system" link
    And I should see a "delete channel" link

  Scenario: Add a config file to newtestchannel
    Given I am testing configuration
    When I follow "Configuration Channels" in the left menu
    And I follow "New Test Channel"
    And I follow "Create configuration file or directory"
    And I enter "/etc/mgr-test-file.cnf" as "cffPath"
    And I enter "MGR_PROXY=yes" in the editor
    And I click on "Create Configuration File"
    Then I should see a "Revision 1 of /etc/mgr-test-file.cnf from channel New Test Channel" text
    And I should see a "Update Configuration File" button

  Scenario: Subscribe system to channel
    Given I am on the Systems overview page of this client
    When I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    And I follow first "Subscribe to Channels" in the content area
    And I check "New Test Channel" in the list
    And I click on "Continue"
    And I click on "Update Channel Rankings"
    Then I should see a "Channel Subscriptions successfully changed for" text

  Scenario: Check Centrally Managed Files
    Given I am testing configuration
    When I follow "Configuration Files" in the left menu
    Then I should see a table line with "/etc/mgr-test-file.cnf", "New Test Channel", "1 system"

  Scenario: Check Centrally Managed Files of this client
    Given I am on the Systems overview page of this client
    When I follow "Configuration" in the content area
    And I follow "View/Modify Files" in the content area
    And I follow "Centrally-Managed Files" in the content area
    Then I should see a table line with "/etc/mgr-test-file.cnf", "New Test Channel", "Revision 1"

  Scenario: Deploy Centrally Managed Files
    Given I am testing configuration
    And I enable all actions
    And I follow "Configuration Channels" in the left menu
    And I follow "New Test Channel"
    And I follow "Deploy all configuration files to all subscribed systems"
    Then I should see a "/etc/mgr-test-file.cnf" link
    And I should see this client as link
    When I click on "Deploy Files to Selected Systems"
    Then I should see a "1 revision-deploy is being scheduled." text
    And I should see a "0 revision-deploys overridden." text

  Scenario: Check File deployment
    Given I am root
    When I run rhn_check on this client
    Then On this client the File "/etc/mgr-test-file.cnf" should exists
    And On this client the File "/etc/mgr-test-file.cnf" should have the content "MGR_PROXY=yes"

  Scenario: Change local file and compare
    Given I am root
    When I change the local file "/etc/mgr-test-file.cnf" to "MGR_PROXY=no"
    Given I am on the Systems overview page of this client
    When I follow "Configuration" in the content area
    And I follow "Compare Files" in the content area
    And I check "/etc/mgr-test-file.cnf" in the list
    And I click on "Compare Files"
    And I click on "Schedule Compare"
    Then I should see a "1 files scheduled for comparison." text
    When I run rhn_check on this client
    #And I wait for "2" seconds
    And I follow "Events" in the content area
    And I follow "History" in the content area
    Then I should see a "Show differences between profiled config files and deployed config files scheduled by testing" link
    When I follow first "Show differences between profiled config files and deployed config files"
    Then I should see a "Differences exist" link
    When I follow "Differences exist"
    Then I should not see a "Differences exist in a file that is not readable by all. Re-deployment of configuration file is recommended." text
    And I should see a "+MGR_PROXY=no" text

  Scenario: Import the changed file
    Given I am on the Systems overview page of this client
    When I follow "Configuration" in the content area
    And I follow "Add Files" in the content area
    And I follow "Import Files" in the content area
    And I check "/etc/mgr-test-file.cnf" in the list
    And I click on "Import Configuration Files"
    And I click on "Confirm"
    Then I should see a "1 files scheduled for upload." text
    When I run rhn_check on this client
    #And I wait for "2" seconds
    And I follow "Configuration" in the content area
    And I follow "View/Modify Files" in the content area
    And I follow "Local Sandbox" in the content area
    Then I should see a table line with "/etc/mgr-test-file.cnf", "Revision 1"

  Scenario: Import the changed file
    Given I am on the Systems overview page of this client
    When I follow "Configuration" in the content area
    And I follow "Add Files" in the content area
    And I follow "Import Files" in the content area
    And I enter "/etc/sysconfig/cron" as "contents"
    And I click on "Import Configuration Files"
    And I click on "Confirm"
    Then I should see a "1 files scheduled for upload." text
    When I run rhn_check on this client
    #And I wait for "2" seconds
    And I follow "Configuration" in the content area
    And I follow "View/Modify Files" in the content area
    And I follow "Local Sandbox" in the content area
    Then I should see a table line with "/etc/sysconfig/cron", "Revision 1"

  Scenario: Copy Sandbox file to Centrally-Managed
    Given I am on the Systems overview page of this client
    When I follow "Configuration" in the content area
    And I follow "View/Modify Files" in the content area
    And I follow "Local Sandbox" in the content area
    And I check "/etc/mgr-test-file.cnf" in the list
    And I click on "Copy Latest to Central Channel"
    And I check "New Test Channel" in the list
    And I click on "Copy To Central Channels"
    Then I should see a "1 file copied into 1 central configuration channel" text
    And I should see a table line with "/etc/mgr-test-file.cnf", "Revision 2"
    And I follow "Local Sandbox" in the content area
    And I check "/etc/sysconfig/cron" in the list
    And I click on "Copy Latest to Central Channel"
    And I check "New Test Channel" in the list
    And I click on "Copy To Central Channels"
    Then I should see a "1 file copied into 1 central configuration channel" text
    And I should see a table line with "/etc/sysconfig/cron", "Revision 1"

  Scenario: Add another config file to newtestchannel
    Given I am testing configuration
    When I follow "Configuration Channels" in the left menu
    And I follow "New Test Channel"
    And I follow "Create configuration file or directory"
    And I enter "/tmp/mycache.txt" as "cffPath"
    And I enter "cache" in the editor
    And I click on "Create Configuration File"
    Then I should see a "Revision 1 of /tmp/mycache.txt from channel New Test Channel" text
    And I should see a "Update Configuration File" button

  Scenario: Change one local file and compare multiple (bsc#910243, bsc#910247)
    Given I am root
    When I change the local file "/etc/mgr-test-file.cnf" to "MGR_PROXY=yes"
    Given I am on the Systems overview page of this client
    When I follow "Configuration" in the content area
    And I follow "Compare Files" in the content area
    And I check "/etc/mgr-test-file.cnf" in the list
    And I check "/etc/sysconfig/cron" in the list
    And I check "/tmp/mycache.txt" in the list
    And I click on "Compare Files"
    And I click on "Schedule Compare"
    Then I should see a "3 files scheduled for comparison." text
    When I run rhn_check on this client
    #And I wait for "2" seconds
    And I follow "Events" in the content area
    And I follow "History" in the content area
    Then I should see a "Show differences between profiled config files and deployed config files scheduled by testing" link
    When I follow first "Show differences between profiled config files and deployed config files"
    Then I should see a "Differences exist" link
    And I should see a "/etc/mgr-test-file.cnf (rev. 2) Differences exist /etc/sysconfig/cron (rev. 1) /tmp/mycache.txt (rev. 1) Missing file" text
    When I follow "Differences exist"
    Then I should not see a "Differences exist in a file that is not readable by all. Re-deployment of configuration file is recommended." text
    And I should see a "+MGR_PROXY=yes" text
