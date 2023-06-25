# Copyright (c) 2017-2022 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature relies on having properly configured
#   /etc/rhn/rhn.conf
# file on your Uyuni server.
#
# For the scope of these tests, we configure it as follows:
#   java.salt_content_staging_window = 0.033 (2 minutes)
#   java.salt_content_staging_advance = 0.05 (3 minutes)
# which means "between 3 and 1 minutes before package installation or patching"

@skip_if_github_validation
@deblike_minion
@scope_deblike
@scope_content_staging
Feature: Install a package on the Debian-like minion with staging enabled

  Scenario: Pre-requisite: install virgo-dummy-1.0 package, make sure orion-dummy is not present on Debian-like minion
    When I enable repository "test_repo_deb_pool" on this "deblike_minion"
    And I run "apt update" on "deblike_minion"
    And I remove package "orion-dummy" from this "deblike_minion"
    And I install old package "virgo-dummy=1.0" on this "deblike_minion"

  Scenario: Pre-requisite: refresh package list on Debian-like minion
    When I refresh packages list via spacecmd on "deblike_minion"
    And I wait until refresh package list on "deblike_minion" is finished
    Then spacecmd should show packages "virgo-dummy-1.0" installed on "deblike_minion"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Pre-requisite: ensure the errata cache is computed for Debian-like minion
    When I follow the left menu "Admin > Task Schedules"
    And I follow "errata-cache-default"
    And I follow "errata-cache-bunch"
    Then I click on "Single Run Schedule"
    And I should see a "bunch was scheduled" text
    Then I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows

  Scenario: Enable content staging for Debian-like minion
    When I follow the left menu "Admin > Organizations"
    And I follow first "SUSE Test"
    And I follow first "Configuration"
    And I check "staging_content_enabled"
    And I click on "Update Organization"
    Then I should see a "was successfully updated." text

  Scenario: Install package in the future and check for staging on Debian-like minion
    Given I am on the Systems overview page of this "deblike_minion"
    And I follow "Software" in the content area
    And I follow "Packages" in the content area
    And I follow "Install" in the content area
    And I enter "orion-dummy-1.1-X" as the filtered package name
    And I click on the filter button
    When I check "orion-dummy-1.1-X" in the list
    And I click on "Install Selected Packages"
    And I pick 3 minutes from now as schedule time
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled for" text
    And I wait until the package "orion-dummy_1.1" has been cached on this "deblike_minion"
    And I wait for "orion-dummy-1.1" to be installed on "deblike_minion"

  # TODO:
  # Scenario: Install patch in the future and check for staging on Debian-like minion

  Scenario: Cleanup: remove virgo-dummy and orion-dummy packages from Debian-like minion
    And I remove package "orion-dummy" from this "deblike_minion"
    And I remove package "virgo-dummy" from this "deblike_minion"
    And I disable repository "test_repo_deb_pool" on this "deblike_minion"
    And I run "apt update" on "deblike_minion"
