# Copyright (c) 2023 SUSE LLC
# Licensed under the terms of the MIT license.
#
#  1) bootstrap a new Red Hat-like minion via salt
#  2) subscribe it to a base channel for testing

@rhlike_minion
Feature: Bootstrap a Red Hat-like minion

  Scenario: Log in as admin user
    Given I am authorized for the "Admin" section

