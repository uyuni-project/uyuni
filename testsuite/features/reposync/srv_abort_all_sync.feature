# Copyright (c) 2019 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Abort all reposync activity

  Scenario: Delete scheduled reposyncs
    Given I am authorized with the feature's user
    When I follow the left menu "Admin > Task Schedules"
    And I follow "mgr-sync-refresh-default"
    And I choose "disabled"
    And I click on "Update Schedule"
    And I click on "Delete Schedule"

  Scenario: Kill running reposyncs
    When I make sure no spacewalk-repo-sync is executing, excepted the ones needed to bootstrap
