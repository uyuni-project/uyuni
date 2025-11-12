# Copyright (c) 2022 SUSE LLC
# SPDX-License-Identifier: MIT

Feature: Turn "disable_local_repos" feature on highstate off
  In order to test the product using shortcuts
  As root user
  I want to add pillar data to disable that feature

  Scenario: Create custom pillar to turn off "disable_local_repos"
    When I turn off disable_local_repos for all clients
