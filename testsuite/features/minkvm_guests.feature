# Copyright (c) 2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Be able to manage KVM virtual machines via the GUI

@virthost_kvm
  Scenario: Bootstrap virtual host
    Given I am authorized
    When I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "kvm-server" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "kvm-server" password
    And I select "1-SUSE-PKG-x86_64" from "activationKeys"
    And I select the hostname of the proxy from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host! " text
    And I wait until onboarding is completed for "kvm-server"
    # Shorten the virtpoller interval to avoid loosing time
    And I reduce virtpoller run interval on "kvm-server"

@virthost_kvm
  Scenario: Setting the virtualization entitlement
    Given I am on the Systems overview page of this "kvm-server"
    When I follow "Details" in the content area
    And I follow "Properties" in the content area
    And I check "virtualization_host"
    And I click on "Update Properties"
    Then I should see a "Since you added a Virtualization system type to the system" text


@virthost_kvm
  Scenario: Prepare a test virtual machine and list it
    Given I am on the "Virtualization" page of this "kvm-server"
    When I create "test-vm" virtual machine on "kvm-server"
    And I wait until I see "test-vm" text

@virthost_kvm
  Scenario: Start a virtual machine
    Given I am on the "Virtualization" page of this "kvm-server"
    When I click on "Start" in row "test-vm"
    Then I should see "test-vm" virtual machine running on "kvm-server"

@virthost_kvm
  Scenario: Suspend a virtual machine
    Given I am on the "Virtualization" page of this "kvm-server"
    When I wait until table row for "test-vm" contains button "Suspend"
    And I click on "Suspend" in row "test-vm"
    And I click on "Suspend" in "Suspend Guest" modal
    Then I should see "test-vm" virtual machine paused on "kvm-server"

@virthost_kvm
  Scenario: Resume a virtual machine
    Given I am on the "Virtualization" page of this "kvm-server"
    When I wait until table row for "test-vm" contains button "Resume"
    And I click on "Resume" in row "test-vm"
    Then I should see "test-vm" virtual machine running on "kvm-server"

@virthost_kvm
  Scenario: Shutdown a virtual machine
    Given I am on the "Virtualization" page of this "kvm-server"
    When I wait until table row for "test-vm" contains button "Stop"
    And I wait until virtual machine "test-vm" on "kvm-server" is started
    And I click on "Stop" in row "test-vm"
    And I click on "Stop" in "Stop Guest" modal
    Then I should see "test-vm" virtual machine shut off on "kvm-server"

@virthost_kvm
  Scenario: Edit a virtual machine
    Given I am on the "Virtualization" page of this "kvm-server"
    When I click on "Edit" in row "test-vm"
    Then I should see "512" in field "memory"
    And I should see "1" in field "vcpu"
    When I enter "1024" as "memory"
    And I enter "2" as "vcpu"
    And I click on "Update"
    Then I should see a "Hosted Virtual Systems" text
    And "test-vm" virtual machine on "kvm-server" should have 1024MB memory and 2 vcpus

@virthost_kvm
  Scenario: Delete a virtual machine
    Given I am on the "Virtualization" page of this "kvm-server"
    When I click on "Delete" in row "test-vm"
    And I click on "Delete" in "Delete Guest" modal
    Then I should not see a "test-vm" virtual machine on "kvm-server"

@virthost_kvm
  Scenario: Cleanup: Unregister the virtualization host
    Given I am on the Systems overview page of this "kvm-server"
    When I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"
    Then I wait until I see "has been deleted" text

@virthost_kvm
  Scenario: Cleanup: Cleanup virtualization host
    When I run "zypper -n mr -e --all" on "kvm-server" without error control
    And I run "zypper -n rr SUSE-Manager-Bootstrap" on "kvm-server" without error control
    And I run "systemctl stop salt-minion" on "kvm-server" without error control
    And I run "rm /etc/salt/minion.d/susemanager*" on "kvm-server" without error control
    And I run "rm /etc/salt/minion.d/libvirt-events.conf" on "kvm-server" without error control
    And I run "rm /etc/salt/pki/minion/minion_master.pub" on "kvm-server" without error control
    # In case the delete VM test failed we need to clean up ourselves.
    And I run "virsh undefine --remove-all-storage test-vm" on "kvm-server" without error control
    # Remove the virtpoller cache to avoid problems
    And I run "rm /var/cache/virt_state.cache" on "kvm-server" without error control
