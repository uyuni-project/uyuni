# Copyright (c) 2017-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_traditional_client
@scope_configuration_channels
Feature: Configuration management of traditional clients

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Successfully create configuration channel
    When I follow the left menu "Configuration > Channels"
    And I follow "Create Config Channel"
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
    And I should see a "Create Configuration File or Directory" link
    And I should see a "Upload Configuration Files" link
    And I should see a "Import a File from Another Channel or System" link
    And I should see a "Delete Channel" link

  Scenario: Try to create same channel again; this should fail
    When I follow the left menu "Configuration > Channels"
    And I follow "Create Config Channel"
    And I enter "Test Channel" as "cofName"
    And I enter "testchannel" as "cofLabel"
    And I enter "This is a test channel" as "cofDescription"
    And I click on "Create Config Channel"
    Then I should see a "Label 'testchannel' already exists. Please choose a different label for the new channel." text
    And I should see a "Update Channel" button

  Scenario: Try to create a channel with an invalid label
    When I follow the left menu "Configuration > Channels"
    And I follow "Create Config Channel"
    And I enter "Test Channel2" as "cofName"
    And I enter "!testchannel" as "cofLabel"
    And I enter "This is a test channel 2" as "cofDescription"
    And I click on "Create Config Channel"
    Then I should see a "Configuration channel label contains invalid characters. In addition to alphanumeric characters, '-' and '_' are allowed." text
    And I should see a "Update Channel" button

  Scenario: Successfully create a new configuration channel
    When I follow the left menu "Configuration > Channels"
    And I follow "Create Config Channel"
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
    And I should see a "Create Configuration File or Directory" link
    And I should see a "Upload Configuration Files" link
    And I should see a "Import a File from Another Channel or System" link
    And I should see a "Delete Channel" link

  Scenario: Add a configuration file to new configuration channel
    When I follow the left menu "Configuration > Channels"
    And I follow "New Test Channel"
    And I follow "Create Configuration File or Directory"
    And I enter "/etc/mgr-test-file.cnf" as "cffPath"
    And I enter "MGR_PROXY=yes" in the editor
    And I click on "Create Configuration File"
    Then I should see a "Revision 1 of /etc/mgr-test-file.cnf from channel New Test Channel" text
    And I should see a "Update Configuration File" button

  Scenario: Subscribe a system to new configuration channel
    When I am on the Systems overview page of this "sle_client"
    And I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    And I follow first "Subscribe to Channels" in the content area
    And I check "New Test Channel" in the list
    And I click on "Continue"
    And I click on "Update Channel Rankings"
    Then I should see a "Channel Subscriptions successfully changed for" text

  Scenario: Check centrally managed files
    When I follow the left menu "Configuration > Files > Centrally Managed"
    Then I should see a table line with "/etc/mgr-test-file.cnf", "New Test Channel", "1 system"

  Scenario: Check centrally managed files of SLES client
    When I am on the Systems overview page of this "sle_client"
    And I follow "Configuration" in the content area
    And I follow "View/Modify Files" in the content area
    And I follow "Centrally-Managed Files" in the content area
    Then I should see a table line with "/etc/mgr-test-file.cnf", "New Test Channel", "Revision 1"

  Scenario: Deploy centrally managed files
    When I run "rhn-actions-control --enable-all" on "sle_client"
    And I follow the left menu "Configuration > Channels"
    And I follow "New Test Channel"
    And I follow "Deploy all configuration files to all subscribed systems"
    Then I should see a "/etc/mgr-test-file.cnf" link
    And I should see "sle_client" as link
    When I click on "Deploy Files to Selected Systems"
    Then I should see a "1 revision-deploy is being scheduled." text
    And I should see a "0 revision-deploys overridden." text

  Scenario: Check file deployment
    When I run "rhn_check -vvv" on "sle_client"
    Then file "/etc/mgr-test-file.cnf" should exist on "sle_client"
    And file "/etc/mgr-test-file.cnf" should contain "MGR_PROXY=yes" on "sle_client"

  Scenario: Change file on traditional client and compare
    When I am on the Systems overview page of this "sle_client"
    And I store "MGR_PROXY=no" into file "/etc/mgr-test-file.cnf" on "sle_client"
    And I follow "Configuration" in the content area
    And I follow "Compare Files" in the content area
    And I check "/etc/mgr-test-file.cnf" in the list
    And I click on "Compare Files"
    And I click on "Schedule Compare"
    Then I should see a "1 files scheduled for comparison." text
    When I run "rhn_check -vvv" on "sle_client"
    And I wait until event "Show differences between profiled config files and deployed config files scheduled by admin" is completed
    Then I should see a "Differences exist" link
    When I follow "Differences exist"
    Then I should not see a "Differences exist in a file that is not readable by all. Re-deployment of configuration file is recommended." text
    And I should see a "-MGR_PROXY=yes" text
    And I should see a "+MGR_PROXY=no" text

  Scenario: Import the changed test configuration file
    When I am on the Systems overview page of this "sle_client"
    And I follow "Configuration" in the content area
    And I follow "Add Files" in the content area
    And I follow "Import Files" in the content area
    And I check "/etc/mgr-test-file.cnf" in the list
    And I click on "Import Configuration Files"
    And I click on "Confirm"
    Then I should see a "1 files scheduled for upload." text
    When I run "rhn_check -vvv" on "sle_client"
    And I follow "Configuration" in the content area
    And I follow "View/Modify Files" in the content area
    And I follow "Local Sandbox" in the content area
    Then I should see a table line with "/etc/mgr-test-file.cnf", "Revision 1"

  Scenario: Import an existing language configuration file
    When I am on the Systems overview page of this "sle_client"
    And I follow "Configuration" in the content area
    And I follow "Add Files" in the content area
    And I follow "Import Files" in the content area
    And I enter "/etc/sysconfig/language" as "contents"
    And I click on "Import Configuration Files"
    And I click on "Confirm"
    Then I should see a "1 files scheduled for upload." text
    When I run "rhn_check -vvv" on "sle_client"
    And I follow "Configuration" in the content area
    And I follow "View/Modify Files" in the content area
    And I follow "Local Sandbox" in the content area
    Then I should see a table line with "/etc/sysconfig/language", "Revision 1"

  Scenario: Copy sandbox file to centrally managed
    When I am on the Systems overview page of this "sle_client"
    And I follow "Configuration" in the content area
    And I follow "View/Modify Files" in the content area
    And I follow "Local Sandbox" in the content area
    And I check "/etc/mgr-test-file.cnf" in the list
    And I click on "Copy Latest to Centrally-Managed Files"
    And I check "New Test Channel" in the list
    And I click on "Copy To Central Channels"
    Then I should see a "1 file copied into 1 central configuration channel" text
    And I should see a table line with "/etc/mgr-test-file.cnf", "Revision 2"
    When I follow "Local Sandbox" in the content area
    And I check "/etc/sysconfig/language" in the list
    And I click on "Copy Latest to Centrally-Managed Files"
    And I check "New Test Channel" in the list
    And I click on "Copy To Central Channels"
    Then I should see a "1 file copied into 1 central configuration channel" text
    And I should see a table line with "/etc/sysconfig/language", "Revision 1"

  Scenario: Add another configure file to new test channel
    When I follow the left menu "Configuration > Channels"
    And I follow "New Test Channel"
    And I follow "Create Configuration File or Directory"
    And I enter "/tmp/mycache.txt" as "cffPath"
    And I enter "cache" in the editor
    And I click on "Create Configuration File"
    Then I should see a "Revision 1 of /tmp/mycache.txt from channel New Test Channel" text
    And I should see a "Update Configuration File" button

  Scenario: Change one local file and compare multiple files
    # bsc#910243 - configfile.compare: Filelist in Eventhistory is not sorted alphabetically
    # bsc#910247 - configfile.compare task shows different result in Web-UI than 'rhncfg-client verify -o'
    When I am on the Systems overview page of this "sle_client"
    And I follow "Configuration" in the content area
    And I store "MGR_PROXY=yes" into file "/etc/mgr-test-file.cnf" on "sle_client"
    And I follow "Compare Files" in the content area
    And I check "/etc/mgr-test-file.cnf" in the list
    And I check "/etc/sysconfig/language" in the list
    And I check "/tmp/mycache.txt" in the list
    And I click on "Compare Files"
    And I click on "Schedule Compare"
    Then I should see a "3 files scheduled for comparison." text
    When I run "rhn_check -vvv" on "sle_client"
    And I wait until event "Show differences between profiled config files and deployed config files scheduled by admin" is completed
    Then I should see a "Differences exist" link
    And I should see a "/etc/mgr-test-file.cnf (rev. 2) Differences exist" text
    And I should see a "/etc/sysconfig/language (rev. 1)" text
    And I should see a "/tmp/mycache.txt (rev. 1) Missing file" text
    When I follow "Differences exist"
    Then I should not see a "Differences exist in a file that is not readable by all. Re-deployment of configuration file is recommended." text
    And I should see a "+MGR_PROXY=yes" text

  Scenario: Check configuration page content
    When I follow the left menu "Configuration > Overview"
    Then I should see a "Configuration Overview" text
    And I should see a "Configuration Summary" text
    And I should see a "Configuration Actions" text
    And I should see a "Systems with Managed Configuration Files" text
    And I should see a "Configuration Channels" text
    And I should see a "Centrally-managed Configuration Files" text
    And I should see a "Locally-managed Configuration Files" text
    And I should see a "Overview" link in the left menu
    And I should see a "Channels" link in the left menu
    And I should see a "Files" link in the left menu
    And I should see a "Systems" link in the left menu
    And I should see a "View Systems with Managed Configuration Files" link
    And I should see a "View All Managed Configuration Files" link
    And I should see a "View All Managed Configuration Channels" link
    And I should see a "Create a New Configuration Channel" link
    And I should see a "Enable Configuration Management on Systems" link

  Scenario: Show Systems with Managed Configuration Files page
    When I follow the left menu "Configuration > Overview"
    And I follow "View Systems with Managed Configuration Files"
    Then I should see a "Managed" link in the left menu
    And I should see a "Target" link in the left menu

  Scenario: Show All Managed Configuration Files page
    When I follow the left menu "Configuration > Overview"
    And I follow "View All Managed Configuration Files"
    Then I should see a "Centrally Managed" link in the left menu
    And I should see a "Locally Managed" link in the left menu

  Scenario: Show All Managed Configuration Channels page
    When I follow the left menu "Configuration > Overview"
    And I follow "View All Managed Configuration Channels"
    Then I should see a "Create Config Channel" link

  Scenario: Show Enable Configuration Management on Systems page
    When I follow the left menu "Configuration > Overview"
    And I follow "Enable Configuration Management on Systems"
    Then I should see a "Managed" link in the left menu
    And I should see a "Target" link in the left menu

  Scenario: Cleanup: remove system from new configuration channel
    When I follow the left menu "Configuration > Channels"
    And I follow "New Test Channel"
    And I follow "Systems" in the content area
    And I check the "sle_client" client
    And I click on "Unsubscribe systems"
    Then I should see a "Successfully unsubscribed 1 system(s)." text

  Scenario: Cleanup: remove test configuration channel
    When I follow the left menu "Configuration > Channels"
    And I follow "Test Channel"
    And I follow "Delete Channel"
    And I click on "Delete Config Channel"

  Scenario: Cleanup: remove new configuration channel
    When I follow the left menu "Configuration > Channels"
    And I follow "New Test Channel"
    And I follow "Delete Channel"
    And I click on "Delete Config Channel"

  Scenario: Cleanup: delete configuration file on client
    When I remove "/etc/mgr-test-file.cnf" from "sle_client"

  Scenario: Cleanup: remove remaining systems from SSM after tests of configuration channel on traditional client
    And I follow "Clear"
