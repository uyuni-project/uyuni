# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.
#
# The scenarios in this feature are skipped if:
# * there is no proxy3 ($proxy3 is nil)
# * there is no scope @scope_containerized_proxy3
#
# Bootstrap the proxy3 as a Pod

@scope_hub
@peripheral2
@proxy3
Feature: Setup containerized proxy3 on peripheral2
  In order to use a containerized proxy3 with peripheral2
  As the system administrator
  I want to register the containerized proxy3 on peripheral2

  Scenario: Clean up sumaform leftovers on the containerized proxy3
    When I perform a full salt minion cleanup on "proxy3"

@transactional_proxy3
  Scenario: Reboot after clean up
    When I reboot the "proxy3" host through SSH, waiting until it comes back

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section on "peripheral2"

  Scenario: Bootstrap the proxy3 host as a salt minion of peripheral2
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "proxy3" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-proxy3_key" from "activationKeys"
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text

@transactional_proxy3
  Scenario: Reboot the proxy3 host
    When I reboot the "proxy3" minion through the web UI

  Scenario: Wait until the proxy3 host appears
    When I wait until onboarding is completed for "proxy3"

  Scenario: Upgrade mgrpxy tool
    When I upgrade "mgrpxy" on "proxy3" using the API from peripheral2

@transactional_proxy3
  Scenario: Reboot after mgrpxy upgrade
    When I reboot the "proxy3" minion through the web UI

  Scenario: Generate containerized proxy3 configuration
    When I generate the configuration "/tmp/proxy3_container_config.tar.gz" of containerized proxy3 on the peripheral2
    And I copy the configuration "/tmp/proxy3_container_config.tar.gz" of containerized proxy from the peripheral2 to the proxy3

  Scenario: Set up the containerized proxy3 service to support Avahi
    When I add avahi hosts in containerized proxy3 configuration

  Scenario: Run a containerized proxy3
    When I run "mgrpxy install podman /tmp/proxy3_container_config.tar.gz" on "proxy3"

  Scenario: Wait until containerized proxy3 service is active
    And I wait until "uyuni-proxy-pod" service is active on "proxy3"
    And I wait until "uyuni-proxy-httpd" service is active on "proxy3"
    And I wait until "uyuni-proxy-salt-broker" service is active on "proxy3"
    And I wait until "uyuni-proxy-squid" service is active on "proxy3"
    And I wait until "uyuni-proxy-ssh" service is active on "proxy3"
    And I wait until "uyuni-proxy-tftpd" service is active on "proxy3"
    And I wait until port "8022" is listening on "proxy3" container
    And I wait until port "80" is listening on "proxy3" container
    And I wait until port "443" is listening on "proxy3" container
    And I visit "Proxy" endpoint of this "proxy3"

  Scenario: The containerized proxy3 should be registered automatically
    When I follow the left menu "Systems"
    And I wait until I see the name of "proxy3", refreshing the page
