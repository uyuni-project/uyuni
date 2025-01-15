# Copyright (c) 2015-2024 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature can cause failures in:
# - features/reposync/srv_create_activationkey.feature
# - features/reposync/srv_create_fake_repositories.feature
# - features/init_client/sle_minion.feature
# - features/init_client/sle_ssh_minion.feature
# - features/init_client/min_rhlike.feature
# - features/secondary/allcli_software_channels.feature
# - features/secondary/min_deblike_ssh.feature
# - features/secondary/min_rhlike_openscap_audit.feature
# - features/secondary/min_rhlike_salt_install_package_and_patch.feature
# - features/secondary/min_rhlike_ssh.feature
# - features/secondary/srv_content_lifecycle.feature
# - features/secondary/srv_delete_channel_from_ui.feature
# - features/secondary/srv_dist_channel_mapping.feature
# - features/secondary/srv_manage_activationkey.feature
# - features/secondary/srv_manage_channels_page.feature
# - features/secondary/srv_patches_page.feature
# - features/secondary/srv_push_package.feature

Feature: Create fake channels
  In Order to distribute software to the clients
  As an authorized user
  I want to create fake channels for each distribution

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Add a fake base channel for x86_64
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Fake-Base-Channel-SUSE-like" as "Channel Name"
    And I enter "fake-base-channel-suse-like" as "Channel Label"
    And I select "None" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Base channel for testing" as "Channel Summary"
    And I enter "No more description for base channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Fake-Base-Channel-SUSE-like created." text

  Scenario: Add a fake child channel into the fake base channel x86_64
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Fake-Child-Channel-SUSE-like" as "Channel Name"
    And I enter "fake-child-channel-suse-like" as "Channel Label"
    And I select "Fake-Base-Channel-SUSE-like" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Child channel for testing" as "Channel Summary"
    And I enter "Description for Fake Child Channel SUSE like." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Fake-Child-Channel-SUSE-like created." text

@sle_minion
  Scenario: Add a SUSE fake child channel to the SUSE Product base channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Fake-RPM-SUSE-Channel" as "Channel Name"
    And I enter "fake-rpm-suse-channel" as "Channel Label"
    And I select the parent channel for the "sle_minion" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Fake-RPM-SUSE-Channel for testing" as "Channel Summary"
    And I enter "Description for Fake-RPM-SUSE-Channel Child Channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Fake-RPM-SUSE-Channel created." text

  Scenario: Add a fake base channel for i586
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Fake-Base-Channel-i586" as "Channel Name"
    And I enter "fake-base-channel-i586" as "Channel Label"
    And I select "None" from "Parent Channel"
    And I select "IA-32" from "Architecture:"
    And I enter "Fake-Base-Channel-i586 channel for testing" as "Channel Summary"
    And I enter "No more description for base channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Fake-Base-Channel-i586 created." text

  Scenario: Add a fake child channel into the fake base channel i586
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Fake-Child-Channel-i586" as "Channel Name"
    And I enter "fake-child-channel-i586" as "Channel Label"
    And I select "Fake-Base-Channel-i586" from "Parent Channel"
    And I select "IA-32" from "Architecture:"
    And I enter "Fake Child Channel i586 for testing" as "Channel Summary"
    And I enter "Description for Fake Child Channel i586." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Fake-Child-Channel-i586 created." text

  Scenario: Add a test base channel for x86_64
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Test-Base-Channel-x86_64" as "Channel Name"
    And I enter "test-base-channel-x86_64" as "Channel Label"
    And I select "None" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Test-Base-Channel-x86_64 channel for testing" as "Channel Summary"
    And I enter "No more description for base channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Test-Base-Channel-x86_64 created." text

  Scenario: Add a child channel into the test base channel x86_64
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Test-Child-Channel-x86_64" as "Channel Name"
    And I enter "test-child-channel-x86_64" as "Channel Label"
    And I select "Test-Base-Channel-x86_64" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Test-Child-Channel-x86_64 channel for testing" as "Channel Summary"
    And I enter "Description for Test-Child-Channel-x86_64 Channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Test-Child-Channel-x86_64 created." text

@pxeboot_minion
@uyuni
@scc_credentials
  Scenario: Add a fake terminal child channel to the SUSE Product base channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Fake-RPM-Terminal-Channel" as "Channel Name"
    And I enter "fake-rpm-terminal-channel" as "Channel Label"
    And I select the parent channel for the "pxeboot_minion" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Fake-RPM-Terminal-Channel for testing" as "Channel Summary"
    And I enter "Description for Fake-RPM-Terminal-Channel Child Channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Fake-RPM-Terminal-Channel created." text

@deblike_minion
  Scenario: Add Debian-like AMD64 base channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Fake-Base-Channel-Debian-like" as "Channel Name"
    And I enter "fake-base-channel-debian-like" as "Channel Label"
    And I select "None" from "Parent Channel"
    And I select "AMD64 Debian" from "Architecture:"
    And I enter "Fake-Base-Channel-Debian-like for testing" as "Channel Summary"
    And I enter "No more description for base channel." as "Channel Description"
    # WORKAROUND
    # GPG verification of Debian-like repos is possible with an own GPG key.
    # This is not yet part of the testsuite and we run with disabled checkes for Ubuntu/Debian
    And I uncheck "gpg_check"
    # End of WORKAROUND
    And I click on "Create Channel"
    Then I should see a "Channel Fake-Base-Channel-Debian-like created." text

@rhlike_minion
  Scenario: Add a RedHat-like base channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Fake-Base-Channel-RH-like" as "Channel Name"
    And I enter "fake-base-channel-rh-like" as "Channel Label"
    And I select "None" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Fake-Base-Channel-RH-like for testing" as "Channel Summary"
    And I enter "No more description for base channel." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Fake-Base-Channel-RH-like created." text

@rhlike_minion
  Scenario: Add a fake AppStream base channel
    When I follow the left menu "Software > Manage > Channels"
    And I follow "Create Channel"
    And I enter "Fake-Base-Channel-AppStream" as "Channel Name"
    And I enter "fake-base-channel-appstream" as "Channel Label"
    And I select "None" from "Parent Channel"
    And I select "x86_64" from "Architecture:"
    And I enter "Fake-Base-Channel-AppStream for testing" as "Channel Summary"
    And I enter "Description for Fake-Base-Channel-AppStream." as "Channel Description"
    And I click on "Create Channel"
    Then I should see a "Channel Fake-Base-Channel-AppStream created." text
