# Copyright (c) 2025 SUSE LLC
# SPDX-License-Identifier: MIT

@transactional_server
Feature: SELinux debugging
  In order for the server to behave correctly after a reboot
  I want to be sure that there is no wrong SELinux label

  @skip_if_rke2
  Scenario: No previous operation has created wrong SELinux label
    Then files on container volumes should all have the proper SELinux label

  @rke2
  Scenario: Check the SELinux logs on server
    Then the logs messages should not contain SELinux denials
