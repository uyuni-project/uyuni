# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Install a package to the traditional client

  Scenario: Install a package to the traditional client
    Given I am on the Systems overview page of this "sle-client"
    # we don't know if the pkgs are installed before, just remove or don't fail
    When I run "zypper -n rm andromeda-dummy" on "sle-client" without error control
    And I run "zypper -n rm virgo-dummy" on "sle-client" without error control
    And metadata generation finished for "test-channel-x86_64"
    And I follow "Software" in the content area
    And I follow "Install"
    And I check "virgo-dummy" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    And I run "rhn_check -vvv" on "sle-client"
    Then I should see a "1 package install has been scheduled for" text
    And "virgo-dummy-2.0-1.1" is installed on "sle-client"

  Scenario: Enable old packages for testing a patch install
    When I run "zypper -n mr -e Devel_Galaxy_BuildRepo" on "sle-client"
    And I run "zypper -n in --oldpackage andromeda-dummy-1.0-4.1" on "sle-client"
    And I run "rhn_check -vvv" on "sle-client"

  Scenario: Schedule errata refresh after reverting to old package
    Given I am authorized as "admin" with password "admin"
    When I follow "Admin"
    And I follow "Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    And I click on "Single Run Schedule"
    Then I should see a "bunch was scheduled" text
    And I reload the page
    And I reload the page until it does contain a "FINISHED" text in the table first row

  Scenario: Install a patch to the traditional client
    Given I am on the Systems overview page of this "sle-client"
    When I follow "Software" in the content area
    And I follow "Patches" in the content area
    And I check "andromeda-dummy-6789" in the list
    And I click on "Apply Patches"
    And I click on "Confirm"
    And I run "rhn_check -vvv" on "sle-client"
    Then I should see a "1 patch update has been scheduled for" text
    And "andromeda-dummy-2.0-1.1" is installed on "sle-client"

  Scenario: Cleanup: remove packages and restore non-update repo
    And I run "zypper -n rm andromeda-dummy" on "sle-client"
    And I run "zypper -n rm virgo-dummy" on "sle-client"
    And I run "zypper -n mr -d Devel_Galaxy_BuildRepo" on "sle-client"
    And I run "rhn_check -vvv" on "sle-client"
