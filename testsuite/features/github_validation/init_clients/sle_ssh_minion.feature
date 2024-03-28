# Copyright (c) 2023 SUSE LLC
# Licensed under the terms of the MIT license.

@ssh_minion
Feature: Bootstrap a Salt host managed via salt-ssh

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Register this SSH minion for service pack migration
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I check "manageWithSSH"
    And I enter the hostname of "ssh_minion" as "hostname"
    And I enter "linux" as "password"
    And I click on "Bootstrap"
    # workaround for bsc#1222108
    And I wait at most 480 seconds until I see "Bootstrap process initiated." text
    And I follow the left menu "Systems > System List > All"
    And I wait until I see the name of "ssh_minion", refreshing the page

  Scenario: Subscribe the SSH minion to a base channel
    Given I am on the Systems overview page of this "ssh_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Fake-Base-Channel-SUSE-like"
    And I wait until I do not see "Loading..." text
    And I check "Fake-RPM-SUSE-Channel"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed
