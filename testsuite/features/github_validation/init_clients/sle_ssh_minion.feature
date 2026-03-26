# Copyright (c) 2023-2025 SUSE LLC
# Licensed under the terms of the MIT license.

@ssh_minion
Feature: Bootstrap a Salt host managed via salt-ssh

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Register this SSH minion for service pack migration
    When I bootstrap "ssh_minion" via the UI
    And I follow the left menu "Systems > System List > All"
    And I wait until I see the name of "ssh_minion", refreshing the page

  Scenario: Subscribe the SSH minion to a base channel
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "openSUSE Tumbleweed (x86_64)"
    And I wait until I do not see "Loading..." text
    And I check "Fake-RPM-SUSE-Channel"
    And I check "Uyuni Client Tools for openSUSE Tumbleweed (x86_64) (Development)"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed
