# feature/configuration.feature
@javascript
Feature: Check configuration page/tab
  Validate configuration page accessibility 

  Scenario: Check configuration page content
    Given I am authorized as "admin" with password "admin"
    When I go to the configuration page
    Then I should see a "Configuration Overview" text
     And I should see a "Overview" link in "sidenav"
     And I should see a "Configuration Channels" link in "sidenav"
     And I should see a "Configuration Files" link in "sidenav"
     And I should see a "Systems" link in "sidenav"

