# Copyright (c) 2021-2024 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_res
@rhlike_minion
Feature: Remote command on the Red Hat-like Salt minion
  In order to manage a Red Hat-like Salt minion
  As an authorized user
  I want to run a remote command on it

  Scenario: Run a remote command on the Red Hat-like minion
    Given I am authorized as "testing" with password "testing"
    When I follow the left menu "Salt > Remote Commands"
    Then I should see a "Remote Commands" text in the content area
    When I enter command "cat /etc/os-release"
    And I enter target "rhlike_minion"
    And I click on preview
    And I click on run
    Then I should see "rhlike_minion" hostname
    When I wait for "15" seconds
    And I expand the results for "rhlike_minion"
    Then I should see a "rhel centos fedora" text
    And I should see a "REDHAT_SUPPORT_PRODUCT" text
