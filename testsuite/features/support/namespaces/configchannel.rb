# Copyright (c) 2022 SUSE LLC.
# Licensed under the terms of the MIT license.

# "configchannel" namespace
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
  # Returns true if the channel exists, false otherwise
  #
  # Args:
  #   channel: The channel to check for existence.
  def channel_exists(channel)
    @test.call('configchannel.channelExists', sessionKey: @test.token, label: channel)
  end

  ##
  # `list_files` lists the files in a channel
  #
  # Args:
  #   channel: The channel to list files from.
  def list_files(channel)
    @test.call('configchannel.listFiles', sessionKey: @test.token, label: channel)
  end

  ##
  # Returns a list of systems subscribed to the given channel.
  #
  # Args:
  #   channel: The channel you want to list the subscribed systems for.
  def list_subscribed_systems(channel)
    @test.call('configchannel.listSubscribedSystems', sessionKey: @test.token, label: channel)
  end

  def get_file_revision(channel, file_path, revision)
    @test.call('configchannel.getFileRevision', sessionKey: @test.token, label: channel, filePath: file_path, revision: revision)
  end

  ##
  # `create` creates a new
  # resource
  #
  # Args:
  #   label: The label of the field. This is what will be displayed on the form.
  #   name: The name of the field. This is the name that will be used to access the field's value in the form.
  #   description: A description of the parameter.
  #   type:
  def create(label, name, description, type)
    @test.call('configchannel.create', sessionKey: @test.token, label: label, name: name, description: description, type: type)
  end

  ##
  # It creates a new question with the given label, name, description, type, and info.
  #
  # Args:
  #   label: The label of the field. This is what will be displayed to the user.
  #   name: The name of the field. This is the name that will be used to access the field's value in the form's data hash.
  #   description: A short description of the parameter.
  #   type: TODO
  #   info: a hash of information about the field.  The following keys are used:
  def create_with_pathinfo(label, name, description, type, info)
    @test.call('configchannel.create', sessionKey: @test.token, label: label, name: name, description: description, type: type, pathInfo: info)
  end

  ##
  # "Create or update a file in a channel."
  #
  # The first line of the function is a comment. It's a comment because it starts with a `#`
  #
  # Args:
  #   channel: The channel to create or update the file in.
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
  # Deploy all systems to the given channel.
  #
  # Args:
  #   channel: The channel to deploy to.
  def deploy_all_systems(channel)
    @test.call('configchannel.deployAllSystems', sessionKey: @test.token, label: channel)
  end

  ##
  # `delete_channels` deletes the specified channels
  #
  # Args:
  #   channels: A list of channel names to delete.
  def delete_channels(channels)
    @test.call('configchannel.deleteChannels', sessionKey: @test.token, labels: channels)
  end
end
