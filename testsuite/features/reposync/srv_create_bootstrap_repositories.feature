# Copyright (c) 2021-2025 SUSE LLC
# SPDX-License-Identifier: MIT

Feature: Create bootstrap repositories
  In order to be able to enroll clients with MU repositories
  As the system administrator
  I create all bootstrap repos with --with-custom-channels option

  Scenario: Create the bootstrap repositories including custom channels
    When I create the bootstrap repositories including custom channels

@sle_minion
  Scenario: Create the bootstrap repository for SUSE Minion
    When I create the bootstrap repository for "sle_minion" on the server

@proxy
  Scenario: Create the bootstrap repository for Proxy
    When I create the bootstrap repository for "proxy" on the server

@build_host
  Scenario: Create the bootstrap repository for Build Host
    When I create the bootstrap repository for "build_host" on the server
