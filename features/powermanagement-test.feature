# Copyright (c) 2010-2013 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Test Powermanagement

  Scenario: Is the powermanagement page accessible
    Given I am on the Systems overview page of this client
      And I follow "Provisioning" in the content area
      And I follow "Power Management" in class "contentnav-row2"
     Then I should see a "Power Management Settings" text
      And I should see a "IPMI" text
      And I should see a "Save" button

  Scenario: Save powermanagement values
    Given I am on the Systems overview page of this client
      And I follow "Provisioning" in the content area
      And I follow "Power Management" in class "contentnav-row2"
     When I enter "192.168.254.254" as "powerAddress"
      And I enter "admin" as "powerUsername"
      And I enter "qwertz" as "powerPassword"
      And I click on "Save"
     Then I should see a "Settings saved" text
      And the cobbler report contains "Power Management Address       : 192.168.254.254"
      And the cobbler report contains "Power Management Password      : qwertz"
      And the cobbler report contains "Power Management Type          : ipmitool"
      And the cobbler report contains "Power Management Username      : admin"
