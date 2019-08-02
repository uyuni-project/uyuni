# Copyright (c) 2019 SUSE LLC
# Licensed under the terms of the MIT license.
#

Feature: Monitor SUSE Manager server

  # This assumes that monitoring is enabled via sumaform
  Scenario: Disable monitoring from the UI
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Admin > Manager Configuration > Monitoring"
    And I wait until I do not see "Checking services..." text
    And I click on "Disable services"
    And I wait until I do not see "Disabling services..." text
    Then I should see a "Monitoring disabled successfully." text
    And I should see a list item with text "System" and bullet with "danger" icon
    And I should see a list item with text "PostgreSQL database" and bullet with "danger" icon
    And I should see a list item with text "Taskomatic (Java JMX)" and bullet with "danger" icon
    And I should see a list item with text "Tomcat (Java JMX)" and bullet with "danger" icon
    And I should see a "Enable services" button

  Scenario: Check that monitoring is disabled using the UI
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Admin > Manager Configuration > Monitoring"
    And I wait until I do not see "Checking services..." text
    Then I should see a list item with text "System" and bullet with "danger" icon
    And I should see a list item with text "PostgreSQL database" and bullet with "danger" icon
    And I should see a list item with text "Taskomatic (Java JMX)" and bullet with "danger" icon
    And I should see a list item with text "Tomcat (Java JMX)" and bullet with "danger" icon
    And I should see a "Enable services" button

  Scenario: Enable monitoring from the UI
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Admin > Manager Configuration > Monitoring"
    And I wait until I do not see "Checking services..." text
    And I click on "Enable services"
    And I wait until I do not see "Enabling services..." text
    Then I should see a "Monitoring enabled successfully." text
    And I should see a list item with text "System" and bullet with "success" icon
    And I should see a list item with text "PostgreSQL database" and bullet with "success" icon
    And I should see a list item with text "Taskomatic (Java JMX)" and bullet with "success" icon
    And I should see a list item with text "Tomcat (Java JMX)" and bullet with "success" icon
    And I should see a "Disable services" button
