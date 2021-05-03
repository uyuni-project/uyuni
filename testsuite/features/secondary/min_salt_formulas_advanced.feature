# Copyright (c) 2017-2018 SUSE LLC
# Licensed under the terms of the MIT license.

@scope_formulas
Feature: Use advanced features of Salt formulas
  In order to use forms to apply changes to minions
  As an authorized user
  I want to be able to install and use Salt formulas

   Scenario: Log in as admin user
      Given I am authorized for the "Admin" section

  Scenario: Install a test formula package on the server
     When I install "form.yml" to custom formula metadata directory "testform"
     And I install "metadata.yml" to custom formula metadata directory "testform"
     When I follow the left menu "Salt > Formula Catalog"
     Then I should see a "testform" text

  Scenario: Assign test formula to minion via group formula
     When I follow the left menu "Systems > System Groups"
     When I follow "Create Group"
     And I enter "test-formula-group" as "name"
     And I enter "Test group with testform formula added" as "description"
     And I click on "Create Group"
     Then I should see a "System group test-formula-group created." text
     When I follow "Formulas" in the content area
     Then I should see a "Choose formulas:" text
     And I should see a "General System Configuration" text
     And I should see a "Testform" text
     When I check the "testform" formula
     And I click on "Save"
     And I follow "Target Systems"
     And I check the "sle_minion" client
     And I click on "Add Systems"
     Then I should see a "1 systems were added to test-formula-group server group." text


  Scenario: Verify default values
#    The refresh is necessary, bsc#1028285 does not cover this.
     When I refresh the pillar data
     Then the pillar data for "testing:str" should be "" on "sle_minion"
     And the pillar data for "testing:str_def" should be "defvalue" on "sle_minion"
     And the pillar data for "testing:str_or_null" should be "None" on "sle_minion"
     And the pillar data for "testing" should not contain "str_opt" on "sle_minion"
     And the pillar data for "testing:num" should be "" on "sle_minion"
     And the pillar data for "testing:num_def" should be "0" on "sle_minion"
     And the pillar data for "testing:num_or_null" should be "None" on "sle_minion"
     And the pillar data for "testing" should not contain "num_opt" on "sle_minion"
     And the pillar data for "testing:pw" should be "" on "sle_minion"
     And the pillar data for "testing:pw_or_null" should be "None" on "sle_minion"
     And the pillar data for "testing" should not contain "pw_opt" on "sle_minion"
     And the pillar data for "testing:list_of_strings" should contain "def_item1" on "sle_minion"
     And the pillar data for "testing:list_of_strings" should contain "def_item2" on "sle_minion"
     And the pillar data for "testing:dict_of_strings:name1" should be "def_value1" on "sle_minion"
     And the pillar data for "testing:dict_of_strings:name2" should be "def_value2" on "sle_minion"
     And the pillar data for "testing:list_of_dicts:0:name" should be "def_user" on "sle_minion"
     And the pillar data for "testing:list_of_dicts:0:password" should be "secret1" on "sle_minion"
     And the pillar data for "testing:list_of_dicts:0" should not contain "full_name" on "sle_minion"
     And the pillar data for "testing:dict_of_dicts:def_user:name" should be "root" on "sle_minion"
     And the pillar data for "testing:dict_of_dicts:def_user:password" should be "secret2" on "sle_minion"
     And the pillar data for "testing:dict_of_dicts:def_user:full_name" should be "None" on "sle_minion"
     And the pillar data for "testing:recursive_dict_of_dicts:def_gr1:group_name" should be "default group 1" on "sle_minion"
     And the pillar data for "testing:recursive_dict_of_dicts:def_gr1:entries:def_entry1:entry_name" should be "default entry 1" on "sle_minion"
     And the pillar data for "testing:recursive_dict_of_dicts:def_gr1:entries:def_entry1:entry_desc" should be "some text" on "sle_minion"

  Scenario: Fill in and verify non-default values in group formula
     When I follow the left menu "Systems > System Groups"
     When I follow "test-formula-group" in the content area
     And I follow "Formulas" in the content area
     And I follow first "Testform" in the content area
     And I enter "text1" as "testing#str"
     And I enter "text2" as "testing#str_def"
     And I enter "text3" as "testing#str_or_null"
     And I enter "text4" as "testing#str_opt"
     And I enter "1" as "testing#num"
     And I enter "2" as "testing#num_def"
     And I enter "3" as "testing#num_or_null"
     And I enter "4" as "testing#num_opt"
     And I enter "pw1" as "testing#pw"
     And I enter "pw2" as "testing#pw_or_null"
     And I enter "pw3" as "testing#pw_opt"
     And I click on "Save Formula"
     Then I should see a "Formula saved" text
     And the pillar data for "testing:str" should be "text1" on "sle_minion"
     And the pillar data for "testing:str_def" should be "text2" on "sle_minion"
     And the pillar data for "testing:str_or_null" should be "text3" on "sle_minion"
     And the pillar data for "testing:str_opt" should be "text4" on "sle_minion"
     And the pillar data for "testing:num" should be "1" on "sle_minion"
     And the pillar data for "testing:num_def" should be "2" on "sle_minion"
     And the pillar data for "testing:num_or_null" should be "3" on "sle_minion"
     And the pillar data for "testing:num_opt" should be "4" on "sle_minion"
     And the pillar data for "testing:pw" should be "pw1" on "sle_minion"
     And the pillar data for "testing:pw_or_null" should be "pw2" on "sle_minion"
     And the pillar data for "testing:pw_opt" should be "pw3" on "sle_minion"

  Scenario: Clear values in group formula and verify the defaults again
     When I follow the left menu "Systems > System Groups"
     When I follow "test-formula-group" in the content area
     And I follow "Formulas" in the content area
     And I follow first "Testform" in the content area
     And I click on "Clear values" and confirm
     And I click on "Save Formula"
     Then I should see a "Formula saved" text
     And the pillar data for "testing:str" should be "" on "sle_minion"
     And the pillar data for "testing:str_def" should be "defvalue" on "sle_minion"
     And the pillar data for "testing:str_or_null" should be "None" on "sle_minion"
     And the pillar data for "testing" should not contain "str_opt" on "sle_minion"
     And the pillar data for "testing:num" should be "" on "sle_minion"
     And the pillar data for "testing:num_def" should be "0" on "sle_minion"
     And the pillar data for "testing:num_or_null" should be "None" on "sle_minion"
     And the pillar data for "testing" should not contain "num_opt" on "sle_minion"
     And the pillar data for "testing:pw" should be "" on "sle_minion"
     And the pillar data for "testing:pw_or_null" should be "None" on "sle_minion"
     And the pillar data for "testing" should not contain "pw_opt" on "sle_minion"
     And the pillar data for "testing:list_of_strings" should contain "def_item1" on "sle_minion"
     And the pillar data for "testing:list_of_strings" should contain "def_item2" on "sle_minion"
     And the pillar data for "testing:dict_of_strings:name1" should be "def_value1" on "sle_minion"
     And the pillar data for "testing:dict_of_strings:name2" should be "def_value2" on "sle_minion"
     And the pillar data for "testing:list_of_dicts:0:name" should be "def_user" on "sle_minion"
     And the pillar data for "testing:list_of_dicts:0:password" should be "secret1" on "sle_minion"
     And the pillar data for "testing:list_of_dicts:0" should not contain "full_name" on "sle_minion"
     And the pillar data for "testing:dict_of_dicts:def_user:name" should be "root" on "sle_minion"
     And the pillar data for "testing:dict_of_dicts:def_user:password" should be "secret2" on "sle_minion"
     And the pillar data for "testing:dict_of_dicts:def_user:full_name" should be "None" on "sle_minion"
     And the pillar data for "testing:recursive_dict_of_dicts:def_gr1:group_name" should be "default group 1" on "sle_minion"
     And the pillar data for "testing:recursive_dict_of_dicts:def_gr1:entries:def_entry1:entry_name" should be "default entry 1" on "sle_minion"
     And the pillar data for "testing:recursive_dict_of_dicts:def_gr1:entries:def_entry1:entry_desc" should be "some text" on "sle_minion"

  Scenario: Fill in and verify mix of default and non-default values in group formula
     When I follow the left menu "Systems > System Groups"
     When I follow "test-formula-group" in the content area
     And I follow "Formulas" in the content area
     And I follow first "Testform" in the content area
     And I enter "text1" as "testing#str"
     And I enter "1" as "testing#num"
     And I enter "2" as "testing#num_def"
     And I enter "pw1" as "testing#pw"
     And I click on "Save Formula"
     Then I should see a "Formula saved" text
     And the pillar data for "testing:str" should be "text1" on "sle_minion"
     And the pillar data for "testing:str_def" should be "defvalue" on "sle_minion"
     And the pillar data for "testing:str_or_null" should be "None" on "sle_minion"
     And the pillar data for "testing" should not contain "str_opt" on "sle_minion"
     And the pillar data for "testing:num" should be "1" on "sle_minion"
     And the pillar data for "testing:num_def" should be "2" on "sle_minion"
     And the pillar data for "testing:num_or_null" should be "None" on "sle_minion"
     And the pillar data for "testing" should not contain "num_opt" on "sle_minion"
     And the pillar data for "testing:pw" should be "pw1" on "sle_minion"
     And the pillar data for "testing:pw_or_null" should be "None" on "sle_minion"
     And the pillar data for "testing" should not contain "pw_opt" on "sle_minion"

  Scenario: Verify that minion form inherits the values from group form
     Given I am on the Systems overview page of this "sle_minion"
     When I follow "Formulas" in the content area
     And I follow first "Testform" in the content area
     And I click on "Save Formula"
     Then I should see a "Formula saved" text
     And the pillar data for "testing:str" should be "text1" on "sle_minion"
     And the pillar data for "testing:str_def" should be "defvalue" on "sle_minion"
     And the pillar data for "testing:str_or_null" should be "None" on "sle_minion"
     And the pillar data for "testing" should not contain "str_opt" on "sle_minion"
     And the pillar data for "testing:num" should be "1" on "sle_minion"
     And the pillar data for "testing:num_def" should be "2" on "sle_minion"
     And the pillar data for "testing:num_or_null" should be "None" on "sle_minion"
     And the pillar data for "testing" should not contain "num_opt" on "sle_minion"
     And the pillar data for "testing:pw" should be "pw1" on "sle_minion"
     And the pillar data for "testing:pw_or_null" should be "None" on "sle_minion"
     And the pillar data for "testing" should not contain "pw_opt" on "sle_minion"

  Scenario: Fill in and verify non-default values in minion formula
     When I follow "Formulas" in the content area
     And I follow first "Testform" in the content area
     And I enter "min_text1" as "testing#str"
     And I enter "min_text2" as "testing#str_def"
     And I enter "min_text3" as "testing#str_or_null"
     And I enter "min_text4" as "testing#str_opt"
     And I enter "101" as "testing#num"
     And I enter "102" as "testing#num_def"
     And I enter "103" as "testing#num_or_null"
     And I enter "104" as "testing#num_opt"
     And I enter "min_pw1" as "testing#pw"
     And I enter "min_pw2" as "testing#pw_or_null"
     And I enter "min_pw3" as "testing#pw_opt"
     And I click on "Save Formula"
     Then I should see a "Formula saved" text
     And the pillar data for "testing:str" should be "min_text1" on "sle_minion"
     And the pillar data for "testing:str_def" should be "min_text2" on "sle_minion"
     And the pillar data for "testing:str_or_null" should be "min_text3" on "sle_minion"
     And the pillar data for "testing:str_opt" should be "min_text4" on "sle_minion"
     And the pillar data for "testing:num" should be "101" on "sle_minion"
     And the pillar data for "testing:num_def" should be "102" on "sle_minion"
     And the pillar data for "testing:num_or_null" should be "103" on "sle_minion"
     And the pillar data for "testing:num_opt" should be "104" on "sle_minion"
     And the pillar data for "testing:pw" should be "min_pw1" on "sle_minion"
     And the pillar data for "testing:pw_or_null" should be "min_pw2" on "sle_minion"
     And the pillar data for "testing:pw_opt" should be "min_pw3" on "sle_minion"

# https://github.com/SUSE/spacewalk/issues/4546
  Scenario: Clear values in minion formula and verify that the pillar is set to group values
     When I follow "Formulas" in the content area
     And I follow first "Testform" in the content area
     And I click on "Clear values" and confirm
     And I click on "Save Formula"
     Then I should see a "Formula saved" text
     And the pillar data for "testing:str" should be "text1" on "sle_minion"
     And the pillar data for "testing:str_def" should be "defvalue" on "sle_minion"
     And the pillar data for "testing:str_or_null" should be "None" on "sle_minion"
     And the pillar data for "testing" should not contain "str_opt" on "sle_minion"
     And the pillar data for "testing:num" should be "1" on "sle_minion"
     And the pillar data for "testing:num_def" should be "2" on "sle_minion"
     And the pillar data for "testing:num_or_null" should be "None" on "sle_minion"
     And the pillar data for "testing" should not contain "num_opt" on "sle_minion"
     And the pillar data for "testing:pw" should be "pw1" on "sle_minion"
     And the pillar data for "testing:pw_or_null" should be "None" on "sle_minion"
     And the pillar data for "testing" should not contain "pw_opt" on "sle_minion"

#TODO test for adding/removing items in edit-group

# this should not be necessary, but it is currently required to run this test repeatedly
# https://github.com/SUSE/spacewalk/issues/4513
  Scenario: Cleanup: remove "Testform" formula from "test-formula-group"
     When I follow the left menu "Systems > System Groups"
     And I follow "test-formula-group" in the content area
     And I follow "Formulas" in the content area
     Then I should see a "Choose formulas:" text
     And I should see a "Testform" text
     When I uncheck the "testform" formula
     And I click on "Save"
     Then I should see a "Formula saved" text
#    The refresh is necessary, bsc#1028285 does not cover this.
     When I refresh the pillar data
     Then the pillar data for "testing" should be empty on "sle_minion"

  Scenario: Cleanup: remove "test-formula-group" system group
     When I follow the left menu "Systems > System Groups"
     And I follow "test-formula-group" in the content area
     And I follow "Delete Group" in the content area
     When I click on "Confirm Deletion"
     Then I should see a "System group" text
     Then I should see a "test-formula-group" text
     And I should see a "deleted" text
#    The refresh is necessary, bsc#1028285 does not cover this.
     When I refresh the pillar data
     Then the pillar data for "testing" should be empty on "sle_minion"
