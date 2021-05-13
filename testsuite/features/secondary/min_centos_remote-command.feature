# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_res
Feature: Remote command on CentOS Salt minion
  In order to manage a CentOS Salt minion
  As an authorized user
  I want to run a remote command on it

@centos_minion
  Scenario: Run a remote command on the CentOS minion
    Given I am authorized as "testing" with password "testing"
    When I follow the left menu "Salt > Remote Commands"
    Then I should see a "Remote Commands" text in the content area
    When I enter command "cat /etc/os-release"
    And I enter target "*centos*"
    And I click on preview
    And I click on run
    Then I should see "ceos_minion" hostname
    When I wait for "15" seconds
    And I expand the results for "ceos_minion"
    Then I should see a "rhel fedora" text
    And I should see a "REDHAT_SUPPORT_PRODUCT" text
