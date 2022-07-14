# Copyright (c) 2021-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_api
Feature: IPMI Power management API

  Scenario: Setup an IPMI host for API test
    When the server starts mocking an IPMI host
    And I am logged in API as user "admin" and password "admin"

  Scenario: Check the power management settings for API test
    When I want to operate on this "sle_client"
    And I fetch power management values
    Then power management results should have "ipmitool" for "powerType"

  Scenario: Save power management values  for API test
    When I want to operate on this "sle_client"
    And I set power management value "127.0.0.1" for "powerAddress"
    And I set power management value "ipmiusr" for "powerUsername"
    And I set power management value "test" for "powerPassword"
    And I set power management value "ipmitool" for "powerType"
    Then the cobbler report should contain "Power Management Address       : 127.0.0.1" for "sle_client"
    And the cobbler report should contain "Power Management Username      : ipmiusr" for "sle_client"
    And the cobbler report should contain "Power Management Password      : test" for "sle_client"
    And the cobbler report should contain "Power Management Type          : ipmitool" for "sle_client"

  Scenario: Test IPMI functions for API test
    When I want to operate on this "sle_client"
    And I turn power on
    Then the power status is "on"
    When I turn power off
    Then the power status is "off"
    When I do power management reboot
    Then the power status is "on"

  Scenario: Cleanup: reset IPMI values for API test
    When I want to operate on this "sle_client"
    And I set power management value "" for "powerAddress"
    And I set power management value "" for "powerUsername"
    And I set power management value "" for "powerPassword"
    Then the cobbler report should contain "Power Management Address       :" for "sle_client"
    And the cobbler report should contain "Power Management Username      :" for "sle_client"
    And the cobbler report should contain "Power Management Password      :" for "sle_client"
    And the cobbler report should contain "Power Management Type          : ipmitool" for "sle_client"

  Scenario: Cleanup: tear down the IPMI host for API test
    When the server stops mocking an IPMI host
    And I logout from API
