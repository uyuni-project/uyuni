# Copyright (c) 2022-2024 SUSE LLC.
# Licensed under the terms of the MIT license.

@scope_cobbler
@skip_if_github_validation
Feature: Cobbler buildiso
  Builds several ISOs with Cobbler and checks the configuration files and ISOs afterwards.

  Scenario: Start Cobbler monitoring
    When I start local monitoring of Cobbler
    And I backup Cobbler settings file

  Scenario: Log in as testing user in the cobbler buildiso context
    Given I am authorized as "testing" with password "testing"
    And I am logged in via the Cobbler API as user "testing" with password "testing"

  Scenario: Copy cobbler profiles on the server in the cobbler buildiso context
    When I copy autoinstall mocked files on server

  Scenario: Create a dummy distro in the cobbler buildiso context
    Given cobblerd is running
    When I create distro "buildisodistro"

  Scenario: Create dummy profiles in the cobbler buildiso context
    Given distro "buildisodistro" exists
    When I create profile "orchid" for distro "buildisodistro"
    And I create profile "flame" for distro "buildisodistro"
    And I create profile "pearl" for distro "buildisodistro"

  Scenario: Check cobbler created a distro and profiles in the cobbler buildiso context
    When I follow the left menu "Systems > Autoinstallation > Profiles"
    Then I should see a "buildisodistro" text
    And I should see a "orchid" text
    And I should see a "flame" text
    And I should see a "pearl" text

  Scenario: Create dummy system in the Cobbler buildiso context
    Given profile "orchid" exists
    When I create system "testsystem" for profile "orchid"
    And I add the Cobbler parameter "name-servers" with value "9.9.9.9" to item "system" with name "testsystem"

  Scenario: Prepare the cobbler buildiso context
    When I prepare Cobbler for the buildiso command

  Scenario: Run Cobbler buildiso with all profiles and check isolinux config file in the cobbler buildiso context
    When I run Cobbler buildiso for distro "buildisodistro" and all profiles
    And I check Cobbler buildiso ISO "profile_all" with xorriso
    And I check the Cobbler parameter "nameserver" with value "9.9.9.9" in the isolinux.cfg
    And I cleanup xorriso temp files

  Scenario: Run Cobbler buildiso with selected profile in the cobbler buildiso context
    When I run Cobbler buildiso for distro "buildisodistro" and profile "orchid"
    And I check Cobbler buildiso ISO "orchid" with xorriso
    And I cleanup xorriso temp files

  Scenario: Run Cobbler buildiso with selected profile and without dns entries in the cobbler buildiso context
    When I run Cobbler buildiso for distro "buildisodistro" and profile "orchid" without dns entries
    And I check Cobbler buildiso ISO "orchid" with xorriso
    And I cleanup xorriso temp files

  Scenario: Run Cobbler buildiso airgapped with all profiles in the cobbler buildiso context
    When I run Cobbler buildiso "airgapped" for distro "buildisodistro"
    And I check Cobbler buildiso ISO "airgapped" with xorriso
    And I cleanup xorriso temp files

  Scenario: Run Cobbler buildiso standalone with all profiles in the cobbler buildiso context
    When I run Cobbler buildiso "standalone" for distro "buildisodistro"
    And I check Cobbler buildiso ISO "standalone" with xorriso
    And I cleanup xorriso temp files

  Scenario: Cleanup: delete test distro and profiles in the cobbler buildiso context
    Given I am authorized as "testing" with password "testing"
    # the order is important here. First systems, then profiles then distros
    When I remove system "testsystem"
    And I remove profile "orchid" as user "testing" with password "testing"
    And I remove profile "flame" as user "testing" with password "testing"
    And I remove profile "pearl" as user "testing" with password "testing"
    And I remove distro "buildisodistro" as user "testing" with password "testing"
    And I follow the left menu "Systems > Autoinstallation > Profiles"
    Then I should not see a "buildisodistro" text
    And I should not see a "orchid" text
    And I should not see a "flame" text
    And I should not see a "flame" text

  Scenario: Cleanup: Remove buildiso tmpdir and built ISO file in the cobbler buildiso context
    When I cleanup after Cobbler buildiso
    And I log out from Cobbler via the API

@flaky
  Scenario: Check for errors in Cobbler monitoring
    Then the local logs for Cobbler should not contain errors
