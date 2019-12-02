# Copyright (c) 2010-2020 SUSE LLC.
# Licensed under the terms of the MIT license.

Feature: Cobbler and distribution autoinstallation

  Background:
    Given I am authorized with the feature's user

  Scenario: Ask cobbler to create a distribution via XML-RPC
    Given cobblerd is running
    Then create distro "testdistro" as user "testing" with password "testing"

  Scenario: Create dummy profile
    Given cobblerd is running
    And distro "testdistro" exists
    Then create profile "testprofile" as user "testing" with password "testing"

  Scenario: Check cobbler created distro and profile
    When I follow the left menu "Systems > Autoinstallation > Profiles"
    Then I should see a "testprofile" text
    And I should see a "testdistro" text

  Scenario: Create SUSE Distibution with installer updates
    When I follow the left menu "Systems > Autoinstallation > Distributions"
    And I follow "Create Distribution"
    And I enter "SLE-15-FAKE" as "label"
    And I enter "/install/SLES11-SP1-x86_64/DVD1/" as "basepath"
    And I select "SLE-Product-SLES15-Pool for x86_64" from "channelid"
    And I select "SUSE Linux Enterprise 15" from "installtype"
    And I click on "Create Autoinstallable Distribution"
    Then I should see a "Autoinstallable Distributions" text
    And I should see a "SLE-15-FAKE" link
    When I follow "SLE-15-FAKE"
    Then I should see "self_update=http://" in field "kernelopts"

  Scenario: Create a distribution via the UI
    When I follow the left menu "Systems > Autoinstallation > Distributions"
    And I follow "Create Distribution"
    When I enter "fedora_kickstart_distro" as "label"
    And I enter "/install/Fedora_12_i386/" as "basepath"
    And I select "Fedora" from "installtype"
    And I click on "Create Autoinstallable Distribution"
    Then I should see a "Autoinstallable Distributions" text
    And I should see a "fedora_kickstart_distro" link

  Scenario: Create a profile via the UI
    When I follow the left menu "Systems > Autoinstallation > Profiles"
    And I follow "Create Kickstart Profile"
    When I enter "fedora_kickstart_profile" as "kickstartLabel"
    And I click on "Next"
    And I click on "Next"
    And I enter "linux" as "rootPassword"
    And I enter "linux" as "rootPasswordConfirm"
    And I click on "Finish"
    Then I should see a "Autoinstallation: fedora_kickstart_profile" text
    And I should see a "Autoinstallation Details" link

  Scenario: Autoinstallation profiles page
    When I am on the Create Autoinstallation Profile page
    When I follow the left menu "Systems > Autoinstallation > Profiles"
    Then I should see a "Distributions" text

  Scenario: Upload a profile via the UI
    When I follow the left menu "Systems > Autoinstallation > Profiles"
    And I follow "Upload Kickstart/Autoyast File"
    When I enter "fedora_kickstart_profile_upload" as "kickstartLabel"
    And I attach the file "/example.ks" to "fileUpload"
    And I click on "Create"
    Then I should see a "Autoinstallation: fedora_kickstart_profile_upload" text
    And I should see a "Autoinstallation Details" text

  Scenario: Add an unprovisioned range to the created profile
    When I follow the left menu "Systems > Autoinstallation > Profiles"
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

  Scenario: Add a variable to the uploaded profile
    When I follow the left menu "Systems > Autoinstallation > Profiles"
    And I follow "fedora_kickstart_profile_upload"
    And I follow "Variables"
    And I enter "my_var=A_Test_String" as "variables"
    And I click on "Update Variables"
    And I follow "Autoinstallation File"
    Then I should see a "A_Test_String" text

  Scenario: Add a kernel option to the created profile
    When I follow the left menu "Systems > Autoinstallation > Profiles"
    And I follow "fedora_kickstart_profile"
    And I enter "kernel_option=a_value" as "kernel_options"
    And I click on "Update"
    And I wait until file "/srv/tftpboot/pxelinux.cfg/default" contains "kernel_option=a_value" on server

  Scenario: Add a kernel option to the uploaded profile
    When I follow the left menu "Systems > Autoinstallation > Profiles"
    And I follow "fedora_kickstart_profile_upload"
    And I enter "kernel_option2=a_value2" as "kernel_options"
    And I click on "Update"
    And I wait until file "/srv/tftpboot/pxelinux.cfg/default" contains "kernel_option2=a_value2" on server

  Scenario: Check default snippets
    When I follow the left menu "Systems > Autoinstallation > Autoinstallation Snippets"
    And I follow "Default Snippets"
    And I click on "Next Page"
    And I follow "spacewalk/sles_no_signature_checks"
    Then I should see "<signature-handling>" in the textarea

  Scenario: Create a snippet
    When I follow the left menu "Systems > Autoinstallation > Autoinstallation Snippets"
    And I follow "Create Snippet"
    And I enter "created_test_snippet" as "name"
    And I enter "<test_element>a text string</test_element>" in the editor
    And I click on "Create Snippet"
    Then I should see a "created_test_snippet created successfully." text

  Scenario: Delete a snippet
    When I follow the left menu "Systems > Autoinstallation > Autoinstallation Snippets"
    And I follow "created_test_snippet"
    And I follow "delete snippet"
    And I click on "Delete Snippet"
    Then I should see a "created_test_snippet deleted successfully." text

  Scenario: Test for PXE environment files
    Given cobblerd is running
    Then file "/srv/tftpboot/pxelinux.cfg/default" should exist on server
    When I wait until file "/srv/tftpboot/pxelinux.cfg/default" contains "ks=.*fedora_kickstart_profile:1" on server
    And I wait until file "/srv/tftpboot/pxelinux.cfg/default" contains "ks=.*fedora_kickstart_profile_upload:1" on server
    And I wait until file "/srv/tftpboot/images/fedora_kickstart_distro:1:SUSETest/initrd.img" exists on server
    And I wait until file "/srv/tftpboot/images/fedora_kickstart_distro:1:SUSETest/vmlinuz" exists on server
    And I wait until file "/srv/tftpboot/menu.c32" exists on server
    And I wait until file "/srv/tftpboot/pxelinux.0" exists on server

  Scenario: Trigger the creation of a cobbler system record
    When I trigger cobbler system record

  Scenario: Create a cobbler system record via XML-RPC
    When I am logged in via XML-RPC system with the feature's user
    And I create a System Record
    Then I wait until file "/srv/tftpboot/pxelinux.cfg/01-00-22-22-77-ee-cc" contains "ks=.*testserver:1" on server
    And the cobbler report should contain "testserver.example.com" for cobbler system name "testserver:1"
    And the cobbler report should contain "1.1.1.1" for cobbler system name "testserver:1"
    And the cobbler report should contain "00:22:22:77:ee:cc" for cobbler system name "testserver:1"

  Scenario: Cleanup: delete test distro and profiles
    Then I remove kickstart profiles and distros
