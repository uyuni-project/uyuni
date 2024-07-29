# Copyright (c) 2017-2022 SUSE LLC
# Licensed under the terms of the MIT license.
#
# The scenarios in this feature are skipped if there is no proxy
# ($proxy is nil)
#
# Alternative: Bootstrap the proxy as a Salt minion from script

@scope_proxy
@proxy
Feature: Setup SUSE Manager proxy
  In order to use a proxy with the SUSE Manager server
  As the system administrator
  I want to register the proxy to the server

  Scenario: Install proxy software
    When I refresh the metadata for "proxy"
    And I install "SUSE-Manager-Proxy" product on the proxy
    And I install proxy pattern on the proxy
    And I let squid use avahi on the proxy

@skip_if_salt_bundle
  Scenario: Create the bootstrap script for the proxy and use it
    When I execute mgr-bootstrap "--activation-keys=1-PROXY-KEY-x86_64 --script=bootstrap-proxy.sh --no-up2date"
    Then I should get "* bootstrap script (written):"
    And I should get "    '/srv/www/htdocs/pub/bootstrap/bootstrap-proxy.sh'"
    When I fetch "pub/bootstrap/bootstrap-proxy.sh" to "proxy"
    And I run "sh ./bootstrap-proxy.sh" on "proxy"

@salt_bundle
  Scenario: Create the bundle-aware bootstrap script for the proxy and use it
    When I execute mgr-bootstrap "--activation-keys=1-PROXY-KEY-x86_64 --script=bootstrap-proxy.sh --no-up2date --force-bundle"
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

  Scenario: Use correct kernel image on the proxy
    When I remove package "kernel-default-base" from this "proxy"
    And I install package "kernel-default" on this "proxy"

  Scenario: Reboot the proxy to use the new kernel
    When I reboot the "proxy" host through SSH, waiting until it comes back

@susemanager
  Scenario: Check proxy system details
    When I am on the Systems overview page of this "proxy"
    Then I should see "proxy" hostname
    When I wait until I see "SUSE Manager Proxy" text, refreshing the page
    Then I should see a "Proxy" link in the content area

@uyuni
  Scenario: Check proxy system details
    When I am on the Systems overview page of this "proxy"
    Then I should see "proxy" hostname
    And I should see a "Proxy" link in the content area

  Scenario: Cleanup: remove proxy bootstrap scripts
    When I run "rm /srv/www/htdocs/pub/bootstrap/bootstrap-proxy.sh" on "server"
    And I run "rm /root/bootstrap-proxy.sh" on "proxy"

@uyuni
  Scenario: Assign the correct channels to the proxy
    Given I am on the Systems overview page of this "proxy"
    When I follow "Software" in the content area
    And I follow "Software Channels" in the content area
    And I wait until I do not see "Loading..." text
    And I check radio button "openSUSE Leap 15.4 (x86_64)"
    And I wait until I do not see "Loading..." text
    And I check "openSUSE 15.4 non oss (x86_64)"
    And I check "openSUSE Leap 15.4 non oss Updates (x86_64)"
    And I check "openSUSE Leap 15.4 Updates (x86_64)"
    And I check "Update repository of openSUSE Leap 15.4 Backports (x86_64)"
    And I check "Update repository with updates from SUSE Linux Enterprise 15 for openSUSE Leap 15.4 (x86_64)"
    And I check "Uyuni Client Tools for openSUSE Leap 15.4 (x86_64) (Development)"
    And I check "Uyuni Proxy Devel for openSUSE Leap 15.4 (x86_64)"
    And I click on "Next"
    Then I should see a "Confirm Software Channel Change" text
    When I click on "Confirm"
    Then I should see a "Changing the channels has been scheduled." text
    When I follow "scheduled" in the content area
    And I wait until I see "1 system successfully completed this action." text, refreshing the page
