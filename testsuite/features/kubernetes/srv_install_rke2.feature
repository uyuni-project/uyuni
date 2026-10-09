# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@rke2
@no_user_creation
@skip_if_external_cluster
Feature: Install RKE2 server on a transactional system

  @transactional_server
  Scenario: Reboot the server to activate everything before starting
    When I reboot the "server" host through SSH, waiting until it comes back

  Scenario: Install RKE2
    When I install RKE2 on "server"

  @skip_if_transactional_server
  Scenario: Install selinux package
    When I install packages "rke2-selinux" on this "server"

  @transactional_server
  Scenario: Reboot the server to activate the transaction with the RKE2 content
    When I reboot the "server" host through SSH, waiting until it comes back

  Scenario: Enable and start the RKE2 server service
    When I enable the "rke2-server" service on "server"
    And I start the "rke2-server" service on "server"
    And I wait until "rke2-server" service is active on "server"
    Then service "rke2-server" is enabled on "server"
    And service "rke2-server" is active on "server"

  Scenario: Create symlinks for RKE2 tools
    When I create a ln between "/var/lib/rancher/rke2/bin/kubectl" and "/usr/local/bin/kubectl" on "server" with parameters "-sf"
    And I create a ln between "/var/lib/rancher/rke2/bin/crictl" and "/usr/local/bin/crictl" on "server" with parameters "-sf"
    And I create a ln between "/var/lib/rancher/rke2/bin/ctr" and "/usr/local/bin/ctr" on "server" with parameters "-sf"
