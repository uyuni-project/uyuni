# Copyright (c) 2026 SUSE LLC.
# Licensed under the terms of the MIT license.

@containerized_server
@mgradm_backup_restore
Feature: Be able to backup and restore the server
  As a system administrator
  I want to back up and restore my containerized server
  So that I can recover from system failures

# exclude the repository volume due to space constraints
  Scenario: Perform a test backup
    When I backup the server excluding the spacewalk volume
    Then the backup should exist

# stop the service before the restore, otherwise podman network creation will fail
  Scenario: Restore succeeds with force flag
    Given the backup exist
    When I stop the services on server
    Then I restore the backup

  Scenario: Remove backup and start services
    Given the backup exist
    When I remove the backup directory on server
    And I start the services on server
    Then service "uyuni-server" is running on server
    And service "uyuni-db" is running on server
