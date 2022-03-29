# Copyright (c) 2022 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_reportdb
Feature: ReportDB
  In order to use reporting tools
  As an authorized user
  I want to be able to access and use the Report Database named "reportdb"

Scenario: Log in as admin user
  Given I am authorized for the "Admin" section

Scenario: Populate the report database after bootstrapping minions
  When I schedule a task to update ReportDB

Scenario: Connect to the ReportDB on the server with admin user
  Given I can connect to the ReportDB on the Server
  And I have a user with admin access to the ReportDB

Scenario: Create read-only user
  When I create a read-only user for the ReportDB
  Then I should see the read-only user listed on the ReportDB user accounts

Scenario: External read-only user can connect to ReportDB and make queries
  When I connect to the ReportDB with read-only user from external machine
  Then I should be able to query the ReportDB

Scenario: Read-only user can't make changes in the ReportDB
  Then I should not be able to "insert" data in a ReportDB "table" as a read-only user
  And I should not be able to "update" data in a ReportDB "table" as a read-only user
  And I should not be able to "delete" data in a ReportDB "table" as a read-only user
  And I should not be able to "insert" data in a ReportDB "view" as a read-only user
  And I should not be able to "update" data in a ReportDB "view" as a read-only user
  And I should not be able to "delete" data in a ReportDB "view" as a read-only user

Scenario: ReportDB admin user can't access product database from external machine
  Given I know the ReportDB admin user credentials
  Then I should be able to connect to the ReportDB with the ReportDB admin user
  And I should not be able to connect to product database with the ReportDB admin user

Scenario: The systems should match between the UI and the ReportDB
  When I follow the left menu "Systems > Overview"
  And I make a list of the existing systems
  Then I should find the systems from the UI in the ReportDB

@sle_minion
Scenario: System changes should be reflected in systems, on ReportDB
  Given I have "sle_minion" with "Arrakeen" as "City" property
  And I know the current synced_date for "sle_minion"
  When I schedule a task to update ReportDB
  Then I should find the updated "City" property as "Arrakeen" on the "sle_minion", on ReportDB

Scenario: Cleanup: delete read-only user
  When I delete the read-only user for the ReportDB
  Then I shouldn't see the read-only user listed on the ReportDB user accounts

