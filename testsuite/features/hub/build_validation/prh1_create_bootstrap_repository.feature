# Copyright (c) 2023-2024 SUSE LLC
# Licensed under the terms of the MIT license.


@scope_hub
Feature: Create bootstrap repository for sles15sp7
In order to be able to enroll clients with MU repositories
As the system administrator
I create all bootstrap repos with --with-custom-channels option

  Scenario: Create the bootstrap repository for sles15sp7
    When I create the bootstrap repository for "sles15sp7_minion" on server2

  Scenario: Create the bootstrap repository for sles15sp7
    When I create the bootstrap repository for "proxy2" on server2
