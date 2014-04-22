# Copyright 2011-2014 Novell, Inc.

Feature: I want to verify the Setup Wizard


   Scenario: I want to test the proxy setup
      Given I am on the Admin page
        And I should see a "HTTP Proxy Hostname" text
        And I should see a "HTTP Proxy Username" text
        And I should see a "HTTP Proxy Password" text
       Then I click on "Edit" link in the setup wizard
        And I click on "Save and Verify"

   Scenario: I want to test the credentials setup
      Given I am on the Admin page
        And I follow "Mirror Credentials" in the content area
        And I want to add a new credential
        And I enter "test@suse.com" as "edit-email"
        And I enter "asdf" as "edit-user"
        And I enter "asdf" as "edit-password"
        And I click on "Save"
        And I should see a "asdf" text
        And I should see a "test@suse.com" text
        And I make the credentials primary
        And I view the primary subscription list
        And I should see a "No subscriptions available" text
        And I click on "Close"
        And I delete the primary credentials
        And I view the primary subscription list
        And I should not see a "No subscriptions available" text
        And I click on "Close"
        And I should not see a "asdf" text
        And I should not see a "test@suse.com" text

   Scenario: I want to test the products page
     Given I am on the Admin page
       And I follow "Admin" in the tab bar
       And I follow "SUSE Products" in the content area
       # HACK: this should not be needed at all, but Capybara 2.1.0/WebDriver loses browser
       # connection if next requests arrive concurrently with the AJAX request. Might be removed
       # in future versions
       And I wait for "30" seconds
       And I should see a "Available Products Below" text
       And I should see a "Architecture" text
       And I should see a "Status" text
       And I should not see a "WebYaST 1.3" text
       And I select "SUSE Linux Enterprise Server 11 SP3 VMWare" as a product for the "x86_64" architecture
       And I should see a "WebYaST 1.3" text
       And I select "WebYaST 1.3" as a product for the "x86_64" architecture
       And I click the Add Product button
       And I wait until it has finished
       And I verify the products were added
