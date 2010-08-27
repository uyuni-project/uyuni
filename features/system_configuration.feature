# features/system_configuration.feature
Feature: System configuration
  In Order to subscribe a system to a configuration channel
  As an admin user
  I want to go to the systems configuration page
  Scenario: Accessing system configuration
    Given I am authorized
    When I follow "Systems"
    Then I should see "System Overview"
    And I should see "manteltest.suse.de"
 