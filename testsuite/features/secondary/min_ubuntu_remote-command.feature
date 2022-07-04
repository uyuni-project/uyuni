# Copyright (c) 2021-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_deb
@deb_minion
Feature: Remote command on Debian-like Salt minion
  In order to manage an Debian-like Salt minion
  As an authorized user
  I want to run a remote command on it

  Scenario: Run a remote command on the Debian-like minion
    Given I am authorized for the "Admin" section
    When I follow the left menu "Salt > Remote Commands"
    Then I should see a "Remote Commands" text in the content area
    When I enter command "cat /etc/os-release"
    And I enter target "*ubuntu*"
    And I click on preview
    And I click on run
    Then I should see "deb_minion" hostname
    When I wait until I see "show response" text
    And I expand the results for "deb_minion"
    Then I should see a "ID=ubuntu" text
