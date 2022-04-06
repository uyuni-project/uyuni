# Copyright (c) 2017-2021 SUSE LLC
# Licensed under the terms of the MIT license.
#
# The scenarios in this feature are skipped if there is no proxy
# ($proxy is nil)
#
# Alternative: Bootstrap the proxy as a Salt minion from script

@scope_proxy
@proxy
Feature: Setup Uyuni proxy
  In order to use a proxy with the Uyuni server
  As the system administrator
  I want to register the proxy to the server

  Scenario: Install proxy software
    When I refresh the metadata for "proxy"
    # workaround for bug 1192195, remove when fixed
    And I adapt zyppconfig
    # uncomment when product is out:
    # When I install "SUSE-Manager-Proxy" product on the proxy
    And I install proxy pattern on the proxy
    And I let squid use avahi on the proxy

  @skip_if_salt_bundle
  Scenario: Create the bootstrap script for the proxy and use it
    When I execute mgr-bootstrap "--script=bootstrap-proxy.sh --no-up2date"
    Then I should get "* bootstrap script (written):"
    And I should get "    '/srv/www/htdocs/pub/bootstrap/bootstrap-proxy.sh'"
    When I fetch "pub/bootstrap/bootstrap-proxy.sh" to "proxy"
    And I run "sh ./bootstrap-proxy.sh" on "proxy"

  @salt_bundle
  Scenario: Create the bootstrap script for the proxy and use it
    When I execute mgr-bootstrap "--script=bootstrap-proxy.sh --no-up2date --force-bundle"
    Then I should get "* bootstrap script (written):"
    And I should get "    '/srv/www/htdocs/pub/bootstrap/bootstrap-proxy.sh'"
    When I fetch "pub/bootstrap/bootstrap-proxy.sh" to "proxy"
    And I run "sh ./bootstrap-proxy.sh" on "proxy"

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Accept the key for the proxy
    When I follow the left menu "Salt > Keys"
    And I wait until I see "pending" text, refreshing the page
    And I accept "proxy" key

  Scenario: Wait until the proxy appears
    When I wait until onboarding is completed for "proxy"

  Scenario: Detect latest Salt changes on the proxy
    When I query latest Salt changes on "proxy"

  Scenario: Copy the keys and configure the proxy
    When I copy server's keys to the proxy
    And I configure the proxy
    Then I should see "proxy" via spacecmd
    And service "salt-broker" is active on "proxy"

@susemanager
  Scenario: Check proxy system details
    When I am on the Systems overview page of this "proxy"
    Then I should see "proxy" hostname
    # TODO: uncomment when SCC product becomes available
    # When I wait until I see "SUSE Manager Proxy" text, refreshing the page
    Then I should see a "Proxy" link in the content area

@uyuni
  Scenario: Check proxy system details
    When I am on the Systems overview page of this "proxy"
    Then I should see "proxy" hostname
    # TODO: uncomment when SCC product becomes available
    # When I wait until I see "Uyuni Proxy" text, refreshing the page
    Then I should see a "Proxy" link in the content area

@skip_if_cloud
  Scenario: Install expect package on proxy for bootstrapping minion via script
    When I enable repositories before installing branch server
    And I install package "expect" on this "proxy"
    And I disable repositories after installing branch server

  Scenario: Cleanup: remove proxy bootstrap scripts
    When I run "rm /srv/www/htdocs/pub/bootstrap/bootstrap-proxy.sh" on "server"
    And I run "rm /root/bootstrap-proxy.sh" on "proxy"
