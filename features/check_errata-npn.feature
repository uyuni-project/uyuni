# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Check errata
  In Order to check if the errata import was successfull
  As the testing user
  I want to see the erratums in the web page including the packages

  Scenario: check all errata exists
    Given I am on the errata page
     When I follow "Relevant" in the left menu
     Then I should see a kernel update in the list
      And I should see a "slessp2-aaa_base-6544" link
      And I should see a "slessp2-nfs-client-6222" link

  Scenario: check slessp2-kernel-6648 errata
    Given I am on the errata page
     When I follow "All" in the left menu
     When I follow "slessp2-kernel-6648"
     Then I should see a "slessp2-kernel-6648 - Security Advisory" text
      And I should see a "Security update for Linux kernel" text
      And I should see a "SLES11-SP2-Updates x86_64 Channel" link
      And I should see a "CVE-2012-3400" link
      And I should see a "bug number 774285" link
      And I should see a "reboot_suggested" text

  Scenario: check slessp2-kernel-6648 errata packages
    Given I am on the errata page
     When I follow "All" in the left menu
     When I follow "slessp2-kernel-6648"
     When I follow "Packages"
     Then I should see a "SLES11-SP2-Updates x86_64 Channel" text
      And I should see a "sha256:8df43dc2fde43c20ce1764b80c2426fd85a2607e9173bcf879551db3bfa8ebaa" text
      And I should see a "kernel-default-3.0.38-0.5.1-x86_64" link
      And I should see a "sha256:abfbd95841d8d17140dcaed8c7919235601c8bc5be77e6be869fa28ef1c518d9" text
      And I should see a "kernel-default-base-3.0.38-0.5.1-x86_64" link
      And I should see a "sha256:8ddafcb2c5e68b55d3783b7302caf4e39f81e56a4ad50f3642b870ed900b6a09" text
      And I should see a "kernel-default-devel-3.0.38-0.5.1-x86_64" link
      And I should see a "sha256:93eca7a674f12b0e155ae2033097ca4e5208619c52151f8527e1485b752901f8" text
      And I should see a "kernel-ec2-3.0.38-0.5.1-x86_64" link
      And I should see a "sha256:64670232949f640f8c4669f35d17b6671f8ff93734590c330aab7660b71ac270" text
      And I should see a "kernel-ec2-base-3.0.38-0.5.1-x86_64" link
      And I should see a "sha256:961f6d4366b836d1b9fb07b3568225ac6681e8419ea9a252d381b703d51d1e5f" text
      And I should see a "kernel-ec2-devel-3.0.38-0.5.1-x86_64" link
      And I should see a "sha256:285b317d49b48dff46742fcd84c5a80fd63fe277a9384865b85b13fff02f1f47" text
      And I should see a "kernel-source-3.0.38-0.5.1-x86_64" link
      And I should see a "sha256:9cc632c5bedd4e44374bc9b97669f4b4959e14f52971e60d15853d1b288529e0" text
      And I should see a "kernel-syms-3.0.38-0.5.1-x86_64" link
      And I should see a "sha256:28d62a0703ba4daad2bc796a2d19448ea7d038921b6243432b75e49cb7c17162" text
      And I should see a "kernel-trace-3.0.38-0.5.1-x86_64" link
      And I should see a "sha256:37fd30a3ad050d2cfa09e63e6c617195f1b307ba8225644ea012dc8555708564" text
      And I should see a "kernel-trace-base-3.0.38-0.5.1-x86_64" link
      And I should see a "sha256:2066795e8e4123607c88c47c616aaf51872e8a1bd2b36039987e04efc869835a" text
      And I should see a "kernel-trace-devel-3.0.38-0.5.1-x86_64" link
      And I should see a "sha256:f063cc09c54f1786b5c054481c468cefe56a082d0bc65f2775a243f2db13aa60" text
      And I should see a "kernel-xen-3.0.38-0.5.1-x86_64" link
      And I should see a "sha256:6a48faaba82c8f87f2b9fe72f13b872df83add819d0a3bc28a1953e32390cb5b" text
      And I should see a "kernel-xen-base-3.0.38-0.5.1-x86_64" link
      And I should see a "sha256:2c9450b53a4531f767bdcb9f09a4bf474b7ac9c10bcf1f55d14cf1ef17b317e9" text
      And I should see a "kernel-xen-devel-3.0.38-0.5.1-x86_64" link

  Scenario: check slessp2-kernel-6641 errata
    Given I am on the errata page
     When I follow "All" in the left menu
     When I follow "Security Patches"
     When I follow "slessp2-kernel-6641"
     Then I should see a "slessp2-kernel-6641 - Security Advisory" text
      And I should see a "Security update for Linux kernel" text
      And I should see a "SLES11-SP2-Updates i586 Channel" link
      And I should see a "CVE-2012-3400" link
      And I should see a "bug number 774285" link
      And I should see a "reboot_suggested" text

  Scenario: check slessp2-kernel-6641 errata packages
    Given I am on the errata page
     When I follow "All" in the left menu
     When I follow "Security Patches"
     When I follow "slessp2-kernel-6641"
     When I follow "Packages"
     Then I should see a "SLES11-SP2-Updates i586 Channel" text
      And I should see a "sha256:3a0ac03693dec69394d54f45a8b6d398b697257a949a5a6cfffc9286a6a058d2" text
      And I should see a "kernel-default-3.0.38-0.5.1-i586" link
      And I should see a "sha256:3f6ee87700c6060d442e1f3ca285e9bcf1fd202a22411a3f3707713483bfc402" text
      And I should see a "kernel-default-base-3.0.38-0.5.1-i586" link
      And I should see a "sha256:671aa42ec5e409bd5fd81113733bae0edaa3874458071cce86300a85b1726511" text
      And I should see a "kernel-default-devel-3.0.38-0.5.1-i586" link
      And I should see a "sha256:c2709fa33e24066b78f8f58a3cd3d4160d116db5aefb85adc58e24c816c420f2" text
      And I should see a "kernel-ec2-3.0.38-0.5.1-i586" link
      And I should see a "sha256:97549f2c3bafc3c9df3823ad0d9543deaec3899cd871ba4b89a1c4035557ff8b" text
      And I should see a "kernel-ec2-base-3.0.38-0.5.1-i586" link
      And I should see a "sha256:f26ebb8c7eed10aca4d6441087775d5735a6d72b4fee3a6055a371e8347cd05a" text
      And I should see a "kernel-ec2-devel-3.0.38-0.5.1-i586" link
      And I should see a "sha256:39acdbdeccf5513c6974223d07de1d19016714062e7353a197c609499c677ee0" text
      And I should see a "kernel-pae-3.0.38-0.5.1-i586" link
      And I should see a "sha256:fd3731b2d27fe674e55557cc9dd8acae9cdb6b3c84efe365debdb13a4b494d0e" text
      And I should see a "kernel-pae-base-3.0.38-0.5.1-i586" link
      And I should see a "sha256:f1a5acd134472c1f4eb9d351d0591ebd56b3da628bb771b0580088e66638c715" text
      And I should see a "kernel-pae-devel-3.0.38-0.5.1-i586" link
      And I should see a "sha256:9edf4daa26e1ac6954f9947eb178b331c5129d756f78f2910e6251332ece06bd" text
      And I should see a "kernel-source-3.0.38-0.5.1-i586" link
      And I should see a "sha256:24000755328c50757b83a98b9b7f669ecd83b7fce11431a956141b8fd5b8c38c" text
      And I should see a "kernel-syms-3.0.38-0.5.1-i586" link
      And I should see a "sha256:fd1c9eadde0067f5369070353b989f772ff178142cad726fbe45e76d56e57fc8" text
      And I should see a "kernel-trace-3.0.38-0.5.1-i586" link
      And I should see a "sha256:70833a0fad7cd2c659f007248955e3bc023a99c0bd917cd5d36f9045d58e378f" text
      And I should see a "kernel-trace-base-3.0.38-0.5.1-i586" link
      And I should see a "sha256:862a1f3de93746eec80a469157b342829e8768adb23d5794351fb8af01591a6d" text
      And I should see a "kernel-trace-devel-3.0.38-0.5.1-i586" link
      And I should see a "sha256:6b15a2e0a1c10cac70399e3452dd5cc8087a4d965c952704349cd55eb9fcf13a" text
      And I should see a "kernel-xen-3.0.38-0.5.1-i586" link
      And I should see a "sha256:5c08b42d0c4341b0c8da7be0d883e4ef02832df07917fd1f837728d598704b9b" text
      And I should see a "kernel-xen-base-3.0.38-0.5.1-i586" link
      And I should see a "sha256:b223fd1fc836f457b8e5ab51f55aef2521feb59ef364bb73f923b4068f0b2500" text
      And I should see a "kernel-xen-devel-3.0.38-0.5.1-i586" link

  Scenario: check slessp2-nfs-client-6222 errata
    Given I am on the errata page
     When I follow "Relevant" in the left menu
     When I follow "slessp2-nfs-client-6222"
     Then I should see a "slessp2-nfs-client-6222 - Bug Fix Advisory" text
      And I should see a "Recommended update for NFS" text
      And I should see a "SLES11-SP2-Updates x86_64 Channel" link
      And I should see a "SLES11-SP2-Updates i586 Channel" link
      And I should see a "bug number 758492" link

  Scenario: check slessp2-nfs-client-6222 errata packages
    Given I am on the errata page
     When I follow "Relevant" in the left menu
     When I follow "slessp2-nfs-client-6222"
     When I follow "Packages"
     Then I should see a "SLES11-SP2-Updates x86_64 Channel" text
      And I should see a "sha256:ccaeb5bf742a88d13b93c7843f551d639d0968075c55a641dd32c299b50ef787" text
      And I should see a "nfs-client-1.2.3-18.23.1-x86_64" link
      And I should see a "sha256:ec4271aca114c77b79cc60df48aa574781f3df0b23d42078f12bcf90f92f7252" text
      And I should see a "nfs-doc-1.2.3-18.23.1-x86_64" link
      And I should see a "sha256:ef444f98c8bb3121470704d146d22b63c7c271834072089d964ca9ac3fdd4147" text
      And I should see a "nfs-kernel-server-1.2.3-18.23.1-x86_64" link
      And I should see a "SLES11-SP2-Updates i586 Channel" text
      And I should see a "sha256:e01f82d89098f786fcd482f2d2f0c8f77a32f87f8b2e943d83596fb98d899a65" text
      And I should see a "nfs-client-1.2.3-18.23.1-i586" link
      And I should see a "sha256:e30ee5d70a2940f56d10fd96a890540059c053d906857a7966bd4bda38436345" text
      And I should see a "nfs-doc-1.2.3-18.23.1-i586" link
      And I should see a "sha256:b6c47c384e69ebda60cd29e05636448a8633764bef9a70668c6bd1b01a833f1c" text
      And I should see a "nfs-kernel-server-1.2.3-18.23.1-i586" link

  Scenario: check relevant errata for this client
    Given I am on the Systems overview page of this client
     When I follow "Software" in class "content-nav"
     When I follow "Errata" in class "contentnav-row2"
     Then I should see a "Relevant Errata" text
      And I should see a "Security update for Linux kernel" text
      And I should see a "Recommended update for aaa_base" text
      And I should see a "Recommended update for NFS" text
      And I should see three links to the errata in the list
