# feature/configuration.feature
@javascript
Feature: Check configuration page/tab
  Validate configuration page accessibility 

  Scenario: Check configuration page content
    Given I am authorized as "admin" with password "admin"
    When I go to the configuration page
    Then I should see a "Configuration Overview" text
     And I should see a "Configuration Summary" text
     And I should see a "Configuration Actions" text
     And I should see a "Systems with Managed Configuration Files" text
     And I should see a "Configuration Channels" text
     And I should see a "Centrally-managed Configuration Files" text
     And I should see a "Locally-managed Configuration Files" text
     And I should see a "Overview" link in "sidenav"
     And I should see a "Configuration Channels" link in "sidenav"
     And I should see a "Configuration Files" link in "sidenav"
     And I should see a "Systems" link in "sidenav"
     And I should see a "Manage" link
     And I should see a "Clear" link
     And I should see a "View Systems with Managed Configuration Files" link
     And I should see a "View All Managed Configuration Files" link
     And I should see a "View All Managed Configuration Channels" link
     And I should see a "Create a New Configuration Channel" link
     And I should see a "Enable Configuration Management on Systems" link

