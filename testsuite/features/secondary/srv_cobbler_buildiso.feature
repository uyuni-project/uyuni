# Copyright (c) 2022 SUSE LLC.
# Licensed under the terms of the MIT license.

@scope_cobbler @scope_iso
Feature: Cobbler buildiso

  Scenario: Log in as testing user in the cobbler buildiso context
    Given I am authorized as "testing" with password "testing"

  Scenario: Copy cobbler profiles on the server in the cobbler buildiso context
    When I copy autoinstall mocked files on server

  Scenario: Create a dummy distro in the cobbler buildiso context
    Given cobblerd is running
    Then create distro "testdistro" as user "testing" with password "testing"

  Scenario: Create dummy profiles in the cobbler buildiso context
    Given cobblerd is running
    And distro "testdistro" exists
    Then create profile "orchid" for distro "testdistro" as user "testing" with password "testing"
    Then create profile "flame" for distro "testdistro" as user "testing" with password "testing"
    Then create profile "pearl" for distro "testdistro" as user "testing" with password "testing"

  Scenario: Check cobbler created a distro and profiles in the cobbler buildiso context
    When I follow the left menu "Systems > Autoinstallation > Profiles"
    Then I should see a "testdistro" text
    And I should see a "orchid" text
    And I should see a "flame" text
    And I should see a "pearl" text

  Scenario: Create dummy system in the Cobbler buildiso context
    Given cobblerd is running
    And profile "orchid" exists
    Then create system "testsystem" for profile "orchid" as user "testing" with password "testing"
    Then I add the parameter "name-servers" with value "9.9.9.9" to item "system" with name "testsystem"

  Scenario: Prepare the cobbler buildiso context
    Given cobblerd is running
    When I prepare Cobbler for the buildiso command

  Scenario: Run Cobbler buildiso with all profiles and check isolinux config file in the cobbler buildiso context
    Given cobblerd is running
    When I run Cobbler buildiso for distro "testdistro" and all profiles
    And I check Cobbler buildiso ISO "profile_all" with xorriso
    And I check the parameter "nameserver" with value "9.9.9.9" in the isolinux.cfg

  Scenario: Run Cobbler buildiso with selected profile in the cobbler buildiso context
    When I run Cobbler buildiso for distro "testdistro" and profile "orchid"
    And I check Cobbler buildiso ISO "orchid" with xorriso

  # TODO: Enable when https://github.com/cobbler/cobbler/issues/2995 is fixed
#  Scenario: Run Cobbler buildiso with selected profile and without dns entries in the cobbler buildiso context
#    When I run Cobbler buildiso for distro "testdistro" and profile "orchid" without dns entries
#    And I check Cobbler buildiso ISO "orchid" with xorriso

  Scenario: Run Cobbler buildiso airgapped with all profiles in the cobbler buildiso context
    When I run Cobbler buildiso "airgapped" for distro "testdistro"
    And I check Cobbler buildiso ISO "airgapped" with xorriso

  Scenario: Run Cobbler buildiso standalone with all profiles in the cobbler buildiso context
    When I run Cobbler buildiso "standalone" for distro "testdistro"
    And I check Cobbler buildiso ISO "standalone" with xorriso

  Scenario: Cleanup: delete test distro and profiles in the cobbler buildiso context
    Given I am authorized as "testing" with password "testing"
    # FIXME: Remove system before removing profile
    When I remove kickstart profiles and distros
    And I follow the left menu "Systems > Autoinstallation > Profiles"
    Then I should not see a "testdistro" text
    And I should not see a "orchid" text
    And I should not see a "flame" text
    And I should not see a "flame" text

  Scenario: Cleanup: Remove buildiso tmpdir and built ISO file in the cobbler buildiso context
    Then I run "rm -Rf /var/cache/cobbler" on "server"
