# Copyright (c) 2011-2021 SUSE LLC.
# Licensed under the terms of the MIT license.

require_relative 'xmlrpc_test'

# Channel Software API Namespace
class XMLRPCRepositoryTest < XMLRPCBaseTest
  # Get a list of repositories
  def repo_list
    repos = @connection.call('channel.software.listUserRepos', @sid)
    repos.map { |key| key['label'] }
  end
end
