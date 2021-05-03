# Copyright (c) 2015-17 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_traditional_client
Feature: Metadata attached to systems

  Scenario: Check pre requires
    When I refresh the metadata for "sle_client"
    Then I should have 'pre="1"' in the metadata for "sle_client"

  Scenario: Check for empty epoch
    When I refresh the metadata for "sle_client"
    Then I should not have 'epoch="0"' in the metadata for "sle_client"

  Scenario: Check local metadata does not contain \n at the end of the summary
    When I refresh the metadata for "sle_client"
    Then I should have 'summary.*</summary' in the metadata for "sle_client"

  Scenario: Check local metadata for susedata.xml
    When I refresh the metadata for "sle_client"
    Then "susedata.xml.gz" should exist in the metadata for "sle_client"
