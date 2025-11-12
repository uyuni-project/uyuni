# Copyright (c) 2010-2025 SUSE LLC
# SPDX-License-Identifier: MIT
#
# This feature can cause failures in:
# If the SUSE-KEY-x86_64 fails to be created:
# - features/init_client/buildhost_bootstrap.feature
# - features/init_client/sle_minion.feature
# - features/secondary/buildhost_docker_auth_registry.feature
# - features/secondary/buildhost_docker_build_image.feature
# - features/secondary/buildhost_osimage_build_image.feature
# - features/secondary/min_bootstrap_api.feature
# - features/secondary/min_bootstrap_reactivation.feature
# - features/secondary/min_bootstrap_script.feature
# - features/secondary/min_docker_api.feature
# - features/secondary/min_move_from_and_to_proxy.feature
# - features/secondary/min_salt_mgrcompat_state.feature
# - features/secondary/min_salt_minions_page.feature
# - features/secondary/proxy_cobbler_pxeboot.feature
# - features/secondary/proxy_retail_pxeboot.feature
# - features/secondary/srv_docker_advanced_content_management.feature
# If the RH-LIKE-KEY fails to be created:
# - features/secondary/min_rhlike_salt.feature
# If the DEBLIKE-KEY fails to be created:
# - features/secondary/min_debike_salt.feature
# If the SUSE-SSH-KEY-x86_64 fails to be created:
# - features/secondary/minssh_tunnel.feature
# - features/secondary/minssh_move_from_and_to_proxy.feature

# The WebUI coverage of Activation keys is covered in:
# - features/secondary/srv_manage_activationkey.feature

Feature: Create activation keys
  In order to register systems to the server
  As the testing user
  I want to use activation keys

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

@sle_minion
  Scenario: Create an activation key with a channel
    When I create an activation key with id "SUSE-KEY-x86_64", description "SUSE Test Key x86_64", limit of 20 and contact method "default"
    Then I should get the new activation key "1-SUSE-KEY-x86_64"

@rhlike_minion
  Scenario: Create an activation key for RedHat-like minion
    When I create an activation key with id "RH-LIKE-KEY", description "RedHat like Test Key", base channel "fake-base-channel-rh-like", limit of 20 and contact method "default"
    Then I should get the new activation key "1-RH-LIKE-KEY"

@deblike_minion
  Scenario: Create an activation key for Debian-like minion
    When I create an activation key with id "DEBLIKE-KEY", description "Debian-like Test Key", base channel "fake-base-channel-debian-like", limit of 20 and contact method "default"
    Then I should get the new activation key "1-DEBLIKE-KEY"

@ssh_minion
  Scenario: Create an activation key with a channel for salt-ssh
    When I create an activation key with id "SUSE-SSH-KEY-x86_64", description "SUSE SSH Test Key x86_64", limit of 20 and contact method "ssh-push"
    Then I should get the new activation key "1-SUSE-SSH-KEY-x86_64"

@ssh_minion
  Scenario: Create an activation key with a channel for salt-ssh via tunnel
    When I create an activation key with id "SUSE-SSH-TUNNEL-KEY-x86_64", description "SUSE SSH Tunnel Test Key x86_64", limit of 20 and contact method "ssh-push-tunnel"
    Then I should get the new activation key "1-SUSE-SSH-TUNNEL-KEY-x86_64"

@proxy
  Scenario: Create an activation key for the proxy
    When I create an activation key with id "PROXY-KEY-x86_64", description "Proxy Key x86_64", limit of 1
    Then I should get the new activation key "1-PROXY-KEY-x86_64"

@build_host
  Scenario: Create an activation key for the build host
    When I create an activation key with id "BUILD-HOST-KEY-x86_64", description "Build host Key x86_64"
    And I set the entitlements of the activation key "1-BUILD-HOST-KEY-x86_64" to "container_build_host, osimage_build_host"
    Then I should get the new activation key "1-BUILD-HOST-KEY-x86_64"

@scc_credentials
  Scenario: Create an activation key for the terminal
    When I create an activation key with id "TERMINAL-KEY-x86_64", description "Terminal Key x86_64"
    Then I should get the new activation key "1-TERMINAL-KEY-x86_64"
