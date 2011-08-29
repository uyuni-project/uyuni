# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Test Oracle and PostgreSQL databases connections.
  Scenario: I prepare to connect to the Databases
    And I define user "spacewalk" for Oracle DB
    And I define password "spacewalk" for Oracle DB
    And I select the database "susemanager" for Oracle DB
    And I define user "spaceuser" for Postgres
    And I define password "spacepassword" for Postgres
    And I select the database "spaceschema" for Postgres
    Then I should connect and see at least one result back
