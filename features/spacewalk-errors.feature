# Copyright (c) 2016 SUSE LLC
# Licensed under the terms of the MIT license

Feature: Check spacewalk logs for errors

  Scenario: Check spacewalk upd2date logs
    Then I control that up2date logs on client under test contains no Traceback error
