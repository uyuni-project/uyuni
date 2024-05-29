# Copyright (c) 2019-2024 SUSE LLC
# Licensed under the terms of the MIT license.
#
# In order to use different end-point to download rpms other than the manager instance itself, one can do so with
# setting pillar data values as mentioned in upload_files/rpm_enpoint.sls. These scenarios test this feature

@scope_onboarding
@custom_download_endpoint
Feature: Repos file generation based on custom pillar data

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Select the channels of the SLES minion
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "SLE-Product-SLES15-SP4-Pool for x86_64"
    And I wait until I see "SLE-Module-Basesystem15-SP4-Pool for x86_64" text
    And I uncheck "SLE-Module-Basesystem15-SP4-Pool for x86_64"
    And I uncheck "SLE-Product-SLES15-SP4-LTSS-Updates for x86_64"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page

  Scenario: Check the default RPM download point values
    Given I am on the Systems overview page of this "sle_minion"
    Then the susemanager repo file should exist on the "sle_minion"
    And the repo file should contain the normal download endpoint on the "sle_minion"

  Scenario: Set the custom RPM download point
    Given I am on the Systems overview page of this "sle_minion"
    When I install a salt pillar top file for "pkg_endpoint" with target "*" on the server
    And I wait for "1" seconds
    And I install the package download endpoint pillar file on the server
    And I refresh the pillar data

  Scenario: Select the channels of the SLES minion again so new RPM end point will be taken into account
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I wait until I see "SLE-Module-Basesystem15-SP4-Pool for x86_64" text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page

  Scenario: Check the channel.repo file to see the custom RPM download point
    Given I am on the Systems overview page of this "sle_minion"
    Then the susemanager repo file should exist on the "sle_minion"
    And the repo file should contain the custom download endpoint on the "sle_minion"

  Scenario: Cleanup: remove the custom RPM download point
    When I delete the package download endpoint pillar file from the server
    And I install a salt pillar top file for "disable_local_repos_off, salt_bundle_config" with target "*" on the server
    And I refresh the pillar data

  Scenario: Cleanup: select the channels of the SLES minion as before
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "SLE-Product-SLES15-SP4-Pool for x86_64"
    And I wait until I see "SLE15-SP4-Installer-Updates for x86_64" text
    And I include the recommended child channels
    And I check "SLE-Module-DevTools15-SP4-Pool for x86_64"
    And I check "Fake-RPM-SUSE-Channel"
    And I check "SLE-Product-SLES15-SP4-LTSS-Updates for x86_64"
    And I check "SLE-Module-Containers15-SP4-Pool for x86_64"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page

  Scenario: Cleanup: recheck the default RPM download point values
    Then the susemanager repo file should exist on the "sle_minion"
    And the repo file should contain the normal download endpoint on the "sle_minion"
