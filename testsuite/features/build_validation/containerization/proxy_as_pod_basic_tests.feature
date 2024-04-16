# Copyright (c) 2022-2023 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature DON'T aims to prepare the Proxy to be tested by all our supported clients
# This feature will run in isolation only validating the basic steps on a SLES 15 SP4 Minion
#
# The scenarios in this feature are skipped if:
# * there is no proxy ($proxy is nil)
# * there is no salt minion ($sle15sp4_minion is nil)

@proxy
@sle15sp4_minion
Feature: Register and test a Containerized Proxy
  In order to test Containerized Proxy
  As the system administrator
  I want to register the proxy to the server
  
  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Pre-requisite: Unregister Salt minion in the traditional proxy
    Given I am on the Systems overview page of this "sle15sp4_minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    And I wait until Salt client is inactive on "sle15sp4_minion"
    Then "sle15sp4_minion" should not be registered

  Scenario: Pre-requisite: Stop traditional proxy service
    When I stop salt-minion on "proxy"
    And I run "spacewalk-proxy stop" on "proxy"
    # workaround for bsc#1205976
    And I stop the "tftp" service on "proxy"
    And I wait until "squid" service is inactive on "proxy"
    And I wait until "apache2" service is inactive on "proxy"
    And I wait until "jabberd" service is inactive on "proxy"
    And I wait until "tftp" service is inactive on "proxy"

  Scenario: Pre-requisite: Install last uyuni-proxy-systemd-services package
    When I install package "uyuni-proxy-systemd-services" on this "proxy"

  Scenario: Generate Containerized Proxy configuration
    When I generate the configuration "/tmp/proxy_container_config.tar.gz" of Containerized Proxy on the server
    And I copy "/tmp/proxy_container_config.tar.gz" file from "server" to "proxy"
    And I run "tar xzf /tmp/proxy_container_config.tar.gz -C /etc/uyuni/proxy/" on "proxy"

  Scenario: Set-up the Containerized Proxy service to support Avahi
    And I add avahi hosts in Containerized Proxy configuration

  Scenario: Start Containerized Proxy services
    When I start the "uyuni-proxy-pod" service on "proxy"
    And I wait until "uyuni-proxy-pod" service is active on "proxy"
    And I wait until "uyuni-proxy-httpd" service is active on "proxy"
    And I wait until "uyuni-proxy-salt-broker" service is active on "proxy"
    And I wait until "uyuni-proxy-squid" service is active on "proxy"
    And I wait until "uyuni-proxy-ssh" service is active on "proxy"
    And I wait until "uyuni-proxy-tftpd" service is active on "proxy"
    And I wait until port "8022" is listening on "proxy"
    And I wait until port "8080" is listening on "proxy"
    And I wait until port "443" is listening on "proxy"
    And I visit "Proxy" endpoint of this "proxy"

  Scenario: Containerized Proxy should be registered automatically
    When I follow the left menu "Systems"
    And I wait until I see the name of "containerized_proxy", refreshing the page

  Scenario: Remove the offending key in salt known hosts
    When I remove offending SSH key of "containerized_proxy" at port "8022" for "/var/lib/salt/.ssh/known_hosts" on "server"

  Scenario: Bootstrap a Salt minion in the Containerized Proxy
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle15sp4_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select the hostname of "containerized_proxy" from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text

  Scenario: Check the new bootstrapped minion in System Overview page
    When I follow the left menu "Salt > Keys"
    And I wait until I do not see "Loading..." text
    Then I should see a "accepted" text
    When I follow the left menu "Systems > System List > All"
    And I wait until I see the name of "sle15sp4_minion", refreshing the page
    And I wait until onboarding is completed for "sle15sp4_minion"
    Then the Salt master can reach "sle15sp4_minion"

  Scenario: Check connection from minion to Containerized Proxy
    Given I am on the Systems overview page of this "sle15sp4_minion"
    When I follow "Details" in the content area
    And I follow "Connection" in the content area
    Then I should see "containerized_proxy" short hostname

  Scenario: Check registration on Containerized Proxy of minion
    When I follow the left menu "Systems > System List > All"
    And I follow this "containerized_proxy" link
    And I follow "Details" in the content area
    And I follow "Proxy" in the content area
    Then I should see "sle15sp4_minion" hostname

  Scenario: Salt minion grains are displayed correctly on the details page
    Given I am on the Systems overview page of this "sle15sp4_minion"
    Then the hostname for "sle15sp4_minion" should be correct
    And the kernel for "sle15sp4_minion" should be correct
    And the OS version for "sle15sp4_minion" should be correct
    And the IPv4 address for "sle15sp4_minion" should be correct
    And the IPv6 address for "sle15sp4_minion" should be correct
    And the system ID for "sle15sp4_minion" should be correct
    And the system name for "sle15sp4_minion" should be correct
    And the uptime for "sle15sp4_minion" should be correct
    And I should see several text fields

  Scenario: Install a patch on the Salt minion
    When I follow "Software" in the content area
    And I follow "Patches" in the content area
    When I check the first patch in the list
    And I click on "Apply Patches"
    And I click on "Confirm"
    Then I should see a "1 patch update has been scheduled for" text
    And I wait until event "Patch Update:" is completed

  Scenario: Install a package on the Salt minion
    When I follow "Software" in the content area
    And I follow "Install"
    And I enter the package for "sle15sp4_minion" as the filtered package name
    And I click on the filter button
    And I check the package for "sle15sp4_minion" in the list
    And I click on "Install Selected Packages"
    And I click on "Confirm"
    Then I should see a "1 package install has been scheduled for" text
    And I wait until event "Package Install/Upgrade scheduled by admin" is completed
  
  Scenario: Remove package from the Salt minion
    When I follow "Software" in the content area
    And I follow "List / Remove"
    And I enter the package for "sle_minion" as the filtered package name
    And I click on the filter button
    And I check the package for "sle_minion" in the list
    And I click on "Remove Packages"
    And I click on "Confirm"
    Then I should see a "1 package removal has been scheduled for" text
    And I wait until event "Package Removal scheduled by admin" is completed

  Scenario: Run a remote command on Salt minion
    When I follow the left menu "Salt > Remote Commands"
    Then I should see a "Remote Commands" text in the content area
    When I enter command "echo 'My remote command output'"
    And I enter the hostname of "sle15sp4_minion" as "target"
    And I click on preview
    Then I should see a "Target systems (1)" text
    When I wait until I do not see "pending" text
    And I click on run
    And I wait until I see "show response" text
    And I expand the results for "sle15sp4_minion"
    Then I should see "My remote command output" in the command output for "sle15sp4_minion"

  Scenario: Check that Software package refresh works on a Salt minion
    Given I am on the Systems overview page of this "sle15sp4_minion"
    When I follow "Software" in the content area
    And I click on "Update Package List"
    And I wait until event "Package List Refresh scheduled by admin" is completed

  Scenario: Check that Hardware Refresh button works on a Salt minion
    When I follow "Details" in the content area
    And I follow "Hardware"
    And I click on "Schedule Hardware Refresh"
    Then I should see a "You have successfully scheduled a hardware profile refresh" text
    And I wait until event "Hardware List Refresh scheduled by admin" is completed

  Scenario: Create a configuration channel named "Pod Proxy Channel"
    When I follow the left menu "Configuration > Channels"
    And I follow "Create Config Channel"
    And I enter "Pod Proxy Channel" as "cofName"
    And I enter "podproxychannel" as "cofLabel"
    And I enter "This is a configuration channel for different system types" as "cofDescription"
    And I click on "Create Config Channel"
    Then I should see a "Pod Proxy Channel" text

  Scenario: Add a configuration file into the "Pod Proxy Channel" configuration channel
    When I follow the left menu "Configuration > Channels"
    And I follow "Pod Proxy Channel"
    And I follow "Create Configuration File or Directory"
    And I enter "/etc/s-mgr/config" as "cffPath"
    And I enter "COLOR=white" in the editor
    And I click on "Create Configuration File"
    Then I should see a "Revision 1 of /etc/s-mgr/config from channel Pod Proxy Channel" text

  Scenario: Subscribe a Salt minion to the configuration channel
    Given I am on the Systems overview page of this "sle15sp4_minion"
    When I follow "Configuration" in the content area
    And I follow "Manage Configuration Channels" in the content area
    And I follow first "Subscribe to Channels" in the content area
    And I check "Pod Proxy Channel" in the list
    And I click on "Continue"
    And I click on "Update Channel Rankings"
    Then I should see a "Channel Subscriptions successfully changed for" text

  Scenario: Deploy the configuration file to Salt minion
    And I follow the left menu "Configuration > Channels"
    And I follow "Pod Proxy Channel"
    And I follow "Deploy all configuration files to selected subscribed systems"
    And I enter the hostname of "sle15sp4_minion" as the filtered system name
    And I click on the filter button
    And I check the "sle15sp4_minion" client
    And I click on "Confirm & Deploy to Selected Systems"
    Then I should see a "/etc/s-mgr/config" link
    When I click on "Deploy Files to Selected Systems"
    Then I should see a "being scheduled" text
    And I should see a "0 revision-deploys overridden." text
    And I wait until file "/etc/s-mgr/config" exists on "sle15sp4_minion"
    Then file "/etc/s-mgr/config" should contain "COLOR=white" on "sle15sp4_minion"

  Scenario: Reboot the Salt minion and wait until reboot is completed
    Given I am on the Systems overview page of this "sle15sp4_minion"
    When I follow first "Schedule System Reboot"
    Then I should see a "System Reboot Confirmation" text
    And I should see a "Reboot system" button
    When I click on "Reboot system"
    Then I should see a "Reboot scheduled for system" text
    And I wait at most 600 seconds until event "System reboot scheduled by admin" is completed
    Then I should see a "This action's status is: Completed" text

  Scenario: Cleanup: Unregister a Salt minion in the Containerized Proxy
    Given I am on the Systems overview page of this "sle15sp4_minion"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    And I wait until Salt client is inactive on "sle15sp4_minion"
    Then "sle15sp4_minion" should not be registered

  Scenario: Cleanup: Unregister Containerized Proxy
    When I follow the left menu "Systems > System List > All"
    And I follow this "containerized_proxy" link
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "containerized_proxy" should not be registered

  Scenario: Cleanup: Stop Containerized Proxy services
    When I stop the "uyuni-proxy-pod" service on "proxy"

  Scenario: Cleanup: Remove Containerized Proxy configuration
    When I ensure folder "/etc/uyuni/proxy/*" doesn't exist on "proxy"
    And I remove "/tmp/proxy_container_config.zip" from "proxy"
    And I remove "/tmp/proxy_container_config.zip" from "server"

  Scenario: Cleanup: Remove "Pod Proxy Channel" configuration channel
    When I follow the left menu "Configuration > Channels"
    And I follow "Pod Proxy Channel"
    And I follow "Delete Channel"
    And I click on "Delete Config Channel"
    Then I should see a "Channel 'Pod Proxy Channel' has been deleted" text

  Scenario: Cleanup: Start traditional proxy service
    When I start salt-minion on "proxy"
    And I run "spacewalk-proxy start" on "proxy"
    # workaround for bsc#1205976
    And I start the "tftp" service on "proxy"
    And I wait until "squid" service is active on "proxy"
    And I wait until "apache2" service is active on "proxy"
    And I wait until "jabberd" service is active on "proxy"
    And I wait until "tftp" service is active on "proxy"

  Scenario: Cleanup: Bootstrap a Salt minion in the traditional proxy
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "sle15sp4_minion" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select the hostname of "proxy" from "proxies"
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text

  Scenario: Cleanup: Check the new bootstrapped minion in System Overview page
    When I follow the left menu "Salt > Keys"
    And I wait until I do not see "Loading..." text
    Then I should see a "accepted" text
    When I follow the left menu "Systems > System List > All"
    And I wait until I see the name of "sle15sp4_minion", refreshing the page
    And I wait until onboarding is completed for "sle15sp4_minion"
    Then the Salt master can reach "sle15sp4_minion"
