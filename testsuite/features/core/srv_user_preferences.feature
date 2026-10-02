# Copyright (c) 2021 SUSE LLC
# SPDX-License-Identifier: MIT

Feature: Change personal preferences
  In order to set up my personal preferences
  As admin
  I want to navigate through "Home" submenus changing some settings

  Scenario: Change page size to 100 per page in admin user
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Home > My Preferences"
    And I select "100" from "pagesize"
    And I click on "Save Preferences"
    Then I should see a "Preferences modified" text

  Scenario: Change page size to 100 per page in testing user
    Given I am authorized as "testing" with password "testing"
    When I follow the left menu "Home > My Preferences"
    And I select "100" from "pagesize"
    And I click on "Save Preferences"
    Then I should see a "Preferences modified" text
