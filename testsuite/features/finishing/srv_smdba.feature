# Copyright (c) 2015-2025 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_smdba
Feature: SMDBA database helper tool
  In order to protect the data in Uyuni
  As a database administrator
  I want to easily take backups and snapshots

  Scenario: Shutdown spacewalk services
    Then I shutdown the spacewalk service

  Scenario: Check embedded database running
    Given a postgresql database is running
    When I start database with the command "smdba db-start"
    And I issue command "smdba db-status"
    Then "online" should be in the output

  Scenario: Check embedded database can be stopped, started and restarted
    Given a postgresql database is running
    When I start database with the command "smdba db-start"
    And I see that the database is "online" or "failed" as it might already running
    And I stop the database with the command "smdba db-stop"
    And I check the database status with the command "smdba db-status"
    Then the database should be "offline"
    When I start database with the command "smdba db-start"
    And I issue command "smdba db-status"
    Then the database should be "online"

  Scenario: Check system check of the database sets optimal configuration
    Given a postgresql database is running
    When I stop the database with the command "smdba db-stop"
    And I configure "/var/lib/pgsql/data/postgresql.conf" parameter "wal_level" to "logical"
    Then I start database with the command "smdba db-start"
    When I issue command "smdba db-status"
    Then the database should be "online"
    When I check internally configuration for "wal_level" option
    Then the configuration should be set to "logical"
    When I issue command "smdba system-check"
    And I stop the database with the command "smdba db-stop"
    And I start database with the command "smdba db-start"
    And I check internally configuration for "wal_level" option
    Then the configuration should not be set to "logical"

  Scenario: Check database utilities
    Given a postgresql database is running
    When I issue command "smdba space-overview"
    Then tablespace "susemanager" should be listed
    And tablespace "template" should be listed
    When I issue command "smdba space-reclaim"
    Then none of core examination, database analysis, and space reclamation should be "failed"
    When I issue command "smdba space-tables"
    Then table "public.rhnserver" should be listed
    And table "public.rhnpackage" should be listed
    And table "public.web_contact" should be listed

  Scenario: Check SMDBA backup setup facility
    Given a postgresql database is running
    And there is no such "/var/spacewalk/db-backup" directory
    When I create backup directory "/var/spacewalk/db-backup" with UID "root" and GID "root"
    And I take a backup with smdba in folder "/var/spacewalk/db-backup"
    Then I should see error message that asks "/var/spacewalk/db-backup" belong to the same UID/GID as "/var/lib/pgsql/data" directory
    And I remove backup directory "/var/spacewalk/db-backup"
    When I create backup directory "/var/spacewalk/db-backup" with UID "postgres" and GID "postgres"
    And I take a backup with smdba in folder "/var/spacewalk/db-backup"
    Then I should see error message that asks "/var/spacewalk/db-backup" has same permissions as "/var/lib/pgsql/data" directory
    And I remove backup directory "/var/spacewalk/db-backup"

  Scenario: Take backup with SMDBA
    Given a postgresql database is running
    And there is no such "/var/spacewalk/db-backup" directory
    When I create backup directory "/var/spacewalk/db-backup" with UID "postgres" and GID "postgres"
    And I change Access Control List on "/var/spacewalk/db-backup" directory to "0700"
    And I take a backup with smdba in folder "/var/spacewalk/db-backup"
    Then base backup is taken
    And in "/var/spacewalk/db-backup" directory there is "base.tar.gz" file and at least one backup checkpoint file
    And parameter "archive_command" in the configuration file "/var/lib/pgsql/data/postgresql.conf" is "/usr/bin/smdba-pgarchive"
    And "/usr/bin/smdba-pgarchive" destination should be set to "/var/spacewalk/db-backup" in configuration file

  Scenario: Restore backup with SMDBA
    Given a postgresql database is running
    And database "susemanager" has no table "dummy"
    When I set a checkpoint
    And I issue command "smdba backup-hot"
    And in the database I create dummy table "dummy" with column "test" and value "bogus data"
    And I stop the database with the command "smdba db-stop"
    And I destroy "/var/lib/pgsql/data/pg_xlog" directory on server
    And I destroy "/var/lib/pgsql/data/pg_wal" directory on server
    And I restore database from the backup
    And I issue command "smdba db-status"
    Then the database should be "online"

  Scenario: Cleanup: remove backup directory
    Given a postgresql database is running
    And database "susemanager" has no table "dummy"
    When I disable backup in the directory "/var/spacewalk/db-backup"
    And I remove backup directory "/var/spacewalk/db-backup"

  Scenario: Cleanup: start spacewalk services
    When I stop the database with the command "smdba db-stop"
    And I start database with the command "smdba db-start"
    And I restart the spacewalk service
