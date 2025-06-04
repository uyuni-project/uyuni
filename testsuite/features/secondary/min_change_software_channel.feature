# Copyright (c) 2021-2025 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature can cause failures in the following features:
# - features/secondary/allcli_software_channels.feature
# If "SLE15-SP4-Installer-Updates for x86_64" fails to be unchecked

# This test fails on github validation
@skip_if_github_validation
@scc_credentials
@scope_changing_software_channels
@sle_minion
Feature: Assign child channel to a system

  Scenario: Log in as org admin user
    Given I am authorized

@susemanager
  Scenario: Check the system is still subscribed to old channels before channel change completes
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    Then radio button "SLE-Product-SLES15-SP4-Pool for x86_64" should be checked
    And I wait until I do not see "Loading..." text
    And I should see "SLE15-SP4-Installer-Updates for x86_64" as unchecked

@uyuni
  Scenario: Check the system is still subscribed to old channels before channel change completes
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    Then radio button "openSUSE Leap 15.6 (x86_64)" should be checked
    And I wait until I do not see "Loading..." text
    And I should see "Uyuni Client Tools for openSUSE Leap 15.6 (x86_64)" as unchecked

@susemanager
  Scenario: Check via API old channels are still the same on the system before channel change completes
    When I refresh the metadata for "sle_minion"
    Then channel "SLE-Product-SLES15-SP4-Pool for x86_64" should be enabled on "sle_minion"
    And channel "SLE15-SP4-Installer-Updates for x86_64" should be disabled on "sle_minion"

@uyuni
  Scenario: Check via API old channels are still the same on the system before channel change completes
    When I refresh the metadata for "sle_minion"
    Then channel "openSUSE Leap 15.6 (x86_64)" should be enabled on "sle_minion"
    And channel "Uyuni Client Tools for openSUSE Leap 15.6 (x86_64)" should be disabled on "sle_minion"

@susemanager
  Scenario: Assign a child channel to the system
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    Then radio button "SLE-Product-SLES15-SP4-Pool for x86_64" should be checked
    And I wait until I do not see "Loading..." text
    And I check "SLE15-SP4-Installer-Updates for x86_64"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    And I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page
    Then channel "SLE15-SP4-Installer-Updates for x86_64" should be enabled on "sle_minion"

@uyuni
  Scenario: Assign a child channel to the system
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    Then radio button "openSUSE Leap 15.6 (x86_64)" should be checked
    And I wait until I do not see "Loading..." text
    And I check "Uyuni Client Tools for openSUSE Leap 15.6 (x86_64)"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    And I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page
    Then channel "Uyuni Client Tools for openSUSE Leap 15.6 (x86_64)" should be enabled on "sle_minion"

  Scenario: Check channel change has completed for the system
    Given I am on the Systems overview page of this "sle_minion"
    When I wait until event "Subscribe channels scheduled" is completed
    Then I should see a "The client completed this action on" text

@susemanager
  Scenario: Check the system is subscribed to the new channels
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    Then radio button "SLE-Product-SLES15-SP4-Pool for x86_64" should be checked
    And I wait until I do not see "Loading..." text
    And I should see "SLE15-SP4-Installer-Updates for x86_64" as checked

@uyuni
  Scenario: Check the system is subscribed to the new channels
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    Then radio button "openSUSE Leap 15.6 (x86_64)" should be checked
    And I wait until I do not see "Loading..." text
    And I should see "Uyuni Client Tools for openSUSE Leap 15.6 (x86_64)" as checked

@susemanager
  Scenario: Check via API the new channels are enabled on the system
    When I refresh the metadata for "sle_minion"
    Then channel "SLE-Product-SLES15-SP4-Pool for x86_64" should be enabled on "sle_minion"
    And channel "SLE15-SP4-Installer-Updates for x86_64" should be enabled on "sle_minion"

@uyuni
  Scenario: Check via API the new channels are enabled on the system
    When I refresh the metadata for "sle_minion"
    Then channel "openSUSE Leap 15.6 (x86_64)" should be enabled on "sle_minion"
    And channel "Uyuni Client Tools for openSUSE Leap 15.6 (x86_64)" should be enabled on "sle_minion"

@susemanager
  Scenario: Cleanup: subscribe the system back to previous channels
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    Then radio button "SLE-Product-SLES15-SP4-Pool for x86_64" should be checked
    And I wait until I do not see "Loading..." text
    And I wait until I see "SLE15-SP4-Installer-Updates for x86_64" text
    And I include the recommended child channels
    And I check "SLE-Module-DevTools15-SP4-Pool for x86_64"
    And I check "Fake-RPM-SUSE-Channel"
    And I uncheck "SLE15-SP4-Installer-Updates for x86_64"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page
    Then channel "SLE15-SP4-Installer-Updates for x86_64" should be disabled on "sle_minion"

@uyuni
  Scenario: Cleanup: subscribe the system back to previous channels
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    Then radio button "openSUSE Leap 15.6 (x86_64)" should be checked
    And I wait until I do not see "Loading..." text
    And I wait until I see "Uyuni Client Tools for openSUSE Leap 15.6 (x86_64)" text
    And I uncheck "Uyuni Client Tools for openSUSE Leap 15.6 (x86_64)"
    And I check "Uyuni Client Tools for openSUSE Leap 15.6 (x86_64) (Development)"
    And I check "Fake-RPM-SUSE-Channel"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page
    Then channel "Uyuni Client Tools for openSUSE Leap 15.6 (x86_64)" should be disabled on "sle_minion"
