# Copyright (c) 2017-2018 SUSE LLC
# Licensed under the terms of the MIT license.
#
# The scenarios in this feature are skipped if there is no proxy
# ($proxy is nil)
#
# Alternative: Bootstrap the proxy as a Salt minion from script

Feature: Setup SUSE Manager proxy
  In order to use a proxy with the SUSE manager server
  As the system administrator
  I want to register the proxy to the server

@proxy
  Scenario: Create the bootstrap script for the proxy and use it
    When I execute mgr-bootstrap "--script=bootstrap-proxy.sh --no-up2date"
    Then I should get "* bootstrap script (written):"
    And I should get "    '/srv/www/htdocs/pub/bootstrap/bootstrap-proxy.sh'"
    When I fetch "pub/bootstrap/bootstrap-proxy.sh" to "proxy"
    And I run "sh ./bootstrap-proxy.sh" on "proxy"

@proxy
  Scenario: Accept the key for the proxy
    Given I am authorized as "testing" with password "testing"
    When I go to the minion onboarding page
    Then I should see a "pending" text
    When I accept "proxy" key
    And I wait until onboarding is completed for "proxy"

@proxy
  Scenario: Detect latest Salt changes on the proxy
    When I query latest Salt changes on "proxy"

@proxy
  Scenario: Copy the keys and configure the proxy
    When I copy server's keys to the proxy
    And I configure the proxy
    Then I should see "proxy" in spacewalk

@proxy
  Scenario: Check proxy system details
    When I am on the Systems overview page of this "proxy"
    Then I should see "proxy" hostname
    And I wait until I see "SUSE Manager Proxy" text, refreshing the page

@proxy
  Scenario: Cleanup: remove proxy bootstrap scripts
    When I run "rm /srv/www/htdocs/pub/bootstrap/bootstrap-proxy.sh" on "server"
    And I run "rm /root/bootstrap-proxy.sh" on "proxy"
