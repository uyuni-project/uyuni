# Copyright 2017-2021 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Organization credentials in the Setup Wizard

@scc_credentials
  Scenario: Enter valid SCC credentials
    Given I am on the Admin page
    When I follow "Organization Credentials" in the content area
    And I ask to add new credentials
    And I enter the SCC credentials
    And I click on "Save"
    Then the SCC credentials should be valid
