# Copyright (c) 2024 SUSE LLC
# Licensed under the terms of the MIT license.
#
# The scenarios in this feature are skipped if:
# * there is no proxy ($proxy is nil)
#
# Bootstrap the proxy as a Pod

@proxy
Feature: Setup containerized proxy
  In order to use a containerized proxy with the server
  As the system administrator
  I want to register the containerized proxy on the server

  Scenario: Check pod and container statuses
    When I get the contents of the remote file "/tmp/podman-proxy.log" from "server"
    Then it should contain a "uyuni-proxy-test-status: Running" text
    And it should contain a "proxy-http-status: running" text
    And it should contain a "proxy-ssh-status: running" text
    And it should contain a "proxy-squid-status: running" text
    And it should contain a "proxy-salt-broker-status: running" text
    And it should contain a "proxy-tftpd-status: running" text
    And it should contain a "uyuni-proxy-test-containers: 6" text

  # Because of https://github.com/uyuni-project/uyuni/pull/8520 the rest is skipped.
  @skip
  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  @skip
  Scenario: Create an activation key for the Proxy
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I wait until I do not see "Loading..." text
    And I enter "Proxy Key x86_64" as "description"
    And I enter "PROXY-KEY-x86_64" as "key"
    And I click on "Create Activation Key"
    Then I should see a "Activation key Proxy Key x86_64 has been created" text

  @skip
  Scenario: Bootstrap the proxy host as a salt minion
    When I follow the left menu "Systems > Bootstrapping"
    Then I should see a "Bootstrap Minions" text
    When I enter the hostname of "uyuni-proxy-test" as "hostname"
    And I enter "8022" as "port"
    And I enter "root" as "user"
    And I enter "linux" as "password"
    And I select "1-PROXY-KEY-x86_64" from "activationKeys"
    And I click on "Bootstrap"
    And I wait until I see "Bootstrap process initiated." text

  @skip
  Scenario: Wait until the proxy host appears
    When I wait until onboarding is completed for "uyuni-proxy-test"

  @skip
  Scenario: Set-up the containerized proxy service to support Avahi
    When I add avahi hosts in containerized proxy configuration

  @skip
  Scenario: Wait until containerized proxy service is active
    And I wait until "uyuni-proxy-pod" service is active on "uyuni-proxy-test"
    And I wait until "uyuni-proxy-httpd" service is active on "uyuni-proxy-test"
    And I wait until "uyuni-proxy-salt-broker" service is active on "uyuni-proxy-test"
    And I wait until "uyuni-proxy-squid" service is active on "uyuni-proxy-test"
    And I wait until "uyuni-proxy-ssh" service is active on "uyuni-proxy-test"
    And I wait until "uyuni-proxy-tftpd" service is active on "uyuni-proxy-test"
    And I wait until port "8022" is listening on "uyuni-proxy-test" container
    And I wait until port "80" is listening on "uyuni-proxy-test" container
    And I wait until port "443" is listening on "uyuni-proxy-test" container
    And I visit "uyuni-Proxy-test" endpoint of this "uyuni-proxy-test"

  @skip
  Scenario: containerized proxy should be registered automatically
    When I follow the left menu "Systems"
    And I wait until I see the name of "uyuni-proxy-test", refreshing the page
