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
  # Deletes a Kickstart tree and all profiles associated with it.
  #
  # Args:
  #   distro: The name of the distribution you want to delete.
  def delete_tree_and_profiles(distro)
    @test.call('kickstart.tree.deleteTreeAndProfiles', sessionKey: @test.token, treeLabel: distro)
  end
end
