# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Test weak dependencies
  In Order to validate the new added weak dependencies feature
  As an authorized user
  I want to see the weak deps in the package page and in the metadata on the client

  Background:
    Given I am testing channels

  Scenario: Check Package metadata displayed in WebUI (Recommends/Suggests)
    When I follow "Channels"
     And I follow "SLES11-SP1-Updates x86_64 Channel"
     And I follow "Packages"
     And I follow "aspell-0.60.6-26.22.x86_64"
     And I follow "Dependencies"
    Then I should see a "Recommends" text
     And I should see a "Suggests" text
     And I should see a "Supplements" text
     And I should see a "aspell-en" text
     And I should see a "aspell-ispell" text

  Scenario: Check Package metadata displayed in WebUI (Supplements)
    When I follow "Channels"
     And I follow "SLES11-SP1-Updates x86_64 Channel"
     And I follow "Packages"
     And I follow "btrfs-kmp-default-0_2.6.32.23_0.3-0.3.20.x86_64"
     And I follow "Dependencies"
    Then I should see a "Recommends" text
     And I should see a "Suggests" text
     And I should see a "Supplements" text
     And I should see a "packageand(kernel-default:btrfs-kmp)" text

  Scenario: Check local metdata for weak deps
    Given I am root
     When I refresh the metadata
     Then I should have "rpm:recommends.*aspell-en.*rpm:recommends" in the metadata
      And I should have "rpm:suggests.*aspell-ispell.*rpm:suggests" in the metadata
      And I should have "rpm:supplements.*packageand.kernel-default:btrfs-kmp.*rpm:supplements" in the metadata







