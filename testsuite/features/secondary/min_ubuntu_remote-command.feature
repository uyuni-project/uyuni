# Copyright (c) 2020 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Remote command on Ubuntu Salt minion
  In order to manage an Ubuntu Salt minion
  As an authorized user
  I want to run a remote command on it

@ubuntu_minion
  Scenario: Run a remote command on the Ubuntu minion
    Given I am authorized
    When I follow the left menu "Salt > Remote Commands"
    Then I should see a "Remote Commands" text in the content area
    When I enter command "cat /etc/os-release"
    And I enter target "*ubuntu*"
    And I click on preview
    And I click on run
    Then I should see "ubuntu_minion" hostname
    When I wait until I see "show response" text
    And I expand the results for "ubuntu_minion"
    Then I should see a "ID=ubuntu" text
