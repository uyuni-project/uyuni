# Copyright (c) 2022-2023 SUSE LLC
# Licensed under the terms of the MIT license.

# This feature is not idempotent, we leave the system registered in order to have the history of events available
# and as a dependency for the KVM and Salt bundle tests in secondary.

@virthost_kvm
Feature: Bootstrap a virtualization host minion and set it up for virtualization

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create KVM activation key
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I wait until I do not see "Loading..." text
    When I enter "KVM testing" as "description"
    And I enter "KVM-TEST" as "key"
    And I enter "20" as "usageLimit"
    And I select "Test-Channel-x86_64" from "selectedBaseChannel"
    And I click on "Create Activation Key"
    Then I should see a "Activation key KVM testing has been created" text

  Scenario: Bootstrap KVM virtual host
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "kvm_server" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter KVM Server password
    And I select "1-KVM-TEST" from "activationKeys"
    And I select the hostname of "proxy" from "proxies" if present
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text
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
    And I click on "Expand All Sections"
    And I select "NAT" in virtual network mode field
    And I enter "192.168.124.1" in virtual network IPv4 address field
    And I enter "192.168.124.2" in first IPv4 address for DHCP field
    And I enter "192.168.124.254" in last IPv4 address for DHCP field
    And I click on "Save Formula"
    Then I should see a "Formula saved" text

  Scenario: Apply the KVM virtualization host formula via the highstate
    When I follow "States" in the content area
    And I click on "Apply Highstate"
    And I wait until event "Apply highstate scheduled" is completed
    Then service "libvirtd" is enabled on "kvm_server"

  Scenario: Restart the minion to enable libvirt_events engine configuration
    Then I restart salt-minion on "kvm_server"
