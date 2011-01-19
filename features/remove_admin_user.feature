# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

# feature/remove_admin_user.feature
#  see also: init_user_create.feature
@cleanup @database
Feature: Remove initial users
  In Order to revert to a pristine state
  As a testing users
  I need to remove the admin user

  Scenario: Remove Admin users
    Given I have low-level access to the database
    When I remove the admin user
# hack to make it DRY
    Given I access the host the first time
