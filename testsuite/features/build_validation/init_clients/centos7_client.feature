# Copyright (c) 2020-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@centos7_client
Feature: Bootstrap a CentOS 7 traditional client

  Scenario: Clean up sumaform leftovers on a CentOS 7 traditional client
    When I perform a full salt minion cleanup on "centos7_client"

  Scenario: Prepare a CentOS 7 traditional client
    When I enable repository "CentOS-Base tools_pool_repo" on this "centos7_client" without error control
    And I bootstrap traditional client "centos7_client" using bootstrap script with activation key "1-centos7_client_key" from the proxy
    And I install the traditional stack utils on "centos7_client"
    And I run "mgr-actions-control --enable-all" on "centos7_client"
    Then I should see "centos7_client" via spacecmd

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: The onboarding of CentOS 7 traditional client is completed
    When I wait until onboarding is completed for "centos7_client"

@proxy
  Scenario: Check connection from CentOS 7 traditional client to proxy
    Given I am on the Systems overview page of this "centos7_client"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of CentOS 7 traditional client
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "centos7_client" hostname
