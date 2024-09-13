# Copyright (c) 2022-2024 SUSE LLC
# Licensed under the terms of the MIT license.

@skip_if_github_validation
Feature: Update activation keys
  In order to register systems to the spacewalk server
  As admin
  I want to update activation keys to use synchronized base products

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

@scc_credentials
@susemanager
  Scenario: Update SLE key with synced base product
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE Test Key x86_64" in the content area
    And I wait for child channels to appear
    And I select the parent channel for the "sle_minion" from "selectedBaseChannel"
    And I wait for child channels to appear
    And I include the recommended child channels
    And I wait until "SLE-Module-Basesystem15-SP4-Pool for x86_64" has been checked
    And I wait until "SLE-Module-Basesystem15-SP4-Updates for x86_64" has been checked
    And I wait until "SLE-Module-Server-Applications15-SP4-Pool for x86_64" has been checked
    And I wait until "SLE-Module-Server-Applications15-SP4-Updates for x86_64" has been checked
    And I check "SLE-Module-DevTools15-SP4-Pool for x86_64"
    And I wait until "SLE-Module-DevTools15-SP4-Updates for x86_64" has been checked
    And I wait until "SLE-Module-Desktop-Applications15-SP4-Pool for x86_64" has been checked
    And I wait until "SLE-Module-Desktop-Applications15-SP4-Updates for x86_64" has been checked
    And I check "SLE-Module-Containers15-SP4-Pool for x86_64"
    And I wait until "SLE-Module-Containers15-SP4-Updates for x86_64" has been checked
    And I check "SLE-Product-SLES15-SP4-LTSS-Updates for x86_64"
    And I wait until "SLE-Product-SLES15-SP4-LTSS-Updates for x86_64" has been checked
    And I check "Fake-RPM-SUSE-Channel"
    And I wait until "Fake-RPM-SUSE-Channel" has been checked
    And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE Test Key x86_64 has been modified" text

@uyuni
  Scenario: Update openSUSE Leap key with synced base product
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE Test Key x86_64" in the content area
    And I wait until I do not see "Loading..." text
    And I select the parent channel for the "sle_minion" from "selectedBaseChannel"
    And I wait until I do not see "Loading..." text
    And I check "openSUSE 15.5 non oss (x86_64)"
    And I check "openSUSE Leap 15.5 non oss Updates (x86_64)"
    And I check "openSUSE Leap 15.5 Updates (x86_64)"
    And I check "Update repository of openSUSE Leap 15.5 Backports (x86_64)"
    And I check "Update repository with updates from SUSE Linux Enterprise 15 for openSUSE Leap 15.5 (x86_64)"
    And I check "Uyuni Client Tools for openSUSE Leap 15.5 (x86_64) (Development)"
    And I check "Fake-RPM-SUSE-Channel"
    And I wait until "Fake-RPM-SUSE-Channel" has been checked
    And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE Test Key x86_64 has been modified" text

@scc_credentials
@susemanager
  Scenario: Update SLE SSH key with synced base product
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE SSH Test Key x86_64" in the content area
    And I wait for child channels to appear
    And I select the parent channel for the "sle_minion" from "selectedBaseChannel"
    And I wait for child channels to appear
    And I include the recommended child channels
    And I check "SLE-Module-DevTools15-SP4-Pool for x86_64"
    And I wait until "SLE-Module-DevTools15-SP4-Updates for x86_64" has been checked
    And I wait until "SLE-Module-Desktop-Applications15-SP4-Pool for x86_64" has been checked
    And I wait until "SLE-Module-Desktop-Applications15-SP4-Updates for x86_64" has been checked
    And I check "SLE-Product-SLES15-SP4-LTSS-Updates for x86_64"
    And I wait until "SLE-Product-SLES15-SP4-LTSS-Updates for x86_64" has been checked
    And I check "Fake-RPM-SUSE-Channel"
    And I wait until "Fake-RPM-SUSE-Channel" has been checked
    And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE SSH Test Key x86_64 has been modified" text

@uyuni
  Scenario: Update openSUSE Leap SSH key with synced base product
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE SSH Test Key x86_64" in the content area
    And I wait until I do not see "Loading..." text
    And I select the parent channel for the "sle_minion" from "selectedBaseChannel"
    And I wait until I do not see "Loading..." text
    And I check "openSUSE 15.5 non oss (x86_64)"
    And I check "openSUSE Leap 15.5 non oss Updates (x86_64)"
    And I check "openSUSE Leap 15.5 Updates (x86_64)"
    And I check "Update repository of openSUSE Leap 15.5 Backports (x86_64)"
    And I check "Update repository with updates from SUSE Linux Enterprise 15 for openSUSE Leap 15.5 (x86_64)"
    And I check "Uyuni Client Tools for openSUSE Leap 15.5 (x86_64) (Development)"
    And I check "Fake-RPM-SUSE-Channel"
    And I wait until "Fake-RPM-SUSE-Channel" has been checked
    And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE SSH Test Key x86_64 has been modified" text

@scc_credentials
@susemanager
  Scenario: Update SLE SSH tunnel key with synced base product
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE SSH Tunnel Test Key x86_64" in the content area
    And I wait for child channels to appear
    And I select the parent channel for the "sle_minion" from "selectedBaseChannel"
    And I wait for child channels to appear
    And I include the recommended child channels
    And I check "SLE-Module-DevTools15-SP4-Pool for x86_64"
    And I wait until "SLE-Module-DevTools15-SP4-Updates for x86_64" has been checked
    And I wait until "SLE-Module-Desktop-Applications15-SP4-Pool for x86_64" has been checked
    And I wait until "SLE-Module-Desktop-Applications15-SP4-Updates for x86_64" has been checked
    And I check "SLE-Product-SLES15-SP4-LTSS-Updates for x86_64"
    And I wait until "SLE-Product-SLES15-SP4-LTSS-Updates for x86_64" has been checked
    And I check "Fake-RPM-SUSE-Channel"
    And I wait until "Fake-RPM-SUSE-Channel" has been checked
    And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE SSH Tunnel Test Key x86_64 has been modified" text

@uyuni
  Scenario: Update openSUSE Leap SSH tunnel key with synced base product
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE SSH Tunnel Test Key x86_64" in the content area
    And I wait until I do not see "Loading..." text
    And I select the parent channel for the "sle_minion" from "selectedBaseChannel"
    And I wait until I do not see "Loading..." text
    And I check "openSUSE 15.5 non oss (x86_64)"
    And I check "openSUSE Leap 15.5 non oss Updates (x86_64)"
    And I check "openSUSE Leap 15.5 Updates (x86_64)"
    And I check "Update repository of openSUSE Leap 15.5 Backports (x86_64)"
    And I check "Update repository with updates from SUSE Linux Enterprise 15 for openSUSE Leap 15.5 (x86_64)"
    And I check "Uyuni Client Tools for openSUSE Leap 15.5 (x86_64) (Development)"
    And I check "Fake-RPM-SUSE-Channel"
    And I wait until "Fake-RPM-SUSE-Channel" has been checked
    And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE SSH Tunnel Test Key x86_64 has been modified" text

@scc_credentials
@susemanager
@proxy
@containerized_server
  Scenario: Update the SLE Micro proxy key with synced base product
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Proxy Key x86_64" in the content area
    And I wait for child channels to appear
    And I select the parent channel for the "proxy_container" from "selectedBaseChannel"
    And I wait for child channels to appear
    And I include the recommended child channels
    And I wait until "SLE-Manager-Tools-For-Micro5-Pool for x86_64 5.5" has been checked
    And I check "SUSE-Manager-Proxy-5.0-Pool for x86_64"
    And I check "SUSE-Manager-Proxy-5.0-Updates for x86_64"
    And I click on "Update Activation Key"
    Then I should see a "Activation key Proxy Key x86_64 has been modified" text

@uyuni
@proxy
@cloud
  Scenario: Update the openSUSE Leap proxy key with synced base product
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Proxy Key x86_64" in the content area
    And I wait for child channels to appear
    And I select the parent channel for the "proxy_traditional" from "selectedBaseChannel"
    And I wait for child channels to appear
    And I check "openSUSE 15.5 non oss (x86_64)"
    And I check "openSUSE Leap 15.5 non oss Updates (x86_64)"
    And I check "openSUSE Leap 15.5 Updates (x86_64)"
    And I check "Update repository of openSUSE Leap 15.5 Backports (x86_64)"
    And I check "Update repository with updates from SUSE Linux Enterprise 15 for openSUSE Leap 15.5 (x86_64)"
    And I check "Uyuni Client Tools for openSUSE Leap 15.5 (x86_64) (Development)"
    And I check "Uyuni Proxy Devel for openSUSE Leap 15.5 (x86_64)"
    And I click on "Update Activation Key"
    Then I should see a "Activation key Proxy Key x86_64 has been modified" text

@containerized_server
@uyuni
@proxy
@skip_if_cloud
  Scenario: Update the openSUSE Leap Micro 5.5 Proxy Host key with synced base product
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Proxy Key x86_64" in the content area
    And I wait for child channels to appear
    And I select the parent channel for the "proxy_container" from "selectedBaseChannel"
    And I wait for child channels to appear
    And I check "SLE Micro 5.5 Update Repository (x86_64)"
    And I check "Uyuni Client Tools for openSUSE Leap Micro 5.5 (x86_64)"
    And I check "Uyuni Client Tools for openSUSE Leap Micro 5.5 (x86_64) (Development)"
    And I click on "Update Activation Key"
    Then I should see a "Activation key Proxy Key x86_64 has been modified" text

@scc_credentials
  Scenario: Update build host key with synced base product
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Build host Key x86_64" in the content area
    And I wait for child channels to appear
    And I select the parent channel for the "buildhost" from "selectedBaseChannel"
    And I wait for child channels to appear
    And I include the recommended child channels
    And I wait until "SLE-Module-Basesystem15-SP4-Pool for x86_64" has been checked
    And I wait until "SLE-Module-Basesystem15-SP4-Updates for x86_64" has been checked
    And I wait until "SLE-Module-Server-Applications15-SP4-Pool for x86_64" has been checked
    And I wait until "SLE-Module-Server-Applications15-SP4-Updates for x86_64" has been checked
    And I check "SLE-Module-DevTools15-SP4-Pool for x86_64"
    And I wait until "SLE-Module-DevTools15-SP4-Updates for x86_64" has been checked
    And I wait until "SLE-Module-Desktop-Applications15-SP4-Pool for x86_64" has been checked
    And I wait until "SLE-Module-Desktop-Applications15-SP4-Updates for x86_64" has been checked
    And I check "SLE-Module-Containers15-SP4-Pool for x86_64"
    And I wait until "SLE-Module-Containers15-SP4-Updates for x86_64" has been checked
    And I check "SLE-Product-SLES15-SP4-LTSS-Updates for x86_64"
    And I wait until "SLE-Product-SLES15-SP4-LTSS-Updates for x86_64" has been checked
    And I click on "Update Activation Key"
    Then I should see a "Activation key Build host Key x86_64 has been modified" text

@scc_credentials
  Scenario: Update terminal key with synced base product
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Terminal Key x86_64" in the content area
    And I wait for child channels to appear
    And I select the parent channel for the "pxeboot_minion" from "selectedBaseChannel"
    And I wait for child channels to appear
    And I include the recommended child channels
    And I wait until "SLE-Module-Basesystem15-SP4-Pool for x86_64" has been checked
    And I wait until "SLE-Module-Basesystem15-SP4-Updates for x86_64" has been checked
    And I wait until "SLE-Module-Server-Applications15-SP4-Pool for x86_64" has been checked
    And I wait until "SLE-Module-Server-Applications15-SP4-Updates for x86_64" has been checked
    And I check "SLE-Module-DevTools15-SP4-Pool for x86_64"
    And I wait until "SLE-Module-DevTools15-SP4-Updates for x86_64" has been checked
    And I wait until "SLE-Module-Desktop-Applications15-SP4-Pool for x86_64" has been checked
    And I wait until "SLE-Module-Desktop-Applications15-SP4-Updates for x86_64" has been checked
    And I check "SLE-Module-Containers15-SP4-Pool for x86_64"
    And I wait until "SLE-Module-Containers15-SP4-Updates for x86_64" has been checked
    And I click on "Update Activation Key"
    Then I should see a "Activation key Terminal Key x86_64 has been modified" text

@susemanager
@scc_credentials
  Scenario: Update terminal key with normal SUSE fake channel
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Terminal Key x86_64" in the content area
    And I wait for child channels to appear
    And I check "Fake-RPM-SUSE-Channel"
    And I wait until "Fake-RPM-SUSE-Channel" has been checked
    And I click on "Update Activation Key"
    Then I should see a "Activation key Terminal Key x86_64 has been modified" text

@pxeboot_minion
@uyuni
@scc_credentials
  Scenario: Update terminal key with specific fake channel
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Terminal Key x86_64" in the content area
    And I wait for child channels to appear
    And I check "Fake-RPM-Terminal-Channel"
    And I wait until "Fake-RPM-Terminal-Channel" has been checked
    And I click on "Update Activation Key"
    Then I should see a "Activation key Terminal Key x86_64 has been modified" text
