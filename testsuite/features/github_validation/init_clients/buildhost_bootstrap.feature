# Copyright (c) 2016-2023 SUSE LLC
# Licensed under the terms of the MIT license.

@buildhost
Feature: Bootstrap a build host via the GUI

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap a build host
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "build_host" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-BUILD-HOST-KEY-x86_64" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text

  Scenario: Check the new bootstrapped build host in System Overview page
    When I follow the left menu "Salt > Keys"
    Then I should see a "accepted" text
    When I follow the left menu "Systems > System List > All"
    # the build host entitlement adds some extra minutes to apply the salt high-state
    And I wait at most 500 seconds until I see the name of "build_host", refreshing the page
    And I wait at most 500 seconds until onboarding is completed for "build_host"
    Then the Salt master can reach "build_host"

  Scenario: Enable "Container Build Host" system type
   Given I am on the Systems overview page of this "build_host"
   When I follow "Properties" in the content area
   And I check "Container Build Host"
   And I check "OS Image Build Host"
   And I click on "Update Properties"

  Scenario: Check that the build host is a build host
    Given I am on the Systems overview page of this "build_host"
    Then I should see a "[Container Build Host]" text
    Then I should see a "[OS Image Build Host]" text

  Scenario: Check events history for failures on SLES build host
    Given I am on the Systems overview page of this "build_host"
    Then I check for failed events on history event page
