# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.


@scope_hub
Feature: Create bootstrap repository for peripheral2
In order to be able to enroll clients with MU repositories
As the system administrator
I create all bootstrap repos with --with-custom-channels option

  Scenario: Create the bootstrap repository for proxy3
    When I create the bootstrap repository for "proxy3" on peripheral2

  Scenario: Create the bootstrap repository for slmicro62
    When I create the bootstrap repository for "slmicro62_minion" on peripheral2

  Scenario: Create the bootstrap repository for rocky10
    When I create the bootstrap repository for "rocky10_minion" on peripheral2
