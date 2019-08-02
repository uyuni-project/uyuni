# Copyright (c) 2019 SUSE LLC
# Licensed under the terms of the MIT license.
#
#
# 1) delete SLES minion and register again with bootstrap script
# 2) subscribe minion to a base channels
# 3) install and remove a package
# 4) cleanup: re-add build host entitlements

Feature: Register a Salt minion via Bootstrap-script

  Scenario: Delete SLES minion system profile before script bootstrap test
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    And I cleanup minion "sle-minion"
    Then "sle-minion" should not be registered

  Scenario: Create bootstrap script
    Given I am authorized as "admin" with password "admin"
    When I follow the left menu "Admin > Manager Configuration > Bootstrap Script"
    And I uncheck "Enable Client GPG checking"
    Then I should see a "SUSE Manager Configuration - Bootstrap" text
    And I should see "Bootstrap using Salt" as checked
    When I click on "Update"
    Then I should see a "Bootstrap script successfully generated" text
    And I set the activation key "1-SUSE-PKG-x86_64" in the bootstrap script on the server

  Scenario: Download script to minion
    When I fetch "pub/bootstrap/bootstrap.sh" to "sle-minion"
    Then I wait until file "/root/bootstrap.sh" exists on "sle-minion"
    And file "/root/bootstrap.sh" should contain "ACTIVATION_KEYS=1-SUSE-PKG-x86_64" on "sle-minion"

  Scenario: Bootstrap the minion using the script
    Given I am authorized as "admin" with password "admin"
    When I run "sh /root/bootstrap.sh" on "sle-minion"
    And I wait for "5" seconds
    When I follow the left menu "Salt > Keys"
    And I wait until I see the name of "sle-minion", refreshing the page
    And I should see a "pending" text
    And I accept "sle-minion" key

  Scenario: Check if onboarding for the script-bootstrapped minion was successful
    Given I am authorized as "admin" with password "admin"
    When I navigate to "rhn/systems/Overview.do" page
    And I wait until I see the name of "sle-minion", refreshing the page
    And I wait until onboarding is completed for "sle-minion"
    And I run "rm /srv/www/htdocs/pub/bootstrap/bootstrap.sh" on "server"
    And I run "rm /root/bootstrap.sh" on "sle-minion"

  Scenario: Detect latest Salt changes on the script-bootstrapped SLES minion
    When I query latest Salt changes on "sle-minion"

  Scenario: Check the activation key
    Given I am on the Systems overview page of this "sle-minion"
    Then I should see a "1-SUSE-PKG-x86_64" text

  Scenario: Subscribe the script-bootstrapped SLES minion to a base channel
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "Test-Channel-x86_64"
    And I wait until I do not see "Loading..." text
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    And I wait until event "Subscribe channels scheduled by admin" is completed

  Scenario: Install a package to the script-bootstrapped SLES minion
   Given I am on the Systems overview page of this "sle-minion"
   When I follow "Software" in the content area
   And I follow "Install"
   And I check "orion-dummy" in the list
   And I click on "Install Selected Packages"
   And I click on "Confirm"
   Then I should see a "1 package install has been scheduled for" text
   When I wait until event "Package Install/Upgrade scheduled by admin" is completed
   Then "orion-dummy-1.1-1.1" should be installed on "sle-minion"

  Scenario: Cleanup: remove package from script-bootstrapped SLES minion
   When I remove package "orion-dummy-1.1-1.1" from this "sle-minion"
   Then "orion-dummy-1.1-1.1" should not be installed on "sle-minion"

  Scenario: Cleanup: turn the SLES minion into a container build host after script-bootstrap
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Details" in the content area
    And I follow "Properties" in the content area
    And I check "container_build_host"
    And I click on "Update Properties"
    Then I should see a "Container Build Host type has been applied." text
    And I should see a "Note: This action will not result in state application" text
    And I should see a "To apply the state, either use the states page or run state.highstate from the command line." text
    And I should see a "System properties changed" text

  Scenario: Cleanup: turn the SLES minion into a OS image build host after script-bootstrap
    Given I am on the Systems overview page of this "sle-minion"
    When I follow "Details" in the content area
    And I follow "Properties" in the content area
    And I check "osimage_build_host"
    And I click on "Update Properties"
    Then I should see a "OS Image Build Host type has been applied." text
    And I should see a "Note: This action will not result in state application" text
    And I should see a "To apply the state, either use the states page or run state.highstate from the command line." text
    And I should see a "System properties changed" text

  Scenario: Cleanup: apply the highstate to build host after script-bootstrap
    Given I am on the Systems overview page of this "sle-minion"
    When I wait until no Salt job is running on "sle-minion"
    And I enable repositories before installing Docker
    And I apply highstate on "sle-minion"
    And I wait until "docker" service is active on "sle-minion"
    And I wait until file "/var/lib/Kiwi/repo/rhn-org-trusted-ssl-cert-osimage-1.0-1.noarch.rpm" exists on "sle-minion"
    And I disable repositories after installing Docker

  Scenario: Cleanup: check that the minion is now a build host after script-bootstrap
    Given I am on the Systems overview page of this "sle-minion"
    Then I should see a "[Container Build Host]" text
    Then I should see a "[OS Image Build Host]" text
