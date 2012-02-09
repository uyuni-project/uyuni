# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

#
# Explore the systems page
#
Feature: Explore the main landing page
  In Order to validate completeness of the systems page
  As a authorized user
  I want to see all the texts and links

  Scenario: Completeness of the side navigation bar and the content frame
    Given I am authorized
    When I follow "Systems"
    Then I should see a "System Overview" text
     And I should see a "No systems." text
     And I should see a "Overview" link in the left menu
     And I should see a "Systems" link in the left menu
     And I should see a "System Groups" link in the left menu
     And I should see a "System Set Manager" link in the left menu
     And I should see a "Advanced Search" link in the left menu
     And I should see a "Activation Keys" link in the left menu
     And I should see a "Stored Profiles" link in the left menu
     And I should see a "Custom System Info" link in the left menu
     And I should see a "Kickstart" link in the left menu
     And I should see a "View System Groups" link
     And I should see a "Download CSV" link
     And I should see a "Sign Out" link

  @monitoring
  Scenario: Completeness of the main navigation bar
    Given I am authorized
    When I follow "Systems"
    Then I should see a "Overview" link in the tab bar
     And I should see a "Systems" link in the tab bar
     And I should see a "Errata" link in the tab bar
     And I should see a "Channels" link in the tab bar
     And I should see a "Configuration" link in the tab bar
     And I should see a "Schedule" link in the tab bar
     And I should see a "Users" link in the tab bar
     And I should see a "Monitoring" link in the tab bar
     And I should see a "Help" link in the tab bar

  @without_monitoring
  Scenario: Completeness of the main navigation bar
    Given I am authorized
    When I follow "Systems"
    Then I should see a "Overview" link in the tab bar
     And I should see a "Systems" link in the tab bar
     And I should see a "Errata" link in the tab bar
     And I should see a "Channels" link in the tab bar
     And I should see a "Configuration" link in the tab bar
     And I should see a "Schedule" link in the tab bar
     And I should see a "Users" link in the tab bar
     And I should see a "Help" link in the tab bar

  Scenario: Check sidebar link destination for Systems
    Given I am on the Systems page
    When I follow "Systems" in the left menu
    Then I should see a "All" link in the left menu
     And I should see a "Virtual Systems" link in the left menu
     And I should see a "Out of Date" link in the left menu
     And I should see a "Unentitled" link in the left menu
     And I should see a "Ungrouped" link in the left menu
     And I should see a "Inactive" link in the left menu
     And I should see a "Recently Registered" link in the left menu
     And I should see a "Duplicate Systems" link in the left menu
     And I should see a "System Currency" link in the left menu
     And I should see a "Systems" text

  Scenario: Check sidebar link destination for Systems => Virtual Systems
    Given I am on the Systems page
      And I follow "Systems" in the left menu
      And I follow "Virtual Systems" in the left menu
    Then I should see a "Virtual Systems" text
     And I should see a "No Virtual Systems." text
     And the current path is "/rhn/systems/VirtualSystemsList.do"

  Scenario: Check sidebar link destination for Systems => Out of Date
    Given I am on the Systems page
      And I follow "Systems" in the left menu
      And I follow "Out of Date" in the left menu
    Then I should see a "Out of Date Systems" text
     And I should see a "No systems." text
     And the current path is "/rhn/systems/OutOfDate.do"

  Scenario: Check sidebar link destination for Systems => Unentitled
    Given I am on the Systems page
      And I follow "Systems" in the left menu
      And I follow "Unentitled" in the left menu
    Then I should see a "Unentitled Systems" text
     And I should see a "No systems." text
     And the current path is "/rhn/systems/Unentitled.do"

  Scenario: Check sidebar link destination for Systems => Ungrouped
    Given I am on the Systems page
      And I follow "Systems" in the left menu
      And I follow "Ungrouped" in the left menu
    Then I should see a "Ungrouped Systems" text
     And I should see a "No systems." text
     And the current path is "/rhn/systems/Ungrouped.do"

  Scenario: Check sidebar link destination for Systems => Inactive
    Given I am on the Systems page
      And I follow "Systems" in the left menu
      And I follow "Inactive" in the left menu
    Then I should see a "Inactive Systems" text
     And I should see a "No systems." text
     And the current path is "/rhn/systems/Inactive.do"

  Scenario: Check sidebar link destination for Systems => Recently Registered
    Given I am on the Systems page
      And I follow "Systems" in the left menu
      And I follow "Recently Registered" in the left menu
    Then I should see a "Recently Registered Systems" text
     And I should see a "No systems." text
     And I should see a "View systems registered:" text
     And the current path is "/rhn/systems/Registered.do"

  Scenario: Check sidebar link destination for Systems => Duplicate Systems
    Given I am on the Systems page
      And I follow "Systems" in the left menu
      And I follow "Duplicate Systems" in the left menu
    Then I should see a "Duplicate System Profiles" text
     And I should see a "No systems." text
     And the current path is "/rhn/systems/DuplicateIPList.do"
     And I should see a "Duplicate IP Address" link
     And I should see a "Duplicate Hostname" link
     And I should see a "Duplicate MAC Address" link
     And I should see a "Show All" link
     And I should see a "Hide All" link

  Scenario: Check sidebar link destination for Systems => System Currency
    Given I am on the Systems page
      And I follow "Systems" in the left menu
      And I follow "System Currency" in the left menu
    Then I should see a "System Currency Report" text
     And I should see a "No systems." text
     And the current path is "/rhn/systems/SystemCurrency.do"

  Scenario: Check sidebar link destination for Systems => System Groups
    Given I am on the Systems page
      And I follow "System Groups" in the left menu
    Then I should see a "System Groups" text
     And I should see a "create new group" link
     And I should see a "Your organization has no system groups." text

  @failed
  Scenario: Check sidebar link destination for Systems => System Set Manager
    Given I am on the Systems page
      And I follow "System Set Manager" in the left menu
    Then I should see a "System Set Manager" text
     And I should see a "Status" link in the left menu
     And I should see a "Overview" link in element "content-nav"
     And I should see a "Systems" link in element "content-nav"
     And I should see a "Errata" link in element "content-nav"
     And I should see a "Packages" link in element "content-nav"
     And I should see a "Groups" link in element "content-nav"
     And I should see a "Channels" link in element "content-nav"
     And I should see a "Configuration" link in element "content-nav"
     And I should see a "Provisioning" link in element "content-nav"
     And I should see a "Misc" link in element "content-nav"

  Scenario: Check sidebar link destination for Systems => Advanced Search
    Given I am on the Systems page
      And I follow "Advanced Search" in the left menu
    Then I should see a "System Search" text

  Scenario: Check sidebar link destination for Systems => Activation Keys
    Given I am on the Systems page
      And I follow "Activation Keys" in the left menu
    Then I should see a "Activation Keys" text
     And I should see a "create new key" link
     And I should see a "No activation keys available" text

  Scenario: Check sidebar link destination for Systems => Stored Profiles
    Given I am on the Systems page
      And I follow "Stored Profiles" in the left menu
    Then I should see a "Stored Profiles" text
     And I should see a "No stored profiles." text

  Scenario: Check sidebar link destination for Systems => Custom System Info
    Given I am on the Systems page
      And I follow "Custom System Info" in the left menu
    Then I should see a "Custom System Info Keys" text
     And I should see a "create new key" link
     And I should see a "No Custom Info Keys Found" text

   Scenario: Check sidebar link destination for Systems => Kickstart
    Given I am on the Systems page
      And I follow "Kickstart" in the left menu
    Then I should see a "Kickstart Overview" text
     And I should see a "Profiles" link in the left menu
     And I should see a "Bare Metal" link in the left menu
     And I should see a "GPG and SSL Keys" link in the left menu
     And I should see a "Distributions" link in the left menu
     And I should see a "File Preservation" link in the left menu
     And I should see a "Autoinstallation Snippets" link in the left menu
     And I should see a "create new kickstart profile" link
     And I should see a "upload new kickstart file" link
     And I should see a "View a List of Kickstart Profiles" link
     And I should see a "Create a New Kickstart Profile" link
     And I should see a "Upload a New Kickstart File" link

  Scenario: Check sidebar link destination for Systems => Kickstart => Profiles
    Given I am on the Systems page
      And I follow "Kickstart" in the left menu
      And I follow "Profiles" in the left menu
    Then I should see a "Kickstart Profiles" text
     And I should see a "create new kickstart profile" link
     And I should see a "upload new kickstart file" link

  Scenario: Check sidebar link destination for Systems => Kickstart => Bare Metal
    Given I am on the Systems page
      And I follow "Kickstart" in the left menu
      And I follow "Bare Metal" in the left menu
    Then I should see a "Bare Metal Kickstart By IP" text
     And I should see a "No Ip Ranges Found" text

  Scenario: Check sidebar link destination for Systems => Kickstart => GPG and SSL Keys
    Given I am on the Systems page
      And I follow "Kickstart" in the left menu
      And I follow "GPG and SSL Keys" in the left menu
    Then I should see a "GPG Public Keys and SSL Certificates" text
     And I should see a "create new stored key/cert" link
     And I should see a "RHN Reference Guide" link
     And I should see a "RHN-ORG-TRUSTED-SSL-CERT" link

  Scenario: Check sidebar link destination for Systems => Kickstart => Distributions
    Given I am on the Systems page
      And I follow "Kickstart" in the left menu
      And I follow "Distributions" in the left menu
    Then I should see a "Kickstartable Distributions" text
     And I should see a "No kickstartable distributions available." text
     And I should see a "create new distribution" link

  Scenario: Check sidebar link destination for Systems => Kickstart => File Preservation
    Given I am on the Systems page
      And I follow "Kickstart" in the left menu
      And I follow "File Preservation" in the left menu
    Then I should see a "File Preservation" text
     And I should see a "RHN Reference Guide" link
     And I should see a "create new file preservation list" link

  Scenario: Check sidebar link destination for Systems => Kickstart => Kickstart Snippets
    Given I am on the Systems page
      And I follow "Kickstart" in the left menu
      And I follow "Autoinstallation Snippets" in the left menu
    Then I should see a "Autoinstallation Snippets" text
     And I should see a "No autoinstallation snippets found." text
     And I should see a "create new snippet" link
     And I should see a "Default Snippets" link in element "content-nav"
     And I should see a "Custom Snippets" link in element "content-nav"
     And I should see a "All Snippets" link in element "content-nav"

  Scenario: Check "create new kickstart profile" page Systems => Kickstart => Profiles => create new kickstart profile
    Given I am on the Systems page
      And I follow "Kickstart" in the left menu
      And I follow "Profiles" in the left menu
      And I follow "create new kickstart profile"
    Then I should see a "Step 1: Create Kickstart Profile" text

  Scenario: Check "upload new kickstart file" page Systems => Kickstart => Profiles => upload new kickstart file
    Given I am on the Systems page
      And I follow "Kickstart" in the left menu
      And I follow "Profiles" in the left menu
      And I follow "upload new kickstart file"
    Then I should see a "Create Kickstart Profile" text
      And I should see a "File Contents:" text
      And I should see a "Kickstart Details" text

  Scenario: Check "create kickstart distribution" page Systems => Kickstart => Distributions => create new kickstart distribution
    Given I am on the Systems page
      And I follow "Kickstart" in the left menu
      And I follow "Distributions" in the left menu
      And I follow "create new distribution"
    Then I should see a "Create Kickstart Distribution" text
      And I should see a "Distribution Label" text

  @cobbler
  Scenario: create a dummy distro with cobbler (not visible in UI, SLES)
    Given cobblerd is running
    Then create distro "testdistro" as user "testing" with password "testing"

  @cobbler
  Scenario: create dummy profile
    Given cobblerd is running
      And distro "testdistro" exists
    Then create profile "testprofile" as user "testing" with password "testing"

  @cobbler
  Scenario: Check cobbler created distro and profile Systems => Kickstart => Profiles
    Given I am on the Systems page
      And I follow "Kickstart" in the left menu
      And I follow "Profiles" in the left menu
    Then I should see a "testprofile" text
     And I should see a "testdistro" text

  @cobbler_ui
  Scenario: create a distro with the UI (requires a base channel)
    Given I am on the Systems page
      And I follow "Kickstart" in the left menu
      And I follow "Distributions" in the left menu
      And I follow "create new distribution"
    When I enter "fedora_kickstart_distro" as "label"
      And I enter "/install/Fedora_12_i386/" as "basepath"
      And I select "Fedora" from "installtype"
      And I click on "Create Autoinstallable Distribution"
    Then I should see a "Autoinstallable Distributions" text
     And I should see a "fedora_kickstart_distro" link

  @cobbler_ui
  Scenario: create a profile with the UI (requires a base channel)
    Given I am on the Systems page
      And I follow "Kickstart" in the left menu
      And I follow "Profiles" in the left menu
      And I follow "create new kickstart profile"
    When I enter "fedora_kickstart_profile" as "kickstartLabel"
      And I click on "Next"
      And I click on "Next"
      And I enter "linux" as "rootPassword"
      And I enter "linux" as "rootPasswordConfirm"
      And I click on "Finish"
    Then I should see a "Kickstart: fedora_kickstart_profile" text
     And I should see a "Kickstart Details" link


  @cobbler_ui
  Scenario: upload a profile with the UI (requires a base channel)
    Given I am on the Systems page
      And I follow "Kickstart" in the left menu
      And I follow "Profiles" in the left menu
      And I follow "upload new kickstart file"
    When I enter "fedora_kickstart_profile_upload" as "kickstartLabel"
      And I attach the file "/example.ks" to "fileUpload"
      And I click on "Upload File"
    Then I should see a "Kickstart: fedora_kickstart_profile_upload" text
     And I should see a "Kickstart Details" text


   @cobbler_ui
   Scenario: adding a bare metal range to a profile (requires fedora_kickstart_profile)
     Given I am on the Systems page
      And I follow "Kickstart" in the left menu
      And I follow "Profiles" in the left menu
      And I follow "fedora_kickstart_profile"
      And I follow "Bare Metal Kickstart"
      And I enter "10" as "octet1a"
      And I enter "10" as "octet1b"
      And I enter "0" as "octet1c"
      And I enter "100" as "octet1d"
      And I enter "10" as "octet2a"
      And I enter "10" as "octet2b"
      And I enter "0" as "octet2c"
      And I enter "200" as "octet2d"
      And I click on "Add IP Range"
    Then I should see a "10.10.0.100 - 10.10.0.200" text

   @cobbler_ui
   Scenario: adding a variable to the uploaded profile (requires fedora_kickstart_profile_upload)
     Given I am on the Systems page
      And I follow "Kickstart" in the left menu
      And I follow "Profiles" in the left menu
      And I follow "fedora_kickstart_profile_upload"
      And I follow "Variables"
      And I enter "my_var=A_Test_String" as "variables"
      And I click on "Update Variables"
      And I follow "Autoinstallation File"
    Then I should see a "A_Test_String" text

   @cobbler_ui
   Scenario: adding a kernel option (requires fedora_kickstart_profile)
     Given I am on the Systems page
      And I follow "Kickstart" in the left menu
      And I follow "Profiles" in the left menu
      And I follow "fedora_kickstart_profile"
      And I enter "kernel_option=a_value" as "kernel_options"
      And I click on "Update Kickstart"
    Then file "/srv/tftpboot/pxelinux.cfg/default" contains "kernel_option=a_value"

   @cobbler_ui
   Scenario: adding a kernel option (requires fedora_kickstart_profile_upload)
     Given I am on the Systems page
      And I follow "Kickstart" in the left menu
      And I follow "Profiles" in the left menu
      And I follow "fedora_kickstart_profile_upload"
      And I enter "kernel_option2=a_value2" as "kernel_options"
      And I click on "Update"
    Then file "/srv/tftpboot/pxelinux.cfg/default" contains "kernel_option2=a_value2"

   @cobbler_ui 
   Scenario: checking default snippets
     Given I am on the Systems page
      And I follow "Autoinstallation" in the left menu
      And I follow "Autoinstallation Snippets" in the left menu
      And I follow "Default Snippets"
      And I click on "Next Page"
      And I follow "spacewalk/sles_no_signature_checks"
    Then I should see a "<signature-handling>" text

   @cobbler_ui 
   Scenario: create a snippet
     Given I am on the Systems page
      And I follow "Autoinstallation" in the left menu
      And I follow "Autoinstallation Snippets" in the left menu
      And I follow "create new snippet"
      And I enter "created_test_snippet" as "name"
      And I uncheck "edit_area_toggle_checkbox_contents"
      And I enter "<test_element>a text string</test_element>" as "contents"
      And I click on "Create Snippet"
   Then I should see a "created_test_snippet created successfully." text

   @cobbler_ui
   Scenario: delete a snippet (requires "create a snippet" test was run)
     Given I am on the Systems page
      And I follow "Autoinstallation" in the left menu
      And I follow "Autoinstallation Snippets" in the left menu
      And I follow "created_test_snippet"
      And I follow "delete snippet"
      And I click on "Delete Snippet"
   Then I should see a "created_test_snippet deleted successfully." text

   @pxe_env
   Scenario: testing for pxe environment files. Requires cobbler_ui tests to have run
     Given cobblerd is running
     Then file "/srv/tftpboot/pxelinux.cfg/default" exists on server
      And file "/srv/tftpboot/pxelinux.cfg/default" contains "ks=.*fedora_kickstart_profile:1"
      And file "/srv/tftpboot/pxelinux.cfg/default" contains "ks=.*fedora_kickstart_profile_upload:1"
      And file "/srv/tftpboot/images/fedora_kickstart_distro:1:Novell/initrd.img" exists on server
      And file "/srv/tftpboot/images/fedora_kickstart_distro:1:Novell/vmlinuz" exists on server



