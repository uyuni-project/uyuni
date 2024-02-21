# Copyright (c) 2024 SUSE LLC
# Licensed under the terms of the MIT license.
#
# The scenarios in this feature are skipped if:
# * there is no proxy ($proxy is nil)
# * there is no scope @scope_containerized_proxy
#
# Bootstrap the proxy as a Pod

@scope_containerized_proxy
@proxy
Feature: Setup Containerized Proxy
  In order to use a Containerized Proxy with the server
  As the system administrator
  I want to register the Containerized Proxy on the server

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

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
    And I wait until I see the name of "proxy", refreshing the page

  Scenario: Remove the offending key in salt known hosts
    When I remove offending SSH key of "proxy" at port "8022" for "/var/lib/salt/.ssh/known_hosts" on "server"
