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
    
  Scenario: check tab links
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

  Scenario: check tab links
    Given I am on the Systems overview page of this client
    When I follow "Software"
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

  Scenario: check tab links
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

  Scenario: check tab links
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

  Scenario: check tab links
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

  Scenario: check tab links
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

  Scenario: check tab links
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
     And I should see a "Cancel Events" button




