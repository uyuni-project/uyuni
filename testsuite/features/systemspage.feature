# Copyright (c) 2010-2016 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Explore the main landing page
  In Order to validate completeness of the systems page
  As a authorized user
  I want to see all the texts and links

  Scenario: Wait for refresh of list of products to finish
    When I wait until mgr-sync refresh is finished

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
    And I should see a "Software Crashes" link in the left menu
    And I should see a "Download CSV" link
    And I should see a Sign Out link

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
    And I should see a "Physical Systems" link in the left menu
    And I should see a "Virtual Systems" link in the left menu
    And I should see a "Out of Date" link in the left menu
    And I should see a "Requiring Reboot" link in the left menu
    And I should see a "Non Compliant" link in the left menu
    And I should see a "Without System Type" link in the left menu
    And I should see a "Ungrouped" link in the left menu
    And I should see a "Inactive" link in the left menu
    And I should see a "Recently Registered" link in the left menu
    And I should see a "Proxy" link in the left menu
    And I should see a "Duplicate Systems" link in the left menu
    And I should see a "System Currency" link in the left menu
    And I should see a "System Types" link in the left menu
    And I should see a "Systems" text

  Scenario: Check sidebar link destination for Systems => Physical Systems
    Given I am on the Systems page
    And I follow "Systems" in the left menu
    And I follow "Physical Systems" in the left menu
    Then I should see a "Physical Systems" text
    And I should see a "No systems." text
    And the current path is "/rhn/systems/PhysicalList.do"

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

  Scenario: Check sidebar link destination for Systems => Requiring Reboot
    Given I am on the Systems page
    And I follow "Systems" in the left menu
    And I follow "Requiring Reboot" in the left menu
    Then I should see a "Systems Requiring Reboot" text
    And I should see a "No systems." text
    And the current path is "/rhn/systems/RequiringReboot.do"

  Scenario: Check sidebar link destination for Systems => Non Compliant
    Given I am on the Systems page
    And I follow "Systems" in the left menu
    And I follow "Non Compliant" in the left menu
    Then I should see a "Non Compliant Systems" text
    And I should see a "No systems." text
    And the current path is "/rhn/systems/ExtraPackagesSystems.do"

  Scenario: Check sidebar link destination for Systems => Without System Type
    Given I am on the Systems page
    And I follow "Systems" in the left menu
    And I follow "Without System Type" in the left menu
    Then I should see a "Systems without System Type" text
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

  Scenario: Check sidebar link destination for Systems => Proxy
    Given I am on the Systems page
    And I follow "Systems" in the left menu
    And I follow "Proxy" in the left menu
    Then I should see a "Proxy Servers" text
    And I should see a "No systems." text
    And the current path is "/rhn/systems/ProxyList.do"

  Scenario: Check sidebar link destination for Systems => Duplicate Systems
    Given I am on the Systems page
    And I follow "Systems" in the left menu
    And I follow "Duplicate Systems" in the left menu
    Then I should see a "Duplicate Systems" text
    And I should see a "No systems." text
    And the current path is "/rhn/systems/DuplicateIPList.do"
    And I should see a "Duplicate IP Address" link
    And I should see a "Duplicate Hostname" link
    And I should see a "Duplicate IPv6 Address" link
    And I should see a "Duplicate MAC Address" link

  Scenario: Check sidebar link destination for Systems => System Currency
    Given I am on the Systems page
    And I follow "Systems" in the left menu
    And I follow "System Currency" in the left menu
    Then I should see a "System Currency Report" text
    And I should see a "No systems." text
    And the current path is "/rhn/systems/SystemCurrency.do"

  Scenario: Check sidebar link destination for Systems => System Types
    Given I am on the Systems page
    And I follow "Systems" in the left menu
    And I follow "System Types" in the left menu
    Then I should see a "System Types" text
    And I should see a "Management:" text
    And I should see a "Salt:" text
    And I should see a "Foreign:" text
    And I should see a "Virtualization Host:" text
    And the current path is "/rhn/systems/SystemEntitlements.do"

  Scenario: Check sidebar link destination for Systems => System Groups
    Given I am on the Systems page
    And I follow "System Groups" in the left menu
    Then I should see a "System Groups" text
    And I should see a "Create Group" link
    And I should see a "Your organization has no system groups." text

  @failed
  Scenario: Check sidebar link destination for Systems => System Set Manager
    Given I am on the Systems page
    And I follow "System Set Manager" in the left menu
    Then I should see a "System Set Manager" text
    And I should see a "Task Log" link in the left menu
    And I should see a "Overview" link in the content area
    And I should see a "Systems" link in the content area
    And I should see a "Errata" link in the content area
    And I should see a "Packages" link in the content area
    And I should see a "Groups" link in the content area
    And I should see a "Channels" link in the content area
    And I should see a "Configuration" link in the content area
    And I should see a "Provisioning" link in the content area
    And I should see a "Misc" link in the content area

  Scenario: Check sidebar link destination for Systems => Advanced Search
    Given I am on the Systems page
    And I follow "Advanced Search" in the left menu
    Then I should see a "Advanced Search" text

  Scenario: Check sidebar link destination for Systems => Activation Keys
    Given I am on the Systems page
    And I follow "Activation Keys" in the left menu
    Then I should see a "Activation Keys" text
    And I should see a "Create Key" link
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
    And I should see a "Create Key" link
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
    And I should see a "Create Kickstart Profile" link
    And I should see a "Upload Kickstart File" link
    And I should see a "View a List of Kickstart Profiles" link
    And I should see a "Create a New Kickstart Profile" link
    And I should see a "Upload a New Kickstart File" link

  Scenario: Check sidebar link destination for Systems => Kickstart => Profiles
    Given I am on the Systems page
    And I follow "Kickstart" in the left menu
    And I follow "Profiles" in the left menu
    Then I should see a "Kickstart Profiles" text
    And I should see a "Create Kickstart Profile" link
    And I should see a "Upload Kickstart File" link

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
    And I should see a "Create Stored Key/Cert" link
    And I should see a "RHN Reference Guide" link
    And I should see a "RHN-ORG-TRUSTED-SSL-CERT" link

  Scenario: Check sidebar link destination for Systems => Kickstart => Distributions
    Given I am on the Systems page
    And I follow "Kickstart" in the left menu
    And I follow "Distributions" in the left menu
    Then I should see a "Kickstartable Distributions" text
    And I should see a "No kickstartable distributions available." text
    And I should see a "Create Distribution" link

  Scenario: Check sidebar link destination for Systems => Kickstart => File Preservation
    Given I am on the Systems page
    And I follow "Kickstart" in the left menu
    And I follow "File Preservation" in the left menu
    Then I should see a "File Preservation" text
    And I should see a "RHN Reference Guide" link
    And I should see a "Create File Preservation List" link

  Scenario: Check sidebar link destination for Systems => Kickstart => Kickstart Snippets
    Given I am on the Systems page
    And I follow "Kickstart" in the left menu
    And I follow "Autoinstallation Snippets" in the left menu
    Then I should see a "Autoinstallation Snippets" text
    And I should see a "No autoinstallation snippets found." text
    And I should see a "Create Snippet" link
    And I should see a "Default Snippets" link in the content area
    And I should see a "Custom Snippets" link in the content area
    And I should see a "All Snippets" link in the content area

  Scenario: Check "Create Kickstart Profile" page Systems => Kickstart => Profiles => Create Kickstart Profile
    Given I am on the Systems page
    And I follow "Kickstart" in the left menu
    And I follow "Profiles" in the left menu
    And I follow "Create Kickstart Profile"
    Then I should see a "Step 1: Create Kickstart Profile" text

  Scenario: Check "Upload Kickstart File" page Systems => Kickstart => Profiles => Upload Kickstart File
    Given I am on the Systems page
    And I follow "Kickstart" in the left menu
    And I follow "Profiles" in the left menu
    And I follow "Upload Kickstart File"
    Then I should see a "Create Autoinstallation Profile" text
     And I should see a "File Contents:" text
     And I should see a "Kickstart Details" text

  Scenario: Check "create kickstart distribution" page Systems => Kickstart => Distributions => create new kickstart distribution
    Given I am on the Systems page
    And I follow "Kickstart" in the left menu
    And I follow "Distributions" in the left menu
    And I follow "Create Distribution"
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
    And I follow "Create Distribution"
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
    And I follow "Create Kickstart Profile"
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
    And I follow "Upload Kickstart File"
    When I enter "fedora_kickstart_profile_upload" as "kickstartLabel"
    And I attach the file "/example.ks" to "fileUpload"
    And I click on "Create"
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
    Then I should see a "Successfully added IP Range" text

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
    And I click on "Update"
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
    #And I wait for "1" seconds
    And I click on "Next Page"
    And I follow "spacewalk/sles_no_signature_checks"
    Then I should see "<signature-handling>" in the textarea

   @cobbler_ui 
  Scenario: create a snippet
    Given I am on the Systems page
    And I follow "Autoinstallation" in the left menu
    And I follow "Autoinstallation Snippets" in the left menu
    And I follow "Create Snippet"
    And I enter "created_test_snippet" as "name"
    And I enter "<test_element>a text string</test_element>" in the editor
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
    And file "/srv/tftpboot/images/fedora_kickstart_distro:1:SUSETest/initrd.img" exists on server
    And file "/srv/tftpboot/images/fedora_kickstart_distro:1:SUSETest/vmlinuz" exists on server
    #bug regression for #668908
    And file "/srv/tftpboot/menu.c32" exists on server
    And file "/srv/tftpboot/pxelinux.0" exists on server
