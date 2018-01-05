# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Be able to bootstrap a Salt host managed via salt-ssh

@sshminion
  Scenario: No Salt package nor service are running on minion
    Given no Salt packages are installed on "ssh-minion"
    Then "ssh-minion" should not be registered

@sshminion
  Scenario: Bootstrap a SLES system managed via salt-ssh
    Given I am authorized
    And I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    And I check "manageWithSSH"
    And I enter remote ssh-minion hostname as "hostname"
    And I enter "linux" as "password"
    And I select the hostname of the proxy from "proxies"
    And I click on "Bootstrap"
    Then I wait until I see "Successfully bootstrapped host! " text
    And I navigate to "rhn/systems/Overview.do" page
    And I wait until I see the name of "ssh-minion", refreshing the page
    And I wait until onboarding is completed for "ssh-minion"

@proxy
@sshminion
  Scenario: Check connection from SSH minion to proxy
    Given I am on the Systems overview page of this "ssh-minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" hostname

@proxy
@sshminion
  Scenario: Check registration on proxy of SSH minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ssh-minion" hostname

@sshminion
  Scenario: Subscribe the SSH-managed SLES minion to a base channel for testing
    Given I am on the Systems overview page of this "ssh-minion"
    And I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I select "Test-Channel-x86_64" from "new_base_channel_id"
    And I click on "Confirm"
    And I click on "Modify Base Software Channel"
    Then I should see a "System's Base Channel has been updated." text

@sshminion
  Scenario: Apply the highstate on ssh-minion to apply base channels
    Given I am on the Systems overview page of this "ssh-minion"
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    Then I should see a "Applying the highstate has been scheduled." text
    When I follow "Events"
    And I follow "Pending"
    And I try to reload page until it does not contain "Apply states scheduled by admin" text

@sshminion
  Scenario: Schedule errata refresh to reflect channel assignment
    Given I am authorized as "admin" with password "admin"
    When I follow "Admin"
    And I follow "Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    When I reload the page
    And I reload the page until it does contain a "FINISHED" text in the table first row

@sshminion
  Scenario: Install a package on the SSH minion
    Given I am on the Systems overview page of this "ssh-minion"
    And I follow "Software" in the content area
    And I follow "Install"
    And I check "hoag-dummy-1.1-2.1" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled" text
    And I wait for "hoag-dummy-1.1-2.1" to be installed on this "ssh-minion"

@sshminion
  Scenario: Reboot the SSH-managed SLES minion
    Given I am on the Systems overview page of this "ssh-minion"
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I wait and check that "ssh-minion" has rebooted
