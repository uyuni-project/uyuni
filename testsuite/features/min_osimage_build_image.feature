# Copyright (c) 2018 SUSE LLC
# Licensed under the terms of the MIT license.
#
# This feature relies on having properly configured
#   /etc/rhn/rhn.conf
# file on your SUSE Manager server.
#
# For the scope of these tests, we configure it as follows:
#   java.kiwi_os_image_building_enabled = true
# which means "Enable Kiwi OS Image building"

Feature: Build OS images

  Scenario: Login as Kiwi image administrator and build an image
    Given I am authorized as "kiwikiwi" with password "kiwikiwi"
    When I navigate to images build webpage
    And I select "suse_os_image" from "profileId"
    And I select sle-minion hostname in Build Host
    And I click on "submit-btn"

  Scenario: Check the OS image built as Kiwi image administrator
    Given I am on the Systems overview page of this "sle-minion"
    When I wait at most 900 seconds until event "Image Build suse_os_image scheduled by kiwikiwi" is completed
    And I wait at most 300 seconds until event "Image Inspect 1//suse_os_image:latest scheduled by kiwikiwi" is completed
    And I navigate to "os-images/1/" page
    Then I should see a "POS_Image_JeOS6" text

#@retail
#  Scenario: Move the image to the build server
#    TBD

  Scenario: Cleanup: remove the image from build host
    When I run "rm /srv/www/os-images/1/*" on "sle-minion"
