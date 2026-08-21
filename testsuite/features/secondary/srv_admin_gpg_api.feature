# Copyright (c) 2026 SUSE LLC.
# Licensed under the terms of the MIT license.

@skip_if_github_validation
@scope_api
Feature: API "admin.gpg" namespace

  Scenario: Prepare GPG key management API tests
    When I make sure the GPG key with fingerprint "D88811AF6B51852351DF538527FA41BD8A7C64F9" is not present via API
    Then I should not see GPG key fingerprint "D88811AF6B51852351DF538527FA41BD8A7C64F9" via API

  Scenario: Upload a GPG key
    When I upload the GPG key "galaxy.key" via API
    Then I should see GPG key fingerprint "D88811AF6B51852351DF538527FA41BD8A7C64F9" via API
    And the GPG key fingerprint "D88811AF6B51852351DF538527FA41BD8A7C64F9" should have user name "Unsupported <unsupported@suse.de>" via API

  Scenario: Remove a GPG key
    When I remove the GPG key with fingerprint "D88811AF6B51852351DF538527FA41BD8A7C64F9" via API
    Then I should not see GPG key fingerprint "D88811AF6B51852351DF538527FA41BD8A7C64F9" via API
