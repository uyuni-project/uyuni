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
  Scenario: Pre-requisite: unsubscribe from old channels
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "SLE-Product-SLES15-SP4-Pool for x86_64"
    Then radio button "SLE-Product-SLES15-SP4-Pool for x86_64" should be checked
    And I wait until I do not see "Loading..." text
    When I uncheck "SLE15-SP4-Installer-Updates for x86_64"
    And I should see "SLE15-SP4-Installer-Updates for x86_64" as unchecked
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text

@uyuni
  Scenario: Pre-requisite: unsubscribe from old channels
    Given I am on the Systems overview page of this "sle_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "openSUSE Tumbleweed (x86_64)"
    Then radio button "openSUSE Tumbleweed (x86_64)" should be checked
    When I wait until I do not see "Loading..." text
    And I uncheck "Uyuni Client Tools for openSUSE Tumbleweed (x86_64)" by label
    Then I should see "Uyuni Client Tools for openSUSE Tumbleweed (x86_64)" as unchecked
    When I uncheck "Uyuni Client Tools for openSUSE Tumbleweed (x86_64) (Development)" by label
    Then I should see "Uyuni Client Tools for openSUSE Tumbleweed (x86_64) (Development)" as unchecked
    When I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text

@susemanager
  Scenario: Pre-requisite: check via API that the system is unsubscribed from old channels
    When I refresh the metadata for "sle_minion"
    Then channel "SLE-Product-SLES15-SP4-Pool for x86_64" should be enabled on "sle_minion"
    And channel "SLE15-SP4-Installer-Updates for x86_64" should be disabled on "sle_minion"

@uyuni
  Scenario: Pre-requisite: check via API that the system is unsubscribed from old channels
    When I refresh the metadata for "sle_minion"
    Then channel "openSUSE Tumbleweed (x86_64)" should be enabled on "sle_minion"
    And channel "Uyuni Client Tools for openSUSE Tumbleweed (x86_64)" should be disabled on "sle_minion"

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
    Then radio button "openSUSE Tumbleweed (x86_64)" should be checked
    When I wait until I do not see "Loading..." text
    And I check "Uyuni Client Tools for openSUSE Tumbleweed (x86_64)" by label
    Then I should see "Uyuni Client Tools for openSUSE Tumbleweed (x86_64)" as checked
    When I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page
    Then channel "Uyuni Client Tools for openSUSE Tumbleweed (x86_64)" should be enabled on "sle_minion"

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
    Then radio button "openSUSE Tumbleweed (x86_64)" should be checked
    And I wait until I do not see "Loading..." text
    And I should see "Uyuni Client Tools for openSUSE Tumbleweed (x86_64)" as checked

@susemanager
  Scenario: Check via API the new channels are enabled on the system
    When I refresh the metadata for "sle_minion"
    Then channel "SLE-Product-SLES15-SP4-Pool for x86_64" should be enabled on "sle_minion"
    And channel "SLE15-SP4-Installer-Updates for x86_64" should be enabled on "sle_minion"

@uyuni
  Scenario: Check via API the new channels are enabled on the system
    When I refresh the metadata for "sle_minion"
    Then channel "openSUSE Tumbleweed (x86_64)" should be enabled on "sle_minion"
    And channel "Uyuni Client Tools for openSUSE Tumbleweed (x86_64)" should be enabled on "sle_minion"

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
    Then radio button "openSUSE Tumbleweed (x86_64)" should be checked
    And I wait until I do not see "Loading..." text
    And I wait until I see "Uyuni Client Tools for openSUSE Tumbleweed (x86_64)" text
    And I uncheck "Uyuni Client Tools for openSUSE Tumbleweed (x86_64)" by label
    Then I should see "Uyuni Client Tools for openSUSE Tumbleweed (x86_64)" as unchecked
    When I check "Uyuni Client Tools for openSUSE Tumbleweed (x86_64) (Development)" by label
    Then I should see "Uyuni Client Tools for openSUSE Tumbleweed (x86_64) (Development)" as checked
    When I check "Fake-RPM-SUSE-Channel"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    And I wait until I see "Changing the channels has been scheduled." text
    And I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page
    Then channel "Uyuni Client Tools for openSUSE Tumbleweed (x86_64)" should be disabled on "sle_minion"
