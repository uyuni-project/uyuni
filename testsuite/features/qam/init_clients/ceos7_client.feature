# Copyright (c) 2020 SUSE LLC
# Licensed under the terms of the MIT license.

@ceos7_client
Feature: Be able to register a CentOS 7 traditional client and do some basic operations on it

  Scenario: Clean up sumaform leftovers on a CentOS 7 traditional client
    When I perform a full salt minion cleanup on "ceos7_client"

  Scenario: Prepare a CentOS 7 traditional client
    When I enable SUSE Manager tools repositories on "ceos7_client"
    And I enable repository "CentOS-Base" on this "ceos7_client"
    And I bootstrap traditional client "ceos7_client" using bootstrap script with activation key "1-ceos7_client_key" from the proxy
    And I install the traditional stack utils on "ceos7_client"
    And I run "mgr-actions-control --enable-all" on "ceos7_client"
    And I wait until onboarding is completed for "ceos7_client"

  @proxy
  Scenario: Check connection from CentOS 7 traditional to proxy
    Given I am on the Systems overview page of this "ceos7_client"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

  @proxy
  Scenario: Check registration on proxy of traditional CentOS 7
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ceos7_client" hostname
