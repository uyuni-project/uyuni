# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Install an erratum to the client

  Scenario: Install an erratum to the client
    Given I am on the Systems overview page of this client
     And I follow "Software" in class "content-nav"
     And I follow "Patches" in class "contentnav-row2"
    When I check "slessp1-suseRegister-2953-channel-" in the list
     And I wait for "2" seconds
     And I click on "Apply Patches"
     And I wait for "2" seconds
     And I click on "Confirm"
     And I wait for "2" seconds
     And I run rhn_check on this client
    Then I should see a "1 patch update has been scheduled for" text
     And "suseRegister-1.4-1.9.1" is installed
