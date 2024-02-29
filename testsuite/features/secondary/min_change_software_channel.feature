# Copyright (c) 2021-2023 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature can cause failures in the following features:
# - features/secondary/allcli_software_channels.feature
# If "SLE15-SP4-Installer-Updates for x86_64" fails to be unchecked

@skip_if_github_validation
@scc_credentials
@scope_changing_software_channels
@sle_minion
Feature: Assign child channel to a system

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

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
    Then radio button "openSUSE Leap 15.5 (x86_64)" should be checked
    And I wait until I do not see "Loading..." text
    And I should see "Uyuni Proxy Devel for openSUSE Leap 15.5 (x86_64) (Development)" as unchecked

# susemanager has the Client Tools channels more than uyuni. (+2)
# in Head also Beta Client Tools Channels (+2)
@susemanager
  Scenario: Check old channels are still enabled on the system before channel change completes
    When I refresh the metadata for "sle_minion"
    Then "17" channels should be enabled on "sle_minion"
    And channel "SLE-Product-SLES15-SP4-Pool for x86_64" should be enabled on "sle_minion"

@uyuni
  Scenario: Check old channels are still enabled on the system before channel change completes
    When I refresh the metadata for "sle_minion"
    Then "8" channels should be enabled on "sle_minion"
    And channel "openSUSE Leap 15.5 (x86_64)" should be enabled on "sle_minion"

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
    Then radio button "openSUSE Leap 15.5 (x86_64)" should be checked
    And I wait until I do not see "Loading..." text
    And I check "Uyuni Proxy Devel for openSUSE Leap 15.5 (x86_64) (Development)"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    And I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page
    Then channel "Uyuni Proxy Devel for openSUSE Leap 15.5 (x86_64) (Development)" should be enabled on "sle_minion"

  Scenario: Check channel change has completed for the system
    Given I am on the Systems overview page of this "sle_minion"
    When I wait until event "Subscribe channels scheduled by admin" is completed
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
    Then radio button "openSUSE Leap 15.5 (x86_64)" should be checked
    And I wait until I do not see "Loading..." text
    And I should see "Uyuni Proxy Devel for openSUSE Leap 15.5 (x86_64) (Development)" as checked

@susemanager
  Scenario: Check the new channels are enabled on the system
    When I refresh the metadata for "sle_minion"
    Then "18" channels should be enabled on "sle_minion"
    And channel "SLE-Product-SLES15-SP4-Pool for x86_64" should be enabled on "sle_minion"
    And channel "SLE15-SP4-Installer-Updates for x86_64" should be enabled on "sle_minion"

@uyuni
  Scenario: Check the new channels are enabled on the system
    When I refresh the metadata for "sle_minion"
    Then "9" channels should be enabled on "sle_minion"
    And channel "openSUSE Leap 15.5 (x86_64)" should be enabled on "sle_minion"
    And channel "Uyuni Proxy Devel for openSUSE Leap 15.5 (x86_64) (Development)" should be enabled on "sle_minion"

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
    Then radio button "openSUSE Leap 15.5 (x86_64)" should be checked
    And I wait until I do not see "Loading..." text
    And I wait until I see "openSUSE Leap 15.5 Updates (x86_64)" text
    And I check "openSUSE 15.5 non oss (x86_64)"
    And I check "openSUSE Leap 15.5 non oss Updates (x86_64)"
    And I check "openSUSE Leap 15.5 Updates (x86_64)"
    And I check "Update repository of openSUSE Leap 15.5 Backports (x86_64)"
    And I check "Update repository with updates from SUSE Linux Enterprise 15 for openSUSE Leap 15.5 (x86_64)"
    And I check "Uyuni Client Tools for openSUSE Leap 15.5 (x86_64) (Development)"
    And I check "Fake-RPM-SUSE-Channel"
    And I uncheck "Uyuni Proxy Devel for openSUSE Leap 15.5 (x86_64) (Development)"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page
    Then channel "Uyuni Proxy Devel for openSUSE Leap 15.5 (x86_64) (Development)" should be disabled on "sle_minion"
