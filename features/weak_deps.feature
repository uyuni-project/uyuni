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
     And I follow "SLES11-SP2-Updates x86_64 Channel"
     And I follow "Packages"
     And I follow "nfs-client-1.2.3-18.23.1.x86_64"
     And I follow "Dependencies"
    Then I should see a "Recommends" text
     And I should see a "Suggests" text
     And I should see a "Supplements" text
     And I should see a "aaa_base:/etc/init.d/nfs" text
     And I should see a "nfs-client = 1.2.3-18.23.1" text

  Scenario: Check Package metadata displayed in WebUI (Supplements)
    When I follow "Channels"
     And I follow "SLES11-SP2-Updates x86_64 Channel"
     And I follow "Packages"
     And I follow "oracleasm-kmp-default-2.0.5_3.0.38_0.5-7.26.3.x86_64"
     And I follow "Dependencies"
    Then I should see a "Recommends" text
     And I should see a "Suggests" text
     And I should see a "Supplements" text
     And I should see a "packageand(kernel-default:oracleasm-kmp)" text

  Scenario: Check local metdata for weak deps
    Given I am root
     When I refresh the metadata
     Then I should have "rpm:recommends.*rpcbind.*rpm:recommends" in the metadata
      And I should have "rpm:supplements.*packageand.kernel-default:kernel-source.*rpm:supplements" in the metadata







