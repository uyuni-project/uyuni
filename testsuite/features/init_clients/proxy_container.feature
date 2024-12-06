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

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap the proxy host as a salt minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "proxy" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-PROXY-KEY-x86_64" from "activationKeys"
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text

  @skip_if_cloud
  Scenario: Reboot the proxy host
    When I reboot the "proxy" host through SSH, waiting until it comes back

  Scenario: Wait until the proxy host appears
    When I wait until onboarding is completed for "proxy"

  Scenario: Generate containerized proxy configuration
    When I generate the configuration "/tmp/proxy_container_config.tar.gz" of containerized proxy on the server
    And I copy the configuration "/tmp/proxy_container_config.tar.gz" of containerized proxy from the server to the proxy

  Scenario: Set up the containerized proxy service to support Avahi
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

  Scenario: The containerized proxy should be registered automatically
    When I follow the left menu "Systems"
    And I wait until I see the name of "proxy", refreshing the page

  Scenario: Check if Monitoring can be enabled on the containerized proxy
    Given I am on the Systems overview page of this "proxy"
    When I follow "Properties" in the content area
    Then the "monitoring_entitled" checkbox should be disabled
