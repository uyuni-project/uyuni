# Copyright (c) 2011-2017 SUSE LLC.
# Licensed under the terms of the MIT license.

require_relative 'xmlrpctest'

# repo xmlrpc test
class XMLRPCRepositoryTest < XMLRPCBaseTest
  def repo_list
    repos = @connection.call('channel.software.listUserRepos', @sid)
    repos.map { |key| key['label'] }
  end
end
