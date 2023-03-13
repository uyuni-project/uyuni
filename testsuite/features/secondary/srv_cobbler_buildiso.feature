# Copyright (c) 2022 SUSE LLC.
# Licensed under the terms of the MIT license.

@scope_cobbler
Feature: Cobbler buildiso
  Builds several ISOs with Cobbler and checks the configuration files and ISOs afterwards.

  Scenario: Log in as testing user in the cobbler buildiso context
    Given I am authorized as "testing" with password "testing"

  Scenario: Copy cobbler profiles on the server in the cobbler buildiso context
    When I copy autoinstall mocked files on server

  Scenario: Create a dummy distro in the cobbler buildiso context
    Given cobblerd is running
    When I create distro "buildisodistro" as user "testing" with password "testing"

  Scenario: Create dummy profiles in the cobbler buildiso context
    Given distro "buildisodistro" exists
    When I create profile "orchid" for distro "buildisodistro" as user "testing" with password "testing"
    And I create profile "flame" for distro "buildisodistro" as user "testing" with password "testing"
    And I create profile "pearl" for distro "buildisodistro" as user "testing" with password "testing"

  Scenario: Check cobbler created a distro and profiles in the cobbler buildiso context
    When I follow the left menu "Systems > Autoinstallation > Profiles"
    Then I should see a "buildisodistro" text
    And I should see a "orchid" text
    And I should see a "flame" text
    And I should see a "pearl" text

  Scenario: Create dummy system in the Cobbler buildiso context
    Given profile "orchid" exists
    When I create system "testsystem" for profile "orchid" as user "testing" with password "testing"
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

  # TODO: Fails unless https://github.com/cobbler/cobbler/issues/2995 is fixed
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
    When I remove system "testsystem" as user "testing" with password "testing"
    And I remove kickstart profiles and distros
    And I follow the left menu "Systems > Autoinstallation > Profiles"
    Then I should not see a "buildisodistro" text
    And I should not see a "orchid" text
    And I should not see a "flame" text
    And I should not see a "flame" text

  Scenario: Cleanup: Remove buildiso tmpdir and built ISO file in the cobbler buildiso context
    When I cleanup after Cobbler buildiso
