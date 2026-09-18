# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.
#
# Configure controller to establish communication via chrome driver
#  with a MLM/Uyuni server installed on RKE2 

@rke2
@no_user_creation
Feature: Establish secure communication with RKE2 server
  Scenario: Configure cert
    When I configure the certificate in the controller
