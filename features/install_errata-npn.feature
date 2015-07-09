# Copyright (c) 2015 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Install an erratum to the client

  Scenario: Install an erratum to the client
    Given I am on the Systems overview page of this client
    And I follow "Software" in the content area
    And I follow "Errata" in the content area
    When I check "virgo-dummy-3456" in the list
#    And I wait for "2" seconds
    And I click on "Apply Errata"
#    And I wait for "2" seconds
    And I click on "Confirm"
#    And I wait for "5" seconds
    And I run rhn_check on this client
    Then I should see a "1 errata update has been scheduled for" text
    And "virgo-dummy-2.0-1.1" is installed
