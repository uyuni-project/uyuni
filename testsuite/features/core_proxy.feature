# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.
#
# The scenarios in this feature are skipped if there is no proxy
# ($proxy is nil)

Feature: Setup SUSE Manager proxy
  In order to use a proxy with the SUSE manager server
  As the system administrator
  I want to register the proxy to the server

@proxy
  Scenario: Create the bootstrap script for the proxy
    When I execute mgr-bootstrap "--script=bootstrap-proxy.sh --no-up2date --traditional"
    Then I should get "* bootstrap script (written):"
     And I should get "    '/srv/www/htdocs/pub/bootstrap/bootstrap-proxy.sh'"

@proxy
  Scenario: Register the proxy, copy the keys and configure the proxy
    When I fetch "pub/bootstrap/bootstrap-proxy.sh" to "proxy"
    And I run "sh ./bootstrap-proxy.sh" on "proxy"
    And I copy server's keys to the proxy
    And I configure the proxy
    Then I should see "proxy" in spacewalk

@proxy
  Scenario: Check proxy system details
    When I am on the Systems overview page of this "proxy"
    Then I should see "proxy" hostname
    And I should see a "$PRODUCT Proxy" text

@proxy
  Scenario: Cleanup: remove proxy bootstrap scripts
   Then I run "rm /srv/www/htdocs/pub/bootstrap/bootstrap-proxy.sh" on "server"
   And I run "rm /root/bootstrap-proxy.sh" on "proxy"
