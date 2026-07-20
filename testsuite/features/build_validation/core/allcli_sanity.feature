# Copyright (c) 2019-2026 SUSE LLC
# Licensed under the terms of the MIT license.

Feature: Sanity checks
  In order to use the product
  I want to be sure to use a sane environment

  Scenario: The server is healthy
    Then "server" should have a FQDN
    And reverse resolution should work for "server"
    And the clock from "server" should be exact
    And service "apache2" is active on "server"
    And service "cobblerd" is active on "server"
    And service "rhn-search" is active on "server"
    And service "salt-api" is active on "server"
    And service "salt-master" is active on "server"
    And service "taskomatic" is active on "server"
    And socket "tftp" is active on "server"
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

@sle12sp5_sshminion
  Scenario: The SLES 12 SP5 Salt SSH minion is healthy
    Then "sle12sp5_minion" should have a FQDN
    And reverse resolution should work for "sle12sp5_minion"
    And "sle12sp5_minion" should communicate with the server using public interface
    And the clock from "sle12sp5_minion" should be exact

@sle15sp3_minion
  Scenario: The SLES 15 SP3 minion is healthy
    Then "sle15sp3_minion" should have a FQDN
    And reverse resolution should work for "sle15sp3_minion"
    And "sle15sp3_minion" should communicate with the server using public interface
    And the clock from "sle15sp3_minion" should be exact

@sle15sp3_sshminion
  Scenario: The SLES 15 SP3 Salt SSH minion is healthy
    Then "sle15sp3_sshminion" should have a FQDN
    And reverse resolution should work for "sle15sp3_sshminion"
    And "sle15sp3_sshminion" should communicate with the server using public interface
    And the clock from "sle15sp3_sshminion" should be exact

@sle15sp4_minion
  Scenario: The SLES 15 SP4 minion is healthy
    Then "sle15sp4_minion" should have a FQDN
    And reverse resolution should work for "sle15sp4_minion"
    And "sle15sp4_minion" should communicate with the server using public interface
    And the clock from "sle15sp4_minion" should be exact

@sle15sp4_sshminion
  Scenario: The SLES 15 SP4 Salt SSH minion is healthy
    Then "sle15sp4_sshminion" should have a FQDN
    And reverse resolution should work for "sle15sp4_sshminion"
    And "sle15sp4_sshminion" should communicate with the server using public interface
    And the clock from "sle15sp4_sshminion" should be exact

@sle15sp5_minion
  Scenario: The SLES 15 SP5 minion is healthy
    Then "sle15sp5_minion" should have a FQDN
    And reverse resolution should work for "sle15sp5_minion"
    And "sle15sp5_minion" should communicate with the server using public interface
    And the clock from "sle15sp5_minion" should be exact

@sle15sp5_sshminion
  Scenario: The SLES 15 SP5 Salt SSH minion is healthy
    Then "sle15sp5_sshminion" should have a FQDN
    And reverse resolution should work for "sle15sp5_sshminion"
    And "sle15sp5_sshminion" should communicate with the server using public interface
    And the clock from "sle15sp5_sshminion" should be exact

@sle15sp6_minion
  Scenario: The SLES 15 SP6 minion is healthy
    Then "sle15sp6_minion" should have a FQDN
    And reverse resolution should work for "sle15sp6_minion"
    And "sle15sp6_minion" should communicate with the server using public interface
    And the clock from "sle15sp6_minion" should be exact

@sle15sp6_sshminion
  Scenario: The SLES 15 SP6 Salt SSH minion is healthy
    Then "sle15sp6_sshminion" should have a FQDN
    And reverse resolution should work for "sle15sp6_sshminion"
    And "sle15sp6_sshminion" should communicate with the server using public interface
    And the clock from "sle15sp6_sshminion" should be exact

@sle15sp7_minion
  Scenario: The SLES 15 SP7 minion is healthy
    Then "sle15sp7_minion" should have a FQDN
    And reverse resolution should work for "sle15sp7_minion"
    And "sle15sp7_minion" should communicate with the server using public interface
    And the clock from "sle15sp7_minion" should be exact

@sle15sp7_sshminion
  Scenario: The SLES 15 SP7 Salt SSH minion is healthy
    Then "sle15sp7_sshminion" should have a FQDN
    And reverse resolution should work for "sle15sp7_sshminion"
    And "sle15sp7_sshminion" should communicate with the server using public interface
    And the clock from "sle15sp7_sshminion" should be exact

@sle160_minion
  Scenario: The SLES 16.0 minion is healthy
    Then "sle160_minion" should have a FQDN
    And reverse resolution should work for "sle160_minion"
    And "sle160_minion" should communicate with the server using public interface
    And the clock from "sle160_minion" should be exact

@sle160_sshminion
  Scenario: The SLES 16.0 Salt SSH minion is healthy
    Then "sle160_sshminion" should have a FQDN
    And reverse resolution should work for "sle160_sshminion"
    And "sle160_sshminion" should communicate with the server using public interface
    And the clock from "sle160_sshminion" should be exact

@slemicro52_minion
  Scenario: The SLE Micro 5.2 minion is healthy
    Then "slemicro52_minion" should have a FQDN
    And reverse resolution should work for "slemicro52_minion"
    And "slemicro52_minion" should communicate with the server using public interface
    And the clock from "slemicro52_minion" should be exact

@slemicro52_sshminion
  Scenario: The SLE Micro 5.2 SSH minion is healthy
    Then "slemicro52_sshminion" should have a FQDN
    And reverse resolution should work for "slemicro52_sshminion"
    And "slemicro52_sshminion" should communicate with the server using public interface
    And the clock from "slemicro52_sshminion" should be exact

@slemicro53_minion
  Scenario: The SLE Micro 5.3 minion is healthy
    Then "slemicro53_minion" should have a FQDN
    And reverse resolution should work for "slemicro53_minion"
    And "slemicro53_minion" should communicate with the server using public interface
    And the clock from "slemicro53_minion" should be exact

@slemicro53_sshminion
  Scenario: The SLE Micro 5.3 SSH minion is healthy
    Then "slemicro53_sshminion" should have a FQDN
    And reverse resolution should work for "slemicro53_sshminion"
    And "slemicro53_sshminion" should communicate with the server using public interface
    And the clock from "slemicro53_sshminion" should be exact

@slemicro54_minion
  Scenario: The SLE Micro 5.4 minion is healthy
    Then "slemicro54_minion" should have a FQDN
    And reverse resolution should work for "slemicro54_minion"
    And "slemicro54_minion" should communicate with the server using public interface
    And the clock from "slemicro54_minion" should be exact

@slemicro54_sshminion
  Scenario: The SLE Micro 5.4 SSH minion is healthy
    Then "slemicro54_sshminion" should have a FQDN
    And reverse resolution should work for "slemicro54_sshminion"
    And "slemicro54_sshminion" should communicate with the server using public interface
    And the clock from "slemicro54_sshminion" should be exact

@slemicro55_minion
  Scenario: The SLE Micro 5.5 minion is healthy
    Then "slemicro55_minion" should have a FQDN
    And reverse resolution should work for "slemicro55_minion"
    And "slemicro55_minion" should communicate with the server using public interface
    And the clock from "slemicro55_minion" should be exact

@slemicro55_sshminion
  Scenario: The SLE Micro 5.5 SSH minion is healthy
    Then "slemicro55_sshminion" should have a FQDN
    And reverse resolution should work for "slemicro55_sshminion"
    And "slemicro55_sshminion" should communicate with the server using public interface
    And the clock from "slemicro55_sshminion" should be exact

@slmicro60_minion
  Scenario: The SL Micro 6.0 minion is healthy
    Then "slmicro60_minion" should have a FQDN
    And reverse resolution should work for "slmicro60_minion"
    And "slmicro60_minion" should communicate with the server using public interface
    And the clock from "slmicro60_minion" should be exact

@slmicro60_sshminion
  Scenario: The SL Micro 6.0 SSH minion is healthy
    Then "slmicro60_sshminion" should have a FQDN
    And reverse resolution should work for "slmicro60_sshminion"
    And "slmicro60_sshminion" should communicate with the server using public interface
    And the clock from "slmicro60_sshminion" should be exact

@slmicro61_minion
  Scenario: The SL Micro 6.1 minion is healthy
    Then "slmicro61_minion" should have a FQDN
    And reverse resolution should work for "slmicro61_minion"
    And "slmicro61_minion" should communicate with the server using public interface
    And the clock from "slmicro61_minion" should be exact

@slmicro61_sshminion
  Scenario: The SL Micro 6.1 SSH minion is healthy
    Then "slmicro61_sshminion" should have a FQDN
    And reverse resolution should work for "slmicro61_sshminion"
    And "slmicro61_sshminion" should communicate with the server using public interface
    And the clock from "slmicro61_sshminion" should be exact

@slmicro62_minion
  Scenario: The SL Micro 6.2 minion is healthy
    Then "slmicro62_minion" should have a FQDN
    And reverse resolution should work for "slmicro62_minion"
    And "slmicro62_minion" should communicate with the server using public interface
    And the clock from "slmicro62_minion" should be exact

@slmicro62_sshminion
  Scenario: The SL Micro 6.2 SSH minion is healthy
    Then "slmicro62_sshminion" should have a FQDN
    And reverse resolution should work for "slmicro62_sshminion"
    And "slmicro62_sshminion" should communicate with the server using public interface
    And the clock from "slmicro62_sshminion" should be exact

@alma8_minion
  Scenario: The Alma 8 Salt minion is healthy
    Then "alma8_minion" should have a FQDN
    And reverse resolution should work for "alma8_minion"
    And "alma8_minion" should communicate with the server using public interface
    And the clock from "alma8_minion" should be exact

@alma8_sshminion
  Scenario: The Alma 8 Salt SSH minion is healthy
    Then "alma8_sshminion" should have a FQDN
    And reverse resolution should work for "alma8_sshminion"
    And "alma8_sshminion" should communicate with the server using public interface
    And the clock from "alma8_sshminion" should be exact

@alma9_minion
  Scenario: The Alma 9 Salt minion is healthy
    Then "alma9_minion" should have a FQDN
    And reverse resolution should work for "alma9_minion"
    And "alma9_minion" should communicate with the server using public interface
    And the clock from "alma9_minion" should be exact

@alma9_sshminion
  Scenario: The Alma 9 Salt SSH minion is healthy
    Then "alma9_sshminion" should have a FQDN
    And reverse resolution should work for "alma9_sshminion"
    And "alma9_sshminion" should communicate with the server using public interface
    And the clock from "alma9_sshminion" should be exact

@alma10_minion
  Scenario: The Alma 10 Salt minion is healthy
    Then "alma10_minion" should have a FQDN
    And reverse resolution should work for "alma10_minion"
    And "alma10_minion" should communicate with the server using public interface
    And the clock from "alma10_minion" should be exact

@alma10_sshminion
  Scenario: The Alma 10 Salt SSH minion is healthy
    Then "alma10_sshminion" should have a FQDN
    And reverse resolution should work for "alma10_sshminion"
    And "alma10_sshminion" should communicate with the server using public interface
    And the clock from "alma10_sshminion" should be exact

@amazon2023_minion
  Scenario: The Amazon 2023 Salt minion is healthy
    Then "amazon2023_minion" should have a FQDN
    And reverse resolution should work for "amazon2023_minion"
    And "amazon2023_minion" should communicate with the server using public interface
    And the clock from "amazon2023_minion" should be exact

@amazon2023_sshminion
  Scenario: The Amazon 2023 Salt SSH minion is healthy
    Then "amazon2023_sshminion" should have a FQDN
    And reverse resolution should work for "amazon2023_sshminion"
    And "amazon2023_sshminion" should communicate with the server using public interface
    And the clock from "amazon2023_sshminion" should be exact

@centos7_minion
  Scenario: The CentOS 7 Salt minion is healthy
    Then "centos7_minion" should have a FQDN
    And reverse resolution should work for "centos7_minion"
    And "centos7_minion" should communicate with the server using public interface
    And the clock from "centos7_minion" should be exact

@centos7_sshminion
  Scenario: The CentOS 7 Salt SSH minion is healthy
    Then "centos7_sshminion" should have a FQDN
    And reverse resolution should work for "centos7_sshminion"
    And "centos7_sshminion" should communicate with the server using public interface
    And the clock from "centos7_sshminion" should be exact

@liberty9_minion
  Scenario: The Liberty 9 Salt minion is healthy
    Then "liberty9_minion" should have a FQDN
    And reverse resolution should work for "liberty9_minion"
    And "liberty9_minion" should communicate with the server using public interface
    And the clock from "liberty9_minion" should be exact

@liberty9_sshminion
  Scenario: The Liberty 9 Salt SSH minion is healthy
    Then "liberty9_sshminion" should have a FQDN
    And reverse resolution should work for "liberty9_sshminion"
    And "liberty9_sshminion" should communicate with the server using public interface
    And the clock from "liberty9_sshminion" should be exact

@oracle9_minion
  Scenario: The Oracle 9 Salt minion is healthy
    Then "oracle9_minion" should have a FQDN
    And reverse resolution should work for "oracle9_minion"
    And "oracle9_minion" should communicate with the server using public interface
    And the clock from "oracle9_minion" should be exact

@oracle9_sshminion
  Scenario: The Oracle 9 Salt SSH minion is healthy
    Then "oracle9_sshminion" should have a FQDN
    And reverse resolution should work for "oracle9_sshminion"
    And "oracle9_sshminion" should communicate with the server using public interface
    And the clock from "oracle9_sshminion" should be exact

@oracle10_minion
  Scenario: The Oracle 10 Salt minion is healthy
    Then "oracle10_minion" should have a FQDN
    And reverse resolution should work for "oracle10_minion"
    And "oracle10_minion" should communicate with the server using public interface
    And the clock from "oracle10_minion" should be exact

@oracle10_sshminion
  Scenario: The Oracle 10 Salt SSH minion is healthy
    Then "oracle10_sshminion" should have a FQDN
    And reverse resolution should work for "oracle10_sshminion"
    And "oracle10_sshminion" should communicate with the server using public interface
    And the clock from "oracle10_sshminion" should be exact

@rhel9_minion
  Scenario: The Red Hat Linux 9 Salt minion is healthy
    Then "rhel9_minion" should have a FQDN
    And reverse resolution should work for "rhel9_minion"
    And "rhel9_minion" should communicate with the server using public interface
    And the clock from "rhel9_minion" should be exact

@rhel9_sshminion
  Scenario: The Red Hat Linux 9 SSH minion is healthy
    Then "rhel9_sshminion" should have a FQDN
    And reverse resolution should work for "rhel9_sshminion"
    And "rhel9_sshminion" should communicate with the server using public interface
    And the clock from "rhel9_sshminion" should be exact

@rocky8_minion
  Scenario: The Rocky 8 Salt minion is healthy
    Then "rocky8_minion" should have a FQDN
    And reverse resolution should work for "rocky8_minion"
    And "rocky8_minion" should communicate with the server using public interface
    And the clock from "rocky8_minion" should be exact

@rocky8_sshminion
  Scenario: The Rocky 8 Salt SSH minion is healthy
    Then "rocky8_sshminion" should have a FQDN
    And reverse resolution should work for "rocky8_sshminion"
    And "rocky8_sshminion" should communicate with the server using public interface
    And the clock from "rocky8_sshminion" should be exact

@rocky9_minion
  Scenario: The Rocky 9 Salt minion is healthy
    Then "rocky9_minion" should have a FQDN
    And reverse resolution should work for "rocky9_minion"
    And "rocky9_minion" should communicate with the server using public interface
    And the clock from "rocky9_minion" should be exact

@rocky9_sshminion
  Scenario: The Rocky 9 Salt SSH minion is healthy
    Then "rocky9_sshminion" should have a FQDN
    And reverse resolution should work for "rocky9_sshminion"
    And "rocky9_sshminion" should communicate with the server using public interface
    And the clock from "rocky9_sshminion" should be exact

@rocky10_minion
  Scenario: The Rocky 10 Salt minion is healthy
    Then "rocky10_minion" should have a FQDN
    And reverse resolution should work for "rocky10_minion"
    And "rocky10_minion" should communicate with the server using public interface
    And the clock from "rocky10_minion" should be exact

@rocky10_sshminion
  Scenario: The Rocky 10 Salt SSH minion is healthy
    Then "rocky10_sshminion" should have a FQDN
    And reverse resolution should work for "rocky10_sshminion"
    And "rocky10_sshminion" should communicate with the server using public interface
    And the clock from "rocky10_sshminion" should be exact

@ubuntu2204_minion
  Scenario: The Ubuntu 22.04 minion is healthy
    Then "ubuntu2204_minion" should have a FQDN
    And reverse resolution should work for "ubuntu2204_minion"
    And "ubuntu2204_minion" should communicate with the server using public interface
    And the clock from "ubuntu2204_minion" should be exact

@ubuntu2204_sshminion
  Scenario: The Ubuntu 22.04 Salt SSH minion is healthy
    Then "ubuntu2204_sshminion" should have a FQDN
    And reverse resolution should work for "ubuntu2204_sshminion"
    And "ubuntu2204_sshminion" should communicate with the server using public interface
    And the clock from "ubuntu2204_sshminion" should be exact

@ubuntu2404_minion
  Scenario: The Ubuntu 24.04 minion is healthy
    Then "ubuntu2404_minion" should have a FQDN
    And reverse resolution should work for "ubuntu2404_minion"
    And "ubuntu2404_minion" should communicate with the server using public interface
    And the clock from "ubuntu2404_minion" should be exact

@ubuntu2404_sshminion
  Scenario: The Ubuntu 24.04 Salt SSH minion is healthy
    Then "ubuntu2404_sshminion" should have a FQDN
    And reverse resolution should work for "ubuntu2404_sshminion"
    And "ubuntu2404_sshminion" should communicate with the server using public interface
    And the clock from "ubuntu2404_sshminion" should be exact

@debian12_minion
  Scenario: The Debian 12 minion is healthy
    Then "debian12_minion" should have a FQDN
    And reverse resolution should work for "debian12_minion"
    And "debian12_minion" should communicate with the server using public interface
    And the clock from "debian12_minion" should be exact

@debian12_sshminion
  Scenario: The Debian 12 Salt SSH minion is healthy
    Then "debian12_sshminion" should have a FQDN
    And reverse resolution should work for "debian12_sshminion"
    And "debian12_sshminion" should communicate with the server using public interface
    And the clock from "debian12_sshminion" should be exact

@debian13_minion
  Scenario: The Debian 13 minion is healthy
    Then "debian13_minion" should have a FQDN
    And reverse resolution should work for "debian13_minion"
    And "debian13_minion" should communicate with the server using public interface
    And the clock from "debian13_minion" should be exact

@debian13_sshminion
  Scenario: The Debian 13 Salt SSH minion is healthy
    Then "debian13_sshminion" should have a FQDN
    And reverse resolution should work for "debian13_sshminion"
    And "debian13_sshminion" should communicate with the server using public interface
    And the clock from "debian13_sshminion" should be exact

@opensuse156arm_minion
  Scenario: The openSUSE 15.6 ARM minion is healthy
    Then "opensuse156arm_minion" should have a FQDN
    And reverse resolution should work for "opensuse156arm_minion"
    And "opensuse156arm_minion" should communicate with the server using public interface
    And the clock from "opensuse156arm_minion" should be exact

@opensuse156arm_sshminion
  Scenario: The openSUSE 15.6 ARM SSH minion is healthy
    Then "opensuse156arm_sshminion" should have a FQDN
    And reverse resolution should work for "opensuse156arm_sshminion"
    And "opensuse156arm_sshminion" should communicate with the server using public interface
    And the clock from "opensuse156arm_sshminion" should be exact

@opensuse160arm_minion
  Scenario: The openSUSE 16.0 ARM minion is healthy
    Then "opensuse160arm_minion" should have a FQDN
    And reverse resolution should work for "opensuse160arm_minion"
    And "opensuse160arm_minion" should communicate with the server using public interface
    And the clock from "opensuse160arm_minion" should be exact

@opensuse160arm_sshminion
  Scenario: The openSUSE 16.0 ARM SSH minion is healthy
    Then "opensuse160arm_sshminion" should have a FQDN
    And reverse resolution should work for "opensuse160arm_sshminion"
    And "opensuse160arm_sshminion" should communicate with the server using public interface
    And the clock from "opensuse160arm_sshminion" should be exact

@sle15sp5s390_minion
  Scenario: The SLES 15 SP5 s390x minion is healthy
    Then "sle15sp5s390_minion" should have a FQDN
    And reverse resolution should work for "sle15sp5s390_minion"
    And "sle15sp5s390_minion" should communicate with the server using public interface
    And the clock from "sle15sp5s390_minion" should be exact

@sle15sp5s390_sshminion
  Scenario: The SLES 15 SP5 s390x SSH minion is healthy
    Then "sle15sp5s390_sshminion" should have a FQDN
    And reverse resolution should work for "sle15sp5s390_sshminion"
    And "sle15sp5s390_sshminion" should communicate with the server using public interface
    And the clock from "sle15sp5s390_sshminion" should be exact

@sle15sp6_buildhost
  Scenario: The SLES 15 SP6 build host is healthy
    Then "sle15sp6_buildhost" should have a FQDN
    And reverse resolution should work for "sle15sp6_buildhost"
    And "sle15sp6_buildhost" should communicate with the server using public interface
    And the clock from "sle15sp6_buildhost" should be exact

@sle15sp7_buildhost
Scenario: The SLES 15 SP7 build host is healthy
  Then "sle15sp7_buildhost" should have a FQDN
  And reverse resolution should work for "sle15sp7_buildhost"
  And "sle15sp7_buildhost" should communicate with the server using public interface
  And the clock from "sle15sp7_buildhost" should be exact

@monitoring_server
  Scenario: The monitoring server is healthy
    Then "monitoring_server" should have a FQDN
    And reverse resolution should work for "monitoring_server"
    And "monitoring_server" should communicate with the server using public interface
    And the clock from "monitoring_server" should be exact
