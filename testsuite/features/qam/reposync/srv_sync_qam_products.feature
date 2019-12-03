# Copyright 2017-2019 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Synchronize products in the products page of the Setup Wizard

  Scenario: Refresh SCC
    Given I am on the Admin page
    When I refresh SCC
    When I follow "SUSE Products" in the content area
    And I wait until I see "Product Description" text

#  Scenario: Add Ubuntu 16.04
#    And I enter "Ubuntu 16.04" in the css "input[name='product-description-filter']"
#    And I select "Ubuntu 16.04" as a product
#    Then I should see the "Ubuntu 16.04" selected
#    When I click the Add Product button
#    And I wait until I see "Ubuntu 16.04" product has been added

  Scenario: Add Ubuntu 18.04
    And I enter "Ubuntu 18.04" in the css "input[name='product-description-filter']"
    And I select "Ubuntu 18.04" as a product
    Then I should see the "Ubuntu 18.04" selected
    When I click the Add Product button
    And I wait until I see "Ubuntu 18.04" product has been added

  Scenario: SUSE Linux Enterprise Server 12 SP4
    And I enter "SUSE Linux Enterprise Server 12 SP4" in the css "input[name='product-description-filter']"
    And I select "x86_64" in the dropdown list of the architecture filter
    And I select "SUSE Linux Enterprise Server 12 SP4 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 12 SP4 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Server 12 SP4 x86_64"
    Then I should see the "SUSE Linux Enterprise Server 12 SP4 x86_64" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 12 SP4 x86_64" product has been added

  Scenario: SUSE Linux Enterprise Server 11 SP4
    And I enter "SUSE Linux Enterprise Server 11 SP4" in the css "input[name='product-description-filter']"
    And I select "x86_64" in the dropdown list of the architecture filter
    And I select "SUSE Linux Enterprise Server 11 SP4 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 11 SP4 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Server 11 SP4 x86_64"
    Then I should see the "SUSE Linux Enterprise Server 11 SP4 x86_64" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 11 SP4 x86_64" product has been added

#  Scenario: SUSE Linux Enterprise Server 15
#    And I enter "SUSE Linux Enterprise Server 15" in the css "input[name='product-description-filter']"
#    And I select "x86_64" in the dropdown list of the architecture filter
#    And I select "SUSE Linux Enterprise Server 15 x86_64" as a product
#    Then I should see the "SUSE Linux Enterprise Server 15 x86_64" selected
#    When I open the sub-list of the product "SUSE Linux Enterprise Server 15 x86_64"
#    Then I should see the "SUSE Linux Enterprise Server 15 x86_64" selected
#    When I click the Add Product button
#    And I wait until I see "SUSE Linux Enterprise Server 15 x86_64" product has been added

  Scenario: SUSE Linux Enterprise Server 15 SP1
    And I enter "SUSE Linux Enterprise Server 15 SP1" in the css "input[name='product-description-filter']"
    And I select "x86_64" in the dropdown list of the architecture filter
    And I select "SUSE Linux Enterprise Server 15 SP1 x86_64" as a product
    Then I should see the "SUSE Linux Enterprise Server 15 SP1 x86_64" selected
    When I open the sub-list of the product "SUSE Linux Enterprise Server 15 SP1 x86_64"
    Then I should see the "SUSE Linux Enterprise Server 15 SP1 x86_64" selected
    When I click the Add Product button
    And I wait until I see "SUSE Linux Enterprise Server 15 SP1 x86_64" product has been added

#  Scenario: Add RHEL Expanded Support 6
#    And I enter "RHEL Expanded Support 6" in the css "input[name='product-description-filter']"
#    And I select "RHEL Expanded Support 6" as a product
#    Then I should see the "RHEL Expanded Support 6" selected
#    When I click the Add Product button
#    And I wait until I see "RHEL Expanded Support 6" product has been added

  Scenario: Add RHEL Expanded Support 7
    And I enter "RHEL Expanded Support 7" in the css "input[name='product-description-filter']"
    And I select "RHEL Expanded Support 7" as a product
    Then I should see the "RHEL Expanded Support 7" selected
    When I click the Add Product button
    And I wait until I see "RHEL Expanded Support 7" product has been added
