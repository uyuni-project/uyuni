# Copyright (c) 2025 SUSE LLC
# SPDX-License-Identifier: MIT

@skip_if_github_validation
Feature: Password Policy Management
  As an organization administrator,
  I want to configure and enforce password complexity requirements,
  So that user accounts comply with security policies

  Scenario: Log in as org admin user
    Given I am authorized as "admin" with password "admin"

  Scenario: Navigate to Password Policy settings page
    When I follow the left menu "Admin > Manager Configuration > Password Policy"
    Then I should see a "Server Configuration - Password Policy" text

  Scenario: Configure password complexity restrictions
    When I set the minimum password length to "5"
    And I set the maximum password length to "12"
    And I enable the following restrictions:
      | Require Digits                  |
      | Require Lowercase Characters    |
      | Require Uppercase Characters    |
      | Require Special Characters      |
      | Restrict Characters Occurrences |
      | Restrict Consecutive Characters |
    And I click on "Save"
    And I should see a "Password Policy Changed" text

  Scenario: Verify password complexity restrictions are saved correctly
    When I refresh the page
    Then the following restrictions should be enabled:
      | Require Digits                  |
      | Require Lowercase Characters    |
      | Require Uppercase Characters    |
      | Require Special Characters      |
      | Restrict Characters Occurrences |
      | Restrict Consecutive Characters |

  # WORKAROUND: Page must be refreshed before editing special characters fields (bsc1244430)
  Scenario: Update special characters list and maximum character occurrence
    When I set the special characters list to "$@?"
    And I set the maximum allowed occurrence of any character to "3"
    And I click on "Save"
    And I should see a "Password Policy Changed" text

  Scenario Outline: Reject invalid passwords based on policy enforcement
    When I create a user with name "password_policy_user" and password "<password>" with roles "config_admin,system_group_admin,activation_key_admin,image_admin"
    Then the user creation should fail with error containing "<error_message>"

    Examples:
      | password      | error_message                                                                              |
      | aB$1          | Passwords must be at least 5 characters                                                    |
      | ab$123        | Passwords must contain at least one upper case character                                   |
      | AB$123        | Passwords must contain at least one lower case character                                   |
      | aB$cde        | Passwords must contain at least one digit                                                  |
      | aBc123        | Passwords must contain at least one special character                                      |
      | aB:123        | Passwords must contain at least one special character, allowed special characters are: $@? |
      | aaB$123       | consecutive_characters_presents                                                            |
      | aB$a12aa3     | Password characters occurrences exceeded maximum allowed 3                                 |
      | aBcdef$123456 | Passwords cannot be more than 12 characters                                                |

  Scenario: Accept valid password complying with policy
    When I create a user with name "password_policy_user" and password "aB$123" with roles "config_admin,system_group_admin,activation_key_admin,image_admin"
    Then the user creation should succeed

  Scenario: Reset password policy to default settings
    When I follow the left menu "Admin > Manager Configuration > Password Policy"
    And I click on "Reset"
    And I should see a "Password Policy Reset to Default" text
    And I refresh the page
    Then the following restrictions should be disabled:
      | Require Digits                  |
      | Require Lowercase Characters    |
      | Require Uppercase Characters    |
      | Require Special Characters      |
      | Restrict Characters Occurrences |
      | Restrict Consecutive Characters |

  Scenario: Cleanup: Delete test user
    When I delete user "password_policy_user"
