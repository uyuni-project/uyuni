# Copyright (c) 2017-2018 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_building_container_images
Feature: XML-RPC image namespace for containers

  Scenario: Test image.store namespace
    When I create and delete an image store via XML-RPC
    And I list image store types and image stores via XML-RPC
    And I set and get details of image store via XML-RPC

  Scenario: Test image.profiles namespace
    When I create and delete profiles via XML-RPC
    And I create and delete profile custom values via XML-RPC
    And I list image profiles via XML-RPC
    And I set and get profile details via XML-RPC

# Note: image building via XML-RPC is not tested here
#       it is tested as another feature

  Scenario: Cleanup: remove custom system info
    Given I am authorized for the "Admin" section
    When I follow the left menu "Systems > Custom System Info"
    And I follow "arancio"
    And I follow "Delete Key"
    And I click on "Delete Key"
