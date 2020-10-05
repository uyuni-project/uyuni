# Copyright (c) 2020 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Power management test for XMLRPC

  Scenario: Fake an IPMI host for XMLRPC test
    Given the server starts mocking an IPMI host

  Scenario: Check the power management settings for XMLRPC test
    Given I am logged in via XML-RPC powermgmt as user "admin" and password "admin"
    And I want to operate on this "sle_client"
    When I fetch power management values
    Then power management results should have "ipmitool" for "powerType"

  Scenario: Save power management values  for XMLRPC test
    Given I am logged in via XML-RPC powermgmt as user "admin" and password "admin"
    And I want to operate on this "sle_client"
    Then I set power management value "127.0.0.1" for "powerAddress"
    And I set power management value "ipmiusr" for "powerUsername"
    And I set power management value "test" for "powerPassword"
    And I set power management value "ipmitool" for "powerType"
    And the cobbler report contains "Power Management Address       : 127.0.0.1"
    And the cobbler report contains "Power Management Username      : ipmiusr"
    And the cobbler report contains "Power Management Password      : test"
    And the cobbler report contains "Power Management Type          : ipmitool"

  Scenario: Test IPMI functions for XMLRPC test
    Given I am logged in via XML-RPC powermgmt as user "admin" and password "admin"
    And I want to operate on this "sle_client"
    Then I turn power on
    And the power status is "on"
    And I turn power off
    And the power status is "off"
    And I do power management reboot
    And the power status is "on"

  Scenario: Cleanup: reset IPMI values for XMLRPC test
    Given I am logged in via XML-RPC powermgmt as user "admin" and password "admin"
    And I want to operate on this "sle_client"
    Then I set power management value "" for "powerAddress"
    And I set power management value "" for "powerUsername"
    And I set power management value "" for "powerPassword"
    And the cobbler report contains "Power Management Address       :"
    And the cobbler report contains "Power Management Username      :"
    And the cobbler report contains "Power Management Password      :"
    And the cobbler report contains "Power Management Type          : ipmitool"

  Scenario: Cleanup: don't fake an IPMI host for XMLRPC test
    Given the server stops mocking an IPMI host
