# Copyright (c) 2016-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@buildhost
Feature: Bootstrap a Salt build host via the GUI 2nd part

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Check the new bootstrapped build host in System Overview page
    When I follow the left menu "Systems > Overview"
    And I wait until I see the name of "build_host", refreshing the page
    And I wait until onboarding is completed for "build_host"
    When I follow the left menu "Salt > Keys"
    Then I should see a "accepted" text
    Then the Salt master can reach "build_host"

@proxy
  Scenario: Check connection from build host to proxy
    Given I am on the Systems overview page of this "build_host"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "proxy" short hostname

@proxy
  Scenario: Check registration on build host of minion
    Given I am on the Systems overview page of this "proxy"
    When I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "build_host" hostname

  Scenario: Detect latest Salt changes on the SLES build host
    When I query latest Salt changes on "build_host"

  Scenario: Turn the SLES build host into a container build host
    Given I am on the Systems overview page of this "build_host"
    When I follow "Details" in the content area
    And I follow "Properties" in the content area
    And I check "container_build_host"
    And I click on "Update Properties"
    Then I should see a "Container Build Host type has been applied." text
    And I should see a "Note: This action will not result in state application" text
    And I should see a "To apply the state, either use the states page or run state.highstate from the command line." text
    And I should see a "System properties changed" text

  Scenario: Turn the SLES build host into a OS image build host
    Given I am on the Systems overview page of this "build_host"
    When I follow "Details" in the content area
    And I follow "Properties" in the content area
    And I check "osimage_build_host"
    And I click on "Update Properties"
    Then I should see a "OS Image Build Host type has been applied." text
    And I should see a "Note: This action will not result in state application" text
    And I should see a "To apply the state, either use the states page or run state.highstate from the command line." text
    And I should see a "System properties changed" text

  Scenario: Apply the highstate to the build host
    Given I am on the Systems overview page of this "build_host"
    When I wait until no Salt job is running on "build_host"
    And I enable repositories before installing Docker
    And I apply highstate on "build_host"
    And I wait until "docker" service is active on "build_host"
    And I wait until file "/var/lib/Kiwi/repo/rhn-org-trusted-ssl-cert-osimage-1.0-1.noarch.rpm" exists on "build_host"
    And I disable repositories after installing Docker

  Scenario: Check that the build host is now a build host
    Given I am on the Systems overview page of this "build_host"
    Then I should see a "[Container Build Host]" text
    Then I should see a "[OS Image Build Host]" text

  Scenario: Check events history for failures on SLES build host
    Given I am on the Systems overview page of this "build_host"
    Then I check for failed events on history event page
