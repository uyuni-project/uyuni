# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.

Feature: Check errata
  In Order to check if the errata import was successfull
  As the testing user
  I want to see the erratums in the web page including the packages

  Scenario: check all errata exists
    Given I am on the errata page
     When I follow "All" in the left menu
     Then I should see a "slessp1-kernel-3280-channel-x86_64" link
      And I should see a "slessp1-kernel-3284-channel-ia32" link
      And I should see a "slessp1-suseRegister-2953-channel-x86_64" link
      And I should see a "slessp1-suseRegister-2953-channel-ia32" link
      And I should see a "slessp1-aaa_base-sysvinit-2610-channel-ia32" link
      And I should see a "slessp1-aaa_base-sysvinit-2610-channel-x86_64" link

  Scenario: check slessp1-kernel-3280-channel-x86_64 errata
    Given I am on the "slessp1-kernel-3280-channel-x86_64" errata Details page
     Then I should see a "slessp1-kernel-3280-channel-x86_64 - Security Advisory" text
      And I should see a "Security update for the Linux kernel" text
      And I should see a "SLES11-SP2-Updates x86_64 Channel" link
      And I should see a "CVE-2010-2954" link
      And I should see a "bug number 643922" link
      And I should see a "reboot_suggested" text

  Scenario: check slessp1-kernel-3280-channel-x86_64 errata packages
    Given I am on the "slessp1-kernel-3280-channel-x86_64" errata Details page
     When I follow "Packages"
     Then I should see a "SLES11-SP2-Updates x86_64 Channel" text
      And I should see a "sha1:c21778f54a584edf499f933ecc576afc2ecee59a" text
      And I should see a "btrfs-kmp-default-0_2.6.32.23_0.3-0.3.20-x86_64" link
      And I should see a "sha1:7b46bb1b8d33f087dbde87185d0f68350ed10561" text
      And I should see a "ext4dev-kmp-default-0_2.6.32.23_0.3-7.3.20-x86_64" link
      And I should see a "sha1:094490150d7ae99006fcbd5764ca4b077c29175a" text
      And I should see a "hyper-v-kmp-default-0_2.6.32.23_0.3-0.7.15-x86_64" link
      And I should see a "sha1:608d36d744c329bdd8beb274d81962542c2dfdac" text
      And I should see a "kernel-default-2.6.32.23-0.3.1-x86_64" link
      And I should see a "sha1:3294bf013fa3e71a18c5be412cbe7e6d17e635b4" text
      And I should see a "kernel-default-base-2.6.32.23-0.3.1-x86_64" link
      And I should see a "sha1:7ceb829d56fe569f273c7bd404a8253d16a6a518" text
      And I should see a "kernel-default-devel-2.6.32.23-0.3.1-x86_64" link
      And I should see a "sha1:c7182864b8bba2552a3ca90a6375f9577eef84d2" text
      And I should see a "kernel-source-2.6.32.23-0.3.1-x86_64" link
      And I should see a "sha1:e17e0e7127175c2a1c596267a7c389d72f49c1e9" text
      And I should see a "kernel-syms-2.6.32.23-0.3.1-x86_64" link


  Scenario: check slessp1-kernel-3284-channel-ia32 errata
    Given I am on the "slessp1-kernel-3284-channel-ia32" errata Details page
     Then I should see a "slessp1-kernel-3284-channel-ia32 - Security Advisory" text
      And I should see a "Security update for the Linux kernel" text
      And I should see a "SLES11-SP2-Updates i586 Channel" link
      And I should see a "CVE-2010-2954" link
      And I should see a "bug number 643922" link
      And I should see a "reboot_suggested" text


  Scenario: check slessp1-kernel-3284-channel-ia32 errata packages
    Given I am on the "slessp1-kernel-3284-channel-ia32" errata Details page
     When I follow "Packages"
     Then I should see a "SLES11-SP2-Updates i586 Channel" text
      And I should see a "sha1:8edd91afef9289446126190c4b4b1e28c7a73d31" text
      And I should see a "btrfs-kmp-default-0_2.6.32.23_0.3-0.3.20-i586" link
      And I should see a "sha1:b6dc68b6733818353a6870de7dee8e96a34ffc8f" text
      And I should see a "btrfs-kmp-pae-0_2.6.32.23_0.3-0.3.20-i586" link
      And I should see a "sha1:e93b2c5e94f8dc1a51c2955c8c37a4ba8daa0db5" text
      And I should see a "ext4dev-kmp-default-0_2.6.32.23_0.3-7.3.20-i586" link
      And I should see a "sha1:e8b8aa1d267852189b9c5c9b7fdf9c5aadb474cf" text
      And I should see a "ext4dev-kmp-pae-0_2.6.32.23_0.3-7.3.20-i586" link
      And I should see a "sha1:0612c8de05a6b9ccb61738f2484b33ef7705414f" text
      And I should see a "hyper-v-kmp-default-0_2.6.32.23_0.3-0.7.15-i586" link
      And I should see a "sha1:772e0fdeda3f981a30eece1aa22460b1f119f485" text
      And I should see a "hyper-v-kmp-pae-0_2.6.32.23_0.3-0.7.15-i586" link
      And I should see a "sha1:673e28b63c8645eaf01540b8e2acac8443f1b095" text
      And I should see a "kernel-default-2.6.32.23-0.3.1-i586" link
      And I should see a "sha1:a86b73d3fef4da9c0a3b8e07b7b68568e63246f8" text
      And I should see a "kernel-default-base-2.6.32.23-0.3.1-i586" link
      And I should see a "sha1:2dd172160ad3c43685b96a4b35817a50c2b93b1d" text
      And I should see a "kernel-default-devel-2.6.32.23-0.3.1-i586" link
      And I should see a "sha1:f56f74fd82c98c60c7e7b770c7d6ef93ab3354d5" text
      And I should see a "kernel-pae-2.6.32.23-0.3.1-i586" link
      And I should see a "sha1:7ae32973e3c2a6b71973c3f5e90f001052ac290d" text
      And I should see a "kernel-pae-base-2.6.32.23-0.3.1-i586" link
      And I should see a "sha1:6bf54fc9eafcbf59231ddd3812e11c6714cd9e12" text
      And I should see a "kernel-pae-devel-2.6.32.23-0.3.1-i586" link
      And I should see a "sha1:443f103922e8c8c66b8237aabac06783139c7a1c" text
      And I should see a "kernel-source-2.6.32.23-0.3.1-i586" link
      And I should see a "sha1:5bf7870468911be1c3e29e0867feb9bdf36154dd" text
      And I should see a "kernel-syms-2.6.32.23-0.3.1-i586" link

  Scenario: check slessp1-suseRegister-2953-channel-x86_64 errata
    Given I am on the "slessp1-suseRegister-2953-channel-x86_64" errata Details page
     Then I should see a "slessp1-suseRegister-2953-channel-x86_64 - Bug Fix Advisory" text
      And I should see a "Recommended update for suseRegister" text
      And I should see a "SLES11-SP2-Updates x86_64 Channel" link
      And I should see a "bug number 546142" link
      And I should see a "restart_suggested" text

  Scenario: check slessp1-suseRegister-2953-channel-x86_64 errata packages
    Given I am on the "slessp1-suseRegister-2953-channel-x86_64" errata Details page
     When I follow "Packages"
     Then I should see a "SLES11-SP2-Updates x86_64 Channel" text
      And I should see a "sha1:053d8944718abcdb6ba1f5f641ecbecf8f961b62" text
      And I should see a "suseRegister-1.4-1.9.1-noarch" link

  Scenario: check slessp1-suseRegister-2953-channel-ia32 errata
    Given I am on the "slessp1-suseRegister-2953-channel-ia32" errata Details page
     Then I should see a "slessp1-suseRegister-2953-channel-ia32 - Bug Fix Advisory" text
      And I should see a "Recommended update for suseRegister" text
      And I should see a "SLES11-SP2-Updates i586 Channel" link
      And I should see a "bug number 546142" link
      And I should see a "restart_suggested" text

  Scenario: check slessp1-suseRegister-2953-channel-ia32 errata packages
    Given I am on the "slessp1-suseRegister-2953-channel-ia32" errata Details page
     When I follow "Packages"
     Then I should see a "SLES11-SP2-Updates i586 Channel" text
      And I should see a "sha1:76318bbd6b8ff7db3781c0ddb5f0c1b792abed7b" text
      And I should see a "suseRegister-1.4-1.9.1-noarch" link

  Scenario: check relevant errata for this client
    Given I am on the Systems overview page of this client
     When I follow "Software" in class "content-nav"
     When I follow "Errata" in class "contentnav-row2"
     Then I should see a "Relevant Errata" text
      And I should see a "Security update for the Linux kernel" text
      And I should see a "Recommended update for suseRegister" text
      And I should see a "Recommended update for aaa_base" text
      And I should see three links to the errata in the list
