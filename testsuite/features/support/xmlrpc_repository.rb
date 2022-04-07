# Copyright (c) 2011-2022 SUSE LLC.
# Licensed under the terms of the MIT license.

require_relative 'api_test'

# APIRepositoryTest class
class APIRepositoryTest < APITestBase
  def repo_list
    repos = @connection.call('channel.software.listUserRepos', @sid)
    repos.map { |key| key['label'] }
  end
end
