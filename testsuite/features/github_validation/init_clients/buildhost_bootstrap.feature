# Copyright (c) 2016-2023 SUSE LLC
# Licensed under the terms of the MIT license.

@buildhost
Feature: Bootstrap a build host via the GUI

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Check the new bootstrapped build host in System Overview page
    When I follow the left menu "Salt > Keys"
    And I accept "build_host" key in the Salt master
    And I wait until I do not see "Loading..." text
    Then I should see a "accepted" text
    When I follow the left menu "Systems > System List > All"
    # the build host entitlement adds some extra minutes to apply the salt high-state
    And I wait at most 500 seconds until I see the name of "build_host", refreshing the page
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

