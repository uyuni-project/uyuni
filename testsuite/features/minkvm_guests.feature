# Copyright (c) 2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Be able to manage KVM virtual machines via the GUI

@virthost_kvm
  Scenario: Bootstrap KVM virtual host
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
  Scenario: Setting the virtualization entitlement for KVM
    Given I am on the Systems overview page of this "kvm-server"
    When I follow "Details" in the content area
    And I follow "Properties" in the content area
    And I check "virtualization_host"
    And I click on "Update Properties"
    Then I should see a "Since you added a Virtualization system type to the system" text


@virthost_kvm
  Scenario: Prepare a KVM test virtual machine and list it
    Given I am on the "Virtualization" page of this "kvm-server"
    When I create default virtual network on "kvm-server"
    And I create test-net0 virtual network on "kvm-server"
    And I create "test-vm" virtual machine on "kvm-server"
    And I wait until I see "test-vm" text

@virthost_kvm
  Scenario: Start a KVM virtual machine
    Given I am on the "Virtualization" page of this "kvm-server"
    When I click on "Start" in row "test-vm"
    Then I should see "test-vm" virtual machine running on "kvm-server"

@virthost_kvm
  Scenario: Show the VNC graphical console for KVM
    Given I am on the "Virtualization" page of this "kvm-server"
    When I click on "Graphical Console" in row "test-vm"
    Then I wait until I see the VNC graphical console
    And I close the window

@virthost_kvm
  Scenario: Suspend a KVM virtual machine
    Given I am on the "Virtualization" page of this "kvm-server"
    When I wait until table row for "test-vm" contains button "Suspend"
    And I click on "Suspend" in row "test-vm"
    And I click on "Suspend" in "Suspend Guest" modal
    Then I should see "test-vm" virtual machine paused on "kvm-server"

@virthost_kvm
  Scenario: Resume a KVM virtual machine
    Given I am on the "Virtualization" page of this "kvm-server"
    When I wait until table row for "test-vm" contains button "Resume"
    And I click on "Resume" in row "test-vm"
    Then I should see "test-vm" virtual machine running on "kvm-server"

@virthost_kvm
  Scenario: Shutdown a KVM virtual machine
    Given I am on the "Virtualization" page of this "kvm-server"
    When I wait until table row for "test-vm" contains button "Stop"
    And I wait until virtual machine "test-vm" on "kvm-server" is started
    And I click on "Stop" in row "test-vm"
    And I click on "Stop" in "Stop Guest" modal
    Then I should see "test-vm" virtual machine shut off on "kvm-server"

@virthost_kvm
  Scenario: Edit a KVM virtual machine
    Given I am on the "Virtualization" page of this "kvm-server"
    When I click on "Edit" in row "test-vm"
    Then I should see "512" in field "memory"
    And I should see "1" in field "vcpu"
    And option "VNC" is selected as "graphicsType"
    And option "default" is selected as "network0_source"
    And option "ide" is selected as "disk0_bus"
    When I enter "1024" as "memory"
    And I enter "2" as "vcpu"
    And I select "Spice" from "graphicsType"
    And I select "test-net0" from "network0_source"
    And I enter "02:34:56:78:9a:bc" as "network0_mac"
    And I select "scsi" from "disk0_bus"
    And I click on "Update"
    Then I should see a "Hosted Virtual Systems" text
    And "test-vm" virtual machine on "kvm-server" should have 1024MB memory and 2 vcpus
    And "test-vm" virtual machine on "kvm-server" should have spice graphics device
    And "test-vm" virtual machine on "kvm-server" should have 1 NIC using "test-net0" network
    And "test-vm" virtual machine on "kvm-server" should have a NIC with 02:34:56:78:9a:bc MAC address
    And "test-vm" virtual machine on "kvm-server" should have a "test-vm_disk.qcow2" scsi disk

@virthost_kvm
  Scenario: Add a network interface to a KVM virtual machine
    Given I am on the "Virtualization" page of this "kvm-server"
    When I click on "Edit" in row "test-vm"
    And I click on "add_nic"
    And I select "test-net0" from "network1_source"
    And I click on "Update"
    Then I should see a "Hosted Virtual Systems" text
    And "test-vm" virtual machine on "kvm-server" should have 2 NIC using "test-net0" network

@virthost_kvm
  Scenario: Delete a network interface from a KVM virtual machine
    Given I am on the "Virtualization" page of this "kvm-server"
    When I click on "Edit" in row "test-vm"
    And I click on "remove_nic1"
    And I click on "Update"
    Then I should see a "Hosted Virtual Systems" text
    And "test-vm" virtual machine on "kvm-server" should have 1 NIC using "test-net0" network

@virthost_kvm
  Scenario: Add a disk and a cdrom to a KVM virtual machine
    Given I am on the "Virtualization" page of this "kvm-server"
    When I click on "Edit" in row "test-vm"
    And I click on "add_disk"
    And I click on "add_disk"
    And I select "CDROM" from "disk2_device"
    And I select "ide" from "disk2_bus"
    And I click on "Update"
    Then I should see a "Hosted Virtual Systems" text
    And "test-vm" virtual machine on "kvm-server" should have a "test-vm_disk-1.qcow2" virtio disk
    And "test-vm" virtual machine on "kvm-server" should have a ide cdrom

@virthost_kvm
  Scenario: Delete a disk from a KVM virtual machine
    Given I am on the "Virtualization" page of this "kvm-server"
    When I click on "Edit" in row "test-vm"
    And I click on "remove_disk2"
    And I click on "Update"
    Then I should see a "Hosted Virtual Systems" text
    And "test-vm" virtual machine on "kvm-server" should have no cdrom

@virthost_kvm
  Scenario: Delete a KVM virtual machine
    Given I am on the "Virtualization" page of this "kvm-server"
    When I click on "Delete" in row "test-vm"
    And I click on "Delete" in "Delete Guest" modal
    Then I should not see a "test-vm" virtual machine on "kvm-server"

@virthost_kvm
  Scenario: Create a KVM virtual machine
    Given I am on the "Virtualization" page of this "kvm-server"
    When I follow "Create Guest"
    And I wait until I see "General" text
    And I enter "test-vm2" as "name"
    And I enter "/var/testsuite-data/disk-image-template.qcow2" as "disk0_source_template"
    And I select "Spice" from "graphicsType"
    And I click on "Create"
    Then I should see a "Hosted Virtual Systems" text
    When I wait until I see "test-vm2" text
    And I wait until table row for "test-vm2" contains button "Stop"
    And "test-vm2" virtual machine on "kvm-server" should have 1024MB memory and 1 vcpus
    And "test-vm2" virtual machine on "kvm-server" should have 1 NIC using "default" network
    And "test-vm2" virtual machine on "kvm-server" should have a "test-vm2_system.qcow2" virtio disk

@virthost_kvm
  Scenario: Show the Spice graphical console for KVM
    Given I am on the "Virtualization" page of this "kvm-server"
    When I click on "Graphical Console" in row "test-vm2"
    Then I wait until I see the spice graphical console
    And I close the window

@virthost_kvm
  Scenario: Cleanup: Unregister the KVM virtualization host
    Given I am on the Systems overview page of this "kvm-server"
    When I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"
    Then I wait until I see "has been deleted" text

@virthost_kvm
  Scenario: Cleanup: Cleanup KVM virtualization host
    When I run "zypper -n mr -e --all" on "kvm-server" without error control
    And I run "zypper -n rr SUSE-Manager-Bootstrap" on "kvm-server" without error control
    And I run "systemctl stop salt-minion" on "kvm-server" without error control
    And I run "rm /etc/salt/minion.d/susemanager*" on "kvm-server" without error control
    And I run "rm /etc/salt/minion.d/libvirt-events.conf" on "kvm-server" without error control
    And I run "rm /etc/salt/pki/minion/minion_master.pub" on "kvm-server" without error control
    # In case the delete VM test failed we need to clean up ourselves.
    And I run "virsh undefine --remove-all-storage test-vm" on "kvm-server" without error control
    And I run "virsh destroy test-vm2" on "kvm-server" without error control
    And I run "virsh undefine --remove-all-storage test-vm2" on "kvm-server" without error control
    And I run "virsh net-destroy test-net0" on "kvm-server" without error control
    And I run "virsh net-undefine test-net0" on "kvm-server" without error control
    # Remove the virtpoller cache to avoid problems
    And I run "rm /var/cache/virt_state.cache" on "kvm-server" without error control
