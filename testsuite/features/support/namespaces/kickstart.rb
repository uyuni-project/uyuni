# Copyright (c) 2022-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

# Kickstart namespace
class NamespaceKickstart
  ##
  # It initializes the function.
  #
  # Args:
  #   api_test: This is the test object that is passed to the initialize method.
  def initialize(api_test)
    @test = api_test
    @tree = NamespaceKickstartTree.new(api_test)
    @profile = NamespaceKickstartProfile.new(api_test)
  end

  attr_reader :tree
  attr_reader :profile

  ##
  # Create a new kickstart profile using the default download URL for the kickstartable tree and kickstart host
  # specified.
  #
  # Args:
  #   name: The name of the new kickstart profile
  #   vmType: Virtualization type, or none
  #   kstreelabel: The Label of a kickstartable tree
  #   kshost: The Kickstart hostname (of a server or proxy) used to construct the default download URL
  #   rootPassword: The root password
  #   updateType: Set the automatic ks tree update strategy for the profile. Valid choices are "none" or "all".
  def create_profile(name, kstreelabel, kshost)
    @test.call('kickstart.profile.createProfile', sessionKey: @test.token, profileLabel: name, vmType: 'none', kickstartableTreeLabel: kstreelabel, kickstartHost: kshost, rootPassword: 'linux', updateType: 'all')
  end
end

# Kickstart.profile namespace
class NamespaceKickstartProfile
  ##
  # It initializes the function.
  #
  # Args:
  #   api_test: This is the test object that is passed to the initialize method.
  def initialize(api_test)
    @test = api_test
  end

  ##
  # Associates list of kickstart variables with the specified kickstart profile
  #
  # Args:
  #   profile: The name of the kickstart profile
  #   variables: A list of variables to set
  def set_variables(profile, variables)
    @test.call('kickstart.profile.setVariables', sessionKey: @test.token, ksLabel: profile, variables: variables)
  end
end

# "kickstart.tree" namespace
class NamespaceKickstartTree
  ##
  # It initializes the api_test variable.
  #
  # Args:
  #   api_test: This is the test object that is passed to the initialize method.
  def initialize(api_test)
    @test = api_test
  end

  ##
  # Creates a Kickstart tree (Distribution).
  #
  # Args:
  #   distro:       The name of the kicktart tree (distribution) you want to create.
  #   path:         Path to the base or root of the distribution.
  #   label:        Label of the channel you want to associate with the disribution.
  #   install:      Label for KickstartInstallType - options can be obtained using kickart.tree's listInstallTypes.
  #                 Options can be: suse, sles15generic, sles12generic, sles11generic, sles10generic, rhel_9, rhel_8, rhel_7, rhel_6, generic_rpm, fedora18.
  def create_distro(distro, path, label, install)
    @test.call('kickstart.tree.create', sessionKey: @test.token, treeLabel: distro, basePath: path, channelLabel: label, installType: install)
  end

  ##
  # Creates a Kickstart tree (Distribution), adding kernel options as parameters.
  #
  # Args:
  #   distro:       The name of the kicktart tree (distribution) you want to create.
  #   path:         Path to the base or root of the distribution.
  #   label:        Label of the channel you want to associate with the disribution.
  #   install:      Label for KickstartInstallType - options can be obtained using kickart.tree's listInstallTypes.
  #                 Options can be: suse, sles15generic, sles12generic, sles11generic, sles10generic, rhel_9, rhel_8, rhel_7, rhel_6, generic_rpm, fedora18.
  #   options:      Options to be passed to the kernel when booting for the installation.
  #   post_options: Options to be passed to the kernel when booting for the installation.
  def create_distro_w_kernel_options(distro, path, label, install, options, post_options)
    @test.call('kickstart.tree.create', sessionKey: @test.token, treeLabel: distro, basePath: path, channelLabel: label, installType: install, kernelOptions: options, postKernelOptions: post_options)
  end

  ##
  # Updates a Kickstart tree (Distribution).
  #
  # Args:
  #   distro:       The name of the kicktart tree (distribution) you want to update. This must match an existing distro.
  #   path:         Path to the base or root of the distribution.
  #   label:        Label of the channel you want to associate with the disribution.
  #   install:      Label for KickstartInstallType - options can be obtained using kickart.tree's listInstallTypes.
  #                 Options can be: suse, sles15generic, sles12generic, sles11generic, sles10generic, rhel_9, rhel_8, rhel_7, rhel_6, generic_rpm, fedora18.
  #   options:      Options to be passed to the kernel when booting for the installation.
  #   post_options: Options to be passed to the kernel when booting for the installation.
  def update_distro(distro, path, label, install, options, post_options)
    @test.call('kickstart.tree.update', sessionKey: @test.token, treeLabel: distro, basePath: path, channelLabel: label, installType: install, kernelOptions: options, postKernelOptions: post_options)
  end

  ##
  # Deletes a Kickstart tree and all profiles associated with it.
  #
  # Args:
  #   distro: The name of the distribution you want to delete.
  def delete_tree_and_profiles(distro)
    @test.call('kickstart.tree.deleteTreeAndProfiles', sessionKey: @test.token, treeLabel: distro)
  end
end
