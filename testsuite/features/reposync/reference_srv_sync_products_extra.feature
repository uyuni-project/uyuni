# Copyright (c) 2023-2024 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Synchronize extra products in the products page of the Setup Wizard

@scc_credentials
  Scenario: Let the products page appear
    Given I am authorized for the "Admin" section
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I see "Product Description" text
    Then I should see a "Arch" text
    And I should see a "Channels" text

@uyuni
  Scenario: Add openSUSE Leap 15.5 product, including Uyuni Client Tools
    When I use spacewalk-common-channel to add all "leap15.5" channels with arch "x86_64"

@scc_credentials
@susemanager
  Scenario: Add Rocky 8 product with recommended sub-products
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "Rocky" as the filtered product description
    And I wait until I see "Rocky Linux 8 x86_64" text
    And I select "Rocky Linux 8 x86_64" as a product
    Then I should see the "Rocky Linux 8 x86_64" selected
    When I click the Add Product button
    And I wait until I see "Rocky Linux 8 x86_64" product has been added

@uyuni
  Scenario: Enable Rocky 8 Uyuni client tools for creating bootstrap repositories
    When I use spacewalk-common-channel to add channel "rockylinux8 rockylinux8-appstream rockylinux8-uyuni-client" with arch "x86_64"

@scc_credentials
@susemanager
  Scenario: Add Ubuntu 22.04 product with recommended sub-products
    When I follow the left menu "Admin > Setup Wizard > Products"
    And I wait until I do not see "currently running" text
    And I wait until I do not see "Loading" text
    And I enter "Ubuntu" as the filtered product description
    And I select "amd64-deb" in the dropdown list of the architecture filter
    And I wait until I see "Ubuntu 22.04" text
    And I select "Ubuntu 22.04" as a product
    Then I should see the "Ubuntu 22.04" selected
    When I click the Add Product button
    And I wait until I see "Ubuntu 22.04" product has been added

@uyuni
  Scenario: Enable Ubuntu 22.04 Uyuni client tools for creating bootstrap repositories
    When I use spacewalk-common-channel to add channel "ubuntu-2204-pool-amd64-uyuni ubuntu-2204-amd64-main-uyuni ubuntu-2204-amd64-main-updates-uyuni ubuntu-2204-amd64-main-security-uyuni ubuntu-2204-amd64-uyuni-client" with arch "amd64-deb"
