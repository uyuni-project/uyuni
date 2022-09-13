# Copyright (c) 2018-2022 SUSE LLC
# Licensed under the terms of the MIT license.

# This feature is not idempotent, we leave the system registered in order to have available the history of events,
# but there are no other features testing XEN.

# This feature has not dependencies and it can run in parallel with other features

@scope_virtualization
@virthost_xen
Feature: Be able to manage XEN virtual machines via the GUI

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap Xen virtual host
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "xen_server" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "xen_server" password
    And I select "1-SUSE-KEY-x86_64" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait at most 300 seconds until I see "Successfully bootstrapped host!" text
    And I wait until onboarding is completed for "xen_server"

  Scenario: Show the Xen host system overview
    Given I am on the Systems overview page of this "xen_server"

  Scenario: Set the virtualization entitlement for Xen
    When I follow "Details" in the content area
    And I follow "Properties" in the content area
    And I check "virtualization_host"
    And I click on "Update Properties"
    Then I should see a "Since you added a Virtualization system type to the system" text

  Scenario: Enable the virtualization host formula for Xen
    When I follow "Formulas" in the content area
    Then I should see a "Choose formulas" text
    And I should see a "Virtualization" text
    When I check the "virtualization-host" formula
    And I click on "Save"
    And I wait until I see "Formula saved." text
    Then the "virtualization-host" formula should be checked

  Scenario: Parametrize the Xen virtualization host
    When I follow "Formulas" in the content area
    And I follow first "Virtualization Host" in the content area
    And I click on "Expand All Sections"
    And I select "Xen" from "hypervisor"
    And I select "NAT" in virtual network mode field
    And I enter "192.168.124.1" in virtual network IPv4 address field
    And I enter "192.168.124.2" in first IPv4 address for DHCP field
    And I enter "192.168.124.254" in last IPv4 address for DHCP field
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: Apply the Xen virtualization host formula via the highstate
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    And I wait until event "Apply highstate scheduled by admin" is completed
    Then service "libvirtd" is enabled on "xen_server"

  Scenario: Restart the minion to enable libvirt_events engine configuration
    Then I restart salt-minion on "xen_server"

  Scenario: Prepare a Xen test virtual machine and list it
    When I follow "Virtualization" in the content area
    And I delete default virtual network on "xen_server"
    And I create test-net0 virtual network on "xen_server"
    And I create test-net1 virtual network on "xen_server"
    And I delete default virtual storage pool on "xen_server"
    And I create test-pool0 virtual storage pool on "xen_server"
    And I create "test-vm" virtual machine on "xen_server"
    And I wait until I see "test-vm" text

  Scenario: Show the Xen host virtualization tab
    Given I follow "Virtualization" in the content area

  Scenario: Start a Xen virtual machine
    And I click on "Start" in row "test-vm"
    Then I should see "test-vm" virtual machine running on "xen_server"

  Scenario: Suspend a Xen virtual machine
    When I follow "Virtualization" in the content area
    And I wait until table row for "test-vm" contains button "Suspend"
    And I click on "Suspend" in row "test-vm"
    And I click on "Suspend" in "Suspend Guest" modal
    Then I should see "test-vm" virtual machine paused on "xen_server"

  Scenario: Resume a Xen virtual machine
    When I follow "Virtualization" in the content area
    And I wait until table row for "test-vm" contains button "Resume"
    And I click on "Resume" in row "test-vm"
    Then I should see "test-vm" virtual machine running on "xen_server"

  Scenario: Shutdown a Xen virtual machine
    When I follow "Virtualization" in the content area
    And I wait until table row for "test-vm" contains button "Stop"
    And I wait until virtual machine "test-vm" on "xen_server" is started
    And I click on "Stop" in row "test-vm"
    And I click on "Stop" in "Stop Guest" modal
    Then I should see "test-vm" virtual machine shut off on "xen_server"

  Scenario: Edit a Xen virtual machine
    When I follow "Virtualization" in the content area
    And I click on "Edit" in row "test-vm"
    And I wait until I do not see "Loading..." text
    And I should see "1" in field "vcpu"
    And option "VNC" is selected as "graphicsType"
    And option "test-net0" is selected as "network0_source"
    When I enter "1024" as "memory"
    And I enter "2" as "vcpu"
    And I select "Spice" from "graphicsType"
    And I select "test-net1" from "network0_source"
    And I enter "02:34:56:78:9a:bc" as "network0_mac"
    And I click on "Update"
    Then I should see a "Hosted Virtual Systems" text
    And "test-vm" virtual machine on "xen_server" should have spice graphics device
    And "test-vm" virtual machine on "xen_server" should have 1 NIC using "test-net1" network
    And "test-vm" virtual machine on "xen_server" should have a NIC with 02:34:56:78:9a:bc MAC address
    And "test-vm" virtual machine on "xen_server" should have a "test-vm_disk.qcow2" ide disk

  Scenario: Add a network interface to a Xen virtual machine
    When I follow "Virtualization" in the content area
    And I click on "Edit" in row "test-vm"
    And I wait until I do not see "Loading..." text
    And I click on "add_network"
    And I select "test-net1" from "network1_source"
    And I click on "Update"
    Then I should see a "Hosted Virtual Systems" text
    And "test-vm" virtual machine on "xen_server" should have 2 NIC using "test-net1" network

  Scenario: Delete a network interface from a Xen virtual machine
    When I follow "Virtualization" in the content area
    And I click on "Edit" in row "test-vm"
    And I wait until I do not see "Loading..." text
    And I click on "remove_network1"
    And I click on "Update"
    Then I should see a "Hosted Virtual Systems" text
    And "test-vm" virtual machine on "xen_server" should have 1 NIC using "test-net1" network

  Scenario: Add a disk and a cdrom to a Xen virtual machine
    When I follow "Virtualization" in the content area
    And I click on "Edit" in row "test-vm"
    And I wait until I do not see "Loading..." text
    And I click on "add_disk"
    And I select "test-pool0" from "disk1_source_pool"
    And I click on "add_disk"
    And I select "CDROM" from "disk2_device"
    And I select "ide" from "disk2_bus"
    And I click on "Update"
    Then I should see a "Hosted Virtual Systems" text
    And "test-vm" virtual machine on "xen_server" should have a "/var/lib/libvirt/images/test-pool0/test-vm_disk-1" xen disk
    And "test-vm" virtual machine on "xen_server" should have a ide cdrom

  Scenario: Delete a disk from a Xen virtual machine
    When I follow "Virtualization" in the content area
    And I click on "Edit" in row "test-vm"
    And I wait until I do not see "Loading..." text
    # The libvirt disk order is not the same than for KVM
    And I click on "remove_disk1"
    And I click on "Update"
    Then I should see a "Hosted Virtual Systems" text
    And "test-vm" virtual machine on "xen_server" should have no cdrom

  Scenario: Delete a Xen virtual machine
    When I follow "Virtualization" in the content area
    And I click on "Delete" in row "test-vm"
    And I click on "Delete" in "Delete Guest" modal
    Then I should not see a "test-vm" virtual machine on "xen_server"

  Scenario: Create a Xen paravirtualized guest
    When I follow "Virtualization" in the content area
    And I follow "Create Guest"
    And I wait until I see "General" text
    And I enter "test-vm2" as "name"
    And I enter "512" as "memory"
    And I enter "/var/testsuite-data/disk-image-template-xenpv.qcow2" as "disk0_source_template"
    And I select "test-net0" from "network0_source"
    And I select "VNC" from "graphicsType"
    And I click on "Create"
    And I wait until I see "Hosted Virtual Systems" text
    And I wait until event "Creates a virtual domain: test-vm2" is completed
    And I follow "Virtualization" in the content area
    And I wait at most 500 seconds until table row for "test-vm2" contains button "Stop"
    And "test-vm2" virtual machine on "xen_server" should have 1 NIC using "test-net0" network
    And "test-vm2" virtual machine on "xen_server" should have a "/var/lib/libvirt/images/test-pool0/test-vm2_system" xen disk

  Scenario: Show the VNC graphical console for Xen
    When I follow "Virtualization" in the content area
    And I click on "Graphical Console" in row "test-vm2"
    And I switch to last opened window
    And I wait until I see the VNC graphical console
    And I close the last opened window

  Scenario: Create a Xen fully virtualized guest
    When I follow "Virtualization" in the content area
    And I follow "Create Guest"
    And I wait until I see "General" text
    And I enter "test-vm3" as "name"
    And I select "Fully Virtualized" from "osType"
    And I enter "512" as "memory"
    And I enter "/var/testsuite-data/disk-image-template.qcow2" as "disk0_source_template"
    And I select "test-net0" from "network0_source"
    And I select "Spice" from "graphicsType"
    And I click on "Create"
    And I wait until I see "Hosted Virtual Systems" text
    And I wait until event "Creates a virtual domain: test-vm3" is completed
    And I follow "Virtualization" in the content area
    And I wait at most 500 seconds until table row for "test-vm3" contains button "Stop"
    And "test-vm3" virtual machine on "xen_server" should have 1 NIC using "test-net0" network
    And "test-vm3" virtual machine on "xen_server" should have a "/var/lib/libvirt/images/test-pool0/test-vm3_system" xen disk

  Scenario: Show the Spice graphical console for Xen
    When I follow "Virtualization" in the content area
    And I click on "Graphical Console" in row "test-vm3"
    And I switch to last opened window
    And I wait until I see the spice graphical console
    And I close the last opened window

  Scenario: Show the virtual storage pools and volumes for Xen
    When I follow "Virtualization" in the content area
    And I refresh the "test-pool0" storage pool of this "xen_server"
    And I follow "Storage"
    And I open the sub-list of the product "test-pool0"
    Then I wait until I see "test-vm2_system" text

  Scenario: delete a running Xen virtual machine
    When I follow "Virtualization" in the content area
    And I click on "Delete" in row "test-vm3"
    And I click on "Delete" in "Delete Guest" modal
    Then I should not see a "test-vm3" virtual machine on "xen_server"
