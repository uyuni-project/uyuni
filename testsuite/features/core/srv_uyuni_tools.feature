# Copyright (c) 2025 SUSE LLC
# Licensed under the terms of the MIT license.

@susemanager
@containerized_server
Feature: Check mgradm support ptf podman command availability
  From the server host
  All uyuni tool commands must be accessible
  So they can be used reliably with valid parameters

  Scenario: Check that 'mgradm support ptf podman' is available
    Given the "mgradm" command is available on the host
    Then the argument "support ptf podman" is valid in mgradm
