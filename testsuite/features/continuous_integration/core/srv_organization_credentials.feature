# Copyright 2017-2023 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature is a dependency for all features and scenarios that include the @scc_credentials tag

Feature: Organization credentials in the Setup Wizard

@scc_credentials
  Scenario: Enter valid SCC credentials
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Organization Credentials"
    And I ask to add new credentials
    And I enter the SCC credentials
    And I click on "Save"
    And I wait until the SCC credentials are valid
