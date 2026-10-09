# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@rke2
@no_user_creation
@skip_if_external_cluster
Feature: Install RKE2 proxy on a transactional system

@transactional_server
  Scenario: Reboot the proxy to activate everything before starting
    When I reboot the "proxy" host through SSH, waiting until it comes back

  Scenario: Install RKE2
    When I install RKE2 on "proxy"

  @skip_if_transactional_server
  Scenario: Install selinux package
    When I install packages "rke2-selinux" on this "proxy"

  @transactional_server
  Scenario: Reboot the proxy to activate the transaction with the RKE2 content
    When I reboot the "proxy" host through SSH, waiting until it comes back

  Scenario: Enable and start the RKE2 proxy service
    When I enable the "rke2-server" service on "proxy"
    And I start the "rke2-server" service on "proxy"
    And I wait until "rke2-server" service is active on "proxy"
    Then service "rke2-server" is enabled on "proxy"
    And service "rke2-server" is active on "proxy"

  Scenario: Create symlinks for RKE2 tools
    When I create a ln between "/var/lib/rancher/rke2/bin/kubectl" and "/usr/local/bin/kubectl" on "proxy" with parameters "-sf"
    And I create a ln between "/var/lib/rancher/rke2/bin/crictl" and "/usr/local/bin/crictl" on "proxy" with parameters "-sf"
    And I create a ln between "/var/lib/rancher/rke2/bin/ctr" and "/usr/local/bin/ctr" on "proxy" with parameters "-sf"
