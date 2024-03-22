# Copyright (c) 2024 SUSE LLC
# Licensed under the terms of the MIT license.
#
# The scenarios in this feature are skipped if:
# * there is no proxy ($proxy is nil)
# * there is no scope @scope_containerized_proxy
#
# Bootstrap the proxy as a Pod

@proxy
Feature: Bootstrap containerized proxy

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Bootstrap the proxy host as a salt minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "proxy-ssh" as "hostname"
    And I enter "22" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-PROXY-KEY-x86_64" from "activationKeys"
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text

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