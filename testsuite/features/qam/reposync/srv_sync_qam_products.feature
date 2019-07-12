# Copyright 2017-2020 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Synchronize products in the products page of the Setup Wizard

  Scenario: Refresh SCC
    Given I am on the Admin page
    When I refresh SCC
    When I follow "SUSE Products" in the content area
    And I wait until I see "Product Description" text

#  Scenario: Add Ubuntu 16.04
#    Given I am on the Admin page
#    When I follow "SUSE Products" in the content area
#    And I wait until I see "Product Description" text
#    And I enter "Ubuntu 16.04" as the filtered product description
#    And I select "Ubuntu 16.04" as a product
#    Then I should see the "Ubuntu 16.04" selected
#    When I click the Add Product button
#    And I wait until I see "Ubuntu 16.04" product has been added

  Scenario: Add Ubuntu 18.04
    Given I am on the Admin page
    When I follow "SUSE Products" in the content area
    And I wait until I see "Product Description" text
    And I enter "Ubuntu 18.04" as the filtered product description
    And I select "Ubuntu 18.04" as a product
    Then I should see the "Ubuntu 18.04" selected
    When I click the Add Product button
    And I wait until I see "Ubuntu 18.04" product has been added

  Scenario: SUSE Linux Enterprise Server 12 SP4
    Given I am on the Admin page
    When I follow "SUSE Products" in the content area
    And I wait until I see "Product Description" text
    And I enter "SUSE Linux Enterprise Server 12 SP4" as the filtered product description
    And I select "SUSE Linux Enterprise Server 12 SP4 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 12 SP4 x86_64" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 12 SP4 x86_64" product has been added

  Scenario: SUSE Linux Enterprise Server 11 SP4
    Given I am on the Admin page
    When I follow "SUSE Products" in the content area
    And I wait until I see "Product Description" text
    And I enter "SUSE Linux Enterprise Server 11 SP4" as the filtered product description
    And I select "SUSE Linux Enterprise Server 11 SP4 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 11 SP4 x86_64" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 11 SP4 x86_64" product has been added

  Scenario: SUSE Linux Enterprise Server 15
    Given I am on the Admin page
    When I follow "SUSE Products" in the content area
    And I wait until I see "Product Description" text
    And I enter "SUSE Linux Enterprise Server 15" as the filtered product description
    And I select "SUSE Linux Enterprise Server 15 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 15 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Server 15 x86_64"
    Then I should see the "SUSE Linux Enterprise Server 15 x86_64" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 15 x86_64" product has been added

  Scenario: SUSE Linux Enterprise Server 15 SP1
    Given I am on the Admin page
    When I follow "SUSE Products" in the content area
    And I wait until I see "Product Description" text
    And I enter "SUSE Linux Enterprise Server 15 SP1" as the filtered product description
    And I select "SUSE Linux Enterprise Server 15 SP1 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 15 SP1 x86_64" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 15 SP1 x86_64" product has been added

#  Scenario: Add RHEL Expanded Support 6
#    Given I am on the Admin page
#    When I follow "SUSE Products" in the content area
#    And I wait until I see "Product Description" text
#    And I enter "RHEL Expanded Support 6" as the filtered product description
#    And I select "RHEL Expanded Support 6" as a product
#    Then I should see the "RHEL Expanded Support 6" selected
#    When I click the Add Product button
#    And I wait until I see "RHEL Expanded Support 6" product has been added

#  Scenario: Add RHEL Expanded Support 7
#    Given I am on the Admin page
#    When I follow "SUSE Products" in the content area
#    And I wait until I see "Product Description" text
#    Given I am on the SUSE Products page
#    And I enter "RHEL Expanded Support 7" as the filtered product description
#    And I select "RHEL Expanded Support 7" as a product
#    Then I should see the "RHEL Expanded Support 7" selected
#    When I click the Add Product button
#    And I wait until I see "RHEL Expanded Support 7" product has been added
