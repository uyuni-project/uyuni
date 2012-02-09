# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Check client registration
  In Order check the registration of a client to the spacewalk server
  As the testing
  I want to see the system in the spacewalk server

  Scenario: check registration values
    Given I update the profile of this client
      And I am on the Systems page
      And I follow "Systems" in the left menu
    When I follow this client link
    Then I should see a "System Status" text
     And I should see a "Software Updates Available" text
     And I should see a "Critical:" link
     And I should see a "Non-Critical:" link
     And I should see a "Packages:" link
     And I should see a "Edit These Properties" link
     And I should see a "[Monitoring]" text
     And I should see a "[Provisioning]" text
     And I should see a "[Management]" text
     And I should see a "add to ssm" link
     And I should see a "delete system" link
     And I should see a "Initial Registration Parameters:" text
     And I should see a "OS: sles-release" text
     And I should see a "Release: 11.1" text
     #And I should see a "[Virtualization]" text

  Scenario: check tab links "Details"
    Given I am on the Systems page
      And I follow "Systems" in the left menu
    When I follow this client link
    Then I should see a "Details" link in element "content-nav"
     And I should see a "Software" link in element "content-nav"
     And I should see a "Configuration" link in element "content-nav"
     And I should see a "Provisioning" link in element "content-nav"
     And I should see a "Groups" link in element "content-nav"
     And I should see a "Events" link in element "content-nav"
     And I should see a "Overview" link in element "contentnav-row2"
     And I should see a "Properties" link in element "contentnav-row2"
     And I should see a "Remote Command" link in element "contentnav-row2"
     And I should see a "Reactivation" link in element "contentnav-row2"
     And I should see a "Hardware" link in element "contentnav-row2"
     And I should see a "Migrate" link in element "contentnav-row2"
     And I should see a "Notes" link in element "contentnav-row2"
     And I should see a "Custom Info" link in element "contentnav-row2"
     #And I should see a "Virtualization" link in element "content-nav"

  Scenario: check tab links "Software"
    Given I am on the Systems overview page of this client
    When I follow "Software" in class "content-nav"
    Then I should see a "Details" link in element "content-nav"
     And I should see a "Software" link in element "content-nav"
     And I should see a "Configuration" link in element "content-nav"
     And I should see a "Provisioning" link in element "content-nav"
     And I should see a "Groups" link in element "content-nav"
     And I should see a "Events" link in element "content-nav"
     And I should see a "Errata" link in element "contentnav-row2"
     And I should see a "Packages" link in element "contentnav-row2"
     And I should see a "Software Channels" link in element "contentnav-row2"
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
     #And I should see a "Virtualization" link in element "content-nav"

  Scenario: check tab links "Configuration"
    Given I am on the Systems overview page of this client
    When I follow "Configuration" in class "content-nav"
    Then I should see a "Details" link in element "content-nav"
     And I should see a "Software" link in element "content-nav"
     And I should see a "Configuration" link in element "content-nav"
     And I should see a "Provisioning" link in element "content-nav"
     And I should see a "Groups" link in element "content-nav"
     And I should see a "Events" link in element "content-nav"
     And I should see a "Overview" link in element "contentnav-row2"
     And I should see a "View/Modify Files" link in element "contentnav-row2"
     And I should see a "Add Files" link in element "contentnav-row2"
     And I should see a "Deploy Files" link in element "contentnav-row2"
     And I should see a "Compare Files" link in element "contentnav-row2"
     And I should see a "Manage Configuration Channels" link in element "contentnav-row2"
     And I should see a "Deploy all managed config files" link
     And I should see a "Deploy selected config files" link
     And I should see a "Compare all managed files to system" link
     And I should see a "Compare selected managed files to system" link
     And I should see a "Create a new config file or dir" link
     And I should see a "Upload config files" link
     And I should see a "Import all managed files from system" link
     And I should see a "Import selected files from system" link
     And I should see a "Schedule Deploy Action" link
     And I should see a "Schedule System Comparison" link
     #And I should see a "Virtualization" link in element "content-nav"

  Scenario: check tab links "Provisioning"
    Given I am on the Systems overview page of this client
    When I follow "Provisioning" in class "content-nav"
    Then I should see a "Details" link in element "content-nav"
     And I should see a "Software" link in element "content-nav"
     And I should see a "Configuration" link in element "content-nav"
     And I should see a "Provisioning" link in element "content-nav"
     And I should see a "Groups" link in element "content-nav"
     And I should see a "Events" link in element "content-nav"
     And I should see a "Kickstart" link in element "contentnav-row2"
     And I should see a "Snapshots" link in element "contentnav-row2"
     And I should see a "Snapshot Tags" link in element "contentnav-row2"
     And I should see a "Schedule" link in element "content-nav"
     #And I should see a "Virtualization" link in element "content-nav"

# if cobbler test was running there is a profile
#     And I should see a "No profiles found that are compatible with this System. Either you haven't created any Kickstart Profiles or this system does not have a Base Channel." text

  @monitoring
  Scenario: check tab links "Monitoring"
    Given I am on the Systems overview page of this client
    When I follow "Monitoring" in class "content-nav"
    Then I should see a "create new probe" link
     And I should see a "Configured Monitoring Probes" text

  Scenario: check tab links "Groups"
    Given I am on the Systems overview page of this client
    When I follow "Groups" in class "content-nav"
    Then I should see a "Details" link in element "content-nav"
     And I should see a "Software" link in element "content-nav"
     And I should see a "Configuration" link in element "content-nav"
     And I should see a "Provisioning" link in element "content-nav"
     And I should see a "Groups" link in element "content-nav"
     And I should see a "Events" link in element "content-nav"
     And I should see a "List / Leave" link in element "contentnav-row2"
     And I should see a "Join" link in element "contentnav-row2"
     And I should see a "Join" link
     And I should see a "System Groups" text
     #And I should see a "Virtualization" link in element "content-nav"

  #Scenario: check tab links "Virtualization"
  #  Given I am on the Systems overview page of this client
  #  When I follow "Virtualization" in class "content-nav"
  #  Then I should see a "Details" link in element "content-nav"
  #   And I should see a "Software" link in element "content-nav"
  #   And I should see a "Configuration" link in element "content-nav"
  #   And I should see a "Provisioning" link in element "content-nav"
  #   And I should see a "Groups" link in element "content-nav"
  #   And I should see a "Events" link in element "content-nav"
  #   And I should see a "Details" link in element "contentnav-row2"
  #   And I should see a "Provisioning" link in element "contentnav-row2"
  #   And I should see a "Apply Action" button
  #   And I should see a "Apply Changes" button
  #   And I should see a "Virtualization" link in element "content-nav"

  Scenario: check tab links "Events"
    Given I am on the Systems overview page of this client
    When I follow "Events" in class "content-nav"
    Then I should see a "Details" link in element "content-nav"
     And I should see a "Software" link in element "content-nav"
     And I should see a "Configuration" link in element "content-nav"
     And I should see a "Provisioning" link in element "content-nav"
     And I should see a "Groups" link in element "content-nav"
     And I should see a "Events" link in element "content-nav"
     And I should see a "Pending" link in element "contentnav-row2"
     And I should see a "History" link in element "contentnav-row2"
     And I should see a " Pending Events" text
     #And I should see a "Virtualization" link in element "content-nav"

  Scenario: check tab links "Details" => "Properties"
    Given I am on the Systems overview page of this client
    When I follow "Properties" in class "contentnav-row2"
    Then I should see a "Edit System Details" text
     And I should see a "system_name" element in "systemDetailsForm" form
     And I should see a "baseentitlement" element in "systemDetailsForm" form
     And I should see a "monitoring_entitled" element in "systemDetailsForm" form
     And I should see a "provisioning_entitled" element in "systemDetailsForm" form
     #And I should see a "virtualization_host" element in "systemDetailsForm" form
     #And I should see a "virtualization_host_platform" element in "systemDetailsForm" form
     And I should see a "receive_notifications" element in "systemDetailsForm" form
     And I should see a "summary" element in "systemDetailsForm" form
     And I should see a "autoerrataupdate" element in "systemDetailsForm" form
     And I should see a "description" element in "systemDetailsForm" form
     And I should see a "address" element in "systemDetailsForm" form
     And I should see a "city" element in "systemDetailsForm" form
     And I should see a "state" element in "systemDetailsForm" form
     And I should see a "country" element in "systemDetailsForm" form
     And I should see a "building" element in "systemDetailsForm" form
     And I should see a "room" element in "systemDetailsForm" form
     And I should see a "rack" element in "systemDetailsForm" form
     And I should see a "Update Properties" button

  Scenario: check tab links "Details" => "Remote Command"
    Given I am on the Systems overview page of this client
    When I follow "Remote Command" in class "contentnav-row2"
    Then I should see a "Run Remote Command" text
     And I should see a "username" element in "remote_command_form" form
     And I should see a "group" element in "remote_command_form" form
     And I should see a "timeout" element in "remote_command_form" form
     And I should see a "script" element in "remote_command_form" form
     And I should see a "month" element in "remote_command_form" form
     And I should see a "day" element in "remote_command_form" form
     And I should see a "year" element in "remote_command_form" form
     And I should see a "hour" element in "remote_command_form" form
     And I should see a "minute" element in "remote_command_form" form
     And I should see a "am_pm" element in "remote_command_form" form
     And I should see a "Schedule Remote Command" button

  Scenario: check tab links "Details" => "Reactivation"
    Given I am on the Systems overview page of this client
    When I follow "Reactivation" in class "contentnav-row2"
    Then I should see a "System Activation Key" text
     And I should see a "Generate New Key" button

  Scenario: check tab links "Details" => "Hardware"
    Given I am on the Systems overview page of this client
    When I follow "Hardware" in class "contentnav-row2"
    Then I should see a "Refresh Hardware List" text
     And I should see a "Schedule Hardware Refresh" button

  Scenario: check tab links "Details" => "Migrate"
    Given I am on the Systems overview page of this client
    When I follow "Migrate" in class "contentnav-row2"
    Then I should see a "Migrate System Between Organizations" text
     And I should see a "Migrate System" button

  Scenario: check tab links "Details" => "Notes"
    Given I am on the Systems overview page of this client
    When I follow "Notes" in class "contentnav-row2"
    Then I should see a "System Notes" text
     And I should see a "create new note" link

  Scenario: check tab links "Details" => "Custom Info"
    Given I am on the Systems overview page of this client
    When I follow "Custom Info" in class "contentnav-row2"
    Then I should see a "Custom System Information" text
     And I should see a "create new value" link
     And I should see a "Custom System Information" link

  Scenario: check tab links "Software" => "Errata"
    Given I am on the Systems overview page of this client
    When I follow "Software" in class "content-nav"
    When I follow "Errata" in class "contentnav-row2"
    Then I should see a "Relevant Errata" text
     And I should see a "Show" button
     And I should see a "Security update for the Linux kernel" text
     And I should see a "slessp1-kernel" link
     And I should see a "Recommended update for aaa_base" text
     And I should see a "slessp1-aaa_base-sysvinit" link

  Scenario: check tab links "Software" => "Software Channels"
    Given I am on the Systems overview page of this client
    When I follow "Software" in class "content-nav"
    When I follow "Software Channels" in class "contentnav-row2"
    Then I should see a "Software Channel Subscriptions" text
     And I should see a "Base Software Channel" text
     And I should see a "Change Subscriptions" button
     And I should see a "Confirm" button

  Scenario: check tab links "Configuration" => "View/Modify Files"
    Given I am on the Systems overview page of this client
    When I follow "Configuration" in class "content-nav"
    When I follow "View/Modify Files" in class "contentnav-row2"
    Then I should see a "Configuration Overview" text
     And I should see a "Centrally-Managed Files" link in element "content-nav"
     And I should see a "Locally-Managed Files" link in element "content-nav"
     And I should see a "Local Sandbox" link in element "content-nav"
     And I should see a "No files found" text

  Scenario: check tab links "Configuration" => "Add Files"
    Given I am on the Systems overview page of this client
    When I follow "Configuration" in class "content-nav"
    When I follow "Add Files" in class "contentnav-row2"
    Then I should see a "Upload Local File" text
     And I should see a "Upload File" link in element "content-nav"
     And I should see a "Import Files" link in element "content-nav"
     And I should see a "Create File" link in element "content-nav"
     And I should see a "cffUpload" element in "configFileForm" form
     And I should see a "binary" element in "configFileForm" form
     And I should see a "cffPath" element in "configFileForm" form
     And I should see a "cffUid" element in "configFileForm" form
     And I should see a "cffGid" element in "configFileForm" form
     And I should see a "cffPermissions" element in "configFileForm" form
     And I should see a "cffSELinuxCtx" element in "configFileForm" form
     And I should see a "cffMacroStart" element in "configFileForm" form
     And I should see a "cffMacroEnd" element in "configFileForm" form
     And I should see a "Upload Configuration File" button

  Scenario: check tab links "Configuration" => "Add Files" => "Import Files"
    Given I am on the Systems overview page of this client
    When I follow "Configuration" in class "content-nav"
    When I follow "Add Files" in class "contentnav-row2"
    When I follow "Import Files" in class "content-nav"
    Then I should see a "Import Configuration Files from" text
     And I should see a "contents" element in "configFileForm" form
     And I should see a "Import Configuration Files" button

  Scenario: check tab links "Configuration" => "Add Files" => "Create File"
    Given I am on the Systems overview page of this client
    When I follow "Configuration" in class "content-nav"
    When I follow "Add Files" in class "contentnav-row2"
    When I follow "Create File" in class "content-nav"
    Then I should see a "Create Local File" text
     And I uncheck "edit_area_toggle_checkbox_contents"
     And I should see a "file_radio" element in "configFileForm" form
     And I should see a "dir_radio" element in "configFileForm" form
     And I should see a "symlink_radio" element in "configFileForm" form
     And I should see a "cffPath" element in "configFileForm" form
     And I should see a "targetPath" element in "configFileForm" form
     And I should see a "cffUid" element in "configFileForm" form
     And I should see a "cffGid" element in "configFileForm" form
     And I should see a "cffPermissions" element in "configFileForm" form
     And I should see a "cffSELinuxCtx" element in "configFileForm" form
     And I should see a "cffMacroStart" element in "configFileForm" form
     And I should see a "cffMacroEnd" element in "configFileForm" form
     And I should see a "contents" element in "configFileForm" form
     And I should see a "Create Configuration File" button

  Scenario: check tab links "Configuration" => "Deploy Files"
    Given I am on the Systems overview page of this client
    When I follow "Configuration" in class "content-nav"
    When I follow "Deploy Files" in class "contentnav-row2"
    Then I should see a "Deploy Files" text

  Scenario: check tab links "Configuration" => "Compare Files"
    Given I am on the Systems overview page of this client
    When I follow "Configuration" in class "content-nav"
    When I follow "Compare Files" in class "contentnav-row2"
    Then I should see a "Compare Files" text

  Scenario: check tab links "Configuration" => "Manage Configuration Channels"
    Given I am on the Systems overview page of this client
    When I follow "Configuration" in class "content-nav"
    When I follow "Manage Configuration Channels" in class "contentnav-row2"
    Then I should see a "Configuration Channels" text
     And I should see a "List/Unsubscribe from Channels" link in element "content-nav"
     And I should see a "Subscribe to Channels" link in element "content-nav"
     And I should see a "View/Modify Rankings" link in element "content-nav"

  Scenario: check tab links "Configuration" => "Manage Configuration Channels" => "Subscribe to Channels"
    Given I am on the Systems overview page of this client
    When I follow "Configuration" in class "content-nav"
     And I follow "Manage Configuration Channels" in class "contentnav-row2"
     And I follow "Subscribe to Channels" in class "content-nav"
    Then I should see a "Configuration Channel Subscriptions" text

  Scenario: check tab links "Configuration" => "Manage Configuration Channels" => "View/Modify Rankings"
    Given I am on the Systems overview page of this client
    When I follow "Configuration" in class "content-nav"
     And I follow "Manage Configuration Channels" in class "contentnav-row2"
     And I follow "View/Modify Rankings" in class "content-nav"
    Then I should see a "View/Modify Configuration Channel Ranks" text
     And I should see a "selectedChannel" element in "channelRanksForm" form
     And I should see a "up" button in "channelRanksForm" form
     And I should see a "down" button in "channelRanksForm" form
     And I should see a "Update Channel Rankings" button

  Scenario: check tab links "Provisioning" => "Snapshots"
    Given I am on the Systems overview page of this client
    When I follow "Provisioning" in class "content-nav"
     And I follow "Snapshots" in class "contentnav-row2"
    Then I should see a "System Snapshots" text
     And I should see a "Package profile changed" link

  @unsure
  Scenario: check tab links "Provisioning" => "Snapshots"
    Given I am on the Systems overview page of this client
    When I follow "Provisioning" in class "content-nav"
     And I follow "Snapshots" in class "contentnav-row2"
     And I follow "Package profile changed"
    Then I should see a "Rollback" link in list "content-nav-rowthree"
     And I should see a "Groups" link in list "content-nav-rowthree"
     And I should see a "Channels" link in list "content-nav-rowthree"
     And I should see a "Packages" link in list "content-nav-rowthree"
     And I should see a "Config Channels" link in list "content-nav-rowthree"
     And I should see a "Config Files" link in list "content-nav-rowthree"
     And I should see a "Snapshot Tags" link in list "content-nav-rowthree"
     And I should see a "Rollback to Snapshot" button

  Scenario: check tab links "Provisioning" => "Snapshot Tags"
    Given I am on the Systems overview page of this client
    When I follow "Provisioning" in class "content-nav"
     And I follow "Snapshot Tags" in class "contentnav-row2"
    Then I should see a "Snapshot Tags" text
      And I should see a "create new system tag" link

  Scenario: check tab links "Groups" => "Join"
    Given I am on the Systems overview page of this client
    When I follow "Groups" in class "content-nav"
     And I follow "Join" in class "contentnav-row2"
    Then I should see a "System Group Membership" text

  #@unsure
  #Scenario: check tab links "Virtualization" => "Provisioning"
  #  Given I am on the Systems overview page of this client
  #  When I follow "Virtualization" in class "content-nav"
  #   And I follow "Provisioning" in class "contentnav-row2"
  #  Then I should see a "Select a Kickstart Profile" text

  Scenario: check tab links "Events" => "History"
    Given I am on the Systems overview page of this client
    When I follow "Events" in class "content-nav"
     And I follow "History" in class "contentnav-row2"
    Then I should see a "System History" text


