# Copyright (c) 2010-2017 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: test cobbler and distro Kickstart

  Background:
    Given I am authorized
    And I follow "Home" in the left menu
    And I follow "Systems" in the left menu
    And I follow "Overview" in the left menu

  Scenario: create a dummy distro with cobbler (not visible in UI, SLES)
    Given cobblerd is running
    Then create distro "testdistro" as user "testing" with password "testing"

  Scenario: create dummy profile
    Given cobblerd is running
    And distro "testdistro" exists
    Then create profile "testprofile" as user "testing" with password "testing"

  Scenario: Check cobbler created distro and profile Systems => Kickstart => Profiles
    When I follow "Autoinstallation" in the left menu
    And I follow "Profiles" in the left menu
    Then I should see a "testprofile" text
    And I should see a "testdistro" text

  Scenario: Create mock initrd if download via sumaform fails
    Then I create mock initrd if download fails

  Scenario: Create a distro with the UI (requires a base channel)
    When I follow "Autoinstallation" in the left menu
    And I follow "Distributions" in the left menu
    And I follow "Create Distribution"
    When I enter "fedora_kickstart_distro" as "label"
    And I enter "/install/Fedora_12_i386/" as "basepath"
    And I select "Fedora" from "installtype"
    And I click on "Create Autoinstallable Distribution"
    Then I should see a "Autoinstallable Distributions" text
   And I should see a "fedora_kickstart_distro" link

  Scenario: create a profile with the UI (requires a base channel)
    When I follow "Autoinstallation" in the left menu
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

  Scenario: test Upload Kickstart/Autoyast File page
    When I am on the Create Autoinstallation Profile page
    And I follow "Profiles" in the left menu
    Then I should see a "Distributions" text

  Scenario: upload a profile with the UI (requires a base channel)
    When I follow "Autoinstallation" in the left menu
    And I follow "Profiles" in the left menu
    And I follow "Upload Kickstart File"
    When I enter "fedora_kickstart_profile_upload" as "kickstartLabel"
    And I attach the file "/example.ks" to "fileUpload"
    And I click on "Create"
    Then I should see a "Kickstart: fedora_kickstart_profile_upload" text
    And I should see a "Kickstart Details" text

  Scenario: adding a unprovisioned range to a profile (requires fedora_kickstart_profile)
    When I follow "Autoinstallation" in the left menu
    And I follow "Profiles" in the left menu
    And I follow "fedora_kickstart_profile"
    And I follow "Unprovisioned Autoinstallation"
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

  Scenario: adding a variable to the uploaded profile (requires fedora_kickstart_profile_upload)
    When I follow "Autoinstallation" in the left menu
    And I follow "Profiles" in the left menu
    And I follow "fedora_kickstart_profile_upload"
    And I follow "Variables"
    And I enter "my_var=A_Test_String" as "variables"
    And I click on "Update Variables"
    And I follow "Autoinstallation File"
    Then I should see a "A_Test_String" text

  Scenario: adding a kernel option (requires fedora_kickstart_profile)
    When I follow "Autoinstallation" in the left menu
    And I follow "Profiles" in the left menu
    And I follow "fedora_kickstart_profile"
    And I enter "kernel_option=a_value" as "kernel_options"
    And I click on "Update"
    And I wait for "5" seconds
    Then file "/srv/tftpboot/pxelinux.cfg/default" contains "kernel_option=a_value"

  Scenario: adding a kernel option (requires fedora_kickstart_profile_upload)
    When I follow "Autoinstallation" in the left menu
    And I follow "Profiles" in the left menu
    And I follow "fedora_kickstart_profile_upload"
    And I enter "kernel_option2=a_value2" as "kernel_options"
    And I click on "Update"
    And I wait for "5" seconds
    Then file "/srv/tftpboot/pxelinux.cfg/default" contains "kernel_option2=a_value2"

  Scenario: checking default snippets
    When I follow "Autoinstallation" in the left menu
    And I follow "Autoinstallation Snippets" in the left menu
    And I follow "Default Snippets"
    And I click on "Next Page"
    And I follow "spacewalk/sles_no_signature_checks"
    Then I should see "<signature-handling>" in the textarea

  Scenario: create a snippet
    When I follow "Autoinstallation" in the left menu
    And I follow "Autoinstallation Snippets" in the left menu
    And I follow "Create Snippet"
    And I enter "created_test_snippet" as "name"
    And I enter "<test_element>a text string</test_element>" in the editor
    And I click on "Create Snippet"
    Then I should see a "created_test_snippet created successfully." text

  Scenario: delete a snippet (requires "create a snippet" test was run)
    When I follow "Autoinstallation" in the left menu
    And I follow "Autoinstallation Snippets" in the left menu
    And I follow "created_test_snippet"
    And I follow "delete snippet"
    And I click on "Delete Snippet"
    Then I should see a "created_test_snippet deleted successfully." text

  Scenario: testing for pxe environment files. Requires cobbler_ui tests to have run
    Given cobblerd is running
    Then file "/srv/tftpboot/pxelinux.cfg/default" exists on server
    And file "/srv/tftpboot/pxelinux.cfg/default" contains "ks=.*fedora_kickstart_profile:1"
    And file "/srv/tftpboot/pxelinux.cfg/default" contains "ks=.*fedora_kickstart_profile_upload:1"
    And file "/srv/tftpboot/images/fedora_kickstart_distro:1:SUSETest/initrd.img" exists on server
    And file "/srv/tftpboot/images/fedora_kickstart_distro:1:SUSETest/vmlinuz" exists on server
    And file "/srv/tftpboot/menu.c32" exists on server
    And file "/srv/tftpboot/pxelinux.0" exists on server

  Scenario: trigger the creation of a cobbler system record
    And I follow this "sle-client" link
    And I follow "Provisioning"
    And I click on "Create PXE installation configuration"
    And I click on "Continue"
    Then file "/srv/tftpboot/pxelinux.cfg/01-*" contains "ks="

  Scenario: cleanup distro clobber feature
  Given I am authorized
  When Cleanup for distro_clobber_feature
