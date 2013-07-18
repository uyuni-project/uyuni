# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Test weak dependencies
  In Order to validate the new added weak dependencies feature
  As an authorized user
  I want to see the weak deps in the package page and in the metadata on the client

  Background:
    Given I am testing channels

  Scenario: Check Package metadata displayed in WebUI (Supplements)
    When I follow "Channels"
     And I follow "SLES11-SP2-Updates x86_64 Channel"
     And I follow "Packages"
     And I follow "xz-lang-5.0.3-0.12.1.x86_64"
     And I follow "Dependencies"
    Then I should see a "Recommends" text
     And I should see a "Suggests" text
     And I should see a "Supplements" text
     And I should see a "packageand(bundle-lang-other:xz)" text


  Scenario: Check Package metadata displayed in WebUI (Recommends)
    When I follow "Channels"
     And I follow "SLES11-SP2-Updates x86_64 Channel"
     And I follow "Packages"
     And I follow "xz-5.0.3-0.12.1.x86_64"
     And I follow "Dependencies"
    Then I should see a "Recommends" text
     And I should see a "Suggests" text
     And I should see a "Supplements" text
     And I should see a "xz-lang" text


  Scenario: Check Package metadata displayed in WebUI (Suggests)
    When I follow "Channels"
     And I follow "SLES11-SP2-Updates x86_64 Channel"
     And I follow "Packages"
     And I follow "hplip-hpijs-3.11.10-0.6.7.1.x86_64"
     And I follow "Dependencies"
    Then I should see a "Recommends" text
     And I should see a "Suggests" text
     And I should see a "Supplements" text
     And I should see a "hplip = 3.11.10" text

  Scenario: Check local metdata for weak deps
    Given I am root
     When I refresh the metadata
     Then I should have 'rpm:recommends.*xz-lang.*rpm:recommends' in the metadata
      And I should have 'rpm:supplements.*packageand.bundle-lang-other:xz.*rpm:supplements' in the metadata
      And I should have 'rpm:suggests.*hplip.*rpm:suggests' in the metadata

