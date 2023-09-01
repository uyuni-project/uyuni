# Copyright (c) 2023 SUSE LLC
# Licensed under the terms of the MIT license.

# This feature depends on
# - srv_sync_channels.feature which syncs SLES 15 SP4 and the client tools.
# We also test 'Bootstrapping using the command line' in this feature

@scope_salt
@scope_salt_ssh
@scope_virtualization
@virthost_kvm
Feature: Migrate Salt to bundled Salt on a nested Minion VM

  Scenario: Log in as admin user in the Salt migration context
    Given I am authorized for the "Admin" section

  Scenario: Prepare a SLES 15 SP4 KVM test VM and list it in the Salt migration context
    Given I am on the Systems overview page of this "kvm_server"
    And I create salt-sles virtual network on "kvm_server"
    And I delete default virtual storage pool on "kvm_server"
    And I create salt-sles virtual storage pool on "kvm_server"
    And I create a sles virtual machine named "salt_migration_minion" with cloudinit on "kvm_server"
    And I follow "Virtualization" in the content area
    And I wait until I see "salt_migration_minion" text

  Scenario: Start the nested VM in the Salt migration context
    Given I follow "Virtualization" in the content area
    When I click on "Start" in row "salt_migration_minion"
    Then I should see "salt_migration_minion" virtual machine running on "kvm_server"

  Scenario: Check configuration files on the nested VM in the Salt migration context
    When I check the cloud-init status on "salt_migration_minion"
    And file "/etc/salt/minion" should contain "server_id_use_crc: adler32" on "salt_migration_minion"
    And file "/etc/nsswitch.conf" should contain "files dns" on "salt_migration_minion"

  Scenario: Do a late hostname init on the nested VM in the Salt migration context
    Then I do a late hostname initialization of host "salt_migration_minion"

  Scenario: Bootstrap the nested VM without the Salt bundle  in the Salt migration context
    When I wait at most 1000 seconds until Salt master sees "salt_migration_minion" as "unaccepted"
    And I follow the left menu "Salt > Keys"
    Then I wait at most 120 seconds until I see "-nested" text
    When I accept "salt_migration_minion" key
    And I list all Salt keys shown on the Salt master
    And I am on the Systems page
    And I wait until onboarding is completed for "salt_migration_minion"

  Scenario: Create the bootstrap repository for the SLES 15 KVM VM in the Salt migration context
    When I create the bootstrap repository for "sle_minion" on the server

@susemanager
  Scenario: Subscribe the SLES 15 KVM VM to the SLES 15 SP4 channel and enable client tools in the Salt migration context
    Given I am on the Systems overview page of this "salt_migration_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "SLE-Product-SLES15-SP4-Pool for x86_64"
    And I wait until I see "SLE-Module-Basesystem15-SP4-Pool for x86_64" text
    And I check "SLE-Manager-Tools15-Pool for x86_64 SP4"
    And I check "SLE-Manager-Tools15-Updates for x86_64 SP4"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page

@uyuni
  Scenario: Subscribe the SLES 15 KVM VM to the SLES 15 SP4 channel and enable client tools in the Salt migration context
    Given I am on the Systems overview page of this "salt_migration_minion"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "SLE-Product-SLES15-SP4-Pool for x86_64"
    And I wait until I see "SLE-Module-Basesystem15-SP4-Pool for x86_64" text
    And I check "Uyuni Client Tools for SLES15 SP4 x86_64"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page

  Scenario: Do some basic testing on the nested VM without Salt bundle
    When I install packages "venv-salt-minion" on this "salt_migration_minion"
    Then "venv-salt-minion" should be installed on "salt_migration_minion"
    When I follow the left menu "Salt > Remote Commands"
    Then I should see a "Remote Commands" text in the content area
    When I enter command "file /etc/salt"
    And I enter the hostname of "salt_migration_minion" as "target"
    And I click on preview
    Then I should see a "Target systems (1)" text
    And I should see a "Stop waiting" text
    And I click on stop waiting
    And I click on run
    And I wait until I do not see "pending" text
    And I expand the results for "salt_migration_minion"
    Then I should see "/etc/salt: directory" in the command output for "salt_migration_minion"

  Scenario: Migrate the nested VM to the Salt bundle
    Given the Salt master can reach "salt_migration_minion"
    When I migrate "salt_migration_minion" from salt-minion to venv-salt-minion

  Scenario: Purge the Minion from the old salt-minion leftovers
    When I purge salt-minion on "salt_migration_minion" after a migration

  # This will fail until bsc#1209251 will be fixed
  Scenario: Check if the Salt bundle migration was successful
    When I follow the left menu "Salt > Remote Commands"
    Then I should see a "Remote Commands" text in the content area
    When I enter command "file /etc/salt"
    And I click on preview
    Then I should see "salt_migration_minion" hostname
    And I wait until I do not see "pending" text
    When I click on run
    And I wait until I do not see "pending" text
    And I expand the results for "salt_migration_minion"
    Then I should see "/etc/salt: cannot open `/etc/salt' (No such file or directory)" in the command output for "salt_migration_minion"
    When I enter command "file /etc/venv-salt-minion"
    And I click on preview
    Then I should see "salt_migration_minion" hostname
    And I wait until I do not see "pending" text
    When I click on run
    And I wait until I do not see "pending" text
    And I expand the results for "salt_migration_minion"
    Then I should see "/etc/venv-salt-minion: directory" in the command output for "salt_migration_minion"

  Scenario: Do some basic testing on the nested VM with the Salt bundle in the Salt migration context
    Given I am on the Systems overview page of this "salt_migration_minion"
    And I follow "Software" in the content area
    And I follow "Install"
    And I enter "adobe-sourcecodepro-fonts" as the filtered package name
    And I click on the filter button
    And I wait until I see "adobe-sourcecodepro-fonts" text
    And I check "adobe-sourcecodepro-fonts" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    When I wait until event "Package Install/Upgrade scheduled" is completed
    Then "adobe-sourcecodepro-fonts" should be installed on "salt_migration_minion"
    And I follow "Software" in the content area
    And I follow "Install"
    When I follow "List / Remove"
    And I enter "adobe-sourcecodepro-fonts" as the filtered package name
    And I click on the filter button
    And I wait until I see "adobe-sourcecodepro-fonts" text
    And I check "adobe-sourcecodepro-fonts" in the list
    And I click on "Remove Packages"
    And I click on "Confirm"
    When I wait until event "Package Removal scheduled" is completed
    Then "adobe-sourcecodepro-fonts" should not be installed on "salt_migration_minion"

  Scenario: Cleanup: remove the nested VM in the Salt migration context
    Given I am on the Systems overview page of this "salt_migration_minion"
    And I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "salt_migration_minion" should not be registered

  Scenario: Cleanup: delete the created nested VM in the Salt migration context
    When I delete the virtual machine named "salt_migration_minion" on "kvm_server"

  Scenario: Cleanup: delete the created SLES 15 VM network and storage pool in the Salt migration context
    And I delete salt-sles virtual network on "kvm_server"
    And I delete salt-sles virtual storage pool on "kvm_server"

  Scenario: Cleanup: delete the create common VM storage pools
    And I delete testsuite virtual storage pool on "kvm_server"
    And I delete tmp virtual storage pool on "kvm_server"
