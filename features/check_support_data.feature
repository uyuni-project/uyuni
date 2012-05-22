# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Test support data available in the repos
  In Order to check if the susedata.xml file was created
  As an authorized user
  I'll test for the file

  Background:
    Given I am testing channels

  Scenario: Check local metdata for susedata.xml
    Given I am root
     When I refresh the metadata
     Then "susedata.xml.gz" should exists in the metadata

