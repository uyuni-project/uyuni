# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test the remote commands via salt for redhat minion
  In Order to test the remote commands via salt
  As an authorized user
  I want to verify that the remote commands function works for redhat minion

  Scenario: Run a remote command on redhat
    Given I am authorized as "testing" with password "testing"
    And I follow "Salt"
    And I follow "Remote Commands"
    And I should see a "Remote Commands" text
    Then I enter command "cat /etc/os-release"
    And I click on preview
    Then I should see "rh-minion" hostname
    And I click on run
    Then I wait for "3" seconds
    And I expand the results for "rh-minion"
    And I should see a "rhel fedora" text
    Then I should see a "REDHAT_SUPPORT_PRODUCT" text
