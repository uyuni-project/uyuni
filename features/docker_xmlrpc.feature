# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature:  Container Image namespace tests

  Scenario: Test image.store Namespace
  Given I am authorized as "admin" with password "admin"
  Then I run image.store tests via xmlrpc
