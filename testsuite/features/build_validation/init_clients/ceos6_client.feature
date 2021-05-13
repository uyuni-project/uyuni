# Copyright (c) 2020-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@ceos6_client
Feature: Bootstrap a CentOS 6 traditional client

  Scenario: Clean up sumaform leftovers on a CentOS 6 traditional client
    When I perform a full salt minion cleanup on "ceos6_client"

  Scenario: Prepare a CentOS 6 traditional client
    When I enable the repositories "centos_base_backup centos_updates_backup tools_pool_repo" on this "ceos6_client" without error control
    And I run "/usr/bin/update-ca-trust force-enable" on "ceos6_client"
    And I bootstrap traditional client "ceos6_client" using bootstrap script with activation key "1-ceos6_client_key" from the proxy
    And I install the traditional stack utils on "ceos6_client"
    And I run "mgr-actions-control --enable-all" on "ceos6_client"
    Then I should see "ceos6_client" via spacecmd

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: The onboarding of CentOS 6 traditional client is completed
    When I wait until onboarding is completed for "ceos6_client"

@proxy
  Scenario: Check connection from CentOS 6 traditional client to proxy
    Given I am on the Systems overview page of this "ceos6_client"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on proxy of CentOS 6 traditional client
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ceos6_client" hostname
