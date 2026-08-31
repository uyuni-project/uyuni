# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@transactional_server
@rke2
@no_user_creation
Feature: Install RKE2 on transactional systems

  Scenario: Reboot the server to activate everything before starting
    When I reboot the "server" host through SSH, waiting until it comes back

  Scenario: Check the RKE2 configuration
    Then the environment variable "RKE2_VERSION" is set on "server"
    And file "/etc/rancher/rke2/config.yaml" should exist on "server"

  Scenario: Install RKE2 via RPM method
    When I run "set -o pipefail; curl -sfL https://get.rke2.io | sudo INSTALL_RKE2_VERSION=$RKE2_VERSION INSTALL_RKE2_METHOD=rpm sh -" on "server"

  Scenario: Reboot the server to activate the transaction with the RKE2 content
    When I reboot the "server" host through SSH, waiting until it comes back

  Scenario: Enable and start the RKE2 server service
    When I enable the "rke2-server" service on "server"
    And I start the "rke2-server" service on "server"
    And I wait until "rke2-server" service is active on "server"
    Then service "rke2-server" is enabled on "server"
    And service "rke2-server" is active on "server"

  Scenario: Create symlinks for RKE2 tools
    When I run "ln -sf /var/lib/rancher/rke2/bin/kubectl /usr/local/bin/kubectl" on "server"
    And I run "ln -sf /var/lib/rancher/rke2/bin/crictl /usr/local/bin/crictl" on "server"
    And I run "ln -sf /var/lib/rancher/rke2/bin/ctr /usr/local/bin/ctr" on "server"

