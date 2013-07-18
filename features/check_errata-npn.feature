# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Check errata
  In Order to check if the errata import was successfull
  As the testing user
  I want to see the erratums in the web page including the packages

  Scenario: check all errata exists
    Given I am on the errata page
     When I follow "Relevant" in the left menu
     Then I should see an update in the list
      And I should see a "TestsuiteRepo" link

  Scenario: check TestsuiteRepo errata
    Given I am on the errata page
     When I follow "All" in the left menu
     When I follow "TestsuiteRepo"
     Then I should see a "TestsuiteRepo - Bug Fix Advisory" text
      And I should see a "Test update for sles-release" text
      And I should see a "SLES11-SP2-Updates x86_64 Channel" link
      And I should see a "SLES11-SP2-Updates i586 Channel" link
      And I should see a "reboot_suggested" text

  Scenario: check TestsuiteRepo errata packages
    Given I am on the errata page
     When I follow "All" in the left menu
     When I follow "TestsuiteRepo"
     When I follow "Packages"
     Then I should see a "SLES11-SP2-Updates x86_64 Channel" link
      And I should see a "SLES11-SP2-Updates i586 Channel" link
      And I should see a "sha1:e1a9d6fbfec6ae5833bc4b71604dffc9c52328c7" text
      And I should see a "sles-release-11.3-99.1-i586" link
      And I should see a "sha1:3236c19066c784159cc425ba620a26c5914e5f99" text
      And I should see a "sles-release-11.3-99.1-x86_64" link
      And I should see a "sha1:2a221a1a2af454ab039cd2924f670b8e035250aa" text
      And I should see a "sles-release-DVD-11.3-99.1-i586" link
      And I should see a "sha1:8a5cd7daced20d8582eb5c6902156730a0cdbf5b" text
      And I should see a "sles-release-DVD-11.3-99.1-x86_64" link
      And I should see a "sha1:5bb2a19a2c9154c32652e98db4cb3520802aa289" text
      And I should see a "sles-release-MINI-11.3-99.1-i586" link
      And I should see a "sha1:ea6fd7ad584a22c589c235253048fa135c9241af" text
      And I should see a "sles-release-MINI-11.3-99.1-x86_64" link

  Scenario: check relevant errata for this client
    Given I am on the Systems overview page of this client
     When I follow "Software" in class "content-nav"
     When I follow "Errata" in class "contentnav-row2"
     Then I should see a "Relevant Errata" text
      And I should see a "Test update for sles-release" text

