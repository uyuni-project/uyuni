# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test configuration channel basic functions
  Create config channel.
  Subscribe system to channel, deploy some files

   Scenario: Successfully create configuration channel
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Configuration Channels" in the left menu
    When I follow "Create Config Channel"
    And I enter "Test Channel" as "cofName"
    And I enter "testchannel" as "cofLabel"
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

  Scenario: Try to create same channel again; this should fail
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Configuration Channels" in the left menu
    When I follow "Create Config Channel"
    And I enter "Test Channel" as "cofName"
    And I enter "testchannel" as "cofLabel"
    And I enter "This is a test channel" as "cofDescription"
    And I click on "Create Config Channel"
    Then I should see a "Label 'testchannel' already exists. Please choose a different label for the new channel." text
    And I should see a "Update Channel" button

  Scenario: Try to create a channel with an invalid label
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Configuration Channels" in the left menu
    When I follow "Create Config Channel"
    And I enter "Test Channel2" as "cofName"
    And I enter "!testchannel" as "cofLabel"
    And I enter "This is a test channel 2" as "cofDescription"
    And I click on "Create Config Channel"
    Then I should see a "Configuration channel label contains invalid characters. In addition to alphanumeric characters, '-', '_', and '.' are allowed." text
    And I should see a "Update Channel" button

  Scenario: Successfully create newtestchannel configuration channel
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Configuration Channels" in the left menu
    When I follow "Create Config Channel"
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
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Configuration Channels" in the left menu
    When I follow "New Test Channel"
    And I follow "Create configuration file or directory"
    And I enter "/etc/mgr-test-file.cnf" as "cffPath"
    And I enter "MGR_PROXY=yes" in the editor
    And I click on "Create Configuration File"
    Then I should see a "Revision 1 of /etc/mgr-test-file.cnf from channel New Test Channel" text
    And I should see a "Update Configuration File" button

  Scenario: Subscribe system to channel "new test challen"
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    When I follow "Systems" in the left menu
    And I follow "Overview" in the left menu 
    And I follow this "sle-client" link
    When I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    And I follow first "Subscribe to Channels" in the content area
    And I check "New Test Channel" in the list
    And I click on "Continue"
    And I click on "Update Channel Rankings"
    Then I should see a "Channel Subscriptions successfully changed for" text

  Scenario: Check Centrally Managed Files
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    When I follow "Configuration Files" in the left menu
    And I follow "Centrally Managed Files" in the left menu
    Then I should see a table line with "/etc/mgr-test-file.cnf", "New Test Channel", "1 system"

  Scenario: Check Centrally Managed Files of "sle-client"
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    When I follow "Systems" in the left menu
    And I follow "Overview" in the left menu 
    And I follow this "sle-client" link
    When I follow "Configuration" in the content area
    And I follow "View/Modify Files" in the content area
    And I follow "Centrally-Managed Files" in the content area
    Then I should see a table line with "/etc/mgr-test-file.cnf", "New Test Channel", "Revision 1"

  Scenario: Deploy Centrally Managed Files
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I run "rhn-actions-control --enable-all" on "sle-client"
    And I follow "Configuration Channels" in the left menu
    And I follow "New Test Channel"
    And I follow "Deploy all configuration files to all subscribed systems"
    Then I should see a "/etc/mgr-test-file.cnf" link
    And I should see "sle-client" as link
    When I click on "Deploy Files to Selected Systems"
    Then I should see a "1 revision-deploy is being scheduled." text
    And I should see a "0 revision-deploys overridden." text

  Scenario: Check File deployment
    When I run rhn_check on this client
    Then On this client the File "/etc/mgr-test-file.cnf" should exists
    And On this client the File "/etc/mgr-test-file.cnf" should have the content "MGR_PROXY=yes"

  Scenario: Change local file and compare
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    When I follow "Systems" in the left menu
    And I follow "Overview" in the left menu 
    And I follow this "sle-client" link
    And I change the local file "/etc/mgr-test-file.cnf" to "MGR_PROXY=no"
    When I follow "Configuration" in the content area
    And I follow "Compare Files" in the content area
    And I check "/etc/mgr-test-file.cnf" in the list
    And I click on "Compare Files"
    And I click on "Schedule Compare"
    Then I should see a "1 files scheduled for comparison." text
    When I run rhn_check on this client
    And I follow "Events" in the content area
    And I follow "History" in the content area
    Then I should see a "Show differences between profiled config files and deployed config files scheduled by admin" link
    When I follow first "Show differences between profiled config files and deployed config files"
    Then I should see a "Differences exist" link
    When I follow "Differences exist"
    Then I should not see a "Differences exist in a file that is not readable by all. Re-deployment of configuration file is recommended." text
    And I should see a "+MGR_PROXY=no" text

  Scenario: Import the changed file mgr-test-file.cnf
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    When I follow "Systems" in the left menu
    And I follow "Overview" in the left menu 
    And I follow this "sle-client" link
    When I follow "Configuration" in the content area
    And I follow "Add Files" in the content area
    And I follow "Import Files" in the content area
    And I check "/etc/mgr-test-file.cnf" in the list
    And I click on "Import Configuration Files"
    And I click on "Confirm"
    Then I should see a "1 files scheduled for upload." text
    When I run rhn_check on this client
    And I follow "Configuration" in the content area
    And I follow "View/Modify Files" in the content area
    And I follow "Local Sandbox" in the content area
    Then I should see a table line with "/etc/mgr-test-file.cnf", "Revision 1"

 Scenario: Import the changed file sysconfig cron
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    When I follow "Systems" in the left menu
    And I follow "Overview" in the left menu 
    And I follow this "sle-client" link
    When I follow "Configuration" in the content area
    And I follow "Add Files" in the content area
    And I follow "Import Files" in the content area
    And I enter "/etc/sysconfig/cron" as "contents"
    And I click on "Import Configuration Files"
    And I click on "Confirm"
    Then I should see a "1 files scheduled for upload." text
    When I run rhn_check on this client
    And I follow "Configuration" in the content area
    And I follow "View/Modify Files" in the content area
    And I follow "Local Sandbox" in the content area
    Then I should see a table line with "/etc/sysconfig/cron", "Revision 1"

    Scenario: Copy Sandbox file to Centrally-Managed
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    When I follow "Systems" in the left menu
    And I follow "Overview" in the left menu 
    And I follow this "sle-client" link
    When I follow "Configuration" in the content area
    And I follow "View/Modify Files" in the content area
    And I follow "Local Sandbox" in the content area
    And I check "/etc/mgr-test-file.cnf" in the list
    And I click on "Copy Latest to Centrally-Managed Files"
    And I check "New Test Channel" in the list
    And I click on "Copy To Central Channels"
    Then I should see a "1 file copied into 1 central configuration channel" text
    And I should see a table line with "/etc/mgr-test-file.cnf", "Revision 2"
    And I follow "Local Sandbox" in the content area
    And I check "/etc/sysconfig/cron" in the list
    And I click on "Copy Latest to Centrally-Managed Files"
    And I check "New Test Channel" in the list
    And I click on "Copy To Central Channels"
    Then I should see a "1 file copied into 1 central configuration channel" text
    And I should see a table line with "/etc/sysconfig/cron", "Revision 1"

  Scenario: Add another config file to newtestchannel
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    When I follow "Configuration Channels" in the left menu
    And I follow "New Test Channel"
    And I follow "Create configuration file or directory"
    And I enter "/tmp/mycache.txt" as "cffPath"
    And I enter "cache" in the editor
    And I click on "Create Configuration File"
    Then I should see a "Revision 1 of /tmp/mycache.txt from channel New Test Channel" text
    And I should see a "Update Configuration File" button

  Scenario: Change one local file and compare multiple (bsc#910243, bsc#910247)
    Given I change the local file "/etc/mgr-test-file.cnf" to "MGR_PROXY=yes"
    And I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    When I follow "Systems" in the left menu
    And I follow "Overview" in the left menu 
    And I follow this "sle-client" link
    When I follow "Configuration" in the content area
    And I follow "Compare Files" in the content area
    And I check "/etc/mgr-test-file.cnf" in the list
    And I check "/etc/sysconfig/cron" in the list
    And I check "/tmp/mycache.txt" in the list
    And I click on "Compare Files"
    And I click on "Schedule Compare"
    Then I should see a "3 files scheduled for comparison." text
    When I run rhn_check on this client
    And I follow "Events" in the content area
    And I follow "History" in the content area
    Then I should see a "Show differences between profiled config files and deployed config files scheduled by admin" link
    When I follow first "Show differences between profiled config files and deployed config files"
    Then I should see a "Differences exist" link
    And I should see a "/etc/mgr-test-file.cnf (rev. 2) Differences exist /etc/sysconfig/cron (rev. 1) /tmp/mycache.txt (rev. 1) Missing file" text
    When I follow "Differences exist"
    Then I should not see a "Differences exist in a file that is not readable by all. Re-deployment of configuration file is recommended." text
    And I should see a "+MGR_PROXY=yes" text

  Scenario: Check configuration page content
   Given I am authorized as "admin" with password "admin"
   And I follow "Home" in the left menu
   And I follow "Configuration" in the left menu
   And I follow "Overview" in the left menu
   Then I should see a "Configuration Overview" text
    And I should see a "Configuration Summary" text
    And I should see a "Configuration Actions" text
    And I should see a "Systems with Managed Configuration Files" text
    And I should see a "Configuration Channels" text
    And I should see a "Centrally-managed Configuration Files" text
    And I should see a "Locally-managed Configuration Files" text
    And I should see a "Overview" link in the left menu
    And I should see a "Configuration Channels" link in the left menu
    And I should see a "Configuration Files" link in the left menu
    And I should see a "Systems" link in the left menu
    And I should see a "View Systems with Managed Configuration Files" link
    And I should see a "View All Managed Configuration Files" link
    And I should see a "View All Managed Configuration Channels" link
    And I should see a "Create a New Configuration Channel" link
    And I should see a "Enable Configuration Management on Systems" link

   Scenario: Check "View Systems with Managed Configuration Files"
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Overview" in the left menu
    When I follow "View Systems with Managed Configuration Files"
    Then I should see a "Managed Systems" link in the left menu
    And I should see a "Target Systems" link in the left menu
 
   Scenario: Check "View All Managed Configuration Files"
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Overview" in the left menu
    When I follow "View All Managed Configuration Files"
    Then I should see a "Centrally Managed Files" link in the left menu
    And I should see a "Locally Managed Files" link in the left menu
 
   Scenario: Check "View All Managed Configuration Channels"
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Overview" in the left menu
    When I follow "View All Managed Configuration Channels"
    Then I should see a "Create Config Channel" link
 
   Scenario: Check "Enable Configuration Management on Systems"
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Overview" in the left menu
    When I follow "Enable Configuration Management on Systems"
    Then I should see a "Managed Systems" link in the left menu
    And I should see a "Target Systems" link in the left menu

  Scenario: Remove system from conf channel "new test channel"
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    When I follow "Configuration" in the left menu
    And I follow "Configuration Channels" in the left menu
    And I follow "New Test Channel"
    And I follow "Systems" in the content area
    And I check this client
    And I click on "Unsubscribe systems"
    Then I should see a "Successfully unsubscribed 1 system(s)." text

  Scenario: CLEAN_UP: remove configuration channel: Test Channel
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Configuration Channels" in the left menu
    And I follow "Test Channel"
    And I follow "delete channel"
    And I click on "Delete Config Channel"
 
 Scenario: CLEAN_UP: remove configuration channel: New Test Channel
    Given I am authorized as "admin" with password "admin"
    And I follow "Home" in the left menu
    And I follow "Configuration" in the left menu
    And I follow "Configuration Channels" in the left menu
    And I follow "New Test Channel"
    And I follow "delete channel"
    And I click on "Delete Config Channel"
