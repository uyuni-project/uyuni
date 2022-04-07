# Copyright (c) 2020-2022 SUSE LLC.
# Licensed under the terms of the MIT license.

require_relative 'api_test'

# APIConfigChannelTest class
class APIConfigChannelTest < APITestBase
  def channel_exists(channel)
    @connection.call('configchannel.channel_exists', @sid, channel)
  end

  def list_files(channel)
    @connection.call('configchannel.list_files', @sid, channel)
  end

  def list_subscribed_systems(channel)
    @connection.call('configchannel.list_subscribed_systems', @sid, channel)
  end

  def get_file_revision(channel, file_path, revision)
    @connection.call('configchannel.get_file_revision', @sid, channel, file_path, revision)
  end

  def create_channel(label, name, description, type)
    @connection.call('configchannel.create', @sid, label, name, description, type)
  end

  def create_channel_with_data(label, name, description, type, data)
    @connection.call('configchannel.create', @sid, label, name, description, type, data)
  end

  def create_or_update_path(channel, file, contents)
    @connection.call('configchannel.create_or_update_path',
                     @sid,
                     channel,
                     file,
                     false,
                     { "contents" => contents,
                       "owner" => "root",
                       "group" => "root",
                       "permissions" => "644" }
                    )
  end

  def deploy_all_systems(channel)
    @connection.call('configchannel.deploy_all_systems', @sid, channel)
  end

  def delete_channels(channels)
    @connection.call('configchannel.delete_channels', @sid, channels)
  end
end
