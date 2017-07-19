# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Install a package to the trad-client

  Scenario: Install a package to the trad-client
    Given I am on the Systems overview page of this "sle-client"
    # we don't know if the pkgs are installed before, just remove or don't fail
    And I run "zypper -n rm andromeda-dummy" on "sle-client" without error control
    And I run "zypper -n rm virgo-dummy" on "sle-client" without error control
    And metadata generation finished for "test-channel-x86_64"
    And I follow "Software" in the content area
    And I follow "Install"
    When I check "virgo-dummy" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    And I run rhn_check on this client
    Then I should see a "1 package install has been scheduled for" text
    And "virgo-dummy-2.0-1.1" is installed on "client"

  Scenario: enable old-packages for test a patch install
    And I run "zypper -n mr -e Devel_Galaxy_BuildRepo" on "sle-client"
    And I run "zypper -n in --oldpackage andromeda-dummy-1.0-4.1" on "sle-client"

  Scenario: Install an patch to the trad-client
    Given I am on the Systems overview page of this "sle-client"
    And I follow "Software" in the content area
    And I follow "Patches" in the content area
    When I check "andromeda-dummy-6789" in the list
    And I click on "Apply Patches"
    And I click on "Confirm"
    And I run rhn_check on this client
    Then I should see a "1 patch update has been scheduled for" text
    And "virgo-dummy-2.0-1.1" is installed on "client"

  Scenario: Cleanup: remove virgo-dummy and restore non-update repo
    And I run "zypper -n rm andromeda-dummy" on "sle-client"
    And I run "zypper -n rm virgo-dummy" on "sle-client"
    And I run "zypper -n mr -d Devel_Galaxy_BuildRepo" on "sle-client"
