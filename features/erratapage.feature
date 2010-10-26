#
# Explore the systems page
#

Feature: Explore the main landing page
  In Order to validate completeness of the errata page
  As a authorized user
  I want to see all the texts and links

  @errata
  Scenario: Completeness of the side navigation bar and the content frame
    Given I am authorized
    When I follow "Errata"
    Then I should see a "Errata Overview" text
     And I should see a "Relevant" link in the left menu
     And I should see a "All" link in the left menu
     And I should see a "Advanced Search" link in the left menu
     And I should see a "Manage Errata" link in the left menu
     And I should see a "Clone Errata" link in the left menu
     And I should see a "Bugfix Errata" link
     And I should see a "Enhancement Errata" link
     And I should see a "Security Errata" link
     And I should see a "Sign Out" link

  @errata
  Scenario: Completeness of the main navigation bar
    Given I am authorized
    When I follow "Errata"
    Then I should see a "Overview" link in the tab bar
     And I should see a "Systems" link in the tab bar
     And I should see a "Errata" link in the tab bar
     And I should see a "Channels" link in the tab bar
     And I should see a "Audit" link in the tab bar
     And I should see a "Configuration" link in the tab bar
     And I should see a "Schedule" link in the tab bar
     And I should see a "Users" link in the tab bar
     And I should see a "Monitoring" link in the tab bar
     And I should see a "Help" link in the tab bar

  @errata
  Scenario: Create new Erratum with bnc URL
    Given I am on the Errata page
    And I follow "Manage Errata" in the left menu
    And I follow "create new erratum"
    When I enter "Test Erratum" as "synopsis"
    And I enter "Test Advisory" as "advisoryName"
    And I enter "Test Product" as "product"
    And I enter "Test Topic" as "topic"
    And I enter "Test Description" as "description"
    And I enter "Test Solution" as "solution"
    And I enter "620212" as "buglistId"
    And I enter "Test Summary" as "buglistSummary"
    And I enter "https://bugzilla.novell.com/show_bug.cgi?id=620212" as "buglistUrl"
    And I enter "test,keywords" as "keywords"
    And I enter "Test Reference" as "refersTo"
    And I enter "Test Note" as "notes"
    And I click on "Create Errata"
    Then I should see a "Errata Test Advisory-1 created." text

  @errata_w
  Scenario: Publish erratum
    Given I am on the Errata page
    And I follow "Manage Errata" in the left menu
    And I follow "Unpublished" in the left menu
    And I follow "Test Advisory"
    And I click on "Publish Errata"
    And I check test channel
    And I click on "Publish Errata"
    Then I should see a "All Errata" text
    And I should see a "Test Erratum" text

  @errata_w
  Scenario: Delete erratum
    Given I am on the Errata page
    And I follow "Manage Errata" in the left menu
    And I check test erratum
    And I click on "Delete Errata"
    And I click on "Confirm"
    Then I should see a "Successfully deleted 1 errata." text

