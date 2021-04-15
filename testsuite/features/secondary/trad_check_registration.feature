# Copyright (c) 2015-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_traditional_client
Feature: Client display after registration

  Scenario: Show links in Details tab
    When I am on the Systems overview page of this "sle_client"
    Then I should see a "Details" link in the content area
    And I should see a "Software" link in the content area
    And I should see a "Configuration" link in the content area
    And I should see a "Provisioning" link in the content area
    And I should see a "Groups" link in the content area
    And I should see a "Events" link in the content area
    And I should see a "Overview" link in the content area
    And I should see a "Properties" link in the content area
    And I should see a "Remote Command" link in the content area
    And I should see a "Reactivation" link in the content area
    And I should see a "Hardware" link in the content area
    And I should see a "Migrate" link in the content area
    And I should see a "Notes" link in the content area
    And I should see a "Custom Info" link in the content area

  Scenario: Show links in Software tab
    When I am on the Systems overview page of this "sle_client"
    And I follow "Software" in the content area
    Then I should see a "Details" link in the content area
    And I should see a "Software" link in the content area
    And I should see a "Configuration" link in the content area
    And I should see a "Provisioning" link in the content area
    And I should see a "Groups" link in the content area
    And I should see a "Events" link in the content area
    And I should see a "Patches" link in the content area
    And I should see a "Packages" link in the content area
    And I should see a "Software Channels" link in the content area
    And I should see a "List / Remove" link
    And I should see a "Upgrade" link
    And I should see a "Install" link
    And I should see a "Verify" link
    And I should see a "Profiles" link
    And I should see a "List / Remove Installed Packages" link
    And I should see a "Verify Files and Packages" link
    And I should see a "Upgrade Packages" link
    And I should see a "Install New Packages" link
    And I should see a "Compare Package Profiles / Manage Package Profiles" link
    And I should see a "Update Package List" button

  Scenario: Show links in Configuration tab
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Configuration" in the content area
    Then I should see a "Details" link in the content area
    And I should see a "Software" link in the content area
    And I should see a "Configuration" link in the content area
    And I should see a "Provisioning" link in the content area
    And I should see a "Groups" link in the content area
    And I should see a "Events" link in the content area
    And I should see a "Overview" link in the content area
    And I should see a "View/Modify Files" link in the content area
    And I should see a "Add Files" link in the content area
    And I should see a "Deploy Files" link in the content area
    And I should see a "Compare Files" link in the content area
    And I should see a "Manage Configuration Channels" link in the content area
    And I should see a "Deploy all managed config files" link
    And I should see a "Deploy selected config files" link
    And I should see a "Compare all managed files to system" link
    And I should see a "Compare selected managed files to system" link
    And I should see a "Create a new config file or dir" link
    And I should see a "Upload config files" link
    And I should see a "Import all managed files from system" link
    And I should see a "Import selected files from system" link

  Scenario: Show links in Provisioning tab
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Provisioning" in the content area
    Then I should see a "Details" link in the content area
    And I should see a "Software" link in the content area
    And I should see a "Configuration" link in the content area
    And I should see a "Provisioning" link in the content area
    And I should see a "Groups" link in the content area
    And I should see a "Events" link in the content area
    And I should see a "Autoinstallation" link in the content area
    And I should see a "Snapshots" link in the content area
    And I should see a "Snapshot Tags" link in the content area
    And I should see a "Schedule" link in the content area

  Scenario: Show links in Groups tab
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Groups" in the content area
    Then I should see a "Details" link in the content area
    And I should see a "Software" link in the content area
    And I should see a "Configuration" link in the content area
    And I should see a "Provisioning" link in the content area
    And I should see a "Groups" link in the content area
    And I should see a "Events" link in the content area
    And I should see a "List / Leave" link in the content area
    And I should see a "Join" link in row 2 of the content menu
    And I should see a "Join" link in the text
      """
      No System Groups. To add System Groups this system, please visit the Join tab
      """
    And I should see a "System Groups" text

  Scenario: Show links in Events tab
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Events" in the content area
    Then I should see a "Details" link in the content area
    And I should see a "Software" link in the content area
    And I should see a "Configuration" link in the content area
    And I should see a "Provisioning" link in the content area
    And I should see a "Groups" link in the content area
    And I should see a "Events" link in the content area
    And I should see a "Pending" link in the content area
    And I should see a "History" link in the content area
    And I should see a "Pending Events" text

  Scenario: Show Details => Properties page
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Properties" in the content area
    Then I should see a "Edit System Details" text
    And I should see a "system_name" field in "systemDetailsForm" form
    And I should see a "Management" text
    And I should see a "receive_notifications" field in "systemDetailsForm" form
    And I should see a "summary" field in "systemDetailsForm" form
    And I should see a "autoerrataupdate" field in "systemDetailsForm" form
    And I should see a "description" field in "systemDetailsForm" form
    And I should see a "address" field in "systemDetailsForm" form
    And I should see a "city" field in "systemDetailsForm" form
    And I should see a "state" field in "systemDetailsForm" form
    And I should see a "country" field in "systemDetailsForm" form
    And I should see a "building" field in "systemDetailsForm" form
    And I should see a "room" field in "systemDetailsForm" form
    And I should see a "rack" field in "systemDetailsForm" form
    And I should see a "Update Properties" button

  Scenario: Show Details => Remote Command page
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Remote Command" in the content area
    Then I should see a "Remote Command on" text
    And I should see a "uid" field in "remoteCommandForm" form
    And I should see a "gid" field in "remoteCommandForm" form
    And I should see a "timeout" field in "remoteCommandForm" form
    And I should see a "lbl" field in "remoteCommandForm" form
    And I should see a "script_body" field in "remoteCommandForm" form
    And I should see a "date_datepicker_widget_input" field in "remoteCommandForm" form
    And I should see a "date_timepicker_widget_input" field in "remoteCommandForm" form
    And I should see a "Schedule" button

  Scenario: Show Details => Reactivation page
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Reactivation" in the content area
    Then I should see a "System Activation Key" text
     And I should see a "Generate New Key" button

  Scenario: Show Details => Hardware page
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Hardware" in the content area
    Then I should see a "Refresh Hardware List" text
    And I should see a "Schedule Hardware Refresh" button

  Scenario: Show Details => Migrate page
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Migrate" in the content area
    Then I should see a "Migrate System Between Organizations" text
    And I should see a "Migrate System" button

  Scenario: Show Details => Notes page
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Notes" in the content area
    Then I should see a "System Notes" text
    And I should see a "Create Note" link

  Scenario: Show Details => Custom Info page
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Custom Info" in the content area
    Then I should see a "Custom System Information" text
    And I should see a "Create Value" link
    And I should see a "Custom System Information" link

  Scenario: Show Software => Software Channels page
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    Then I should see a "Base Channel" text
    And I should see a "Child Channels" text
    And I wait until I do not see "Loading..." text
    And I should see a "Next" button

  Scenario: Show Configuration => View/Modify Files page
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Configuration" in the content area
    And I follow "View/Modify Files" in the content area
    Then I should see a "Configuration Overview" text
    And I should see a "Centrally-Managed Files" link in the content area
    And I should see a "Locally-Managed Files" link in the content area
    And I should see a "Local Sandbox" link in the content area
    And I should see a "No files found" text

  Scenario: Show Configuration => Add Files page
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Configuration" in the content area
    And I follow "Add Files" in the content area
    Then I should see a "Upload Local File" text
    And I should see a "Upload File" link in the content area
    And I should see a "Import Files" link in the content area
    And I should see a "Create File" link in the content area
    And I should see a "cffUpload" field in "configFileForm" form
    And I should see 2 "binary" fields in "configFileForm" form
    And I should see a "cffPath" field in "configFileForm" form
    And I should see a "cffUid" field in "configFileForm" form
    And I should see a "cffGid" field in "configFileForm" form
    And I should see a "cffPermissions" field in "configFileForm" form
    And I should see a "cffSELinuxCtx" field in "configFileForm" form
    And I should see a "cffMacroStart" field in "configFileForm" form
    And I should see a "cffMacroEnd" field in "configFileForm" form
    And I should see a "Upload Configuration File" button

  Scenario: Show Configuration => Add Files => Import Files page
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Configuration" in the content area
    And I follow "Add Files" in the content area
    And I follow "Import Files" in the content area
    Then I should see a "Import Configuration Files from" text
    And I should see a "contents" field in "configFileForm" form
    And I should see a "Import Configuration Files" button

  Scenario: Show Configuration => Add Files => Create File page
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Configuration" in the content area
    And I follow "Add Files" in the content area
    And I follow "Create File" in the content area
    Then I should see a "Create Local File" text
    And I should see a "file_radio" field in "configFileForm" form
    And I should see a "dir_radio" field in "configFileForm" form
    And I should see a "symlink_radio" field in "configFileForm" form
    And I should see a "cffPath" field in "configFileForm" form
    And the "targetPath" field should be disabled
    And I should see a "cffUid" field in "configFileForm" form
    And I should see a "cffGid" field in "configFileForm" form
    And I should see a "cffPermissions" field in "configFileForm" form
    And I should see a "cffSELinuxCtx" field in "configFileForm" form
    And I should see a "cffMacroStart" field in "configFileForm" form
    And I should see a "cffMacroEnd" field in "configFileForm" form
    And I should see a "contents" editor in "configFileForm" form
    And I should see a "Create Configuration File" button

  Scenario: Show Configuration => Deploy Files page
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Configuration" in the content area
    And I follow "Deploy Files" in the content area
    Then I should see a "Deploy Files" text

  Scenario: Show Configuration => Compare Files page
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Configuration" in the content area
    And I follow "Compare Files" in the content area
    Then I should see a "Compare Files" text

  Scenario: Show Configuration => Manage Configuration Channels page
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    Then I should see a "Configuration Channels" text
    And I should see a "List/Unsubscribe from Channels" link in the content area
    And I should see a "Subscribe to Channels" link in the content area
    And I should see a "View/Modify Rankings" link in the content area

  Scenario: Show Configuration => Manage Configuration Channels => Subscribe to Channels page
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    And I follow first "Subscribe to Channels" in the content area
    Then I should see a "Configuration Channel Subscriptions" text

  Scenario: Show Configuration => Manage Configuration Channels => View/Modify Rankings page
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    And I follow "View/Modify Rankings" in the content area
    Then I should see a "View/Modify Configuration Channel Ranks" text
    And I should see a "selectedChannel" field in "channelRanksForm" form
    And I should see a "up" button in "channelRanksForm" form
    And I should see a "down" button in "channelRanksForm" form
    And I should see a "Update Channel Rankings" button

  Scenario: Show Provisioning => Snapshots page
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Provisioning" in the content area
    And I follow "Snapshots" in the content area
    Then I should see a "System Snapshots" text
    And I should see a "Package profile changed" link

  Scenario: Show Provisioning => Snapshots Rollback page
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Provisioning" in the content area
    And I follow "Snapshots" in the content area
    And I follow first "Package profile changed"
    Then I should see a "Rollback" link in row 3 of the content menu
    And I should see a "Groups" link in row 3 of the content menu
    And I should see a "Channels" link in row 3 of the content menu
    And I should see a "Packages" link in row 3 of the content menu
    And I should see a "Config Channels" link in row 3 of the content menu
    And I should see a "Config Files" link in row 3 of the content menu
    And I should see a "Snapshot Tags" link in row 3 of the content menu
    And I should see a "Rollback to Snapshot" button

  Scenario: Show Provisioning => Snapshot Tags page
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Provisioning" in the content area
    And I follow "Snapshot Tags" in the content area
    Then I should see a "Snapshot Tags" text
    And I should see a "Create System Tag" link

  Scenario: Show Groups => Join page
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Groups" in the content area
    And I follow first "Join" in the content area
    Then I should see a "System Group Membership" text

  Scenario: Show Events => History page
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Events" in the content area
    And I follow "History" in the content area
    Then I should see a "System History" text
