# Copyright (c) 2017-2022 SUSE LLC
# Licensed under the terms of the MIT license.

@skip_if_container
@scope_salt_ssh
@scope_onboarding
@ssh_minion
Feature: Register a salt-ssh system via API

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section
  
  Scenario: Delete SSH minion system profile before API bootstrap test
    Given I am on the Systems overview page of this "ssh_minion"
