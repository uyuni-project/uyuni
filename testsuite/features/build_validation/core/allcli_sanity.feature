# Copyright (c) 2019-2024 SUSE LLC
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

@sle15sp5_minion
  Scenario: The SLES 15 SP5 minion is healthy
    Then "sle15sp5_minion" should have a FQDN
    And reverse resolution should work for "sle15sp5_minion"
    And "sle15sp5_minion" should communicate with the server using public interface
    And the clock from "sle15sp4_minion" should be exact

@sle15sp5_ssh_minion
  Scenario: The SLES 15 SP5 Salt SSH minion is healthy
    Then "sle15sp5_ssh_minion" should have a FQDN
    And reverse resolution should work for "sle15sp5_ssh_minion"
    And "sle15sp5_ssh_minion" should communicate with the server using public interface
    And the clock from "sle15sp5_ssh_minion" should be exact

@slemicro51_minion
  Scenario: The SLE Micro 5.1 minion is healthy
    Then "slemicro51_minion" should have a FQDN
    And reverse resolution should work for "slemicro51_minion"
    And "slemicro51_minion" should communicate with the server using public interface
    And the clock from "slemicro51_minion" should be exact

@slemicro51_ssh_minion
  Scenario: The SLE Micro 5.1 SSH minion is healthy
    Then "slemicro51_ssh_minion" should have a FQDN
    And reverse resolution should work for "slemicro51_ssh_minion"
    And "slemicro51_ssh_minion" should communicate with the server using public interface
    And the clock from "slemicro51_ssh_minion" should be exact

@slemicro52_minion
  Scenario: The SLE Micro 5.2 minion is healthy
    Then "slemicro52_minion" should have a FQDN
    And reverse resolution should work for "slemicro52_minion"
    And "slemicro52_minion" should communicate with the server using public interface
    And the clock from "slemicro52_minion" should be exact

@slemicro52_ssh_minion
  Scenario: The SLE Micro 5.2 SSH minion is healthy
    Then "slemicro52_ssh_minion" should have a FQDN
    And reverse resolution should work for "slemicro52_ssh_minion"
    And "slemicro52_ssh_minion" should communicate with the server using public interface
    And the clock from "slemicro52_ssh_minion" should be exact

@slemicro53_minion
  Scenario: The SLE Micro 5.3 minion is healthy
    Then "slemicro53_minion" should have a FQDN
    And reverse resolution should work for "slemicro53_minion"
    And "slemicro53_minion" should communicate with the server using public interface
    And the clock from "slemicro53_minion" should be exact

@slemicro53_ssh_minion
  Scenario: The SLE Micro 5.3 SSH minion is healthy
    Then "slemicro53_ssh_minion" should have a FQDN
    And reverse resolution should work for "slemicro53_ssh_minion"
    And "slemicro53_ssh_minion" should communicate with the server using public interface
    And the clock from "slemicro53_ssh_minion" should be exact

@slemicro54_minion
  Scenario: The SLE Micro 5.4 minion is healthy
    Then "slemicro54_minion" should have a FQDN
    And reverse resolution should work for "slemicro54_minion"
    And "slemicro54_minion" should communicate with the server using public interface
    And the clock from "slemicro54_minion" should be exact

@slemicro54_ssh_minion
  Scenario: The SLE Micro 5.4 SSH minion is healthy
    Then "slemicro54_ssh_minion" should have a FQDN
    And reverse resolution should work for "slemicro54_ssh_minion"
    And "slemicro54_ssh_minion" should communicate with the server using public interface
    And the clock from "slemicro54_ssh_minion" should be exact

@slemicro55_minion
  Scenario: The SLE Micro 5.5 minion is healthy
    Then "slemicro55_minion" should have a FQDN
    And reverse resolution should work for "slemicro55_minion"
    And "slemicro55_minion" should communicate with the server using public interface
    And the clock from "slemicro55_minion" should be exact

@slemicro55_ssh_minion
  Scenario: The SLE Micro 5.5 SSH minion is healthy
    Then "slemicro55_ssh_minion" should have a FQDN
    And reverse resolution should work for "slemicro55_ssh_minion"
    And "slemicro55_ssh_minion" should communicate with the server using public interface
    And the clock from "slemicro55_ssh_minion" should be exact

@alma9_minion
  Scenario: The Alma 9 Salt minion is healthy
    Then "alma9_minion" should have a FQDN
    And reverse resolution should work for "alma9_minion"
    And "alma9_minion" should communicate with the server using public interface
    And the clock from "alma9_minion" should be exact

@alma9_ssh_minion
  Scenario: The Alma 9 Salt SSH minion is healthy
    Then "alma9_ssh_minion" should have a FQDN
    And reverse resolution should work for "alma9_ssh_minion"
    And "alma9_ssh_minion" should communicate with the server using public interface
    And the clock from "alma9_ssh_minion" should be exact

@centos7_minion
  Scenario: The CentOS 7 Salt minion is healthy
    Then "centos7_minion" should have a FQDN
    And reverse resolution should work for "centos7_minion"
    And "centos7_minion" should communicate with the server using public interface
    And the clock from "centos7_minion" should be exact

@centos7_ssh_minion
  Scenario: The CentOS 7 Salt SSH minion is healthy
    Then "centos7_ssh_minion" should have a FQDN
    And reverse resolution should work for "centos7_ssh_minion"
    And "centos7_ssh_minion" should communicate with the server using public interface
    And the clock from "centos7_ssh_minion" should be exact

@liberty9_minion
  Scenario: The Liberty 9 Salt minion is healthy
    Then "liberty9_minion" should have a FQDN
    And reverse resolution should work for "liberty9_minion"
    And "liberty9_minion" should communicate with the server using public interface
    And the clock from "liberty9_minion" should be exact

@liberty9_ssh_minion
  Scenario: The Liberty 9 Salt SSH minion is healthy
    Then "liberty9_ssh_minion" should have a FQDN
    And reverse resolution should work for "liberty9_ssh_minion"
    And "liberty9_ssh_minion" should communicate with the server using public interface
    And the clock from "liberty9_ssh_minion" should be exact

@oracle9_minion
  Scenario: The Oracle 9 Salt minion is healthy
    Then "oracle9_minion" should have a FQDN
    And reverse resolution should work for "oracle9_minion"
    And "oracle9_minion" should communicate with the server using public interface
    And the clock from "oracle9_minion" should be exact

@oracle9_ssh_minion
  Scenario: The Oracle 9 Salt SSH minion is healthy
    Then "oracle9_ssh_minion" should have a FQDN
    And reverse resolution should work for "oracle9_ssh_minion"
    And "oracle9_ssh_minion" should communicate with the server using public interface
    And the clock from "oracle9_ssh_minion" should be exact

@rhel9_minion
  Scenario: The Red Hat Linux 9 Salt minion is healthy
    Then "rhel9_minion" should have a FQDN
    And reverse resolution should work for "rhel9_minion"
    And "rhel9_minion" should communicate with the server using public interface
    And the clock from "rhel9_minion" should be exact

@rhel9_ssh_minion
  Scenario: The Red Hat Linux 9 SSH minion is healthy
    Then "rhel9_ssh_minion" should have a FQDN
    And reverse resolution should work for "rhel9_ssh_minion"
    And "rhel9_ssh_minion" should communicate with the server using public interface
    And the clock from "rhel9_ssh_minion" should be exact

@rocky8_minion
  Scenario: The Rocky 8 Salt minion is healthy
    Then "rocky8_minion" should have a FQDN
    And reverse resolution should work for "rocky8_minion"
    And "rocky8_minion" should communicate with the server using public interface
    And the clock from "rocky8_minion" should be exact

@rocky8_ssh_minion
  Scenario: The Rocky 8 Salt SSH minion is healthy
    Then "rocky8_ssh_minion" should have a FQDN
    And reverse resolution should work for "rocky8_ssh_minion"
    And "rocky8_ssh_minion" should communicate with the server using public interface
    And the clock from "rocky8_ssh_minion" should be exact

@rocky9_minion
  Scenario: The Rocky 9 Salt minion is healthy
    Then "rocky9_minion" should have a FQDN
    And reverse resolution should work for "rocky9_minion"
    And "rocky9_minion" should communicate with the server using public interface
    And the clock from "rocky9_minion" should be exact

@rocky9_ssh_minion
  Scenario: The Rocky 9 Salt SSH minion is healthy
    Then "rocky9_ssh_minion" should have a FQDN
    And reverse resolution should work for "rocky9_ssh_minion"
    And "rocky9_ssh_minion" should communicate with the server using public interface
    And the clock from "rocky9_ssh_minion" should be exact

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

@debian12_minion
  Scenario: The Debian 12 minion is healthy
    Then "debian12_minion" should have a FQDN
    And reverse resolution should work for "debian12_minion"
    And "debian12_minion" should communicate with the server using public interface
    And the clock from "debian12_minion" should be exact

@debian12_ssh_minion
  Scenario: The Debian 12 Salt SSH minion is healthy
    Then "debian12_ssh_minion" should have a FQDN
    And reverse resolution should work for "debian12_ssh_minion"
    And "debian12_ssh_minion" should communicate with the server using public interface
    And the clock from "debian12_ssh_minion" should be exact

@opensuse154arm_minion
  Scenario: The openSUSE 15.4 ARM minion is healthy
    Then "opensuse154arm_minion" should have a FQDN
    And reverse resolution should work for "opensuse154arm_minion"
    And "opensuse154arm_minion" should communicate with the server using public interface
    And the clock from "opensuse154arm_minion" should be exact

@opensuse154arm_ssh_minion
  Scenario: The openSUSE 15.4 ARM SSH minion is healthy
    Then "opensuse154arm_ssh_minion" should have a FQDN
    And reverse resolution should work for "opensuse154arm_ssh_minion"
    And "opensuse154arm_ssh_minion" should communicate with the server using public interface
    And the clock from "opensuse154arm_ssh_minion" should be exact

@opensuse155arm_minion
  Scenario: The openSUSE 15.5 ARM minion is healthy
    Then "opensuse155arm_minion" should have a FQDN
    And reverse resolution should work for "opensuse155arm_minion"
    And "opensuse155arm_minion" should communicate with the server using public interface
    And the clock from "opensuse155arm_minion" should be exact

@opensuse155arm_ssh_minion
  Scenario: The openSUSE 15.5 ARM SSH minion is healthy
    Then "opensuse155arm_ssh_minion" should have a FQDN
    And reverse resolution should work for "opensuse155arm_ssh_minion"
    And "opensuse155arm_ssh_minion" should communicate with the server using public interface
    And the clock from "opensuse155arm_ssh_minion" should be exact

@sle15sp5s390_minion
  Scenario: The SLES 15 SP5 s390x minion is healthy
    Then "sle15sp5s390_minion" should have a FQDN
    And reverse resolution should work for "sle15sp5s390_minion"
    And "sle15sp5s390_minion" should communicate with the server using public interface
    And the clock from "sle15sp5s390_minion" should be exact

@sle15sp5s390_ssh_minion
  Scenario: The SLES 15 SP5 s390x SSH minion is healthy
    Then "sle15sp5s390_ssh_minion" should have a FQDN
    And reverse resolution should work for "sle15sp5s390_ssh_minion"
    And "sle15sp5s390_ssh_minion" should communicate with the server using public interface
    And the clock from "sle15sp5s390_ssh_minion" should be exact

@sle12sp5_buildhost
  Scenario: The SLES 12 SP5 build host is healthy
    Then "sle12sp5_buildhost" should have a FQDN
    And reverse resolution should work for "sle12sp5_buildhost"
    And "sle12sp5_buildhost" should communicate with the server using public interface
    And the clock from "sle12sp5_buildhost" should be exact

@sle15sp4_buildhost
  Scenario: The SLES 15 SP4 build host is healthy
    Then "sle15sp4_buildhost" should have a FQDN
    And reverse resolution should work for "sle15sp4_buildhost"
    And "sle15sp4_buildhost" should communicate with the server using public interface
    And the clock from "sle15sp4_buildhost" should be exact

@monitoring_server
  Scenario: The monitoring server is healthy
    Then "monitoring_server" should have a FQDN
    And reverse resolution should work for "monitoring_server"
    And "monitoring_server" should communicate with the server using public interface
    And the clock from "monitoring_server" should be exact
