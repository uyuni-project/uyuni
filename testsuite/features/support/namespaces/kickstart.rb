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
  end

  attr_reader :tree
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
