# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature relies on having properly configured
# /usr/share/rhn/config-defaults/rhn_java.conf file on your
# SUSE Manager server.
# For the scope of this tests, we configure it as follows:
#
# salt_content_staging_window = 0.05 (3 minutes)
# salt_content_staging_advance = 0.1 (6 minutes)

Feature: Install a package on the minion with staging enabled

   Scenario: Enable content staging
    Given I am in the organization configuration page
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
    And I pick 3 minutes from now as schedule time
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled for" text
    Then I wait until the package "orion-dummy-1.1-1.1" has been cached on the minion
    And I wait for "orion-dummy-1.1-1.1" to be installed on this "sle-minion"
    Then I remove pkg "orion-dummy-1.1-1.1" on minion
