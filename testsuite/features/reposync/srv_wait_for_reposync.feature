# Copyright (c) 2019-2021 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Wait for reposync activity to finish

@regular_ci
  Scenario: Delete scheduled reposyncs
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Admin > Task Schedules"
    And I follow "mgr-sync-refresh-default"
    And I choose "disabled"
    And I click on "Update Schedule"
    And I click on "Delete Schedule"

@continuous_integration
  Scenario: Kill running reposyncs
    When I kill all running spacewalk-repo-sync, excepted the ones needed to bootstrap

@build_validation
  Scenario: Wait for running reposyncs to finish
    When I wait until all spacewalk-repo-sync finished
