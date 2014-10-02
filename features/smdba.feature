# Copyright (c) 2013 Novell, Inc.
# Licensed under the terms of the MIT license.

# features/smdba.feature
Feature: Verify SMDBA infrastructure
  In order to operate embedded database with SMDBA tool
  As the testing user
  I want to check if infrastructure is consistent

  Scenario: Check if embedded database is PostgreSQL
    When I cannot find file "/var/lib/pgsql/data/postgresql.conf"
    Then I disable all the tests below

  Scenario: Check embedded database running
    When I start database with the command "smdba db-start"
    And when I issue command "smdba db-status"
    Then I want to see if "online" is in the output

  Scenario: Check embedded database can be stopped, started and restarted
    When I start database with the command "smdba db-start"
    And when I see that the database is "online" or "failed" as it might already running
    And when I stop the database with the command "smdba db-stop"
    And when I check the database status with the command "smdba db-status"
    Then I want to see if the database is "offline"
    When I start database with the command "smdba db-start"
    And when I issue command "smdba db-status"
    Then I want to see if the database is "online"

  Scenario: Check system check of the database sets optimal configuration
    When I stop the database with the command "smdba db-stop"
    And when I configure "/var/lib/pgsql/data/postgresql.conf" parameter "wal_level" to "hot_standby"
    Then I start database with the command "smdba db-start"
    And when I issue command "smdba db-status"
    Then I want to see if the database is "online"
    
    And when I check internally configuration for "wal_level" option
    Then I expect to see the configuration is set to "hot_standby"
    Then I issue command "smdba system-check"
    And when I stop the database with the command "smdba db-stop"
    And I start database with the command "smdba db-start"
    And when I check internally configuration for "wal_level" option
    Then I expect to see the configuration is set to "archive"

