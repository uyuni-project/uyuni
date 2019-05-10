# Copyright (c) 2018 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Be able to manage XEN virtual machines via the GUI

@virthost_xen
  Scenario: Bootstrap Xen virtual host
    Given I am authorized
    When I go to the bootstrapping page
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "xen-server" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "xen-server" password
    And I select "1-SUSE-PKG-x86_64" from "activationKeys"
    And I select the hostname of the proxy from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host! " text
    And I wait until onboarding is completed for "xen-server"
    # Shorten the virtpoller interval to avoid loosing time
    And I reduce virtpoller run interval on "xen-server"

@virthost_xen
  Scenario: Setting the virtualization entitlement for Xen
    Given I am on the Systems overview page of this "xen-server"
    When I follow "Details" in the content area
    And I follow "Properties" in the content area
    And I check "virtualization_host"
    And I click on "Update Properties"
    Then I should see a "Since you added a Virtualization system type to the system" text


@virthost_xen
  Scenario: Prepare a Xen test virtual machine and list it
    Given I am on the "Virtualization" page of this "xen-server"
    When I create default virtual network on "xen-server"
    And I create test-net0 virtual network on "xen-server"
    And I create "test-vm" virtual machine on "xen-server"
    And I wait until I see "test-vm" text

@virthost_xen
  Scenario: Start a Xen virtual machine
    Given I am on the "Virtualization" page of this "xen-server"
    When I click on "Start" in row "test-vm"
    Then I should see "test-vm" virtual machine running on "xen-server"

@virthost_xen
  Scenario: Show the VNC graphical console for Xen
    Given I am on the "Virtualization" page of this "xen-server"
    When I click on "Graphical Console" in row "test-vm"
    Then I wait until I see the VNC graphical console
    And I close the window

@virthost_xen
  Scenario: Suspend a Xen virtual machine
    Given I am on the "Virtualization" page of this "xen-server"
    When I wait until table row for "test-vm" contains button "Suspend"
    And I click on "Suspend" in row "test-vm"
    And I click on "Suspend" in "Suspend Guest" modal
    Then I should see "test-vm" virtual machine paused on "xen-server"

@virthost_xen
  Scenario: Resume a Xen virtual machine
    Given I am on the "Virtualization" page of this "xen-server"
    When I wait until table row for "test-vm" contains button "Resume"
    And I click on "Resume" in row "test-vm"
    Then I should see "test-vm" virtual machine running on "xen-server"

@virthost_xen
  Scenario: Shutdown a Xen virtual machine
    Given I am on the "Virtualization" page of this "xen-server"
    When I wait until table row for "test-vm" contains button "Stop"
    And I wait until virtual machine "test-vm" on "xen-server" is started
    And I click on "Stop" in row "test-vm"
    And I click on "Stop" in "Stop Guest" modal
    Then I should see "test-vm" virtual machine shut off on "xen-server"

@virthost_xen
  Scenario: Edit a Xen virtual machine
    Given I am on the "Virtualization" page of this "xen-server"
    When I click on "Edit" in row "test-vm"
    And I wait until I do not see "Loading..." text
    Then I should see "512" in field "memory"
    And I should see "1" in field "vcpu"
    And option "VNC" is selected as "graphicsType"
    When I enter "1024" as "memory"
    And I enter "2" as "vcpu"
    And I select "Spice" from "graphicsType"
    And I select "test-net0" from "network0_source"
    And I enter "02:34:56:78:9a:bc" as "network0_mac"
    And I click on "Update"
    Then I should see a "Hosted Virtual Systems" text
    And "test-vm" virtual machine on "xen-server" should have 1024MB memory and 2 vcpus
    And "test-vm" virtual machine on "xen-server" should have spice graphics device
    And "test-vm" virtual machine on "xen-server" should have 1 NIC using "test-net0" network
    And "test-vm" virtual machine on "xen-server" should have a NIC with 02:34:56:78:9a:bc MAC address
    And "test-vm" virtual machine on "xen-server" should have a "test-vm_disk.qcow2" ide disk

@virthost_xen
  Scenario: Add a network interface to a Xen virtual machine
    Given I am on the "Virtualization" page of this "xen-server"
    When I click on "Edit" in row "test-vm"
    And I wait until I do not see "Loading..." text
    And I click on "add_nic"
    And I select "test-net0" from "network1_source"
    And I click on "Update"
    Then I should see a "Hosted Virtual Systems" text
    And "test-vm" virtual machine on "xen-server" should have 2 NIC using "test-net0" network

@virthost_xen
  Scenario: Delete a network interface from a Xen virtual machine
    Given I am on the "Virtualization" page of this "xen-server"
    When I click on "Edit" in row "test-vm"
    And I wait until I do not see "Loading..." text
    And I click on "remove_nic1"
    And I click on "Update"
    Then I should see a "Hosted Virtual Systems" text
    And "test-vm" virtual machine on "xen-server" should have 1 NIC using "test-net0" network

@virthost_xen
  Scenario: Add a disk and a cdrom to a Xen virtual machine
    Given I am on the "Virtualization" page of this "xen-server"
    When I click on "Edit" in row "test-vm"
    And I wait until I do not see "Loading..." text
    And I click on "add_disk"
    And I click on "add_disk"
    And I select "CDROM" from "disk2_device"
    And I select "ide" from "disk2_bus"
    And I click on "Update"
    Then I should see a "Hosted Virtual Systems" text
    And "test-vm" virtual machine on "xen-server" should have a "test-vm_disk-1.qcow2" xen disk
    And "test-vm" virtual machine on "xen-server" should have a ide cdrom

@virthost_xen
  Scenario: Delete a disk from a Xen virtual machine
    Given I am on the "Virtualization" page of this "xen-server"
    When I click on "Edit" in row "test-vm"
    And I wait until I do not see "Loading..." text
    # The libvirt disk order is not the same than for KVM
    And I click on "remove_disk1"
    And I click on "Update"
    Then I should see a "Hosted Virtual Systems" text
    And "test-vm" virtual machine on "xen-server" should have no cdrom

@virthost_xen
  Scenario: Delete a Xen virtual machine
    Given I am on the "Virtualization" page of this "xen-server"
    When I click on "Delete" in row "test-vm"
    And I click on "Delete" in "Delete Guest" modal
    Then I should not see a "test-vm" virtual machine on "xen-server"

@virthost_xen
  Scenario: Create a Xen paravirtualized guest
    Given I am on the "Virtualization" page of this "xen-server"
    When I follow "Create Guest"
    And I wait until I see "General" text
    And I enter "test-vm2" as "name"
    And I enter "/var/testsuite-data/disk-image-template-xenpv.qcow2" as "disk0_source_template"
    And I select "Spice" from "graphicsType"
    And I click on "Create"
    Then I should see a "Hosted Virtual Systems" text
    When I wait until I see "test-vm2" text
    And I wait until table row for "test-vm2" contains button "Stop"
    And "test-vm2" virtual machine on "xen-server" should have 1024MB memory and 1 vcpus
    And "test-vm2" virtual machine on "xen-server" should have 1 NIC using "default" network
    And "test-vm2" virtual machine on "xen-server" should have a "test-vm2_system.qcow2" xen disk

@virthost_xen
  Scenario: Show the Spice graphical console for Xen
    Given I am on the "Virtualization" page of this "xen-server"
    When I click on "Graphical Console" in row "test-vm2"
    Then I wait until I see the spice graphical console
    And I close the window

@virthost_xen
  Scenario: Create a Xen fully virtualized guest
    Given I am on the "Virtualization" page of this "xen-server"
    When I follow "Create Guest"
    And I wait until I see "General" text
    And I enter "test-vm3" as "name"
    And I select "Fully Virtualized" from "osType"
    And I enter "/var/testsuite-data/disk-image-template.qcow2" as "disk0_source_template"
    And I click on "Create"
    Then I should see a "Hosted Virtual Systems" text
    When I wait until I see "test-vm3" text
    And I wait until table row for "test-vm3" contains button "Stop"
    And "test-vm3" virtual machine on "xen-server" should have 1024MB memory and 1 vcpus
    And "test-vm3" virtual machine on "xen-server" should have 1 NIC using "default" network
    And "test-vm3" virtual machine on "xen-server" should have a "test-vm3_system.qcow2" xen disk

@virthost_xen
  Scenario: Cleanup: Unregister the Xen virtualization host
    Given I am on the Systems overview page of this "xen-server"
    When I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"
    Then I wait until I see "has been deleted" text

@virthost_xen
  Scenario: Cleanup: Cleanup Xen virtualization host
    When I run "zypper -n mr -e --all" on "xen-server" without error control
    And I run "zypper -n rr SUSE-Manager-Bootstrap" on "xen-server" without error control
    And I run "systemctl stop salt-minion" on "xen-server" without error control
    And I run "rm /etc/salt/minion.d/susemanager*" on "xen-server" without error control
    And I run "rm /etc/salt/minion.d/libvirt-events.conf" on "xen-server" without error control
    And I run "rm /etc/salt/pki/minion/minion_master.pub" on "xen-server" without error control
    # In case the delete VM test failed we need to clean up ourselves.
    And I run "virsh undefine --remove-all-storage test-vm" on "xen-server" without error control
    And I run "virsh destroy test-vm2" on "xen-server" without error control
    And I run "virsh undefine --remove-all-storage test-vm2" on "xen-server" without error control
    And I run "virsh destroy test-vm3" on "xen-server" without error control
    And I run "virsh undefine --remove-all-storage test-vm3" on "xen-server" without error control
    And I run "virsh net-destroy test-net0" on "xen-server" without error control
    And I run "virsh net-undefine test-net0" on "xen-server" without error control
    And I delete all "test-vm.*" volumes from "default" pool on "kvm-server" without error control
    # Remove the virtpoller cache to avoid problems
    And I run "rm /var/cache/virt_state.cache" on "xen-server" without error control
