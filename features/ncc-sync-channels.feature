# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: sm-ncc-sync channel listing and enablement
  In Order to validate correct working of sm-ncc-sync
  As user root
  I want to be able to list available channels and enable them

  Scenario: list available channels
     When I execute ncc-sync "--list-channels"
     Then I want to get "[.] suse_sles-11.1.x86_64-base"
      And I want to get "    [.] sles-sle-sdk-11.1.x86_64"

  Scenario: enable suse_sles-11.1.x86_64-base
     When I execute ncc-sync "--channel suse_sles-11.1.x86_64-base"
      And I execute ncc-sync "--list-channels"
     Then I want to get "[P] suse_sles-11.1.x86_64-base"
      And I want to get "    [.] sles-sle-sdk-11.1.x86_64"

  Scenario: enable sles-sle-sdk-11.1.x86_64
     When I execute ncc-sync "--channel sles-sle-sdk-11.1.x86_64"
      And I execute ncc-sync "--list-channels"
     Then I want to get "[P] suse_sles-11.1.x86_64-base"
      And I want to get "    [P] sles-sle-sdk-11.1.x86_64"

