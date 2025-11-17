# Copyright (c) 2021-2022 SUSE LLC
# SPDX-License-Identifier: MIT

@skip_if_github_validation
@scope_api
@sle_minion
Feature: IPMI Power management API

  Scenario: Setup an IPMI host for API test
    When the server starts mocking an IPMI host
    And I want to operate on this "sle_minion"

  Scenario: Check the power management settings for API test
    When I fetch power management values
    Then power management results should have "ipmilan" for "powerType"

  Scenario: Save power management values  for API test
    When I set power management value "fakeipmi" for "powerAddress"
    And I set power management value "ipmiusr" for "powerUsername"
    And I set power management value "test" for "powerPassword"
    And I set power management value "ipmilan" for "powerType"
    Then the cobbler report should contain "Power Management Address       : fakeipmi" for "sle_minion"
    And the cobbler report should contain "Power Management Username      : ipmiusr" for "sle_minion"
    And the cobbler report should contain "Power Management Password      : test" for "sle_minion"
    And the cobbler report should contain "Power Management Type          : ipmilan" for "sle_minion"

  Scenario: Test IPMI functions for API test
    When I turn power on
    Then the power status is "on"
    When I turn power off
    Then the power status is "off"
    When I do power management reboot
    Then the power status is "on"

  Scenario: Cleanup: reset IPMI values for API test
    When I set power management value "" for "powerAddress"
    And I set power management value "" for "powerUsername"
    And I set power management value "" for "powerPassword"
    Then the cobbler report should contain "Power Management Address       :" for "sle_minion"
    And the cobbler report should contain "Power Management Username      :" for "sle_minion"
    And the cobbler report should contain "Power Management Password      :" for "sle_minion"
    And the cobbler report should contain "Power Management Type          : ipmilan" for "sle_minion"

  Scenario: Cleanup: tear down the IPMI host for API test
    When the server stops mocking an IPMI host
