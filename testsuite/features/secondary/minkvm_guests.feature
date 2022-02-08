# Copyright (c) 2018-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_virtualization
@virthost_kvm
Feature: Be able to manage KVM virtual machines via the GUI

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap KVM virtual host
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "kvm_server" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "kvm_server" password
    And I select "1-SUSE-KEY-x86_64" from "activationKeys"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Successfully bootstrapped host!" text
    And I wait until onboarding is completed for "kvm_server"

  Scenario: Show the KVM host system overview
    Given I am on the Systems overview page of this "kvm_server"

  Scenario: Set the virtualization entitlement for KVM
    When I follow "Details" in the content area
    And I follow "Properties" in the content area
    And I check "virtualization_host"
    And I click on "Update Properties"
    Then I should see a "Since you added a Virtualization system type to the system" text

  Scenario: Enable the virtualization host formula for KVM
    When I follow "Formulas" in the content area
    Then I should see a "Choose formulas" text
    And I should see a "Virtualization" text
    When I check the "virtualization-host" formula
    And I click on "Save"
    And I wait until I see "Formula saved." text
    Then the "virtualization-host" formula should be checked

  Scenario: Parametrize the KVM virtualization host
    When I follow "Formulas" in the content area
    And I follow first "Virtualization Host" in the content area
    And I select "NAT" in virtual network mode field
    And I enter "192.168.124.1" in virtual network IPv4 address field
    And I enter "192.168.124.2" in first IPv4 address for DHCP field
    And I enter "192.168.124.254" in last IPv4 address for DHCP field
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: Apply the KVM virtualization host formula via the highstate
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    And I wait until event "Apply highstate scheduled by admin" is completed
    Then service "libvirtd" is enabled on "kvm_server"

  Scenario: Restart the minion to enable libvirt_events engine configuration
    Then I restart salt-minion on "kvm_server"

  Scenario: Prepare a KVM test virtual machine and list it
    When I delete default virtual network on "kvm_server"
    And I create test-net0 virtual network on "kvm_server"
    And I create test-net1 virtual network on "kvm_server"
    And I delete default virtual storage pool on "kvm_server"
    And I create test-pool0 virtual storage pool on "kvm_server"
    And I create "test-vm" virtual machine on "kvm_server"
    And I follow "Virtualization" in the content area
    And I wait until I see "test-vm" text

  Scenario: Show the KVM host virtualization tab
    Given I follow "Virtualization" in the content area

  Scenario: Start a KVM virtual machine
    When I click on "Start" in row "test-vm"
    Then I should see "test-vm" virtual machine running on "kvm_server"

  Scenario: Show the VNC graphical console for KVM
    When I click on "Graphical Console" in row "test-vm"
    And I switch to last opened window
    Then I wait until I see the VNC graphical console
    When I close the last opened window

  Scenario: Suspend a KVM virtual machine
    When I wait until table row for "test-vm" contains button "Suspend"
    And I click on "Suspend" in row "test-vm"
    And I click on "Suspend" in "Suspend Guest" modal
    Then I should see "test-vm" virtual machine paused on "kvm_server"

  Scenario: Resume a KVM virtual machine
    When I wait until table row for "test-vm" contains button "Resume"
    And I click on "Resume" in row "test-vm"
    Then I should see "test-vm" virtual machine running on "kvm_server"

  Scenario: Shutdown a KVM virtual machine
    When I wait until table row for "test-vm" contains button "Stop"
    And I wait until virtual machine "test-vm" on "kvm_server" is started
    And I click on "Stop" in row "test-vm"
    And I click on "Stop" in "Stop Guest" modal
    Then I should see "test-vm" virtual machine shut off on "kvm_server"

  Scenario: Edit a KVM virtual machine
    When I click on "Edit" in row "test-vm"
    And I wait until I do not see "Loading..." text
    Then I should see "512" in field "memory"
    And I should see "1" in field "vcpu"
    And option "VNC" is selected as "graphicsType"
    And option "test-net0" is selected as "network0_source"
    And option "ide" is selected as "disk0_bus"
    When I enter "1024" as "memory"
    And I enter "2" as "vcpu"
    And I select "Spice" from "graphicsType"
    And I select "test-net1" from "network0_source"
    And I enter "02:34:56:78:9a:bc" as "network0_mac"
    And I select "scsi" from "disk0_bus"
    And I click on "Update"
    Then I should see a "Hosted Virtual Systems" text
    And "test-vm" virtual machine on "kvm_server" should have 1024MB memory and 2 vcpus
    And "test-vm" virtual machine on "kvm_server" should have spice graphics device
    And "test-vm" virtual machine on "kvm_server" should have 1 NIC using "test-net1" network
    And "test-vm" virtual machine on "kvm_server" should have a NIC with 02:34:56:78:9a:bc MAC address
    And "test-vm" virtual machine on "kvm_server" should have a "test-vm_disk.qcow2" SCSI disk from pool "tmp"

  Scenario: Add a network interface to a KVM virtual machine
    When I click on "Edit" in row "test-vm"
    And I wait until I do not see "Loading..." text
    And I click on "add_network"
    And I select "test-net1" from "network1_source"
    And I click on "Update"
    Then I should see a "Hosted Virtual Systems" text
    And "test-vm" virtual machine on "kvm_server" should have 2 NIC using "test-net1" network

  Scenario: Delete a network interface from a KVM virtual machine
    When I click on "Edit" in row "test-vm"
    And I wait until I do not see "Loading..." text
    And I click on "remove_network1"
    And I click on "Update"
    Then I should see a "Hosted Virtual Systems" text
    And "test-vm" virtual machine on "kvm_server" should have 1 NIC using "test-net1" network

  Scenario: Add a disk and a cdrom to a KVM virtual machine
    When I click on "Edit" in row "test-vm"
    And I wait until I do not see "Loading..." text
    And I click on "add_disk"
    And I click on "add_disk"
    And I select "CDROM" from "disk2_device"
    And I select "ide" from "disk2_bus"
    And I click on "Update"
    Then I should see a "Hosted Virtual Systems" text
    And "test-vm" virtual machine on "kvm_server" should have a "test-vm_disk-1" virtio disk from pool "test-pool0"
    And "test-vm" virtual machine on "kvm_server" should have a ide cdrom

  Scenario: Attach an image to a cdrom on a KVM virtual machine
    When I click on "Edit" in row "test-vm"
    And I wait until I do not see "Loading..." text
    And I store "" into file "/tmp/test-image.iso" on "kvm_server"
    And I wait until I do not see "Loading..." text
    And I enter "/tmp/test-image.iso" as "disk2_source_file"
    And I click on "Update"
    Then I should see a "Hosted Virtual Systems" text
    And "test-vm" virtual machine on "kvm_server" should have "/tmp/test-image.iso" attached to a cdrom

  Scenario: Delete a disk from a KVM virtual machine
    When I click on "Edit" in row "test-vm"
    And I wait until I do not see "Loading..." text
    And I click on "remove_disk2"
    And I click on "Update"
    Then I should see a "Hosted Virtual Systems" text
    And "test-vm" virtual machine on "kvm_server" should have no cdrom

  Scenario: Delete a KVM virtual machine
    When I click on "Delete" in row "test-vm"
    And I click on "Delete" in "Delete Guest" modal
    Then I should not see a "test-vm" virtual machine on "kvm_server"

  Scenario: Create a KVM virtual machine
    And I create empty "/var/lib/libvirt/images/test-pool0/disk1.qcow2" qcow2 disk file on "kvm_server"
    And I refresh the "test-pool0" storage pool of this "kvm_server"
    When I follow "Create Guest"
    And I wait until I see "General" text
    And I enter "test-vm2" as "name"
    And I enter "/var/testsuite-data/disk-image-template.qcow2" as "disk0_source_template"
    And I select "test-net0" from "network0_source"
    And I select "Spice" from "graphicsType"
    And I click on "add_disk"
    And I select "test-pool0" from "disk1_source_pool"
    And I select "disk1.qcow2" from "disk1_source_file"
    And I click on "Create"
    Then I should see a "Hosted Virtual Systems" text
    When I wait until I see "test-vm2" text
    And I wait until table row for "test-vm2" contains button "Stop"
    And "test-vm2" virtual machine on "kvm_server" should have 1024MB memory and 1 vcpus
    And "test-vm2" virtual machine on "kvm_server" should have 1 NIC using "test-net0" network
    And "test-vm2" virtual machine on "kvm_server" should have a "test-vm2_system" virtio disk from pool "test-pool0"
    And "test-vm2" virtual machine on "kvm_server" should have a "disk1.qcow2" virtio disk from pool "test-pool0"

  Scenario: Show the Spice graphical console for KVM
    When I click on "Graphical Console" in row "test-vm2"
    And I switch to last opened window
    Then I wait until I see the spice graphical console
    When I close the last opened window

  Scenario: Show the virtual storage pools and volumes for KVM
    When I refresh the "test-pool0" storage pool of this "kvm_server"
    And I follow "Storage"
    And I wait until I do not see "Loading..." text
    And I open the sub-list of the product "test-pool0"
    Then I wait until I see "test-vm2_system" text

  Scenario: delete a running KVM virtual machine
    When I follow "Virtualization" in the content area
    And I wait until I do not see "Loading..." text
    And I click on "Delete" in row "test-vm2"
    And I click on "Delete" in "Delete Guest" modal
    Then I should not see a "test-vm2" virtual machine on "kvm_server"

  Scenario: Create a KVM UEFI virtual machine
    When I follow "Create Guest"
    And I wait until I see "General" text
    And I enter "test-vm2" as "name"
    And I enter "/var/testsuite-data/disk-image-template.qcow2" as "disk0_source_template"
    And I select "test-net0" from "network0_source"
    And I check "uefi"
    And I enter "/usr/share/qemu/ovmf-x86_64-ms.bin" as "uefiLoader"
    And I enter "/usr/share/qemu/ovmf-x86_64-ms-vars.bin" as "nvramTemplate"
    And I click on "Create"
    Then I should see a "Hosted Virtual Systems" text
    When I wait until I see "test-vm2" text
    And I wait until table row for "test-vm2" contains button "Stop"
    And "test-vm2" virtual machine on "kvm_server" should have 1024MB memory and 1 vcpus
    And "test-vm2" virtual machine on "kvm_server" should have 1 NIC using "test-net0" network
    And "test-vm2" virtual machine on "kvm_server" should have a "test-vm2_system" virtio disk from pool "test-pool0"
    And "test-vm2" virtual machine on "kvm_server" should be UEFI enabled

  Scenario: delete a running KVM UEFI virtual machine
    When I follow "Virtualization" in the content area
    And I wait until I do not see "Loading..." text
    And I click on "Delete" in row "test-vm2"
    And I click on "Delete" in "Delete Guest" modal
    Then I should not see a "test-vm2" virtual machine on "kvm_server"

  Scenario: Refresh a virtual storage pool for KVM
    When I follow "Storage"
    And I wait until I do not see "Loading..." text
    And I click on "Refresh" in tree item "test-pool0"
    And I wait at most 600 seconds until the tree item "test-pool0" has no sub-list

  Scenario: Stop a virtual storage pool for KVM
    When I follow "Storage"
    And I wait until I do not see "Loading..." text
    And I click on "Stop" in tree item "test-pool0"
    And I wait at most 600 seconds until the tree item "test-pool0" contains "inactive" text

  Scenario: Start a virtual storage pool for KVM
    When I follow "Storage"
    And I wait until I do not see "Loading..." text
    And I click on "Start" in tree item "test-pool0"
    And I wait at most 600 seconds until the tree item "test-pool0" contains "running" text

  Scenario: Delete a virtual storage pool for KVM
    When I follow "Storage"
    And I wait until I do not see "Loading..." text
    And I click on "Delete" in tree item "test-pool0"
    And I check "purge"
    And I click on "Delete" in "Delete Virtual Storage Pool" modal
    Then I wait until I do not see "test-pool0" text
    And file "/var/lib/libvirt/images/test-pool0" should not exist on "kvm_server"

  Scenario: Create a virtual storage pool for KVM
    When I follow "Storage"
    And I follow "Create Pool"
    And I wait until I see "General" text
    And I select "dir" from "type"
    And I enter "test-pool1" as "name"
    And I uncheck "autostart"
    And I enter "/var/lib/libvirt/images/test-pool1" as "target_path"
    And I enter "0755" as "target_mode"
    And I click on "Create"
    Then I should see a "Virtual Storage Pools and Volumes" text
    And I wait at most 600 seconds until the tree item "test-pool1" contains "running" text
    And file "/var/lib/libvirt/images/test-pool1" should have 755 permissions on "kvm_server"

  Scenario: Edit a virtual storage pool for KVM
    When I follow "Storage"
    And I wait until I do not see "Loading..." text
    And I click on "Edit Pool" in tree item "test-pool1"
    And I wait until I see "General" text
    And I enter "0711" as "target_mode"
    And I check "autostart"
    And I click on "Update"
    Then I should see a "Virtual Storage Pools and Volumes" text
    And I wait at most 600 seconds until the tree item "test-pool1" contains "test-pool1 is started automatically" button
    And file "/var/lib/libvirt/images/test-pool1" should have 711 permissions on "kvm_server"

  Scenario: Delete a virtual volume
    When I follow "Storage"
    And I wait until I do not see "Loading..." text
    And I open the sub-list of the product "tmp"
    And I click on "Delete" in tree item "test-net0.xml"
    And I click on "Delete" in "Delete Virtual Storage Volume" modal
    Then I wait until I do not see "test-net0.xml" text

  Scenario: List virtual networks
    When I follow "Networks"
    And I wait until I do not see "Loading..." text
    Then I wait until I see "test-net0" text
    And I should see a "test-net1" text

  Scenario: Stop virtual network
    When I follow "Networks"
    And I wait until I do not see "Loading..." text
    Then table row for "test-net1" should contain "running"
    When I click on "Stop" in row "test-net1"
    And I click on "Stop" in "Stop Network" modal
    Then I wait until table row for "test-net1" contains button "Start"
    And table row for "test-net1" should contain "stopped"

  Scenario: Start virtual network
    When I follow "Networks"
    And I wait until I do not see "Loading..." text
    And I click on "Start" in row "test-net1"
    Then I wait until table row for "test-net1" contains button "Stop"
    And table row for "test-net1" should contain "running"

  Scenario: Delete virtual network
    When I follow "Networks"
    And I wait until I do not see "Loading..." text
    And I click on "Delete" in row "test-net1"
    And I click on "Delete" in "Delete Network" modal
    Then I wait until I do not see "test-net1" text
    And I should not see a "test-net1" virtual network on "kvm_server"

  Scenario: Create a virtual network
    When I follow "Networks"
    And I follow "Create Network"
    And I wait until option "bridge" appears in list "type"
    And I select "isolated" from "type"
    And I enter "test-net2" as "name"
    And I check "autostart"
    And I enter "192.168.128.0" as "ipv4def_address"
    And I enter "24" as "ipv4def_prefix"
    And I click on "add_ipv4def_dhcpranges"
    And I enter "192.168.128.10" as "ipv4def_dhcpranges0_start"
    And I enter "192.168.128.20" as "ipv4def_dhcpranges0_end"
    And I click on "Create"
    Then I should see a "Virtual Networks" text
    And I wait until table row for "test-net2" contains button "Stop"
    And table row for "test-net2" should contain "running"
    And I should see a "test-net2" virtual network on "kvm_server"
    And "test-net2" virtual network on "kvm_server" should have "192.168.128.1" IPv4 address with 24 prefix

@virthost_kvm
  Scenario: Edit a virtual network
    Given I am on the "Virtualization" page of this "kvm_server"
    When I follow "Networks"
    And I wait until I do not see "Loading..." text
    And I click on "Stop" in row "test-net2"
    And I click on "Stop" in "Stop Network" modal
    Then I wait until table row for "test-net2" contains button "Start"
    When I click on "Edit" in row "test-net2"
    And I wait until option "isolated" appears in list "type"
    And I enter "192.168.130.0" as "ipv4def_address"
    And I click on "remove_ipv4def_dhcpranges0"
    And I click on "Update"
    Then I should see a "Virtual Networks" text
    And "test-net2" virtual network on "kvm_server" should have "192.168.130.1" IPv4 address with 24 prefix

# Start provisioning scenarios

@scc_credentials
  Scenario: Create auto installation distribution
    And I install package tftpboot-installation on the server
    And I wait for "tftpboot-installation-SLE-15-SP2-x86_64" to be installed on "server"
    When I follow the left menu "Systems > Autoinstallation > Distributions"
    And I follow "Create Distribution"
    And I enter "SLE-15-SP2-TFTP" as "label"
    And I enter "/usr/share/tftpboot-installation/SLE-15-SP2-x86_64/" as "basepath"
    And I select "SLE-Product-SLES15-SP2-Pool for x86_64" from "channelid"
    And I select "SUSE Linux Enterprise 15" from "installtype"
    And I enter "useonlinerepo insecure=1" as "kernelopts"
    And I click on "Create Autoinstallable Distribution"
    Then I should see a "Autoinstallable Distributions" text
    And I should see a "SLE-15-SP2-TFTP" link

@scc_credentials
  Scenario: Create auto installation profile
    And I follow the left menu "Systems > Autoinstallation > Profiles"
    And I follow "Upload Kickstart/Autoyast File"
    When I enter "15-sp2-kvm" as "kickstartLabel"
    And I select "SLE-15-SP2-TFTP" from "kstreeId"
    And I select "KVM Virtualized Guest" from "virtualizationTypeLabel"
    And I attach the file "/sle-15-sp2-autoyast.xml" to "fileUpload"
    And I click on "Create"
    Then I should see a "Autoinstallation: 15-sp2-kvm" text
    And I should see a "Autoinstallation Details" text

@scc_credentials
  Scenario: Configure auto installation profile
    When I enter "self_update=0" as "kernel_options"
    And I click on "Update"
    And I follow "Variables"
    And I enter "distrotree=SLE-15-SP2-TFTP\nregistration_key=1-SUSE-KEY-x86_64" as "variables" text area
    And I click on "Update Variables"
    And I follow "Autoinstallation File"
    Then I should see a "SLE-15-SP2-TFTP" text

@scc_credentials
  Scenario: Create an auto installing KVM virtual machine
    Given I am on the "Virtualization" page of this "kvm_server"
    And I wait until the channel "sle-module-basesystem15-sp2-updates-x86_64" has been synced
    When I follow "Create Guest"
    And I wait until I see "General" text
    And I enter "test-vm2" as "name"
    And I select "15-sp2-kvm" from "cobbler_profile"
    And I select "test-net0" from "network0_source"
    And I click on "Create"
    Then I should see a "Hosted Virtual Systems" text
    When I wait until I see "test-vm2" text
    And I wait until table row for "test-vm2" contains button "Stop"
    # Test the VM boot params
    Then "test-vm2" virtual machine on "kvm_server" should boot using autoyast
    And "test-vm2" virtual machine on "kvm_server" should stop on reboot
    And "test-vm2" virtual machine on "kvm_server" should boot on hard disk at next start
    And "test-vm2" virtual machine on "kvm_server" should not stop on reboot at next start
    When I click on "Graphical Console" in row "test-vm2"
    And I switch to last opened window
    And I wait until I see the VNC graphical console
    And I wait at most 1000 seconds until Salt master sees "test-vm2" as "unaccepted"
    When I close the last opened window

@scc_credentials
  Scenario: Cleanup: remove the auto installation profile
    And I follow the left menu "Systems > Autoinstallation > Profiles"
    When I follow "15-sp2-kvm"
    And I follow "Delete Autoinstallation"
    And I click on "Delete Autoinstallation"
    Then I should not see a "15-sp2-kvm" text

@scc_credentials
  Scenario: Cleanup: remove the auto installation distribution
    When I follow the left menu "Systems > Autoinstallation > Distributions"
    And I follow "SLE-15-SP2-TFTP"
    And I follow "Delete Distribution"
    And I click on "Delete Distribution"
    And I remove package "tftpboot-installation-SLE-15-SP2-x86_64" from this "server"
    And I wait for "tftpboot-installation-SLE-15-SP2-x86_64" to be uninstalled on "server"
    Then I should not see a "SLE-15-SP2-TFTP" text

# End of provisioning scenarios

  Scenario: Cleanup: Unregister the KVM virtualization host
    Given I am on the Systems overview page of this "kvm_server"
    When I follow "Delete System"
    And I should see a "Confirm System Profile Deletion" text
    And I click on "Delete Profile"
    Then I wait until I see "has been deleted" text

  Scenario: Cleanup: Cleanup KVM virtualization host
    When I run "zypper -n mr -e --all" on "kvm_server" without error control
    And I run "zypper -n rr SUSE-Manager-Bootstrap" on "kvm_server" without error control
    And I stop salt-minion on "kvm_server"
    And I run "rm /etc/salt/minion.d/susemanager*" on "kvm_server" without error control
    And I run "rm /etc/salt/minion.d/libvirt-events.conf" on "kvm_server" without error control
    And I run "rm /etc/salt/pki/minion/minion_master.pub" on "kvm_server" without error control
    # In case the delete VM test failed we need to clean up ourselves.
    And I run "virsh undefine --remove-all-storage test-vm" on "kvm_server" without error control
    And I run "virsh destroy test-vm2" on "kvm_server" without error control
    And I run "virsh undefine --remove-all-storage test-vm2" on "kvm_server" without error control
    And I delete test-net0 virtual network on "kvm_server" without error control
    And I delete test-net1 virtual network on "kvm_server" without error control
    And I delete test-net2 virtual network on "kvm_server" without error control
    And I delete test-pool0 virtual storage pool on "kvm_server" without error control
    And I delete test-pool1 virtual storage pool on "kvm_server" without error control
    And I delete all "test-vm.*" volumes from "test-pool0" pool on "kvm_server" without error control

  @uyuni
  Scenario: Cleanup: Cleanup venv-salt-minion files from KVM virtualization host
    And I run "rm /etc/venv-salt-minion/minion.d/susemanager*" on "kvm_server" without error control
    And I run "rm /etc/venv-salt-minion/minion.d/libvirt-events.conf" on "kvm_server" without error control
    And I run "rm /etc/venv-salt-minion/pki/minion/minion_master.pub" on "kvm_server" without error control
