# Copyright (c) 2020-2021 SUSE LLC.
# Licensed under the terms of the MIT license.
#
require_relative 'xmlrpc_test'

# Config channel API Namespace
class XMLRPCConfigChannelTest < XMLRPCBaseTest
  # Check if configuration channel exist
  def channel_exists(channel)
    @connection.call('configchannel.channel_exists', @sid, channel)
  end

  # List files defined in a configuration channel
  def list_files(channel)
    @connection.call('configchannel.list_files', @sid, channel)
  end

  # List systems subscribed in a configuration channel
  def list_subscribed_systems(channel)
    @connection.call('configchannel.list_subscribed_systems', @sid, channel)
  end

  # Get file revision in a configuration channel
  def file_revision(channel, file_path, revision)
    @connection.call('configchannel.file_revision', @sid, channel, file_path, revision)
  end

  # Create a configuration channel
  def create_channel(label, name, description, type)
    @connection.call('configchannel.create', @sid, label, name, description, type)
  end

  # Create a configuration channel including data
  def create_channel_with_data(label, name, description, type, data)
    @connection.call('configchannel.create', @sid, label, name, description, type, data)
  end

  # Create or update a filepath inside a configuration channel
  def create_or_update_path(channel, file, contents)
    @connection.call(
      'configchannel.create_or_update_path',
      @sid,
      channel,
      file,
      false,
      {
        contents: contents,
        owner: 'root',
        group: 'root',
        permissions: '644'
      }
    )
  end

  # Deploy all systems subscribed into a configuration channel
  def deploy_all_systems(channel)
    @connection.call('configchannel.deploy_all_systems', @sid, channel)
  end

  # Delete a list of configuration channels
  def delete_channels(channels)
    @connection.call('configchannel.delete_channels', @sid, channels)
  end
end
