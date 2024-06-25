# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_retracted_patches
Feature: Retracted patches

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Installed retracted package should show icon in the system packages list
    When I install package "rute-dummy=2.1-1.1" on this "sle_minion"
    And I refresh packages list via spacecmd on "sle_minion"
    And I wait until refresh package list on "sle_minion" is finished
    And I am on the "Software" page of this "sle_minion"
    And I follow "Packages"
    And I follow "List / Remove"
    And I enter "rute-dummy" as the filtered package name
    And I click on the filter button until page does contain "rute-dummy" text
    Then the table row for "rute-dummy-2.1-1.1" should contain "retracted" icon
    When I remove package "rute-dummy" from this "sle_minion"
    And I wait until package "rute-dummy" is removed from "sle_minion" via spacecmd

  Scenario: Retracted package should not be available for installation
    When I am on the "Software" page of this "sle_minion"
    And I follow "Packages"
    And I follow "Install"
    And I enter "rute-dummy" as the filtered package name
    And I click on the filter button until page does contain "rute-dummy" text
    Then I should see a "rute-dummy-2.0-1.2" text
    And I should not see a "rute-dummy-2.1-1.1" text

  Scenario: Retracted package should not be available for upgrade
    When I install old package "rute-dummy=2.0-1.2" on this "sle_minion"
    And I refresh packages list via spacecmd on "sle_minion"
    And I wait until refresh package list on "sle_minion" is finished
    And I am on the "Software" page of this "sle_minion"
    And I follow "Packages"
    And I follow "Upgrade"
    Then I should not see a "rute-dummy-2.1-1.1" text
    When I remove package "rute-dummy" from this "sle_minion"
    And I wait until package "rute-dummy" is removed from "sle_minion" via spacecmd

  Scenario: Retracted patch should not affect any system
    When I install package "rute-dummy=2.0-1.2" on this "sle_minion"
    And I refresh packages list via spacecmd on "sle_minion"
    And I wait until refresh package list on "sle_minion" is finished
    And I follow the left menu "Software > Channel List > All"
    And I follow "Show All Child Channels"
    And I follow "Fake-RPM-SUSE-Channel"
    And I follow "Patches" in the content area
    And I follow "rute-dummy-0817"
    And I follow "Affected Systems"
    Then I should see a "No systems." text
    When I remove package "rute-dummy" from this "sle_minion"
    And I wait until package "rute-dummy" is removed from "sle_minion" via spacecmd
   
  Scenario: Target systems for stable packages should not be empty
    When I follow the left menu "Software > Channel List > All"
    And I follow "Show All Child Channels"
    And I follow "Fake-RPM-SUSE-Channel"
    And I follow "Packages" in the content area
    And I follow "rute-dummy-2.0-1.2.x86_64"
    And I follow "Target Systems"
    And I refresh page until I see "sle_minion" hostname as text
   
  Scenario: Target systems for retracted packages should be empty
    When I follow the left menu "Software > Channel List > All"
    And I follow "Show All Child Channels"
    And I follow "Fake-RPM-SUSE-Channel"
    And I follow "Packages" in the content area
    And I follow "rute-dummy-2.1-1.1.x86_64"
    And I follow "Target Systems"
    Then I should not see "sle_minion" hostname

  Scenario: Retracted packages in the patch detail
    When I follow the left menu "Patches > Patch List > All"
    And I enter "dummy" as the filtered synopsis
    And I click on the filter button
    And I follow "rute-dummy-0815"
    Then I should see a "Status: Retracted" text
    When I go back
    And I enter "dummy" as the filtered synopsis
    And I click on the filter button
    And I follow "rute-dummy-0816"
    Then I should see a "Status: Stable" text
    When I go back
    And I enter "dummy" as the filtered synopsis
    And I click on the filter button
    And I follow "rute-dummy-0817"
    Then I should see a "Status: Retracted" text

  Scenario: Retracted packages in the patches list
    When I follow the left menu "Patches > Patch List > All"
    And I enter "dummy" as the filtered synopsis
    And I click on the filter button
    Then the table row for "rute-dummy-0815" should contain "retracted" icon
    And the table row for "rute-dummy-0816" should not contain "retracted" icon
    And the table row for "rute-dummy-0817" should contain "retracted" icon

  Scenario: Retracted patches in the channel patches list
    When I follow the left menu "Software > Channel List > All"
    And I follow "Show All Child Channels"
    And I follow "Fake-RPM-SUSE-Channel"
    And I follow "Patches" in the content area
    Then the table row for "rute-dummy-0815" should contain "retracted" icon
    And the table row for "rute-dummy-0816" should not contain "retracted" icon
    And the table row for "rute-dummy-0817" should contain "retracted" icon
 
  Scenario: Retracted packages in the channel packages list
    When I follow the left menu "Software > Channel List > All"
    And I follow "Show All Child Channels"
    And I follow "Fake-RPM-SUSE-Channel"
    And I follow "Packages" in the content area
    Then the table row for "rute-dummy-2.0-1.1.x86_64" should contain "retracted" icon
    Then the table row for "rute-dummy-2.0-1.2.x86_64" should not contain "retracted" icon
    Then the table row for "rute-dummy-2.1-1.1.x86_64" should contain "retracted" icon

  Scenario: SSM: Retracted package should not be available for installation
    When I follow the left menu "Systems > System List > All"
    And I click on the clear SSM button
    And I check the "sle_minion" client 
    And I follow the left menu "Systems > System Set Manager > Overview"
    And I follow "Packages" in the content area
    And I follow "Install"
    And I follow "Fake-RPM-SUSE-Channel"
    Then I should see a "rute-dummy-2.0-1.2" text
    And I should not see a "rute-dummy-2.1-1.1" text
    And I click on the clear SSM button
