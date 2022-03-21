# Copyright (c) 2022 SUSE LLC.
# Licensed under the terms of the MIT license.

@scope_cobbler
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
    Then create profile "testprofile1" for distro "testdistro" as user "testing" with password "testing"
#    Then create profile "testprofile2" for distro "testdistro" as user "testing" with password "testing"


  Scenario: Check cobbler created a distro and profiles in the cobbler buildiso context
    When I follow the left menu "Systems > Autoinstallation > Profiles"
    Then I should see a "testdistro" text
    And I should see a "testprofile1" text
#    And I should see a "testprofile2" text

  Scenario: Prepare the cobbler buildiso context
    Given I prepare Cobbler for the buildiso command

  Scenario: Run Cobbler buildiso with all profiles in the cobbler buildiso context
    When I run Cobbler buildiso for distro "testdistro"
    # add check for successful build

 Scenario: Run Cobbler buildiso with selected profile in the cobbler buildiso context
    When I run Cobbler buildiso for distro "testdistro" and profile "testprofile1"
    # add check for successful build

  Scenario: Run Cobbler buildiso airgapped with all profiles in the cobbler buildiso context
    When I run Cobbler buildiso "airgapped" for distro "testdistro"
    # add check for successful build

  Scenario: Run Cobbler buildiso standalone with all profiles in the cobbler buildiso context
    When I run Cobbler buildiso "standalone" for distro "testdistro"
    # add check for successful build

#  Scenario: Cleanup: delete test distro and profiles in the cobbler buildiso context
#    When I remove kickstart profiles and distros
#    And I follow the left menu "Systems > Autoinstallation > Profiles"
#    Then I should not see a "testdistro" text
#    And I should not see a "testprofile" text
#
#  Scenario: Cleanup: Remove buildiso tmpdir and built ISO file in the cobbler buildiso context
#    Then I run "rm -Rf /var/cache/cobbler" on "server"
