# Copyright (c) 2016-2019 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  1) bootstrap a new CentOS minion via salt-ssh
#  2) subscribe it to a base channel for testing

Feature: Bootstrap a SSH-managed CentOS minion and do some basic operations on it

@centos_minion
  Scenario: Bootstrap a SSH-managed CentOS minion
    Given I am authorized
    When I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    When I check "manageWithSSH"
    And I enter the hostname of "ceos-ssh-minion" as "hostname"
    And I enter "linux" as "password"
    And I select the hostname of the proxy from "proxies"
    And I click on "Bootstrap"
    Then I wait until I see "Successfully bootstrapped host! " text
    And I navigate to "rhn/systems/Overview.do" page
    And I wait until I see the name of "ceos-ssh-minion", refreshing the page
    And I wait until onboarding is completed for "ceos-ssh-minion"

@proxy
@centos_minion
  Scenario: Check connection from SSH-managed CentOS minion to proxy
    Given I am on the Systems overview page of this "ceos-ssh-minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" hostname

@proxy
@centos_minion
  Scenario: Check registration on proxy of SSH-managed CentOS minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ceos-ssh-minion" hostname

@centos_minion
  Scenario: Subscribe the SSH-managed CentOS minion to a base channel
    Given I am on the Systems overview page of this "ceos-ssh-minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test Base Channel"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed
