# Copyright 2011-2014 Novell, Inc.

Feature: I want to verify the Setup Wizard

   Scenario: I want to test the proxy setup
      Given I am on the Admin page
      And I wait for "30" seconds
      And I should see a "HTTP Proxy Hostname" text
      And I should see a "HTTP Proxy Username" text
      And I should see a "HTTP Proxy Password" text
     Then I click on "Edit" link in the setup wizard
      And I click on "Save and Verify"
      And I wait for "300" seconds
