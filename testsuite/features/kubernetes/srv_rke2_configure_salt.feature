# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

@rke2
@no_user_creation
Feature: Configure salt on the server after the installation

  Scenario: Create pillar top sls to assign salt bundle config
    When I configure the salt bundle pillar on the RKE2 server
