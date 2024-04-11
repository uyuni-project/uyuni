# Copyright (c) 2024 SUSE LLC
# Licensed under the terms of the MIT license.
#
# The scenarios in this feature are skipped if:
# * there is no proxy ($proxy is nil)
# * there is no scope @scope_containerized_proxy
#
# Bootstrap the proxy as a Pod

@containerized_server
@scope_containerized_proxy
@proxy
Feature: Setup containerized proxy
  In order to use a containerized proxy with the server
  As the system administrator
  I want to register the containerized proxy on the server

  Scenario: Clean up sumaform leftovers on the containerized proxy
    When I perform a full salt minion cleanup on "proxy"
    And I reboot the "proxy" host through SSH, waiting until it comes back

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

# TODO: Remove this tag, once bsc#1222628 is fixed
@skip
  Scenario: Bootstrap the proxy host as a salt minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "proxy" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-proxy_container_key" from "activationKeys"
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text

# TODO: Remove this tag, once bsc#1222628 is fixed
@skip
  Scenario: Reboot the proxy host
    When I reboot the "proxy" host through SSH, waiting until it comes back

# TODO: Remove this tag, once bsc#1222628 is fixed
@skip
  Scenario: Wait until the proxy host appears
    When I wait until onboarding is completed for "proxy"

  Scenario: Generate containerized proxy configuration
    When I generate the configuration "/tmp/proxy_container_config.tar.gz" of containerized proxy on the server
    And I copy the configuration "/tmp/proxy_container_config.tar.gz" of containerized proxy from the server to the proxy

  Scenario: Set-up the containerized proxy service to support Avahi
    When I add avahi hosts in containerized proxy configuration

  Scenario: Run a containerized proxy
    When I run "mgrpxy install podman /tmp/proxy_container_config.tar.gz" on "proxy"

  Scenario: Wait until containerized proxy service is active
    And I wait until "uyuni-proxy-pod" service is active on "proxy"
    And I wait until "uyuni-proxy-httpd" service is active on "proxy"
    And I wait until "uyuni-proxy-salt-broker" service is active on "proxy"
    And I wait until "uyuni-proxy-squid" service is active on "proxy"
    And I wait until "uyuni-proxy-ssh" service is active on "proxy"
    And I wait until "uyuni-proxy-tftpd" service is active on "proxy"
    And I wait until port "8022" is listening on "proxy" container
    And I wait until port "80" is listening on "proxy" container
    And I wait until port "443" is listening on "proxy" container
    And I visit "Proxy" endpoint of this "proxy"

  Scenario: containerized proxy should be registered automatically
    When I follow the left menu "Systems"
    And I wait until I see the name of "proxy", refreshing the page
