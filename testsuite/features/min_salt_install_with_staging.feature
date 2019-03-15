# Copyright (c) 2017-2018 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature relies on having properly configured
#   /etc/rhn/rhn.conf
# file on your SUSE Manager server.
#
# For the scope of these tests, we configure it as follows:
#   java.salt_content_staging_window = 0.033 (2 minutes)
#   java.salt_content_staging_advance = 0.05 (3 minutes)
# which means "beetwen 3 and 1 minutes before package installation or patching"

Feature: Install a package on the minion with staging enabled

  Scenario: Pre-requisite: install virgo-dummy-1.0 and orion-dummy-1.1 packages
    Given I am authorized as "admin" with password "admin"
    And I run "zypper -n mr -e Devel_Galaxy_BuildRepo" on "sle-minion"
    And I run "zypper -n ref" on "sle-minion"
    And I run "zypper -n in --oldpackage virgo-dummy-1.0" on "sle-minion" without error control
    And I run "zypper -n rm orion-dummy" on "sle-minion" without error control

  Scenario: Pre-requisite: refresh package list on SLE minion
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Software" in the content area
    And I click on "Update Package List"
    And I follow "Events" in the content area
    And I wait until I do not see "Package List Refresh scheduled by admin" text, refreshing the page
    Then I wait until event "Package List Refresh scheduled by admin" is completed

  Scenario: Pre-requisite: ensure the errata cache is computed
    Given I am authorized as "admin" with password "admin"
    When I follow "Admin"
    And I follow "Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    Then I click on "Single Run Schedule"
    And I should see a "bunch was scheduled" text
    Then I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Enable content staging
    Given I am authorized as "admin" with password "admin"
    And I follow "Admin"
    And I follow "Organizations"
    And I follow first "SUSE Test"
    And I follow first "Configuration"
    And I check "staging_content_enabled"
    And I click on "Update Organization"
    Then I should see a "was successfully updated." text

  Scenario: Install package in the future and check for staging
    Given I am on the Systems overview page of this "sle-minion"
    And I follow "Software" in the content area
    And I follow "Packages" in the content area
    And I follow "Install" in the content area
    When I check "orion-dummy-1.1-1.1" in the list
    And I click on "Install Selected Packages"
    And I pick 2 minutes from now as schedule time
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled for" text
    And I wait until the package "orion-dummy-1.1-1.1" has been cached on this "sle-minion"
    And I wait for "orion-dummy-1.1-1.1" to be installed on this "sle-minion"
    Then I remove package "orion-dummy-1.1-1.1" from this "sle-minion"

  Scenario: Install patch in the future and check for staging
    Given I am on the Systems overview page of this "sle-minion"
    And I remove package "virgo-dummy" from this "sle-minion"
    And I enable repository "Devel_Galaxy_BuildRepo" on this "sle-minion"
    And I install package "virgo-dummy-1.0" on this "sle-minion"
    And I wait for "30" seconds
    And I follow "Software" in the content area
    And I click on "Update Package List"
    And I wait for "30" seconds
    And I follow "Patches" in the content area
    When I check "virgo-dummy-3456" in the list
    And I click on "Apply Patches"
    And I pick 2 minutes from now as schedule time
    And I click on "Confirm"
    Then I should see a "1 patch update has been scheduled for" text
    And I wait until the package "virgo-dummy-2.0-1.1.noarch" has been cached on this "sle-minion"
    And I wait for "virgo-dummy-2.0-1.1" to be installed on this "sle-minion"
    Then I disable repository "Devel_Galaxy_BuildRepo" on this "sle-minion"

  Scenario: Cleanup: remove virgo-dummy and orion-dummy packages from SLES minion
    Given I am authorized as "admin" with password "admin"
    And I run "zypper -n rm virgo-dummy" on "sle-minion" without error control
    And I run "zypper -n rm orion-dummy" on "sle-minion" without error control
    And I run "zypper -n ref" on "sle-minion"
