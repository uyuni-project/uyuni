# Copyright (c) 2024 SUSE LLC
# Licensed under the terms of the MIT license.

@rhlike_minion
Feature: Native AppStreams support for Red Hat-like Salt minion
  
  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Pre-requisite: Subscribe system to Fake channel
    Given I am on the Systems overview page of this "rhlike_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Fake-Base-Channel-AppStream"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed

  Scenario: Verify that channel AppStreams are available for the system
    Given I am on the Systems overview page of this "rhlike_minion"
    When I follow "Software" in the content area
    And I follow "AppStreams"
    Then I should see "scorpio:2.1-cbox" as unchecked
    And I should see "scorpio:2.0-cbox" as unchecked

  Scenario: Ensure only the non-modular package is available when AppStreams are disabled
    Given I am on the Systems overview page of this "rhlike_minion"
    And I follow "Software" in the content area
    And I follow "Install"
    Then I should see a "scorpio-dummy-1.0" text
    And I should not see a "scorpio-dummy-2.0" text
    And I should not see a "scorpio-dummy-2.1" text

  Scenario: Install non-modular package
    When I install package "scorpio-dummy-1.0-12.1" on this "rhlike_minion"
    And I refresh packages list via spacecmd on "rhlike_minion"
    And I wait until refresh package list on "rhlike_minion" is finished
    And I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Verify that modular packages do not appear as upgrade options when AppStreams are disabled
    And I am on the "Software" page of this "rhlike_minion"
    And I follow "Packages"
    And I follow "Upgrade"
    Then I should not see a "scorpio-dummy-2" text

  Scenario: Enable AppStream module
    Given I am on the Systems overview page of this "rhlike_minion"
    And I follow "Software" in the content area
    And I follow "AppStreams"
    And I check "scorpio:2.0-cbox"
    And I click on "Apply Changes (1)"
    Then I should see a "Changes Summary" text
    When I click on "Confirm"
    Then I should see a "Updating the selection of AppStream modules has been scheduled." text
    And I wait until event "Change AppStreams (enable: [scorpio:2.0]) scheduled by admin" is completed
  
  Scenario: Upgrade to modular package
    Given I am on the Systems overview page of this "rhlike_minion"
    And I follow "Software" in the content area
    And I follow "Packages"
    And I follow "Upgrade"
    Then I wait until I see "scorpio-dummy-2.0" text, refreshing the page
    And I should see a "scorpio:2.0" text
    And I check "scorpio-dummy-2.0" in the list
    And I click on "Upgrade Packages"
    And I click on "Confirm"
    And I should see a "1 package upgrade has been scheduled for" text
    When I wait until event "Package Install/Upgrade scheduled by admin" is completed
    Then "scorpio-dummy-2.0" should be installed on "rhlike_minion"
  
  Scenario: Remove modular package
    Given I am on the Systems overview page of this "rhlike_minion"
    And I follow "Software" in the content area
    And I follow "Packages"
    And I follow "List / Remove"
    And I enter "scorpio-dummy" as the filtered package name
    And I click on the filter button
    And I check "scorpio-dummy-2.0" in the list
    And I click on "Remove Packages"
    And I click on "Confirm"
    Then I should see a "1 package removal has been scheduled" text
    When I wait until event "Package Removal scheduled" is completed

  Scenario: Change enabled AppStream module
    Given I am on the Systems overview page of this "rhlike_minion"
    And I follow "Software" in the content area
    And I follow "AppStreams"
    And I check "scorpio:2.1-cbox"
    And I click on "Apply Changes (2)"
    Then I should see a "Changes Summary" text
    When I click on "Confirm"
    Then I should see a "Updating the selection of AppStream modules has been scheduled." text
    And I wait until event "Change AppStreams (disable: [scorpio:2.0] enable: [scorpio:2.1]) scheduled by admin" is completed

  Scenario: Ensure only the correct modular package is available and install it
    Given I am on the Systems overview page of this "rhlike_minion"
    And I follow "Software" in the content area
    And I follow "Install"
    Then I should see a "scorpio-dummy-2.1" text
    And I should see a "scorpio:2.1" text
    And I should not see a "scorpio-dummy-2.0" text
    And I should not see a "scorpio-dummy-1.0" text
    When I check "scorpio-dummy-2.1" in the list
    And I click on "Install Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled" text
    When I wait until event "Package Install/Upgrade scheduled" is completed
    Then "scorpio-dummy-2.1" should be installed on "rhlike_minion"

  Scenario: Cleanup: remove modular package
    Given I am on the Systems overview page of this "rhlike_minion"
    And I follow "Software" in the content area
    And I follow "Packages"
    And I follow "List / Remove"
    And I enter "scorpio-dummy" as the filtered package name
    And I click on the filter button
    And I check "scorpio-dummy-2.1" in the list
    And I click on "Remove Packages"
    And I click on "Confirm"
    Then I should see a "1 package removal has been scheduled" text
    When I wait until event "Package Removal scheduled" is completed
  
  Scenario: Cleanup: disable AppStream module
    Given I am on the Systems overview page of this "rhlike_minion"
    And I follow "Software" in the content area
    And I follow "AppStreams"
    And I uncheck "scorpio:2.1-cbox"
    And I click on "Apply Changes (1)"
    Then I should see a "Changes Summary" text
    When I click on "Confirm"
    Then I should see a "Updating the selection of AppStream modules has been scheduled." text
    And I wait until event "Change AppStreams (disable: [scorpio:2.1] ) scheduled by admin" is completed

  Scenario: Ensure only the non-modular package is available after disabling AppStreams
    Given I am on the Systems overview page of this "rhlike_minion"
    And I follow "Software" in the content area
    And I follow "Install"
    Then I should see a "scorpio-dummy-1.0" text
    And I should not see a "scorpio-dummy-2.0" text

  Scenario: Cleanup: subscribe system back to default base channel
    Given I am on the Systems overview page of this "rhlike_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check default base channel radio button of this "rhlike_minion"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed
