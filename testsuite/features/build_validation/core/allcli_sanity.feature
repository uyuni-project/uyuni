# Copyright (c) 2019-2022 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Sanity checks
  In order to use the product
  I want to be sure to use a sane environment

  Scenario: The server is healthy
    Then "server" should have a FQDN
    And reverse resolution should work for "server"
    And the clock from "server" should be exact
    And service "apache2" is enabled on "server"
    And service "apache2" is active on "server"
    And service "cobblerd" is enabled on "server"
    And service "cobblerd" is active on "server"
    And service "jabberd" is enabled on "server"
    And service "jabberd" is active on "server"
    And service "osa-dispatcher" is enabled on "server"
    And service "osa-dispatcher" is active on "server"
    And service "rhn-search" is enabled on "server"
    And service "rhn-search" is active on "server"
    And service "salt-api" is enabled on "server"
    And service "salt-api" is active on "server"
    And service "salt-master" is enabled on "server"
    And service "salt-master" is active on "server"
    And service "taskomatic" is enabled on "server"
    And service "taskomatic" is active on "server"
    And socket "tftp" is enabled on "server"
    And socket "tftp" is active on "server"
    And service "tomcat" is enabled on "server"
    And service "tomcat" is active on "server"

@proxy
  Scenario: The proxy is healthy
    Then "proxy" should have a FQDN
    And reverse resolution should work for "proxy"
    And "proxy" should communicate with the server using public interface
    And the clock from "proxy" should be exact

@sle12sp4_client
  Scenario: The SLES 12 SP4 traditional client is healthy
    Then "sle12sp4_client" should have a FQDN
    And reverse resolution should work for "sle12sp4_client"
    And "sle12sp4_client" should communicate with the server using public interface
    And the clock from "sle12sp4_client" should be exact

@sle12sp4_minion
  Scenario: The SLES 12 SP4 minion is healthy
    Then "sle12sp4_minion" should have a FQDN
    And reverse resolution should work for "sle12sp4_minion"
    And "sle12sp4_minion" should communicate with the server using public interface
    And the clock from "sle12sp4_minion" should be exact

@sle12sp4_ssh_minion
  Scenario: The SLES 12 SP4 Salt SSH minion is healthy
    Then "sle12sp4_minion" should have a FQDN
    And reverse resolution should work for "sle12sp4_minion"
    And "sle12sp4_minion" should communicate with the server using public interface
    And the clock from "sle12sp4_minion" should be exact

@sle12sp5_client
  Scenario: The SLES 12 SP5 traditional client is healthy
    Then "sle12sp5_client" should have a FQDN
    And reverse resolution should work for "sle12sp5_client"
    And "sle12sp5_client" should communicate with the server using public interface
    And the clock from "sle12sp5_client" should be exact

@sle12sp5_minion
  Scenario: The SLES 12 SP5 minion is healthy
    Then "sle12sp5_minion" should have a FQDN
    And reverse resolution should work for "sle12sp5_minion"
    And "sle12sp5_minion" should communicate with the server using public interface
    And the clock from "sle12sp5_minion" should be exact

@sle12sp5_ssh_minion
  Scenario: The SLES 12 SP5 Salt SSH minion is healthy
    Then "sle12sp5_minion" should have a FQDN
    And reverse resolution should work for "sle12sp5_minion"
    And "sle12sp5_minion" should communicate with the server using public interface
    And the clock from "sle12sp5_minion" should be exact

@sle15_client
  Scenario: The SLES 15 traditional client is healthy
    Then "sle15_client" should have a FQDN
    And reverse resolution should work for "sle15_client"
    And "sle15_client" should communicate with the server using public interface
    And the clock from "sle15_client" should be exact

@sle15_minion
  Scenario: The SLES 15 minion is healthy
    Then "sle15_minion" should have a FQDN
    And reverse resolution should work for "sle15_minion"
    And "sle15_minion" should communicate with the server using public interface
    And the clock from "sle15_minion" should be exact

@sle15_ssh_minion
  Scenario: The SLES 15 Salt SSH minion is healthy
    Then "sle15_ssh_minion" should have a FQDN
    And reverse resolution should work for "sle15_ssh_minion"
    And "sle15_ssh_minion" should communicate with the server using public interface
    And the clock from "sle15_ssh_minion" should be exact

@sle15sp1_client
  Scenario: The SLES 15 SP1 traditional client is healthy
    Then "sle15sp1_client" should have a FQDN
    And reverse resolution should work for "sle15sp1_client"
    And "sle15sp1_client" should communicate with the server using public interface
    And the clock from "sle15sp1_client" should be exact

@sle15sp1_minion
  Scenario: The SLES 15 SP1 minion is healthy
    Then "sle15sp1_minion" should have a FQDN
    And reverse resolution should work for "sle15sp1_minion"
    And "sle15sp1_minion" should communicate with the server using public interface
    And the clock from "sle15sp1_minion" should be exact

@sle15sp1_ssh_minion
  Scenario: The SLES 15 SP1 Salt SSH minion is healthy
    Then "sle15sp1_ssh_minion" should have a FQDN
    And reverse resolution should work for "sle15sp1_ssh_minion"
    And "sle15sp1_ssh_minion" should communicate with the server using public interface
    And the clock from "sle15sp1_ssh_minion" should be exact

@sle15sp2_client
  Scenario: The SLES 15 SP2 traditional client is healthy
    Then "sle15sp2_client" should have a FQDN
    And reverse resolution should work for "sle15sp2_client"
    And "sle15sp2_client" should communicate with the server using public interface
    And the clock from "sle15sp2_client" should be exact

@sle15sp2_minion
  Scenario: The SLES 15 SP2 minion is healthy
    Then "sle15sp2_minion" should have a FQDN
    And reverse resolution should work for "sle15sp2_minion"
    And "sle15sp2_minion" should communicate with the server using public interface
    And the clock from "sle15sp2_minion" should be exact

@sle15sp2_ssh_minion
  Scenario: The SLES 15 SP2 Salt SSH minion is healthy
    Then "sle15sp2_ssh_minion" should have a FQDN
    And reverse resolution should work for "sle15sp2_ssh_minion"
    And "sle15sp2_ssh_minion" should communicate with the server using public interface
    And the clock from "sle15sp2_ssh_minion" should be exact

@sle15sp3_client
  Scenario: The SLES 15 SP3 traditional client is healthy
    Then "sle15sp3_client" should have a FQDN
    And reverse resolution should work for "sle15sp3_client"
    And "sle15sp3_client" should communicate with the server using public interface
    And the clock from "sle15sp3_client" should be exact

@sle15sp3_minion
  Scenario: The SLES 15 SP3 minion is healthy
    Then "sle15sp3_minion" should have a FQDN
    And reverse resolution should work for "sle15sp3_minion"
    And "sle15sp3_minion" should communicate with the server using public interface
    And the clock from "sle15sp3_minion" should be exact

@sle15sp3_ssh_minion
  Scenario: The SLES 15 SP3 Salt SSH minion is healthy
    Then "sle15sp3_ssh_minion" should have a FQDN
    And reverse resolution should work for "sle15sp3_ssh_minion"
    And "sle15sp3_ssh_minion" should communicate with the server using public interface
    And the clock from "sle15sp3_ssh_minion" should be exact

@sle15sp4_client
  Scenario: The SLES 15 SP4 traditional client is healthy
    Then "sle15sp4_client" should have a FQDN
    And reverse resolution should work for "sle15sp4_client"
    And "sle15sp4_client" should communicate with the server using public interface
    And the clock from "sle15sp4_client" should be exact

@sle15sp4_minion
  Scenario: The SLES 15 SP4 minion is healthy
    Then "sle15sp4_minion" should have a FQDN
    And reverse resolution should work for "sle15sp4_minion"
    And "sle15sp4_minion" should communicate with the server using public interface
    And the clock from "sle15sp4_minion" should be exact

@sle15sp4_ssh_minion
  Scenario: The SLES 15 SP4 Salt SSH minion is healthy
    Then "sle15sp4_ssh_minion" should have a FQDN
    And reverse resolution should work for "sle15sp4_ssh_minion"
    And "sle15sp4_ssh_minion" should communicate with the server using public interface
    And the clock from "sle15sp4_ssh_minion" should be exact

@ceos7_client
  Scenario: The CentOS 7 traditional client is healthy
    Then "ceos7_client" should have a FQDN
    And reverse resolution should work for "ceos7_client"
    And "ceos7_client" should communicate with the server using public interface
    And the clock from "ceos7_client" should be exact

@ceos7_minion
  Scenario: The CentOS 7 Salt minion is healthy
    Then "ceos7_minion" should have a FQDN
    And reverse resolution should work for "ceos7_minion"
    And "ceos7_minion" should communicate with the server using public interface
    And the clock from "ceos7_minion" should be exact

@ceos7_ssh_minion
  Scenario: The CentOS 7 Salt SSH minion is healthy
    Then "ceos7_ssh_minion" should have a FQDN
    And reverse resolution should work for "ceos7_ssh_minion"
    And "ceos7_ssh_minion" should communicate with the server using public interface
    And the clock from "ceos7_ssh_minion" should be exact

@ceos8_minion
  Scenario: The CentOS 8 Salt minion is healthy
    Then "ceos8_minion" should have a FQDN
    And reverse resolution should work for "ceos8_minion"
    And "ceos8_minion" should communicate with the server using public interface
    And the clock from "ceos8_minion" should be exact

@ceos8_ssh_minion
  Scenario: The CentOS 8 Salt SSH minion is healthy
    Then "ceos8_ssh_minion" should have a FQDN
    And reverse resolution should work for "ceos8_ssh_minion"
    And "ceos8_ssh_minion" should communicate with the server using public interface
    And the clock from "ceos8_ssh_minion" should be exact

@ubuntu1804_minion
  Scenario: The Ubuntu 18.04 Salt minion is healthy
    Then "ubuntu1804_minion" should have a FQDN
    And reverse resolution should work for "ubuntu1804_minion"
    And "ubuntu1804_minion" should communicate with the server using public interface
    And the clock from "ubuntu1804_minion" should be exact

@ubuntu1804_ssh_minion
  Scenario: The Ubuntu 18.04 Salt SSH minion is healthy
    Then "ubuntu1804_ssh_minion" should have a FQDN
    And reverse resolution should work for "ubuntu1804_ssh_minion"
    And "ubuntu1804_ssh_minion" should communicate with the server using public interface
    And the clock from "ubuntu1804_ssh_minion" should be exact

@ubuntu2004_minion
  Scenario: The Ubuntu 20.04 minion is healthy
    Then "ubuntu2004_minion" should have a FQDN
    And reverse resolution should work for "ubuntu2004_minion"
    And "ubuntu2004_minion" should communicate with the server using public interface
    And the clock from "ubuntu2004_minion" should be exact

@ubuntu2004_ssh_minion
  Scenario: The Ubuntu 20.04 Salt SSH minion is healthy
    Then "ubuntu2004_ssh_minion" should have a FQDN
    And reverse resolution should work for "ubuntu2004_ssh_minion"
    And "ubuntu2004_ssh_minion" should communicate with the server using public interface
    And the clock from "ubuntu2004_ssh_minion" should be exact

@ubuntu2204_minion
  Scenario: The Ubuntu 22.04 minion is healthy
    Then "ubuntu2204_minion" should have a FQDN
    And reverse resolution should work for "ubuntu2204_minion"
    And "ubuntu2204_minion" should communicate with the server using public interface
    And the clock from "ubuntu2204_minion" should be exact

@ubuntu2204_ssh_minion
  Scenario: The Ubuntu 22.04 Salt SSH minion is healthy
    Then "ubuntu2204_ssh_minion" should have a FQDN
    And reverse resolution should work for "ubuntu2204_ssh_minion"
    And "ubuntu2204_ssh_minion" should communicate with the server using public interface
    And the clock from "ubuntu2204_ssh_minion" should be exact

@debian9_minion
  Scenario: The Debian 9 minion is healthy
    Then "debian9_minion" should have a FQDN
    And reverse resolution should work for "debian9_minion"
    And "debian9_minion" should communicate with the server using public interface
    And the clock from "debian9_minion" should be exact

@debian9_ssh_minion
  Scenario: The Debian 9 Salt SSH minion is healthy
    Then "debian9_ssh_minion" should have a FQDN
    And reverse resolution should work for "debian9_ssh_minion"
    And "debian9_ssh_minion" should communicate with the server using public interface
    And the clock from "debian9_ssh_minion" should be exact

@debian10_minion
  Scenario: The Debian 10 minion is healthy
    Then "debian10_minion" should have a FQDN
    And reverse resolution should work for "debian10_minion"
    And "debian10_minion" should communicate with the server using public interface
    And the clock from "debian10_minion" should be exact

@debian10_ssh_minion
  Scenario: The Debian 10 Salt SSH minion is healthy
    Then "debian10_ssh_minion" should have a FQDN
    And reverse resolution should work for "debian10_ssh_minion"
    And "debian10_ssh_minion" should communicate with the server using public interface
    And the clock from "debian10_ssh_minion" should be exact

@debian11_minion
  Scenario: The Debian 11 minion is healthy
    Then "debian11_minion" should have a FQDN
    And reverse resolution should work for "debian11_minion"
    And "debian11_minion" should communicate with the server using public interface
    And the clock from "debian11_minion" should be exact

@debian11_ssh_minion
  Scenario: The Debian 11 Salt SSH minion is healthy
    Then "debian11_ssh_minion" should have a FQDN
    And reverse resolution should work for "debian11_ssh_minion"
    And "debian11_ssh_minion" should communicate with the server using public interface
    And the clock from "debian11_ssh_minion" should be exact

@sle12sp5_buildhost
  Scenario: The SLES 12 SP5 build host is healthy
    Then "sle12sp5_buildhost" should have a FQDN
    And reverse resolution should work for "sle12sp5_buildhost"
    And "sle12sp5_buildhost" should communicate with the server using public interface
    And the clock from "sle12sp5_buildhost" should be exact

@sle15sp3_buildhost
  Scenario: The SLES 15 SP3 build host is healthy
    Then "sle15sp3_buildhost" should have a FQDN
    And reverse resolution should work for "sle15sp3_buildhost"
    And "sle15sp3_buildhost" should communicate with the server using public interface
    And the clock from "sle15sp3_buildhost" should be exact

@opensuse153arm_minion
  Scenario: The openSUSE 15.3 ARM minion is healthy
    Then "opensuse153arm_minion" should have a FQDN
    And reverse resolution should work for "opensuse153arm_minion"
    And "opensuse153arm_minion" should communicate with the server using public interface
    And the clock from "opensuse153arm_minion" should be exact
