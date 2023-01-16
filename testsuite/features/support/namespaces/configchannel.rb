# Copyright (c) 2022-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

# Configuration Channel namespace
class NamespaceConfigchannel
  ##
  # It initializes the api_test variable.
  #
  # Args:
  #   api_test: This is the test object that is passed in from the test script.
  def initialize(api_test)
    @test = api_test
  end

  ##
  # Returns true if the configuration channel exists, false otherwise.
  #
  # Args:
  #   channel: The channel to check for existence.
  def channel_exists(channel)
    @test.call('configchannel.channelExists', sessionKey: @test.token, label: channel)
  end

  ##
  # Lists the files in a configuration channel.
  #
  # Args:
  #   channel: The configuration channel to list files from.
  def list_files(channel)
    @test.call('configchannel.listFiles', sessionKey: @test.token, label: channel)
  end

  ##
  # Returns a list of systems subscribed to the given configuration channel.
  #
  # Args:
  #   channel: The configuration channel you want to list the subscribed systems for.
  def list_subscribed_systems(channel)
    @test.call('configchannel.listSubscribedSystems', sessionKey: @test.token, label: channel)
  end

  ##
  # Gets a file revision of a configuration channel.
  #
  # Args:
  #   channel: The configuration channel name.
  #   file_path: A file path.
  #   revision: A revision number.
  def get_file_revision(channel, file_path, revision)
    @test.call('configchannel.getFileRevision', sessionKey: @test.token, label: channel, filePath: file_path, revision: revision)
  end

  ##
  # Creates a new configuration channel.
  #
  # Args:
  #   label: The label of the configuration channel.
  #   name: The name of the configuration channel.
  #   description: A description of the configuration channel.
  #   type: TODO
  def create(label, name, description, type)
    @test.call('configchannel.create', sessionKey: @test.token, label: label, name: name, description: description, type: type)
  end

  ##
  # Creates a new configuration channel with path information.
  #
  # Args:
  #   label: The label of the configuration channel.
  #   name: The name of the configuration channel.
  #   description: A short description of the configuration channel.
  #   type: normal, local_override, server_import, state
  #   info: Path information.
  def create_with_pathinfo(label, name, description, type, info)
    @test.call('configchannel.create', sessionKey: @test.token, label: label, name: name, description: description, type: type, pathInfo: info)
  end

  ##
  # Creates or updates a file in a channel.
  #
  # Args:
  #   channel: The configuration channel to create or update the file in.
  #   file: The file name, including the path.
  #   contents: The contents of the file.
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

  ##
  # Deploys all systems to the given configuration channel.
  #
  # Args:
  #   channel: The configuration channel to deploy to.
  def deploy_all_systems(channel)
    @test.call('configchannel.deployAllSystems', sessionKey: @test.token, label: channel)
  end

  ##
  # Deletes the specified channels.
  #
  # Args:
  #   channels: A list of configuration channel names to delete.
  def delete_channels(channels)
    @test.call('configchannel.deleteChannels', sessionKey: @test.token, labels: channels)
  end
end
