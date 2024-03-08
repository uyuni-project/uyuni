# Copyright (c) 2010-2023 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature can cause failures in:
# If the SUSE-KEY-x86_64 fails to be created:
# - features/init_client/buildhost_bootstrap.feature
# - features/init_client/sle_minion.feature
# - features/secondary/buildhost_docker_auth_registry.feature
# - features/secondary/buildhost_docker_build_image.feature
# - features/secondary/buildhost_osimage_build_image.feature
# - features/secondary/min_baremetal_discovery.feature
# - features/secondary/min_bootstrap_api.feature
# - features/secondary/min_bootstrap_reactivation.feature
# - features/secondary/min_bootstrap_script.feature
# - features/secondary/min_docker_api.feature
# - features/secondary/min_move_from_and_to_proxy.feature
# - features/secondary/min_salt_mgrcompat_state.feature
# - features/secondary/min_salt_minions_page.feature
# - features/secondary/minkvm_guests.feature
# - features/secondary/proxy_cobbler_pxeboot.feature
# - features/secondary/proxy_retail_pxeboot.feature
# - features/secondary/srv_docker_advanced_content_management.feature
# If the RH-LIKE-KEY fails to be created:
# - features/secondary/min_rhlike_salt.feature
# If the DEBLIKE-KEY fails to be created:
# - features/secondary/min_debike_salt.feature
# If the SUSE-SSH-KEY-x86_64 fails to be created:
# - features/secondary/min_ssh_tunnel.feature
# - features/secondary/minssh_move_from_and_to_proxy.feature


Feature: Create activation keys
  In order to register systems to the spacewalk server
  As the testing user
  I want to use activation keys

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Create an activation key with a channel
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I wait until I do not see "Loading..." text
    And I enter "SUSE Test Key x86_64" as "description"
    And I enter "SUSE-KEY-x86_64" as "key"
    And I enter "20" as "usageLimit"
    And I click on "Create Activation Key"
    Then I should see a "Activation key SUSE Test Key x86_64 has been created" text
    And I should see a "Details" link
    And I should see a "Packages" link
    And I should see a "Configuration" link in the content area
    And I should see a "Groups" link
    And I should see a "Activated Systems" link

@rhlike_minion
  Scenario: Create an activation key for RedHat-like minion
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I wait until I do not see "Loading..." text
    And I enter "RedHat like Test Key" as "description"
    And I enter "RH-LIKE-KEY" as "key"
    And I select "Fake-Base-Channel-RH-like" from "selectedBaseChannel"
    And I click on "Create Activation Key"
    Then I should see a "Activation key RedHat like Test Key has been created" text

@deblike_minion
  Scenario: Create an activation key for Debian-like minion
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I wait until I do not see "Loading..." text
    And I enter "Debian-like Test Key" as "description"
    And I enter "DEBLIKE-KEY" as "key"
    And I select "Fake-Base-Channel-Debian-like" from "selectedBaseChannel"
    And I click on "Create Activation Key"
    Then I should see a "Activation key Debian-like Test Key has been created" text

  Scenario: Create an activation key with a channel for salt-ssh
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I wait until I do not see "Loading..." text
    And I enter "SUSE SSH Test Key x86_64" as "description"
    And I enter "SUSE-SSH-KEY-x86_64" as "key"
    And I enter "20" as "usageLimit"
    And I select "Push via SSH" from "contact-method"
    And I click on "Create Activation Key"
    Then I should see a "Activation key SUSE SSH Test Key x86_64 has been created" text

  Scenario: Create an activation key with a channel for salt-ssh via tunnel
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I wait until I do not see "Loading..." text
    And I enter "SUSE SSH Tunnel Test Key x86_64" as "description"
    And I enter "SUSE-SSH-TUNNEL-KEY-x86_64" as "key"
    And I enter "20" as "usageLimit"
    And I select "Push via SSH tunnel" from "contact-method"
    And I click on "Create Activation Key"

  Scenario: Create an activation key for the Proxy
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I wait until I do not see "Loading..." text
    And I enter "Proxy Key x86_64" as "description"
    And I enter "PROXY-KEY-x86_64" as "key"
    And I click on "Create Activation Key"
    Then I should see a "Activation key Proxy Key x86_64 has been created" text

  Scenario: Create an activation key for the build host
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I wait for child channels to appear
    And I enter "Build host Key x86_64" as "description"
    And I enter "BUILD-HOST-KEY-x86_64" as "key"
    And I check "Container Build Host"
    And I check "OS Image Build Host"
    And I click on "Create Activation Key"
    Then I should see a "Activation key Build host Key x86_64 has been created" text

@scc_credentials
  Scenario: Create an activation key for the terminal
    When I follow the left menu "Systems > Activation Keys"
    And I follow "Create Key"
    And I wait for child channels to appear
    And I enter "Terminal Key x86_64" as "description"
    And I enter "TERMINAL-KEY-x86_64" as "key"
    And I click on "Create Activation Key"
    Then I should see a "Activation key Terminal Key x86_64 has been created" text
