# Copyright (c) 2017 SUSE LLC
# License under the terms of the MIT License.

@scope_configuration_channels
Feature: Clone a channel

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Clone a channel without patches
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone Channel"
    And I select "Fake-RPM-SUSE-Channel" as the origin channel
    And I choose "original"
    And I click on "Clone Channel"
    And I enter "Fake-Clone-RPM-SLES15SP4-Channel" as "Channel Name"
    And I should see a "Create Software Channel" text
    And I should see a "Original state of the channel" text
    And I click on "Clone Channel"
    Then I should see a "Fake-Clone-RPM-SLES15SP4-Channel" text

  Scenario: Check that this channel has no patches
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-Clone-RPM-SLES15SP4-Channel"
    And I follow "Patches" in the content area
    And I follow "List/Remove Patches"
    Then I should see a "There are no patches associated with this channel." text

  Scenario: Clone a channel with patches
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone Channel"
    And I select "Fake-RPM-SUSE-Channel" as the origin channel
    And I choose "current"
    And I click on "Clone Channel"
    And I enter "Fake-Clone-2-RPM-SLES15SP4-Channel" as "Channel Name"
    And I should see a "Create Software Channel" text
    And I should see a "Current state of the channel" text
    And I click on "Clone Channel"
    Then I should see a "Fake-Clone-2-RPM-SLES15SP4-Channel" text

  Scenario: Check that this channel has patches
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-Clone-2-RPM-SLES15SP4-Channel"
    And I follow "Patches" in the content area
    And I follow "List/Remove Patches"
    Then I should see a "CL-hoag-dummy-7890" link
    And I should see a "CL-virgo-dummy-3456" link
    And I should see a "CL-milkyway-dummy-2345" link
    And I should see a "CL-andromeda-dummy-6789" link

  Scenario: Clone a channel with selected patches
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Clone Channel"
    And I select "Fake-RPM-SUSE-Channel" as the origin channel
    And I choose "select"
    And I click on "Clone Channel"
    And I enter "Fake-Clone-3-RPM-SLES15SP4-Channel" as "Channel Name"
    And I should see a "Create Software Channel" text
    And I should see a "Select patches" text
    And I click on "Clone Channel"
    And I should see a "Fake-Clone-3-RPM-SLES15SP4-Channel" text
    And I should see a "Channel Fake-Clone-3-RPM-SLES15SP4-Channel cloned from channel Fake-RPM-SUSE-Channel." text
    And I should see a "You may now wish to clone the patches associated with Fake-RPM-SUSE-Channel." text
    And I check the row with the "hoag-dummy-7890" link
    And I check the row with the "virgo-dummy-3456" link
    And I click on "Clone Patches"
    And I click on "Confirm"
    Then I should see a "CL-hoag-dummy-7890" link
    And I should see a "CL-virgo-dummy-3456" link

  Scenario: Check that new patches exists
    When I follow the left menu "Patches > Patch List > All"
    And I enter "dummy" as the filtered synopsis
    And I click on the filter button
    And I select "500" from "1154021400_PAGE_SIZE_LABEL"
    Then I should see a "CL-hoag-dummy-7890" link
    And I should see a "CL-virgo-dummy-3456" link
    And I should see a "CL-milkyway-dummy-2345" link
    And I should see a "CL-andromeda-dummy-6789" link

  Scenario: Check CL-hoag-dummy-7890 patches
    When I follow the left menu "Patches > Patch List > All"
    And I enter "dummy" as the filtered synopsis
    And I click on the filter button
    And I select "500" from "1154021400_PAGE_SIZE_LABEL"
    And I follow "CL-hoag-dummy-7890"
    Then I should see a "CL-hoag-dummy-7890 - Security Advisory" text
    And I should see a "mcalmer" text
    And I should see a "https://bugzilla.opensuse.org/show_bug.cgi?id=704608" link

  Scenario: Check CM-virgo-dummy-3456 patches
    When I follow the left menu "Patches > Patch List > All"
    And I enter "dummy" as the filtered synopsis
    And I click on the filter button
    And I select "500" from "1154021400_PAGE_SIZE_LABEL"
    And I follow "CL-virgo-dummy-3456"
    Then I should see a "CL-virgo-dummy-3456 - Bug Fix Advisory" text
    And I should see a "mcalmer" text
    And I should see a "CVE-1999-9998" link

  Scenario: Compare channel packages
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-Clone-2-RPM-SLES15SP4-Channel"
    And I follow "Packages" in the content area
    And I follow "Compare"
    And I select "Fake-Clone-3-RPM-SLES15SP4-Channel" from "selected_channel"
    And I click on "View Packages"
    Then I should see a "andromeda-dummy" text
    And I should see a "2.0-1.1" link
    And I should see a "This channel only" text

  Scenario: Cleanup: remove cloned channels
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Fake-Clone-RPM-SLES15SP4-Channel"
    And I follow "Delete software channel"
    And I check "unsubscribeSystems"
    And I click on "Delete Channel"
    Then I should see a "Fake-Clone-RPM-SLES15SP4-Channel" text
    And I should see a "has been deleted." text
    Given I follow the left menu "Software > Manage > Channels"
    When I follow "Fake-Clone-2-RPM-SLES15SP4-Channel"
    And I follow "Delete software channel"
    And I check "unsubscribeSystems"
    And I click on "Delete Channel"
    Then I should see a "Fake-Clone-2-RPM-SLES15SP4-Channel" text
    And I should see a "has been deleted." text
    Given I follow the left menu "Software > Manage > Channels"
    When I follow "Fake-Clone-3-RPM-SLES15SP4-Channel"
    And I follow "Delete software channel"
    And I check "unsubscribeSystems"
    And I click on "Delete Channel"
    Then I should see a "Fake-Clone-3-RPM-SLES15SP4-Channel" text
    And I should see a "has been deleted." text

  Scenario: Cleanup: remove remaining systems from SSM after channel cloning tests
    When I click on the clear SSM button
