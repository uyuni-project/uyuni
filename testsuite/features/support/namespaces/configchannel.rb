# Copyright (c) 2022-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

# Represents a configuration channel in the Uyuni API.
class NamespaceConfigchannel
  # Initializes the NamespaceConfigchannel object.
  #
  # @param api_test [Object] The test object passed in from the test script.
  def initialize(api_test)
    @test = api_test
  end

  # Checks if the configuration channel exists.
  #
  # @param channel [String] The channel to check for existence.
  # @return [Boolean] Returns true if the configuration channel exists, false otherwise.
  def channel_exists(channel)
    @test.call('configchannel.channelExists', sessionKey: @test.token, label: channel)
  end

  # Lists the files in a configuration channel.
  #
  # @param channel [String] The configuration channel to list files from.
  def list_files(channel)
    @test.call('configchannel.listFiles', sessionKey: @test.token, label: channel)
  end

  # Returns a list of systems subscribed to the given configuration channel.
  #
  # @param channel [String] The configuration channel to list the subscribed systems for.
  def list_subscribed_systems(channel)
    @test.call('configchannel.listSubscribedSystems', sessionKey: @test.token, label: channel)
  end

  # Gets a file revision of a configuration channel.
  #
  # @param channel [String] The configuration channel name.
  # @param file_path [String] A file path.
  # @param revision [Integer] A revision number.
  def get_file_revision(channel, file_path, revision)
    @test.call('configchannel.getFileRevision', sessionKey: @test.token, label: channel, filePath: file_path, revision: revision)
  end

  # Creates a new configuration channel.
  #
  # @param label [String] The label of the configuration channel.
  # @param name [String] The name of the configuration channel.
  # @param description [String] A description of the configuration channel.
  # @param type [String] The type of the configuration channel.
  def create(label, name, description, type)
    @test.call('configchannel.create', sessionKey: @test.token, label: label, name: name, description: description, type: type)
  end

  # Creates a new configuration channel with path information.
  #
  # @param label [String] The label of the configuration channel.
  # @param name [String] The name of the configuration channel.
  # @param description [String] A short description of the configuration channel.
  # @param type [String] The type of the configuration channel.
  # @param info [Hash] Path information.
  def create_with_pathinfo(label, name, description, type, info)
    @test.call('configchannel.create', sessionKey: @test.token, label: label, name: name, description: description, type: type, pathInfo: info)
  end

  # Creates or updates a file in a channel.
  #
  # @param channel [String] The configuration channel to create or update the file in.
  # @param file [String] The file name, including the path.
  # @param contents [String] The contents of the file.
  def create_or_update_path(channel, file, contents)
    @test.call('configchannel.createOrUpdatePath',
               sessionKey: @test.token,
               label: channel,
               path: file,
               isDir: false,
               pathInfo: { contents: contents,
                           owner: 'root',
                           group: 'root',
                           permissions: '644' }
              )
  end

  # Deploys all systems to the given configuration channel.
  #
  # @param channel [String] The configuration channel to deploy to.
  def deploy_all_systems(channel)
    @test.call('configchannel.deployAllSystems', sessionKey: @test.token, label: channel)
  end

  # Deletes the specified channels.
  #
  # @param channels [Array<String>] A list of configuration channel names to delete.
  def delete_channels(channels)
    @test.call('configchannel.deleteChannels', sessionKey: @test.token, labels: channels)
  end
end
