Feature: Register a client
  In Order register a client to the spacewalk server
  As the root user
  I want to call rhnreg_ks

  Scenario: Register a client
    Given I am root
    When I register using "1-SUSE-DEV" key
    Then I should see this client in spacewalk

  Scenario: check registration values
    Given I am on the Systems page
      And I follow "Systems" in "sidenav"
    When I follow this client link
    Then I should see a "System Status" text
     And I should see a "System is up to date" text
     And I should see a "Edit These Properties" link
     And I should see a "[Virtualization] [Monitoring] [Provisioning] [Management]" text
     And I should see a "add to ssm" link
     And I should see a "delete system" link
     And I should see a "Initial Registration Parameters:" text
     And I should see a "OS: openSUSE-release" text
     And I should see a "Release: 11.3" text
    
  Scenario: check tab links "Details"
    Given I am on the Systems page
      And I follow "Systems" in "sidenav"
    When I follow this client link
    Then I should see a "Details" link in "content-nav"
     And I should see a "Software" link in "content-nav"
     And I should see a "Configuration" link in "content-nav"
     And I should see a "Provisioning" link in "content-nav"
     And I should see a "Groups" link in "content-nav"
     And I should see a "Virtualization" link in "content-nav"
     And I should see a "Events" link in "content-nav"
     And I should see a "Overview" link in "contentnav-row2"
     And I should see a "Properties" link in "contentnav-row2"
     And I should see a "Remote Command" link in "contentnav-row2"
     And I should see a "Reactivation" link in "contentnav-row2"
     And I should see a "Hardware" link in "contentnav-row2"
     And I should see a "Migrate" link in "contentnav-row2"
     And I should see a "Notes" link in "contentnav-row2"
     And I should see a "Custom Info" link in "contentnav-row2"

  Scenario: check tab links "Software"
    Given I am on the Systems overview page of this client
    When I follow "Software" in class "content-nav"
    Then I should see a "Details" link in "content-nav"
     And I should see a "Software" link in "content-nav"
     And I should see a "Configuration" link in "content-nav"
     And I should see a "Provisioning" link in "content-nav"
     And I should see a "Groups" link in "content-nav"
     And I should see a "Virtualization" link in "content-nav"
     And I should see a "Events" link in "content-nav"
     And I should see a "Errata" link in "contentnav-row2"
     And I should see a "Packages" link in "contentnav-row2"
     And I should see a "Software Channels" link in "contentnav-row2"
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

  Scenario: check tab links "Configuration"
    Given I am on the Systems overview page of this client
    When I follow "Configuration" in class "content-nav"
    Then I should see a "Details" link in "content-nav"
     And I should see a "Software" link in "content-nav"
     And I should see a "Configuration" link in "content-nav"
     And I should see a "Provisioning" link in "content-nav"
     And I should see a "Groups" link in "content-nav"
     And I should see a "Virtualization" link in "content-nav"
     And I should see a "Events" link in "content-nav"
     And I should see a "Overview" link in "contentnav-row2"
     And I should see a "View/Modify Files" link in "contentnav-row2"
     And I should see a "Add Files" link in "contentnav-row2"
     And I should see a "Deploy Files" link in "contentnav-row2"
     And I should see a "Compare Files" link in "contentnav-row2"
     And I should see a "Manage Configuration Channels" link in "contentnav-row2"
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

  Scenario: check tab links "Provisioning"
    Given I am on the Systems overview page of this client
    When I follow "Provisioning" in class "content-nav"
    Then I should see a "Details" link in "content-nav"
     And I should see a "Software" link in "content-nav"
     And I should see a "Configuration" link in "content-nav"
     And I should see a "Provisioning" link in "content-nav"
     And I should see a "Groups" link in "content-nav"
     And I should see a "Virtualization" link in "content-nav"
     And I should see a "Events" link in "content-nav"
     And I should see a "Kickstart" link in "contentnav-row2"
     And I should see a "Snapshots" link in "contentnav-row2"
     And I should see a "Snapshot Tags" link in "contentnav-row2"
     And I should see a "No profiles found that are compatible with this System. Either you haven't created any Kickstart Profiles or this system does not have a Base Channel." text

  Scenario: check tab links "Groups"
    Given I am on the Systems overview page of this client
    When I follow "Groups" in class "content-nav"
    Then I should see a "Details" link in "content-nav"
     And I should see a "Software" link in "content-nav"
     And I should see a "Configuration" link in "content-nav"
     And I should see a "Provisioning" link in "content-nav"
     And I should see a "Groups" link in "content-nav"
     And I should see a "Virtualization" link in "content-nav"
     And I should see a "Events" link in "content-nav"
     And I should see a "List / Leave" link in "contentnav-row2"
     And I should see a "Join" link in "contentnav-row2"
     And I should see a "Join" link
     And I should see a "System Groups" text

  Scenario: check tab links "Virtualization"
    Given I am on the Systems overview page of this client
    When I follow "Virtualization" in class "content-nav"
    Then I should see a "Details" link in "content-nav"
     And I should see a "Software" link in "content-nav"
     And I should see a "Configuration" link in "content-nav"
     And I should see a "Provisioning" link in "content-nav"
     And I should see a "Groups" link in "content-nav"
     And I should see a "Virtualization" link in "content-nav"
     And I should see a "Events" link in "content-nav"
     And I should see a "Details" link in "contentnav-row2"
     And I should see a "Provisioning" link in "contentnav-row2"
     And I should see a "Apply Action" button
     And I should see a "Apply Changes" button

  Scenario: check tab links "Events"
    Given I am on the Systems overview page of this client
    When I follow "Events" in class "content-nav"
    Then I should see a "Details" link in "content-nav"
     And I should see a "Software" link in "content-nav"
     And I should see a "Configuration" link in "content-nav"
     And I should see a "Provisioning" link in "content-nav"
     And I should see a "Groups" link in "content-nav"
     And I should see a "Virtualization" link in "content-nav"
     And I should see a "Events" link in "content-nav"
     And I should see a "Pending" link in "contentnav-row2"
     And I should see a "History" link in "contentnav-row2"
     And I should see a " Pending Events" text

  Scenario: check tab links "Details" => "Properties"
    Given I am on the Systems overview page of this client
    When I follow "Properties" in class "contentnav-row2"
    Then I should see a "Edit System Details" text
     And I should see a "system_name" element in "systemDetailsForm" form
     And I should see a "baseentitlement" element in "systemDetailsForm" form
     And I should see a "monitoring_entitled" element in "systemDetailsForm" form
     And I should see a "provisioning_entitled" element in "systemDetailsForm" form
     And I should see a "virtualization_host" element in "systemDetailsForm" form
     And I should see a "virtualization_host_platform" element in "systemDetailsForm" form
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
    Then I should see a "Migrate System Between Organisations" text
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
     And I should see a "No Errata Relevant to Your Systems" text

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
     And I should see a "Centrally-Managed Files" link in "content-nav"
     And I should see a "Locally-Managed Files" link in "content-nav"
     And I should see a "Local Sandbox" link in "content-nav"
     And I should see a "No files found" text

  Scenario: check tab links "Configuration" => "Add Files"
    Given I am on the Systems overview page of this client
    When I follow "Configuration" in class "content-nav"
    When I follow "Add Files" in class "contentnav-row2"
    Then I should see a "Upload Local File" text
     And I should see a "Upload File" link in "content-nav"
     And I should see a "Import Files" link in "content-nav"
     And I should see a "Create File" link in "content-nav"
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


