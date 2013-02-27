# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: sm-ncc-sync channel listing and enablement
  In Order to validate correct working of sm-ncc-sync
  As user root
  I want to be able to list available channels and enable them

  Scenario: list available channels
     When I execute ncc-sync "--list-channels --all-childs"
     Then I want to get "[.] sles11-sp1-pool-x86_64"
      And I want to get "    [.] sle11-sdk-sp1-updates-x86_64"

  Scenario: list available mandatory channels
     When I execute ncc-sync "--list-channels --all-childs --no-optional"
     Then I want to get "[.] sles11-sp1-pool-x86_64"
      And I want to get "    [.] sle11-sdk-sp1-updates-x86_64"
      And I wont get "debuginfo"
      And I wont get "sles11-extras"

  Scenario: list products
     When I execute ncc-sync "--list-products"
     Then I want to get "[.] SUSE Linux Enterprise Server 11 SP1 [x86_64]"
      And I want to get "[.] SUSE Manager Proxy 1.2 [x86_64]"

  Scenario: list all products
     When I execute ncc-sync "--list-products --all-childs"
     Then I want to get "[.] SUSE Linux Enterprise Server 11 SP1 [x86_64]"
      And I want to get "[.] SUSE Manager Proxy 1.2 [x86_64]"
      And I want to get "(---)    [.] SUSE Cloud version 1.0 [x86_64]"
      And I want to get "(---)    [.] SUSE Linux Enterprise High Availability Extension 11 SP2 [x86_64]"

  Scenario: list products with filter
    When I execute ncc-sync "--list-products --all-childs --filter x86_64"
    Then I want to get "[.] SUSE Linux Enterprise Server 11 SP1 [x86_64]"
     And I wont get "ppc64"
     And I wont get "s390x"

  Scenario: enable sles11-sp1-pool-x86_64
     When I execute ncc-sync "--channel sles11-sp1-pool-x86_64"
      And I execute ncc-sync "--list-channels"
     Then I want to get "[P] sles11-sp1-pool-x86_64"
      And I want to get "    [.] sle11-sdk-sp1-updates-x86_64"

  Scenario: enable sles11-sp1-updates-x86_64
     When I execute ncc-sync "--channel sles11-sp1-updates-x86_64"
      And I execute ncc-sync "--list-channels"
     Then I want to get "[P] sles11-sp1-pool-x86_64"
      And I want to get "    [P] sles11-sp1-updates-x86_64"

  Scenario: enable sles11-sp2-core-x86_64
     When I execute ncc-sync "--channel sles11-sp2-core-x86_64"
      And I execute ncc-sync "--list-channels"
     Then I want to get "[P] sles11-sp1-pool-x86_64"
      And I want to get "    [P] sles11-sp2-core-x86_64"

  Scenario: enable sles11-sp2-updates-x86_64
     When I execute ncc-sync "--channel sles11-sp2-updates-x86_64"
      And I execute ncc-sync "--list-channels"
     Then I want to get "[P] sles11-sp1-pool-x86_64"
      And I want to get "    [P] sles11-sp2-updates-x86_64"

