# Copyright (c) 2023-2024 SUSE LLC.
# Licensed under the terms of the MIT license.
#
# Motivated by bsc#1207532 - Internal Server Error editing cobbler profiles
# This feature is split up into 2 sections: the first section uses the web UI,
# the second one uses the XML-RPC API

@scope_cobbler
Feature: Edit Cobbler profiles

  Background: The Cobbler service should be running
    Given cobblerd is running

  Scenario: Copy cobbler profiles on the server
    When I copy autoinstall mocked files on server
    And I backup Cobbler settings file

  Scenario: Log in as testing user
    Given I am authorized as "testing" with password "testing"

  Scenario: Start Cobbler monitoring
    When I start local monitoring of Cobbler

  ## UI tests
  Scenario: Create a Cobbler distribution via the UI
    When I follow the left menu "Systems > Autoinstallation > Distributions"
    And I follow "Create Distribution"
    When I enter "isedistro_ui" as "label"
    And I enter "/var/autoinstall/Fedora_12_i386/" as "basepath"
    And I select "Fedora" from "installtype"
    And I click on "Create Autoinstallable Distribution"
    Then I should see a "Autoinstallable Distributions" text
    And I should see a "isedistro_ui" link

  Scenario: Create a Cobbler profile via the UI
    When I follow the left menu "Systems > Autoinstallation > Profiles"
    And I follow "Create Kickstart Profile"
    When I enter "iseprofile_ui" as "kickstartLabel"
    And I click on "Next"
    And I click on "Next"
    And I enter "linux" as "rootPassword"
    And I enter "linux" as "rootPasswordConfirm"
    And I click on "Finish"
    Then I should see a "Autoinstallation: iseprofile_ui" text
    And I should see a "Autoinstallation Details" link

  Scenario: Cobbler created distro and profile via the UI
    When I follow the left menu "Systems > Autoinstallation > Profiles"
    Then I should see a "iseprofile_ui" text
    And I should see a "isedistro_ui" text

  Scenario: Change profile variables using the UI
    When I follow the left menu "Systems > Autoinstallation > Profiles"
    And I follow "iseprofile_ui"
    And I follow "Variables"
    And I enter "ise_ui_test=ISE_UI_TEST" as "variables"
    And I click on "Update Variables"
    And I refresh the page
    Then I should see a "ISE_UI_TEST" text

  ## XML-RPC API tests
  Scenario: Create a Cobbler distribution via the UI in the XML-RPC context
    When I follow the left menu "Systems > Autoinstallation > Distributions"
    And I follow "Create Distribution"
    When I enter "isedistro_api" as "label"
    And I enter "/var/autoinstall/Fedora_12_i386/" as "basepath"
    And I select "Fedora" from "installtype"
    And I click on "Create Autoinstallable Distribution"
    Then I should see a "Autoinstallable Distributions" text
    And I should see a "isedistro_api" link

  Scenario: Create a Cobbler profile via the UI in the XML-RPC context
    When I follow the left menu "Systems > Autoinstallation > Profiles"
    And I follow "Create Kickstart Profile"
    When I enter "iseprofile_api" as "kickstartLabel"
    And I click on "Next"
    And I click on "Next"
    And I enter "linux" as "rootPassword"
    And I enter "linux" as "rootPasswordConfirm"
    And I click on "Finish"
    Then I should see a "Autoinstallation: iseprofile_api" text
    And I should see a "Autoinstallation Details" link

  Scenario: Cobbler created distro and profile via the UI in the XML-RPC context
    When I follow the left menu "Systems > Autoinstallation > Profiles"
    Then I should see a "iseprofile_api" text
    And I should see a "isedistro_api" text

  Scenario: Create a Cobbler system via the XML-RPC API
    When I create a system record with name "isesystem_api" and kickstart label "iseprofile_api"

  Scenario: Create and modify a System profile using the XML-RPC API
    When I create and modify the kickstart system "isesystem_api" with kickstart label "iseprofile_api" and hostname "ise-system.test" via XML-RPC
      | inst.repo   | http://ise.cobbler.test |
      | self_update | http://ise.cobbler.test |

  Scenario: Cleanup: delete test distros and profiles
    When I remove kickstart profiles and distros
    And I follow the left menu "Systems > System List"
    And I wait until I see the "isesystem_api" system, refreshing the page
    And I follow "isesystem_api"
    And I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text

  Scenario: Check for errors in Cobbler monitoring
    Then the local logs for Cobbler should not contain errors
