# Copyright (c) 2025 SUSE LLC
# Licensed under the terms of the MIT license.

@transactional_server
Feature: SELinux debugging
  In order for the server to behave correctly after a reboot
  I want to be sure that there is no wrong SELinux label

  Scenario: No previous operation has created wrong SELinux label
    Then files on container volumes should all have the proper SELinux label
