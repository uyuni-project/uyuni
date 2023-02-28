# Copyright (c) 2021-2023 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Create bootstrap repositories
  In order to be able to enroll clients with MU repositories
  As the system administrator
  I create all bootstrap repos with --with-custom-channels option

@proxy
  Scenario: Create the bootstrap repository for the SUSE Manager proxy
    When I create the bootstrap repository for "proxy" on the server

@sle12sp4_client
  Scenario: Create the bootstrap repository for a SLES 12 SP4 traditional client
    When I create the bootstrap repository for "sle12sp4_client" on the server

@sle12sp4_minion
  Scenario: Create the bootstrap repository for a SLES 12 SP4 minion
    When I create the bootstrap repository for "sle12sp4_minion" on the server

@sle12sp5_client
  Scenario: Create the bootstrap repository for a SLES 12 SP5 traditional client
    When I create the bootstrap repository for "sle12sp5_client" on the server

@sle12sp5_minion
  Scenario: Create the bootstrap repository for a SLES 12 SP5 minion
    When I create the bootstrap repository for "sle12sp5_minion" on the server

@sle15sp1_client
  Scenario: Create the bootstrap repository for a SLES 15 SP1 traditional client
    When I create the bootstrap repository for "sle15sp1_client" on the server

@sle15sp1_minion
  Scenario: Create the bootstrap repository for a SLES 15 SP1 minion
    When I create the bootstrap repository for "sle15sp1_minion" on the server

@sle15sp2_client
  Scenario: Create the bootstrap repository for a SLES 15 SP2 traditional client
    When I create the bootstrap repository for "sle15sp2_client" on the server

@sle15sp2_minion
  Scenario: Create the bootstrap repository for a SLES 15 SP2 minion
    When I create the bootstrap repository for "sle15sp2_minion" on the server

@sle15sp3_client
  Scenario: Create the bootstrap repository for a SLES 15 SP3 traditional client
    When I create the bootstrap repository for "sle15sp3_client" on the server

@sle15sp3_minion
  Scenario: Create the bootstrap repository for a SLES 15 SP3 minion
    When I create the bootstrap repository for "sle15sp3_minion" on the server

@sle15sp4_client
  Scenario: Create the bootstrap repository for a SLES 15 SP4 traditional client
    When I create the bootstrap repository for "sle15sp4_client" on the server

@sle15sp4_minion
  Scenario: Create the bootstrap repository for a SLES 15 SP4 minion
    When I create the bootstrap repository for "sle15sp4_minion" on the server

@monitoring_server
  Scenario: Create the bootstrap repository for the monitoring server
    When I create the bootstrap repository for "monitoring_server" on the server

@slemicro52_minion
  Scenario: Create the bootstrap repository for a SLE Micro 5.2 minion
    When I create the bootstrap repository for "slemicro52_minion" on the server

@slemicro52_ssh_minion
  Scenario: Create the bootstrap repository for a SLE Micro 5.2 SSH minion
    When I create the bootstrap repository for "slemicro52_ssh_minion" on the server

@slemicro53_minion
  Scenario: Create the bootstrap repository for a SLE Micro 5.3 minion
    When I create the bootstrap repository for "slemicro53_minion" on the server

@slemicro53_ssh_minion
  Scenario: Create the bootstrap repository for a SLE Micro 5.3 SSH minion
    When I create the bootstrap repository for "slemicro53_ssh_minion" on the server

@alma9_minion
  Scenario: Create the bootstrap repository for a Alma 9 Salt minion
    When I create the bootstrap repository for "alma9_minion" on the server

@centos7_client
  Scenario: Create the bootstrap repository for a CentOS 7 traditional client
    When I create the bootstrap repository for "centos7_client" on the server

@centos7_minion
  Scenario: Create the bootstrap repository for a CentOS 7 Salt minion
    When I create the bootstrap repository for "centos7_minion" on the server

@liberty9_minion
  Scenario: Create the bootstrap repository for a Liberty 9 Salt minion
    When I create the bootstrap repository for "liberty9_minion" on the server

@oracle9_minion
  Scenario: Create the bootstrap repository for a Oracle 9 Salt minion
    When I create the bootstrap repository for "oracle9_minion" on the server

@rhel9_minion
  Scenario: Create the bootstrap repository for a Rhel 9 Salt minion
    When I create the bootstrap repository for "rhel9_minion" on the server

@rocky8_minion
  Scenario: Create the bootstrap repository for a Rocky 8 Salt minion
    When I create the bootstrap repository for "rocky8_minion" on the server

@rocky9_minion
  Scenario: Create the bootstrap repository for a Rocky 9 Salt minion
    When I create the bootstrap repository for "rocky9_minion" on the server

@ubuntu1804_minion
  Scenario: Create the bootstrap repository for a Ubuntu 18.04 Salt minion
    When I create the bootstrap repository for "ubuntu1804_minion" on the server

@ubuntu2004_minion
  Scenario: Create the bootstrap repository for a Ubuntu 20.04 minion
    When I create the bootstrap repository for "ubuntu2004_minion" on the server

@ubuntu2204_minion
  Scenario: Create the bootstrap repository for a Ubuntu 22.04 minion
    When I create the bootstrap repository for "ubuntu2204_minion" on the server

@debian10_minion
  Scenario: Create the bootstrap repository for a Debian 10 minion
    When I create the bootstrap repository for "debian10_minion" on the server

@debian11_minion
  Scenario: Create the bootstrap repository for a Debian 11 minion
    When I create the bootstrap repository for "debian11_minion" on the server

@opensuse154arm_minion
  Scenario: Create the bootstrap repository for a OpenSUSE 15.4 ARM minion
    When I create the bootstrap repository for "opensuse154arm_minion" on the server
