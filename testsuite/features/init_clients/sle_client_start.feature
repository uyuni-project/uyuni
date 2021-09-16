# Copyright (c) 2015-2021 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Register a traditional client start
  In order to register a traditional client to the Uyuni server
  I want to create, parametrize and run boostrap script from proxy

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

  Scenario: Register a traditional client
    When I bootstrap traditional client "sle_client" using bootstrap script with activation key "1-SUSE-KEY-x86_64" from the proxy
