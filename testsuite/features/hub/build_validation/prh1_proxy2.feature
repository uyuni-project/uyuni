# Copyright (c) 2024-2026 SUSE LLC
# Licensed under the terms of the MIT license.
#
# The scenarios in this feature are skipped if:
# * there is no proxy2 ($proxy2 is nil)
# * there is no scope @scope_containerized_proxy2
#
# Bootstrap the proxy2 as a Pod

@scope_hub
@server2
@proxy2
Feature: Setup containerized proxy2
  In order to use a containerized proxy2 with the server
  As the system administrator
  I want to register the containerized proxy2 on the server

  Scenario: Clean up sumaform leftovers on the containerized proxy2
    When I perform a full salt minion cleanup on "proxy2"

@transactional_proxy2
  Scenario: Reboot after clean up
    When I reboot the "proxy2" host through SSH, waiting until it comes back

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section on "server2"

  Scenario: Bootstrap the proxy2 host as a salt minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "proxy2" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-proxy2_key" from "activationKeys"
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text

@transactional_proxy2
  Scenario: Reboot the proxy2 host
    When I reboot the "proxy2" host through SSH, waiting until it comes back

  Scenario: Wait until the proxy2 host appears
    When I wait until onboarding is completed for "proxy2"

  Scenario: Upgrade mgrpxy tool
    When I upgrade "mgrpxy" on "proxy2" using the API from server2

@transactional_proxy2
  Scenario: Reboot after mgrpxy upgrade
    When I reboot the "proxy2" minion through the web UI

  Scenario: Generate containerized proxy2 configuration
    When I generate the configuration "/tmp/proxy2_container_config.tar.gz" of containerized proxy2 on the server2
    And I copy the configuration "/tmp/proxy2_container_config.tar.gz" of containerized proxy from the server2 to the proxy2

  Scenario: Set up the containerized proxy2 service to support Avahi
    When I add avahi hosts in containerized proxy configuration

  Scenario: Run a containerized proxy2
    When I run "mgrpxy install podman /tmp/proxy2_container_config.tar.gz" on "proxy2"

  Scenario: Wait until containerized proxy2 service is active
    And I wait until "uyuni-proxy-pod" service is active on "proxy2"
    And I wait until "uyuni-proxy-httpd" service is active on "proxy2"
    And I wait until "uyuni-proxy-salt-broker" service is active on "proxy2"
    And I wait until "uyuni-proxy-squid" service is active on "proxy2"
    And I wait until "uyuni-proxy-ssh" service is active on "proxy2"
    And I wait until "uyuni-proxy-tftpd" service is active on "proxy2"
    And I wait until port "8022" is listening on "proxy2" container
    And I wait until port "80" is listening on "proxy2" container
    And I wait until port "443" is listening on "proxy2" container
    And I visit "Proxy" endpoint of this "proxy2"

  Scenario: The containerized proxy2 should be registered automatically
    When I follow the left menu "Systems"
    And I wait until I see the name of "proxy2", refreshing the page
