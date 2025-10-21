# Copyright (c) 2022-2025 SUSE LLC
# Licensed under the terms of the MIT license.

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
  Scenario: Update openSUSE Tumbleweed key with synced base product
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE Test Key x86_64" in the content area
    And I wait until I do not see "Loading..." text
    And I select the parent channel for the "sle_minion" from "selectedBaseChannel"
    And I wait until I do not see "Loading..." text
    And I check "Uyuni Client Tools for openSUSE Tumbleweed (x86_64) (Development)"
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
  Scenario: Update openSUSE Tumbleweed SSH key with synced base product
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE SSH Test Key x86_64" in the content area
    And I wait until I do not see "Loading..." text
    And I select the parent channel for the "sle_minion" from "selectedBaseChannel"
    And I wait until I do not see "Loading..." text
    And I check "Uyuni Client Tools for openSUSE Tumbleweed (x86_64) (Development)"
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
  Scenario: Update openSUSE Tumbleweed SSH tunnel key with synced base product
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE SSH Tunnel Test Key x86_64" in the content area
    And I wait until I do not see "Loading..." text
    And I select the parent channel for the "sle_minion" from "selectedBaseChannel"
    And I wait until I do not see "Loading..." text
    And I check "Uyuni Client Tools for openSUSE Tumbleweed (x86_64) (Development)"
    And I check "Fake-RPM-SUSE-Channel"
    And I wait until "Fake-RPM-SUSE-Channel" has been checked
    And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE SSH Tunnel Test Key x86_64 has been modified" text

@scc_credentials
@susemanager
@proxy
@containerized_server
@transactional_server
  Scenario: Update the SLE Micro proxy key with synced base product
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Proxy Key x86_64" in the content area
    And I wait for child channels to appear
    And I select the parent channel for the "proxy_container" from "selectedBaseChannel"
    And I wait for child channels to appear
    And I include the recommended child channels
    And I wait until "ManagerTools-SL-Micro-6.1 for x86_64" has been checked
    And I check "SUSE-Multi-Linux-Manager-Proxy-5.1 for x86_64"
    And I check "SUSE-Multi-Linux-Manager-Retail-Branch-Server-5.1 for x86_64"
    And I click on "Update Activation Key"
    Then I should see a "Activation key Proxy Key x86_64 has been modified" text

@scc_credentials
@susemanager
@proxy
@containerized_server
@skip_if_transactional_server
  Scenario: Update the SLES proxy key with synced base product
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Proxy Key x86_64" in the content area
    And I wait for child channels to appear
    And I select the parent channel for the "proxy_nontransactional" from "selectedBaseChannel"
    And I wait for child channels to appear
    And I include the recommended child channels
    And I wait until "ManagerTools-SLE15-Pool for x86_64 SP7" has been checked
    And I check "SUSE-Manager-Proxy-5.1-Pool for x86_64"
    And I check "SUSE-Manager-Proxy-5.1-Updates for x86_64"
    And I click on "Update Activation Key"
    Then I should see a "Activation key Proxy Key x86_64 has been modified" text

@uyuni
@proxy
@containerized_server
@skip_if_cloud
  Scenario: Update the openSUSE Tumbleweed Proxy Host key with synced base product
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Proxy Key x86_64" in the content area
    And I wait for child channels to appear
    And I select the parent channel for the "proxy_container" from "selectedBaseChannel"
    And I wait for child channels to appear
    And I check "Uyuni Client Tools for openSUSE Tumbleweed (x86_64) (Development)"
    And I click on "Update Activation Key"
    Then I should see a "Activation key Proxy Key x86_64 has been modified" text

@scc_credentials
  Scenario: Update build host key with synced base product
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Build host Key x86_64" in the content area
    And I wait for child channels to appear
    And I select the parent channel for the "build_host" from "selectedBaseChannel"
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
@uyuni
  Scenario: Update build host key with Uyuni client tools
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Build host Key x86_64" in the content area
    And I wait for child channels to appear
    And I select the parent channel for the "build_host" from "selectedBaseChannel"
    And I wait for child channels to appear
    And I check "Uyuni Client Tools for SLES15 SP4 x86_64 (Development)"
    And I wait until "Uyuni Client Tools for SLES15 SP4 x86_64 (Development)" has been checked
    And I click on "Update Activation Key"
    Then I should see a "Activation key Build host Key x86_64 has been modified" text

@skip_if_github_validation
@scc_credentials
@uyuni
  Scenario: Update build host key with dev child channel
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Build host Key x86_64" in the content area
    And I wait for child channels to appear
    And I select the parent channel for the "build_host" from "selectedBaseChannel"
    And I wait for child channels to appear
    And I check "Devel-Build-Host-Channel"
    And I wait until "Devel-Build-Host-Channel" has been checked
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
    And I check "Uyuni Client Tools for SLES15 SP4 x86_64 (Development)"
    And I wait until "Uyuni Client Tools for SLES15 SP4 x86_64 (Development)" has been checked
    And I click on "Update Activation Key"
    Then I should see a "Activation key Terminal Key x86_64 has been modified" text

@skip_if_github_validation
@sle_minion
  Scenario: Update SLE key with to include dev child channel
    When I follow the left menu "Systems > Activation Keys"
    And I follow "SUSE Test Key x86_64" in the content area
    And I wait for child channels to appear
    And I check "Devel-SUSE-Channel"
    And I click on "Update Activation Key"
    Then I should see a "Activation key SUSE Test Key x86_64 has been modified" text

@skip_if_github_validation
@deblike_minion
  Scenario: Update Debian-like key with to include dev child channel
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Debian-like Test Key" in the content area
    And I wait for child channels to appear
    And I check "Devel-Debian-like-Channel"
    And I click on "Update Activation Key"
    Then I should see a "Activation key Debian-like Test Key has been modified" text

@skip_if_github_validation
@rhlike_minion
  Scenario: Update RedHat-like key with to include dev child channel
    When I follow the left menu "Systems > Activation Keys"
    And I follow "RedHat like Test Key" in the content area
    And I wait for child channels to appear
    And I check "Devel-RH-like-Channel"
    And I click on "Update Activation Key"
    Then I should see a "Activation key RedHat like Test Key has been modified" text
