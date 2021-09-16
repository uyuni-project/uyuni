# Copyright (c) 2016-2021 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  1) bootstrap a new CentOS minion via salt
#  2) subscribe it to a base channel for testing

@centos_minion
Feature: Bootstrap a CentOS minion and do some basic operations on it start

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap a CentOS minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "ceos_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-SUSE-KEY-x86_64" from "activationKeys"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
