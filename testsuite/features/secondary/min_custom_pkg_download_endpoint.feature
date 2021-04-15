# Copyright (c) 2019-2021 SUSE LLC
# Licensed under the terms of the MIT license.
#
# In order to use different end-point to download rpms other than the manager instance itself, one can do so with
# setting pillar data values as mentioned in upload_files/rpm_enpoint.sls. These scenarios test this feature

@scope_onboarding
Feature: Repos file generation based on custom pillar data

  Scenario: Subscribe the SLES minion to a channel
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test-Channel-x86_64"
    And I wait until I see "Test-Channel-x86_64 Child Channel" text
    And I uncheck "Test-Channel-x86_64 Child Channel"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page

  Scenario: Check the default RPM download point values
    Given I am on the Systems overview page of this "sle_minion"
    Then the susemanager repo file should exist on the "sle_minion"
    And I should see "https", "proxy" and "443" in the repo file on the "sle_minion"

  Scenario: Set the custom RPM download point
    Given I am on the Systems overview page of this "sle_minion"
    When I install a salt pillar top file for "pkg_endpoint" with target "*" on the server
    And I wait for "1" seconds
    And I install a salt pillar file with name "pkg_endpoint.sls" on the server
    And I refresh the pillar data

  Scenario: Subscribe the SLES minion to a channel again so new RPM end-point will be taken into account
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test-Channel-x86_64"
    And I wait until I see "Test-Channel-x86_64 Child Channel" text
    And I uncheck "Test-Channel-x86_64 Child Channel"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page

  Scenario: Check the channel.repo file to see the custom RPM download point
    Given I am on the Systems overview page of this "sle_minion"
    Then the susemanager repo file should exist on the "sle_minion"
    And I should see "ftp", "scc.com" and "445" in the repo file on the "sle_minion"

  Scenario: Cleanup: remove the custom RPM download point
    Given I am on the Systems overview page of this "sle_minion"
    When I delete a salt "pillar" file with name "pkg_endpoint.sls" on the server
    When I delete a salt "pillar" file with name "top.sls" on the server
    And I refresh the pillar data

  Scenario: Cleanup: subscribe the SLES minion to a channel
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test-Channel-x86_64"
    And I wait until I see "Test-Channel-x86_64 Child Channel" text
    And I uncheck "Test-Channel-x86_64 Child Channel"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page

  Scenario: Cleanup: recheck the default RPM download point values
    Given I am on the Systems overview page of this "sle_minion"
    Then the susemanager repo file should exist on the "sle_minion"
    And I should see "https", "proxy" and "443" in the repo file on the "sle_minion"

