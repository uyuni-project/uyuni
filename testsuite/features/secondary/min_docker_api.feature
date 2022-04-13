# Copyright (c) 2017-2022 SUSE LLC
# Licensed under the terms of the MIT license.
#
# Note: image building via API is not tested here
#       it is tested in buildhost_docker_build_image.feature and
#                       buildhost_docker_auth_registry.feature

@scope_building_container_images
Feature: API "image" namespace for containers and sub-namespaces

  Scenario: Test "image.store" namespace
    When I am logged in API as user "admin" and password "admin"
    And I create and delete an image store via API
    And I list image store types and image stores via API
    And I set and get details of image store via API

  Scenario: Test "image.profiles" namespace
    When I create and delete profiles via API
    And I create and delete profile custom values via API
    And I list image profiles via API
    And I set and get profile details via API
    And I logout from API

  Scenario: Cleanup: remove custom system info
    Given I am authorized for the "Admin" section
    When I follow the left menu "Systems > Custom System Info"
    And I follow "arancio"
    And I follow "Delete Key"
    And I click on "Delete Key"
