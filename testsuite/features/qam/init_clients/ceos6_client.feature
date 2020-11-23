# Copyright (c) 2020 SUSE LLC
# Licensed under the terms of the MIT license.

@ceos6_client
Feature: Be able to register a CentOS 6 traditional client and do some basic operations on it

  Scenario: Clean up sumaform leftovers on a CentOS 6 traditional client
    When I perform a full salt minion cleanup on "ceos6_client"

  Scenario: Prepare a CentOS 6 traditional client
    When I run "/usr/bin/update-ca-trust force-enable" on "ceos6_client"
    And I bootstrap traditional client "ceos6_client" using bootstrap script with activation key "1-ceos6_client_key" from the proxy
    And I install the traditional stack utils on "ceos6_client"
    And I run "mgr-actions-control --enable-all" on "ceos6_client"
    Then I should see "ceos6_client" via spacecmd

  Scenario: The onboarding of CentOS 6 traditional client is completed
    Given I am authorized
    And I wait until onboarding is completed for "ceos6_client"

  @proxy
  Scenario: Check connection from CentOS 6 traditional to proxy
    Given I am on the Systems overview page of this "ceos6_client"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

  @proxy
  Scenario: Check registration on proxy of traditional CentOS 6
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "ceos6_client" hostname

  Scenario: Schedule some actions on the CentOS 6 traditional client
    Given I am authorized as "admin" with password "admin"
    When I authenticate to XML-RPC
    And I refresh the packages on "ceos6_client" through XML-RPC
    And I run a script on "ceos6_client" through XML-RPC
    And I reboot "ceos6_client" through XML-RPC
    And I unauthenticate from XML-RPC
