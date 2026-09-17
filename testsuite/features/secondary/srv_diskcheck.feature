# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature can cause server stop that would prevent running the following features.
# This feature can cause the server to stop and prevent it from starting. That would block all the following features.
# This feature must NOT run in parallel because it restarts the server and that would break other features running at the same time.

@scope_diskcheck
@containerized_server
@skip_if_github_validation
Feature: Space monitoring via Diskcheck
  In order to be warned before the server runs out of disk space
  As an administrator
  I want taskomatic to monitor the directories listed in the
  configuration file, and to take its settings either from that
  file or from the variables defined in the service file

  Scenario: Setup the disk check and deploy the diskcheck scripts
    Given I am authorized for the "Admin" section
    When I deploy diskcheck scripts on "server"
    Then I change the disk space check schedule to run every minute

  Scenario: Default settings without fillings
    When I go to the home page
    Then I should not see the "The available disk space for the server is critically low" alert danger
    And I should not see the "The available disk space for the server is low" alert warning

  Scenario: Default settings with more than 95% disk space filled and server running for more than 10 minutes
    Given I am not authorized
    When I wait for the "uyuni-server" container to be running for more than "600" seconds on "server"
    And I fill disk space in "/root/diskcheck" up to "96%" on "server"
    Then I check the uyuni server has stopped

  Scenario: Default settings disk space freed
    When I release the space in "/root/diskcheck" on "server"
    And I restart the uyuni server
    Then I check the uyuni server has started

  Scenario: Default settings with less than 90% disk space filled
    Given I am not authorized
    When I fill disk space in "/root/diskcheck" up to "80%" on "server"
    Then I wait for the diskcheck alert notification
    And I go to the home page
    And I should not see the "The available disk space for the server is critically low" alert danger
    And I should see the "The available disk space for the server is low" alert warning

  Scenario: Default settings with more than 90% and less than 95% of disk space filled
    Given I am not authorized
    When I fill disk space in "/root/diskcheck" up to "91%" on "server"
    Then I wait for the diskcheck alert notification
    And I go to the home page
    And I should not see the "The available disk space for the server is critically low" alert danger
    And I should see the "The available disk space for the server is low" alert warning

  Scenario: Default settings with more than 95% disk space filled
    # The restart is needed, otherwise the server shutdown may be quicker than the alert
    When I restart the uyuni server
    And I check the uyuni server is running
    And I fill disk space in "/root/diskcheck" up to "96%" on "server"
    Then I wait for the diskcheck alert notification
    And I go to the home page
    And I should see the "The available disk space for the server is critically low" alert danger
    And I should not see the "The available disk space for the server is low" alert warning
    And I trigger the healthcheck of "uyuni-server" container on "server" and expect it to fail
    # workaround
    #   - when the test starts failing here, check the issue: https://github.com/SUSE/spacewalk/issues/32049
    #   - if fixed, remove the 2 lines below and uncomment the line with "server has stopped"
    And I wait for "10" seconds
    And I check the uyuni server is running
    # /workaround
    #And I check the uyuni server has stopped

  Scenario: Default settings disk space cleanup
    When I cleanup the "/root/diskcheck" on "server"
    And I restart the uyuni server
    Then I check the uyuni server has started

  Scenario: Custom settings without fillings
    When I create a "1000MB" disk image in "/root" and mount it to "/root/mnt-diskcheck" on "server"
    And I configure the uyuni server to watch the "/root/mnt-diskcheck" directory with alert set to "50%" and threshold set to "70%"
    Then I check the uyuni server is running
    And I am not authorized
    And I go to the home page
    And I should not see the "The available disk space for the server is critically low" alert danger
    And I should not see the "The available disk space for the server is low" alert warning

  Scenario: Custom settings with more than 40% and less than 50% disk space filled
    When I fill disk space in "/root/mnt-diskcheck" up to "45%" on "server"
    Then I wait for the diskcheck alert notification
    And I go to the home page
    And I should not see the "The available disk space for the server is critically low" alert danger
    And I should not see the "The available disk space for the server is low" alert warning

  Scenario: Custom settings with more than 50% and less than 70% disk space filled
    When I fill disk space in "/root/mnt-diskcheck" up to "60%" on "server"
    Then I wait for the diskcheck alert notification
    And I go to the home page
    And I should not see the "The available disk space for the server is critically low" alert danger
    And I should see the "The available disk space for the server is low" alert warning

  Scenario: Custom settings with more than 70% and less than 90% disk space filled
    When I fill disk space in "/root/mnt-diskcheck" up to "80%" on "server"
    Then I wait for the diskcheck alert notification
    And I go to the home page
    And I should see the "The available disk space for the server is critically low" alert danger
    And I should not see the "The available disk space for the server is low" alert warning
    And I trigger the healthcheck of "uyuni-server" container on "server" and expect it to fail
    # workaround
    #   - when the test starts failing here, check the issue: https://github.com/SUSE/spacewalk/issues/32049
    #   - if fixed, remove the 2 lines below and uncomment the line with "server has stopped"
    And I wait for "10" seconds
    And I check the uyuni server is running
    # /workaround
    #And I check the uyuni server has stopped

  Scenario: Custom settings cleanup
    When I cleanup the "/root/mnt-diskcheck" on "server"
    And I configure the uyuni server to watch the default directories
    Then I check the uyuni server has started
    And I am not authorized
    And I go to the home page
    And I should not see the "The available disk space for the server is critically low" alert danger
    And I should not see the "The available disk space for the server is low" alert warning

  Scenario: Custom settings in rhn.conf and without fillings
    When I create a "1000MB" disk image in "/root" and mount it to "/root/mnt-diskcheck" on "server"
    And I configure the uyuni server to watch the "/root/mnt-diskcheck" directory set in the rhn config file
    Then I check the uyuni server is running
    And I am not authorized
    And I go to the home page

  Scenario: Custom settings in rhn.conf with less than 90% disk space filled
    When I fill disk space in "/root/mnt-diskcheck" up to "80%" on "server"
    Then I wait for the diskcheck alert notification
    And I go to the home page
    And I should not see the "The available disk space for the server is critically low" alert danger
    And I should not see the "The available disk space for the server is low" alert warning

  Scenario: Custom settings in rhn.conf with more than 90% but less than 95% disk space filled
    When I fill disk space in "/root/mnt-diskcheck" up to "91%" on "server"
    Then I wait for the diskcheck alert notification
    And I go to the home page
    And I should not see the "The available disk space for the server is critically low" alert danger
    And I should not see the "The available disk space for the server is low" alert warning

  Scenario: Custom settings in rhn.conf with more than 95% disk space filled
    When I restart the uyuni server
    And I check the uyuni server is running
    And I fill disk space in "/root/mnt-diskcheck" up to "96%" on "server"
    Then I wait for the diskcheck alert notification
    And I go to the home page
    And I should not see the "The available disk space for the server is critically low" alert danger
    And I should not see the "The available disk space for the server is low" alert warning
    And I trigger the healthcheck of "uyuni-server" container on "server" and expect it to fail
    # workaround
    #   - when the test start failing here, check the issue: https://github.com/SUSE/spacewalk/issues/32049
    #   - if fixed, remove the 2 lines below and uncomment the line with "server has stopped"
    And I wait for "10" seconds
    And I check the uyuni server is running
    # /workaround
    #And I check the uyuni server has stopped

  Scenario: Custom settings in rhn.conf cleanup
    When I release the space in "/root/mnt-diskcheck" on "server"
    And I restart the uyuni server
    And I configure the uyuni server to watch the default directories, cleaning the rhn config file
    And I unmount "/root/mnt-diskcheck" and remove the disk image in "/root" on "server"
    Then I check the uyuni server is running
    And I go to the home page

  Scenario: Cleanup of diskcheck scripts and server schedules
    When I configure the uyuni server to watch the default directories
    And I check the uyuni server is running
    And I am authorized for the "Admin" section
    And I change the disk space check schedule to defaults
    And I undeploy diskcheck scripts on "server"
    Then I go to the home page
    And I should not see the "The available disk space for the server is critically low" alert danger
    And I should not see the "The available disk space for the server is low" alert warning
