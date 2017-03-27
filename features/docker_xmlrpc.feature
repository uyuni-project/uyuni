# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature:  Container Image namespace tests

  Scenario: Test image.store Namespace
  Given I am authorized as "admin" with password "admin"
  Then I run image.store tests via xmlrpc

  Scenario: Scalability tests for image store
  Given I am authorized as "admin" with password "admin"
  Then I create "500" random image stores
  And I follow "Images" in the left menu
  And I follow "Stores" in the left menu
  Then I should see a "Registry" text

  Scenario: Test image.profiles Namespace
  Given I am authorized as "admin" with password "admin"
  Then I run image.profiles tests via xmlrpc

  Scenario: Cleanup image namespaces tests
  Given I am authorized as "admin" with password "admin"
  Then I delete the random image stores

