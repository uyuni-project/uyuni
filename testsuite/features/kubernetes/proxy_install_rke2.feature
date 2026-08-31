# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@transactional_server
@rke2
@no_user_creation
Feature: Install RKE2 proxy on a transactional system

  Scenario: Reboot the proxy to activate everything before starting
    When I reboot the "proxy" host through SSH, waiting until it comes back

  Scenario: Check the RKE2 configuration
    Then the environment variable "RKE2_VERSION" is set on "proxy"
    And file "/etc/rancher/rke2/config.yaml" should exist on "proxy"

  Scenario: Install RKE2 via RPM method
    When I run "set -o pipefail; curl -sfL https://get.rke2.io | sudo INSTALL_RKE2_VERSION=$RKE2_VERSION INSTALL_RKE2_METHOD=rpm sh -" on "proxy"

  Scenario: Reboot the proxy to activate the transaction with the RKE2 content
    When I reboot the "proxy" host through SSH, waiting until it comes back

  Scenario: Enable and start the RKE2 proxy service
    When I enable the "rke2-server" service on "proxy"
    And I start the "rke2-server" service on "proxy"
    And I wait until "rke2-server" service is active on "proxy"
    Then service "rke2-server" is enabled on "proxy"
    And service "rke2-server" is active on "proxy"

  Scenario: Create symlinks for RKE2 tools
    When I run "ln -sf /var/lib/rancher/rke2/bin/kubectl /usr/local/bin/kubectl" on "proxy"
    And I run "ln -sf /var/lib/rancher/rke2/bin/crictl /usr/local/bin/crictl" on "proxy"
    And I run "ln -sf /var/lib/rancher/rke2/bin/ctr /usr/local/bin/ctr" on "proxy"

