# Copyright (c) 2010-2011 SUSE Linux Products GmbH.
# Licensed under the terms of the MIT license.

Feature: Test XML-RPC "api" namespace.

   Scenario: List all Activation Keys
     Given I am logged in via XML-RPC/activationkey as user "admin" and password "admin"

     When I call listActivationKeys I should get some.


   Scenario: Activation key
     Given I am logged in via XML-RPC/activationkey as user "admin" and password "admin"

     When I create an AK with id "testkey", description "Key for testing" and limit of 10
     Then I should get it listed with a call of listActivationKeys.


#   Scenario: Channels
#     Given I am logged in via XML-RPC/activationkey as user "admin" and password "admin"
#     When I add config channels "foo" to a newly created key
#     Then I have to see a new config channel "foo"
#
#     When I add a child channel "bar"
#     Then I can see config child has been added.

     
   Scenario: Cleanup
     Then I should get key deleted.
