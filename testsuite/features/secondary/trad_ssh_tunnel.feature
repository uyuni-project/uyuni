# Copyright (c) 2016-2021 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_traditional_client
Feature: Register a traditional system to be managed via SSH push

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Delete the traditional client for SSH tunnel bootstrap
    Given I am on the Systems overview page of this "sle_client"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "sle_client" should not be registered

  Scenario: Create bootstrap script for traditional SSH push via tunnel
    When I execute mgr-bootstrap "--activation-keys=1-SUSE-SSH-TUNNEL-KEY-x86_64 --script=bootstrap-ssh-push-tunnel.sh --no-up2date --traditional"
    Then I should get "* bootstrap script (written):"
    And I should get "    '/srv/www/htdocs/pub/bootstrap/bootstrap-ssh-push-tunnel.sh'"

  Scenario: Register this client for SSH push via tunnel
    When I register this client for SSH push via tunnel
    Then I should see "sle_ssh_tunnel_client" via spacecmd

  Scenario: Check this client's contact method
    Given I am on the Systems overview page of this "sle_ssh_tunnel_client"
    Then I should see a "Push via SSH tunnel" text

  Scenario: Cleanup: delete the traditional SSH push client
    Given I am on the Systems overview page of this "sle_ssh_tunnel_client"
    When I follow "Delete System"
    Then I should see a "Confirm System Profile Deletion" text
    When I click on "Delete Profile"
    And I wait until I see "has been deleted" text
    Then "sle_ssh_tunnel_client" should not be registered

  Scenario: Cleanup: hosts file of traditional client via SSH tunnel
    Given I am on the Systems page
    And I run "sed -i '/127.0.1.1/d' /etc/hosts" on "sle_ssh_tunnel_client"
    And I run "rm /srv/www/htdocs/pub/bootstrap/bootstrap-ssh-push-tunnel.sh" on "server"
    And I remove server hostname from hosts file on "sle_ssh_tunnel_client"

  Scenario: Cleanup: register a traditional client after SSH tunnel tests
    When I bootstrap traditional client "sle_client" using bootstrap script with activation key "1-SUSE-KEY-x86_64" from the proxy
    Then I should see "sle_client" via spacecmd
