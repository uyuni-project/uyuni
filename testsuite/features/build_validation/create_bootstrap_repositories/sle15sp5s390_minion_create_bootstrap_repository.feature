
# Copyright (c) 2023 SUSE LLC
# Licensed under the terms of the MIT license.

@sle15sp5s390_minion
Feature: Create bootstrap repository for <client>
  In order to be able to enroll clients with MU repositories
  As the system administrator
  I create all bootstrap repos with --with-custom-channels option

Scenario: Create the bootstrap repository for a SLES 15 SP5 s390x minion
  When I create the bootstrap repository for "sle15sp5s390_minion" on the server without flushing