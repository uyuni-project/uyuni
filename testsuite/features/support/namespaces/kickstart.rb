# Copyright (c) 2022-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

# Kickstart namespace
class NamespaceKickstart
  # Initializes a new instance of the NamespaceKickstart class.
  #
  # @param api_test [Object] The test object passed to the initialize method.
  def initialize(api_test)
    @test = api_test
    @tree = NamespaceKickstartTree.new(api_test)
    @profile = NamespaceKickstartProfile.new(api_test)
  end

  attr_reader :tree, :profile

  # Creates a new kickstart profile using the default download URL for the kickstartable tree and kickstart host specified.
  #
  # @param name [String] The name of the new kickstart profile.
  # @param kstreelabel [String] The label of a kickstartable tree.
  # @param kshost [String] The Kickstart hostname (of a server or proxy) used to construct the default download URL.
  # @return [Object] The result of the 'kickstart.profile.createProfile' API call.
  def create_profile(name, kstreelabel, kshost)
    @test.call('kickstart.profile.createProfile', sessionKey: @test.token, profileLabel: name, vmType: 'none', kickstartableTreeLabel: kstreelabel, kickstartHost: kshost, rootPassword: 'linux', updateType: 'all')
  end

  def create_profile_using_import_file(name, kstreelabel, filename)
    file_content = File.read(filename)
    @test.call('kickstart.importRawFile', sessionKey: @test.token, profileLabel: name, vmType: 'none', kickstartableTreeLabel: kstreelabel, kickstartFileContents: file_content )
  end
end

# Kickstart.profile namespace
class NamespaceKickstartProfile
  # Initializes a new instance of the NamespaceKickstartProfile class.
  #
  # @param api_test [Object] The test object that is passed to the initialize method.
  def initialize(api_test)
    @test = api_test
  end

  # Associates a list of kickstart variables with the specified kickstart profile.
  #
  # @param profile [String] The name of the kickstart profile.
  # @param variables [Array<String>] A list of variables to set.
  def set_variables(profile, variables)
    @test.call('kickstart.profile.setVariables', sessionKey: @test.token, ksLabel: profile, variables: variables)
  end
end

# "kickstart.tree" namespace
class NamespaceKickstartTree
  # Initializes a new instance of the NamespaceKickstartTree class.
  #
  # @param api_test [Object] The test object that is passed to the initialize method.
  def initialize(api_test)
    @test = api_test
  end

  # Creates a Kickstart tree (Distribution).
  #
  # @param distro [String] The name of the kickstart tree (distribution) you want to create.
  # @param path [String] Path to the base or root of the distribution.
  # @param label [String] Label of the channel you want to associate with the distribution.
  # @param install [String] Label for KickstartInstallType - options can be obtained using kickart.tree's listInstallTypes.
  #                         Options can be: suse, sles15generic, sles12generic, sles11generic, sles10generic, rhel_9, rhel_8, rhel_7, rhel_6, generic_rpm, fedora18.
  def create_distro(distro, path, label, install)
    @test.call('kickstart.tree.create', sessionKey: @test.token, treeLabel: distro, basePath: path, channelLabel: label, installType: install)
  end

  # Creates a Kickstart tree (Distribution), adding kernel options as parameters.
  #
  # @param distro [String] The name of the kickstart tree (distribution) you want to create.
  # @param path [String] Path to the base or root of the distribution.
  # @param label [String] Label of the channel you want to associate with the distribution.
  # @param install [String] Label for KickstartInstallType - options can be obtained using kickart.tree's listInstallTypes.
  #                         Options can be: suse, sles15generic, sles12generic, sles11generic, sles10generic, rhel_9, rhel_8, rhel_7, rhel_6, generic_rpm, fedora18.
  # @param options [String] Options to be passed to the kernel when booting for the installation.
  # @param post_options [String] Options to be passed to the kernel when booting for the installation.
  def create_distro_w_kernel_options(distro, path, label, install, options, post_options)
    @test.call('kickstart.tree.create', sessionKey: @test.token, treeLabel: distro, basePath: path, channelLabel: label, installType: install, kernelOptions: options, postKernelOptions: post_options)
  end

  # Updates a Kickstart tree (Distribution).
  #
  # @param distro [String] The name of the kickstart tree (distribution) you want to update. This must match an existing distro.
  # @param path [String] Path to the base or root of the distribution.
  # @param label [String] Label of the channel you want to associate with the distribution.
  # @param install [String] Label for KickstartInstallType - options can be obtained using kickart.tree's listInstallTypes.
  #                         Options can be: suse, sles15generic, sles12generic, sles11generic, sles10generic, rhel_9, rhel_8, rhel_7, rhel_6, generic_rpm, fedora18.
  # @param options [String] Options to be passed to the kernel when booting for the installation.
  # @param post_options [String] Options to be passed to the kernel when booting for the installation.
  def update_distro(distro, path, label, install, options, post_options)
    @test.call('kickstart.tree.update', sessionKey: @test.token, treeLabel: distro, basePath: path, channelLabel: label, installType: install, kernelOptions: options, postKernelOptions: post_options)
  end

  # Deletes a Kickstart tree and all profiles associated with it.
  #
  # @param distro [String] The name of the distribution you want to delete.
  def delete_tree_and_profiles(distro)
    @test.call('kickstart.tree.deleteTreeAndProfiles', sessionKey: @test.token, treeLabel: distro)
  end
end
