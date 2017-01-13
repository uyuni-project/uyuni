# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Check configuration page/tab
  Validate configuration page accessibility

  Background:
   Given I am authorized as "admin" with password "admin"
   And I follow "Home" in the left menu
   And I follow "Configuration" in the left menu
   And I follow "Overview" in the left menu

  Scenario: Check configuration page content
   Then I should see a "Configuration Overview" text
    And I should see a "Configuration Summary" text
    And I should see a "Configuration Actions" text
    And I should see a "Systems with Managed Configuration Files" text
    And I should see a "Configuration Channels" text
    And I should see a "Centrally-managed Configuration Files" text
    And I should see a "Locally-managed Configuration Files" text
    And I should see a "Overview" link in the left menu
    And I should see a "Configuration Channels" link in the left menu
    And I should see a "Configuration Files" link in the left menu
    And I should see a "Systems" link in the left menu
    And I should see a "View Systems with Managed Configuration Files" link
    And I should see a "View All Managed Configuration Files" link
    And I should see a "View All Managed Configuration Channels" link
    And I should see a "Create a New Configuration Channel" link
    And I should see a "Enable Configuration Management on Systems" link

  Scenario: Check "View Systems with Managed Configuration Files"
    When I follow "View Systems with Managed Configuration Files"
    Then I should see a "Managed Systems" link in the left menu
    And I should see a "Target Systems" link in the left menu

  Scenario: Check "View All Managed Configuration Files"
    When I follow "View All Managed Configuration Files"
    Then I should see a "Centrally Managed Files" link in the left menu
    And I should see a "Locally Managed Files" link in the left menu

  Scenario: Check "View All Managed Configuration Channels"
    When I follow "View All Managed Configuration Channels"
    Then I should see a "Create Config Channel" link

  Scenario: Check "Enable Configuration Management on Systems"
    When I follow "Enable Configuration Management on Systems"
    Then I should see a "Managed Systems" link in the left menu
    And I should see a "Target Systems" link in the left menu

  Scenario: Check "Create a New Configuration Channel"
    When I follow "Create a New Configuration Channel"
    Then I should see a "New Config Channel" text
    And I should see a "You must enter the configuration channel details below." text
