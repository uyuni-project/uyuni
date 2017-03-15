# Copyright (c) 2017 SUSE LLC
# Licensed under the terms of the MIT license.

Feature:  Remove Tests for docker
         We create dummy data and  remove them to test the 
         remove functionality for container

  Scenario: Remove and check that sles-minion is not anymore a container build host 
  Given I am on the Systems overview page of this "sle-minion"
  # TODO: check that the system is not anymore a container build host 
 
  Scenario: Delete an Image Store without credentials
  Given I am authorized as "admin" with password "admin"

  Scenario: Delete an Image Profile
  Given I am authorized as "admin" with password "admin"

  Scenario: Build a docker Image
  Given I am authorized as "admin" with password "admin"
