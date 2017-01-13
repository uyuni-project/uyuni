# Copyright (c) 2015-17 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Test support data available in the repos
  In Order to check if the susedata.xml file was created
  As an authorized user
  I'll test for the file

  Scenario: Check local metdata for susedata.xml
    When I refresh the metadata
    Then "susedata.xml.gz" should exists in the metadata
