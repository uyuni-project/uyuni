# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Install a package to the client

  Scenario: Install a package to the client
    Given I am on the Systems overview page of this "sle-client"
    And metadata generation finished for "test-channel-x86_64"
    And I follow "Software" in the content area
    And I follow "Install"
    When I check "hoag-dummy-1.1-2.1" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    And I run rhn_check on this client
    Then I should see a "1 package install has been scheduled for" text
    And "hoag-dummy-1.1-2.1" is installed on "client"

  Scenario: Install an erratum to the client
    Given I am on the Systems overview page of this "sle-client"
    And I follow "Software" in the content area
    And I follow "Errata" in the content area
    When I check "virgo-dummy-3456" in the list
    And I click on "Apply Errata"
    And I click on "Confirm"
    And I run rhn_check on this client
    Then I should see a "1 errata update has been scheduled for" text
    And "virgo-dummy-2.0-1.1" is installed on "client"
