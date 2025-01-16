# Copyright (c) 2021-2024 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Create bootstrap repositories
  In order to be able to enroll clients with MU repositories
  As the system administrator
  I create all bootstrap repos with --with-custom-channels option

  Scenario: Create the bootstrap repositories including custom channels
    When I create the bootstrap repositories including custom channels
