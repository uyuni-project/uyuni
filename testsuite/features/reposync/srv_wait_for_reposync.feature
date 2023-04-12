# Copyright (c) 2019-2022 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Wait for reposync activity to finish in CI context

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Delete scheduled reposyncs
    When I follow the left menu "Admin > Task Schedules"
    And I follow "mgr-sync-refresh-default"
    And I choose "disabled"
    And I click on "Update Schedule"
    And I click on "Delete Schedule"

  Scenario: Kill running reposyncs or wait for them to finish
    When I kill all running spacewalk-repo-sync, excepted the ones needed to bootstrap

@uyuni
  Scenario: Enable SLES15 SP4 Uyuni client tools for creating bootstrap repositories
    When I use spacewalk-common-channel to add channel "sle-product-sles15-sp4-pool-x86_64 sles15-sp4-uyuni-client" with arch "x86_64"

  Scenario: Wait until all synchronized channels have finished
    When I wait until all synchronized channels have finished
