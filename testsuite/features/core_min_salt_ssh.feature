# Copyright (c) 2016-2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Be able to bootstrap a Salt host managed via salt-ssh

@ssh_minion
  Scenario: Bootstrap a SLES system managed via salt-ssh
    Given I am authorized
    And I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    And I check "manageWithSSH"
    And I enter the hostname of "ssh-minion" as "hostname"
    And I enter "linux" as "password"
    And I select the hostname of the proxy from "proxies"
    And I click on "Bootstrap"
    Then I wait until I see "Successfully bootstrapped host! " text
    And I navigate to "rhn/systems/Overview.do" page
    And I wait until I see the name of "ssh-minion", refreshing the page
    And I wait until onboarding is completed for "ssh-minion"

@proxy
@ssh_minion
  Scenario: Check connection from SSH minion to proxy
    Given I am on the Systems overview page of this "ssh-minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" hostname

@proxy
@ssh_minion
  Scenario: Check registration on proxy of SSH minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ssh-minion" hostname

@ssh_minion
  Scenario: Subscribe the SSH-managed SLES minion to a base channel
    Given I am on the Systems overview page of this "ssh-minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test-Channel-x86_64"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed

@ssh_minion
  Scenario: Schedule errata refresh to reflect channel assignment on SSH minion
    Given I am authorized as "admin" with password "admin"
    When I follow "Admin"
    And I follow "Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

@ssh_minion
  Scenario: Install a package on the SSH minion
    Given I am on the Systems overview page of this "ssh-minion"
    And I follow "Software" in the content area
    And I follow "Install"
    And I check "hoag-dummy-1.1-2.1" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled" text
    When I wait until event "Package Install/Upgrade scheduled by admin" is completed
    Then "hoag-dummy-1.1-2.1" should be installed on "ssh-minion"

  Scenario: Check events history for failures on SSH minion
    Given I am on the Systems overview page of this "ssh-minion"
    Then I check for failed events on history event page
