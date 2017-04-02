# Copyright (c) 2015-17 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test SUSE Manager generated metadata
  In Order to validate the SUSE Manager generated metadata
  As an local user
  I want to see several xml elements and attributes in the metadata

  Scenario: Check pre requires
    When I refresh the metadata
    Then I should have 'pre="1"' in the metadata

  Scenario: Check for empty epoch
    When I refresh the metadata
    Then I should not have 'epoch="0"' in the metadata

  Scenario: Check local metdata not contain \n at the end of the summary
    Given I am authorized as "admin" with password "admin"
    When I refresh the metadata
    Then I should have 'summary.*</summary' in the metadata

  Scenario: Check local metdata for susedata.xml
    When I refresh the metadata
    Then "susedata.xml.gz" should exists in the metadata
