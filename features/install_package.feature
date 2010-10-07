Feature: Install a package to the client

  Scenario: Install a package to the client
    Given I am on the Systems overview page of this client
     And I follow "Software" in class "content-nav"
     And I follow "Install"
    When I check "sles-manuals_en-pdf-11.1-16.20.1" in the list 
     And I click on "Install Selected Packages"
     And I click on "Confirm"
     And I run rhn_check on this client
    Then I should see a "1 package install has been scheduled for" text
     And "sles-manuals_en-pdf-11.1-16.20.1" is installed
