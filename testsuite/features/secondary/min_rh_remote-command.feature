# Copyright (c) 2021-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_res
@rh_minion
Feature: Remote command on RedHat-like Salt minion
  In order to manage a RedHat-like Salt minion
  As an authorized user
  I want to run a remote command on it

  Scenario: Run a remote command on the RedHat-like minion
    Given I am authorized as "testing" with password "testing"
    When I follow the left menu "Salt > Remote Commands"
    Then I should see a "Remote Commands" text in the content area
    When I enter command "cat /etc/os-release"
    And I enter target "*centos*"
    And I click on preview
    And I click on run
    Then I should see "rh_minion" hostname
    When I wait for "15" seconds
    And I expand the results for "rh_minion"
    Then I should see a "rhel fedora" text
    And I should see a "REDHAT_SUPPORT_PRODUCT" text
