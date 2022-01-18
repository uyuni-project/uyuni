# Copyright (c) 2021-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@sle11sp4_buildhost
Feature: Prepare buildhost and build OS image for SLES 11 SP3

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create the bootstrap repository for a SLES 11 SP4 build host
     When I create the bootstrap repository for "sle11sp4_buildhost" on the server

  Scenario: Set additional records at SLES 11 SP4 buildhost hosts file if avahi is used
    When I add server record into hosts file on "sle11sp4_buildhost" if avahi is used
    And I add proxy record into hosts file on "sle11sp4_buildhost" if avahi is used

  Scenario: Clean up sumaform leftovers on a SLES 11 SP4 build host
    When I perform a full salt minion cleanup on "sle11sp4_buildhost"

  Scenario: Bootstrap a SLES 11 SP4 build host
    When I bootstrap minion client "sle11sp4_buildhost" using bootstrap script with activation key "1-sle11sp4_buildhost_key" from the proxy
    And I wait until onboarding is completed for "sle11sp4_buildhost"

  Scenario: Turn SLE11 SP4 system into build host, prepare profile and build Kiwi image
    When I execute "buildhost" for "SLE11 SP3" via semi-xmlrpc-tester
    And I execute "profiles" for "SLE11 SP3" via semi-xmlrpc-tester
    And I execute "image" for "SLE11 SP3" via semi-xmlrpc-tester
