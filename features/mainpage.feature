#
# Explore the main landing page
#
Feature: Explore the main landing page
  In Order to validate completeness of the landing page
  As a authorized user
  I want to see all the options and preferences
  Scenario: Completeness of the toplevel bar
    Given I am authorized
    When I go to the home page
    Then I should see a "English" text
    And I should see a "change" link
    And I should see a "Knowledgebase" link
    And I should see a "Documentation" link
    And I should see a "User" text
    And I should see a "Organization" text
    And I should see a "Preferences" link
    And I should see a "Sign Out" link
  Scenario: Completeness of the main navigation bar
    Given I am authorized
    When I go to the home page
    Then I should see a "Overview" link
    And I should see a "Systems" link
    And I should see a "Errata" link
    And I should see a "Channels" link
    And I should see a "Configuration" link
    And I should see a "Schedule" link
    And I should see a "Monitoring" link
    And I should see a "Help" link
  Scenario: Completeness of the left sidebar
    Given I am authorized
    When I go to the home page
    Then I should see a "Your Account" link
    And I should see a "Your Preferences" link
    And I should see a "Locale Preferences" link
  Scenario: Completeness of the main content
    Given I am authorized
    When I go to the home page
    Then I should see a "Tasks" text
    And I should see a "Inactive Systems" text
    And I should see a "Most Critical Systems" text
    And I should see a "Critical Probes" text
    And I should see a "Warning Probes" text
    And I should see a "Recently Scheduled Actions" text
    And I should see a "Relevant Security Errata" text
    And I should see a "System Group Name" text
    And I should see a "Recently Registered Systems" text
