# Copyright (c) 2022 SUSE LLC.
# Licensed under the terms of the MIT license.

# "kickstart" namespace
class NamespaceKickstart
  def initialize(api_test)
    @test = api_test
    @tree = NamespaceKickstartTree.new(api_test)
  end

  attr_reader :tree
end

# "kickstart.tree" namespace
class NamespaceKickstartTree
  def initialize(api_test)
    @test = api_test
  end

  def delete_tree_and_profiles(distro)
    @test.call('kickstart.tree.deleteTreeAndProfiles', sessionKey: @test.token, treeLabel: distro)
  end
end
