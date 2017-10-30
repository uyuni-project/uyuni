# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: CVE audit for content management
  I want to see images that need to be patched or not 

  Background:
    Given I am authorized as "admin" with password "admin"

  Scenario: Schedule channel data refresh for content management
    When I follow "Admin"
    And I follow "Task Schedules"
    And I follow "cve-server-channels-default"
    And I follow "cve-server-channels-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I reload the page
    And I reload the page until it does contain a "FINISHED" text in the table first row

  Scenario: Audit images, searching for a known CVE number
    When I follow "Audit" in the left menu
    And I follow "CVE Audit" in the left menu
    And I select "1999" from "cveIdentifierYear"
    And I enter "9999" as "cveIdentifierId"
    And I click on "Audit Images"
    Then I should see a "No action required" text

  Scenario: Audit images, searching for an unknown CVE number
    When I follow "Audit" in the left menu
    And I follow "CVE Audit" in the left menu
    And I select "2012" from "cveIdentifierYear"
    And I enter "2806" as "cveIdentifierId"
    And I click on "Audit Images"
    Then I should see a "The specified CVE number was not found" text
