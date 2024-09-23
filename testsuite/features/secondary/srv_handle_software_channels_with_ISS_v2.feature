# Copyright (c) 2021-2024 SUSE LLC
# Licensed under the terms of the MIT license.

@skip_if_github_validation
Feature: Export and import software channels with new ISS implementation
  Distribute software between servers
  Run export and import with ISS v2

  Scenario: Install inter server sync package
    When I install packages "inter-server-sync" on this "server"
    Then "inter-server-sync" should be installed on "server"

  Scenario: Log in as org admin user
    Given I am authorized

  Scenario: Clone a channel with patches
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone Channel"
    And I select "Fake-RPM-SUSE-Channel" as the origin channel
    And I choose "current"
    And I click on "Clone Channel"
    And I should see a "Create Software Channel" text
    And I should see a "Current state of the channel" text
    And I click on "Clone Channel"
    Then I should see a "Clone of Fake-RPM-SUSE-Channel" text

  Scenario: Check that this channel has patches
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone of Fake-RPM-SUSE-Channel"
    And I follow "Patches" in the content area
    And I follow "List/Remove Patches"
    Then I should see a "CL-hoag-dummy-7890" link
    And I should see a "CL-virgo-dummy-3456" link
    And I should see a "CL-milkyway-dummy-2345" link
    And I should see a "CL-andromeda-dummy-6789" link

  Scenario: Export data with ISS v2
    When I ensure folder "/tmp/export_iss_v2" doesn't exist on "server"
    When I export software channels "clone-fake-rpm-suse-channel" with ISS v2 to "/tmp/export_iss_v2"
    Then "/tmp/export_iss_v2" folder on server is ISS v2 export directory

  Scenario: Cleanup: remove cloned channels
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone of Fake-RPM-SUSE-Channel"
    And I follow "Delete software channel"
    And I check "unsubscribeSystems"
    And I click on "Delete Channel"
    Then I should see a "Clone of Fake-RPM-SUSE-Channel" text

  Scenario: Import data with ISS v2
    When I import data with ISS v2 from "/tmp/export_iss_v2"

  Scenario: Check that this channel was imported and has patches
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone of Fake-RPM-SUSE-Channel"
    And I follow "Patches" in the content area
    And I follow "List/Remove Patches"
    Then I should see a "CL-hoag-dummy-7890" link
    And I should see a "CL-virgo-dummy-3456" link
    And I should see a "CL-milkyway-dummy-2345" link
    And I should see a "CL-andromeda-dummy-6789" link

  Scenario: Cleanup: remove imported channel
    When I follow the left menu "Software > Manage > Channels"
    When I follow "Clone of Fake-RPM-SUSE-Channel"
    And I follow "Delete software channel"
    And I check "unsubscribeSystems"
    And I click on "Delete Channel"
    Then I should see a "Clone of Fake-RPM-SUSE-Channel" text

  Scenario: Cleanup: remove ISS v2 export folder
    When I ensure folder "/tmp/export_iss_v2" doesn't exist on "server"
