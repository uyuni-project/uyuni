# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.
#
# Replaces the sumaform salt/kubernetes_common/install_rke2.sls state for SL-Micro 6.2.
# Before running this feature, the SCC_SLMICRO_REGCODE environment variable must be
# available on the server node so the SCC registration step can resolve it.

Feature: Install RKE2 on SL-Micro 6.2
  In order to deploy Uyuni as a Kubernetes-based application on SL-Micro 6.2
  As the system administrator
  I want to install and configure RKE2 on the server

  Scenario: Register the server to SCC to access RKE2 packages
    When I run "transactional-update register -r $SCC_SLMICRO_REGCODE" on "server"
    And I run "transactional-update apply" on "server"

  Scenario: Create the RKE2 configuration file
    When I run "mkdir -p /etc/rancher/rke2" on "server"
    And I run "printf 'tls-san:\n  - %s\ningress-controller: traefik\nselinux: true\nkubelet-arg:\n  - seccomp-default=true\n' $(hostname -f) > /etc/rancher/rke2/config.yaml" on "server"
    Then file "/etc/rancher/rke2/config.yaml" should exist on "server"

  Scenario: Install RKE2 via RPM method
    When I run "INSTALL_RKE2_VERSION='v1.35.4+rke2r1' INSTALL_RKE2_METHOD=rpm INSTALL_RKE2_SELINUX=true curl -sfL https://get.rke2.io | sh -" on "server"

  Scenario: Reboot the server to activate the RKE2 transaction
    When I reboot the "server" host through SSH, waiting until it comes back

  Scenario: Enable and start the RKE2 server service
    When I enable the "rke2-server" service on "server"
    And I start the "rke2-server" service on "server"
    And I wait until "rke2-server" service is active on "server"
    Then service "rke2-server" is enabled on "server"
    And service "rke2-server" is active on "server"

  Scenario: Create symlinks for RKE2 tools
    When I run "mkdir -p /usr/local/bin && ln -sf /var/lib/rancher/rke2/bin/kubectl /usr/local/bin/kubectl" on "server"
    And I run "ln -sf /var/lib/rancher/rke2/bin/crictl /usr/local/bin/crictl" on "server"
    And I run "ln -sf /var/lib/rancher/rke2/bin/ctr /usr/local/bin/ctr" on "server"

  Scenario: Set up RKE2 environment variables
    When I run "printf 'export PATH=$PATH:/opt/rke2/bin\nexport KUBECONFIG=/etc/rancher/rke2/rke2.yaml\n' > /etc/profile.d/rke2_vars.sh" on "server"
