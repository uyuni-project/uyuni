# Copyright (c) 2016 SUSE LLC.
# Licensed under the terms of the MIT license.

Feature: Sync the necessary channels for the proxy

  Scenario: enable suse-manager-proxy-3.0-pool-x86_64
    When I execute mgr-sync "add channel suse-manager-proxy-3.0-pool-x86_64"
    And I execute mgr-sync "add channel suse-manager-proxy-3.0-updates-x86_64"

