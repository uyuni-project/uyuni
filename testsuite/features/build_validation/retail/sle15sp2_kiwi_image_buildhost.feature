# Copyright (c) 2021 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Bootstrap a SLES 15 SP2 Salt build host via the GUI

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create the bootstrap repository for a SLES 15 SP2 build host
     When I create the bootstrap repository for "sle15sp2_buildhost" on the server

  Scenario: Bootstrap a SLES 15 SP2 build host
     When I follow the left menu "Systems > Bootstrapping"
     Then I should see a "Bootstrap Minions" text
     When I enter the hostname of "sle15sp2_buildhost" as "hostname"
     And I enter "22" as "port"
     And I enter "root" as "user"
     And I enter "linux" as "password"
     And I select "1-sle15sp2_minion_key" from "activationKeys"     
     And I click on "Bootstrap"
     And I wait until I see "Successfully bootstrapped host!" text

  Scenario: Check the new bootstrapped SLES 15 SP2 build host in System Overview page
    When I follow the left menu "Salt > Keys"
    Then I should see a "accepted" text
    When I am on the System Overview page
    And I wait until I see the name of "sle15sp2_buildhost", refreshing the page
    And I wait until onboarding is completed for "sle15sp2_buildhost"
    Then the Salt master can reach "sle15sp2_buildhost"

  Scenario: Detect latest Salt changes on the SLES 15 SP2 build host
    When I query latest Salt changes on "sle15sp2_buildhost"

  Scenario: Turn the SLES 15 SP2 build host into a OS image build host
    Given I am on the Systems overview page of this "sle15sp2_buildhost"
    When I follow "Details" in the content area
    And I follow "Properties" in the content area
    And I check "osimage_build_host"
    And I click on "Update Properties"
    Then I should see a "OS Image Build Host type has been applied." text
    And I should see a "Note: This action will not result in state application" text
    And I should see a "To apply the state, either use the states page or run state.highstate from the command line." text
    And I should see a "System properties changed" text

  Scenario: Apply the highstate to the SLES 15 SP2 build host
    Given I am on the Systems overview page of this "sle15sp2_buildhost"
    When I wait until no Salt job is running on "sle15sp2_buildhost"
    And I apply highstate on "sle15sp2_buildhost"

  Scenario: Check that SLES 15 SP2 build host is now a build host
    Given I am on the Systems overview page of this "sle15sp2_buildhost"
    Then I should see a "[OS Image Build Host]" text

  Scenario: Check events history for failures on SLES 15 SP2 build host
    Given I am on the Systems overview page of this "sle15sp2_buildhost"
    Then I check for failed events on history event page
