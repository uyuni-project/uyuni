# Copyright (c) 2022-2026 SUSE LLC
# Licensed under the terms of the MIT license.
#
# The scenarios in this feature are skipped if there is no proxy
# ($proxy is nil)
#
# Alternative: Bootstrap the proxy as Salt minion from GUI

@server_build_image
@scope_proxy
@proxy
Feature: Setup SUSE Manager proxy
  In order to use a proxy with the SUSE manager server
  As the system administrator
  I want to register the proxy to the server


  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Add SUSE Manager Proxy 4.3 x86_64
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "Loading" text
    And I enter "SUSE Manager Proxy 4.3 x86_64 (ALPHA)" as the filtered product description
    And I select "SUSE Manager Proxy 4.3 x86_64 (ALPHA)" as a product
    Then I should see the "SUSE Manager Proxy 4.3 x86_64 (ALPHA)" selected
    When I click the Add Product button
    And I wait until I see "SUSE Manager Proxy 4.3 x86_64 (ALPHA)" product has been added
    When I wait until all spacewalk-repo-sync finished


  Scenario: Create an activation key for proxy
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I enter "SUSE Proxy Key x86_64" as "description"
    And I enter "SUSE-PROXY-x86_64" as "key"
    And I enter "20" as "usageLimit"
    And I select "SLE-Product-SUSE-Manager-Proxy-4.3-Pool for x86_64" from "selectedBaseChannel"
    And I click on "Create Activation Key"
    Then I should see a "Activation key SUSE Proxy Key x86_64 has been created" text
    And I should see a "Details" link
    And I should see a "Packages" link
    And I should see a "Configuration" link in the content area
    And I should see a "Groups" link
    And I should see a "Activated Systems" link


  Scenario: Bootstrap the proxy as a Salt minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "proxy" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-SUSE-PROXY-x86_64" from "activationKeys"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text

  Scenario: Wait until the proxy appears
    When I wait until onboarding is completed for "proxy"

  Scenario: Detect latest Salt changes on the proxy
    When I query latest Salt changes on "proxy"

  Scenario: Copy the keys and configure the proxy
    When I copy server's keys to the proxy
    And I configure the proxy
    Then I should see "proxy" via spacecmd
    And service "salt-broker" is active on "proxy"

  Scenario: Check proxy system details
    When I am on the Systems overview page of this "proxy"
    Then I should see "proxy" hostname
    When I wait until I see "SUSE Manager Proxy" text, refreshing the page
    Then I should see a "Proxy" link in the content area

  Scenario: Check events history for failures on the proxy
    Given I am on the Systems overview page of this "proxy"
    Then I check for failed events on history event page
