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
      And I should see a "SLES11-SP1-Updates x86_64 Channel" link
      And I should see a "CVE-2010-2954" link
      And I should see a "Bug# 643922 bug number 643922" text
      And I should see a "reboot_suggested" text

  Scenario: check slessp1-kernel-3280-channel-x86_64 errata packages
    Given I am on the "slessp1-kernel-3280-channel-x86_64" errata Details page
     When I follow "Packages"
     Then I should see a "SLES11-SP1-Updates x86_64 Channel" text
      And I should see a "md5:ec01c6ef692ac79c8165eb30067b87db" text
      And I should see a "btrfs-kmp-default-0_2.6.32.23_0.3-0.3.20-x86_64" link
      And I should see a "md5:b541520e544e8502cabb9bcecf4d0340" text
      And I should see a "ext4dev-kmp-default-0_2.6.32.23_0.3-7.3.20-x86_64" link
      And I should see a "md5:77012b185a5069833084ee0f5bc0bc52" text
      And I should see a "hyper-v-kmp-default-0_2.6.32.23_0.3-0.7.15-x86_64" link
      And I should see a "md5:f13ea54d7d767082c60e9024259bf226" text
      And I should see a "kernel-default-2.6.32.23-0.3.1-x86_64" link
      And I should see a "md5:618910413c7bb6d0778291994207ac8b" text
      And I should see a "kernel-default-base-2.6.32.23-0.3.1-x86_64" link
      And I should see a "md5:f32cbce79d4ef2626fd8de34a8e2be06" text
      And I should see a "kernel-default-devel-2.6.32.23-0.3.1-x86_64" link
      And I should see a "md5:4bb575d8e74c0ea19863e6bd1654c70c" text
      And I should see a "kernel-source-2.6.32.23-0.3.1-x86_64" link
      And I should see a "md5:d15281fd59420b7f07ffba59a9484760" text
      And I should see a "kernel-syms-2.6.32.23-0.3.1-x86_64" link

  
  Scenario: check slessp1-kernel-3284-channel-ia32 errata
    Given I am on the "slessp1-kernel-3284-channel-ia32" errata Details page
     Then I should see a "slessp1-kernel-3284-channel-ia32 - Security Advisory" text
      And I should see a "Security update for the Linux kernel" text
      And I should see a "SLES11-SP1-Updates i586 Channel" link
      And I should see a "CVE-2010-2954" link
      And I should see a "Bug# 643922 bug number 643922" text
      And I should see a "reboot_suggested" text

  
  Scenario: check slessp1-kernel-3284-channel-ia32 errata packages
    Given I am on the "slessp1-kernel-3284-channel-ia32" errata Details page
     When I follow "Packages"
     Then I should see a "SLES11-SP1-Updates i586 Channel" text
      And I should see a "md5:3c348cc277fb80d070a0b24667868c18" text
      And I should see a "btrfs-kmp-default-0_2.6.32.23_0.3-0.3.20-i586" link
      And I should see a "md5:b59c757a85966a57d31b77c71aee4e40" text
      And I should see a "btrfs-kmp-pae-0_2.6.32.23_0.3-0.3.20-i586" link
      And I should see a "md5:223f158e3b3b5aac24ce0ba43ff9fb74" text
      And I should see a "ext4dev-kmp-default-0_2.6.32.23_0.3-7.3.20-i586" link
      And I should see a "md5:2848977c7316ca1e4799675038237ef6" text
      And I should see a "ext4dev-kmp-pae-0_2.6.32.23_0.3-7.3.20-i586" link
      And I should see a "md5:5a23e9e54a6e951e7ffeaca2b5e008dd" text
      And I should see a "hyper-v-kmp-default-0_2.6.32.23_0.3-0.7.15-i586" link
      And I should see a "md5:e522d780c76f1f2725e37523084e85d8" text
      And I should see a "hyper-v-kmp-pae-0_2.6.32.23_0.3-0.7.15-i586" link
      And I should see a "md5:4d50140d03718233a1b17ed740a16e40" text
      And I should see a "kernel-default-2.6.32.23-0.3.1-i586" link
      And I should see a "md5:0f69388be62b33803bfc84218c0827bb" text
      And I should see a "kernel-default-base-2.6.32.23-0.3.1-i586" link
      And I should see a "md5:895c1c9cc1aaf589f91d4df00528ab5a" text
      And I should see a "kernel-default-devel-2.6.32.23-0.3.1-i586" link
      And I should see a "md5:e522e4a0b71c13af66119afcf82b5b2c" text
      And I should see a "kernel-pae-2.6.32.23-0.3.1-i586" link
      And I should see a "md5:a6d5c8303bf0cf22f092a5aaa113ce66" text
      And I should see a "kernel-pae-base-2.6.32.23-0.3.1-i586" link
      And I should see a "md5:f0f40c29e1bd2d61d9f360a6a40d3d38" text
      And I should see a "kernel-pae-devel-2.6.32.23-0.3.1-i586" link
      And I should see a "md5:8962b377e791e44e2bf082f98100062d" text
      And I should see a "kernel-source-2.6.32.23-0.3.1-i586" link
      And I should see a "md5:ff526f233f5ba38ea24b059ca7d7495c" text
      And I should see a "kernel-syms-2.6.32.23-0.3.1-i586" link
















      
