# Copyright (c) 2022-2025 SUSE LLC.
# Licensed under the terms of the MIT license.

# Channel namespace
class NamespaceChannel
  # Initializes the NamespaceChannelSoftware class.
  #
  # @param api_test [Object] This is the test object that is passed in from the test script.
  def initialize(api_test)
    @test = api_test
    @software = NamespaceChannelSoftware.new(api_test)
    @appstreams = NamespaceChannelAppstreams.new(api_test)
  end

  attr_reader :software, :appstreams

  # It returns the number of software channels in the system.
  #
  # @return [Integer] The number of software channels in the system.
  def get_software_channels_count
    channels = @test.call('channel.listSoftwareChannels', sessionKey: @test.token)
    channels.nil? ? 0 : channels.length
  end

  # Checks if a channel is valid, based on its label.
  #
  # @param label [String] The label of the channel you want to verify.
  # @return [Boolean] true if the channel is valid, false otherwise.
  def channel_verified?(label)
    @test.call('channel.listSoftwareChannels', sessionKey: @test.token)
         .map { |c| c['label'] }
         .include?(label)
  end

  # Lists all channels in the system.
  #
  # @return [Hash] A hash containing the channel labels as keys and channel details as values.
  def list_all_channels
    channels = @test.call('channel.listAllChannels', sessionKey: @test.token)

    mapped_channels =
      channels.map do |channel|
        [
          channel['label'],
          {
            'id' => channel['id'],
            'name' => channel['name'],
            'provider_name' => channel['provider_name'],
            'packages' => channel['packages'],
            'systems' => channel['systems'],
            'arch_name' => channel['arch_name']
          }
        ]
      end
    mapped_channels.to_h
  end

  # Lists the labels of all software channels in the system.
  #
  # @return [Array<String>] An array of channel labels.
  def list_software_channels
    channels = @test.call('channel.listSoftwareChannels', sessionKey: @test.token)
    channels.map { |channel| channel['label'] }
  end
end

# Software Channel namespace
class NamespaceChannelSoftware
  # Initializes the class.
  #
  # @param api_test [Object] This is the name of the test. It's used to create the test's directory.
  def initialize(api_test)
    @test = api_test
  end

  # Creates a new channel.
  #
  # @param label [String] The label of the channel.
  # @param name [String] The name of the channel.
  # @param summary [String] A short description of the channel.
  # @param arch [String] The architecture of the packages in the repo.
  # @param parent [Object] The parent of the new channel. This is a Channel object.
  def create(label, name, summary, arch, parent)
    @test.call('channel.software.create', sessionKey: @test.token, label: label, name: name, summary: summary, archLabel: arch, parentLabel: parent)
  end

  # Deletes the channel with the given label.
  #
  # @param label [String] The label of the channel to delete.
  def delete(label)
    @test.call('channel.software.delete', sessionKey: @test.token, channelLabel: label)
  end

  # Creates a new repository, with a given label and URL.
  #
  # @param label [String] The name of the repository.
  # @param url [String] The URL of the repository.
  def create_repo(label, url, type = 'yum')
    @test.call('channel.software.createRepo', sessionKey: @test.token, label: label, type: type, url: url)
  end

  # Associates a repository with a channel.
  #
  # @param channel_label [String] The label of the channel you want to associate the repo with.
  # @param repo_label [String] The label of the repository you want to associate with the channel.
  def associate_repo(channel_label, repo_label)
    @test.call('channel.software.associateRepo', sessionKey: @test.token, channelLabel: channel_label, repoLabel: repo_label)
  end

  # Removes a repository from the list of repositories to be processed.
  #
  # @param label [String] The label of the repo you want to remove.
  #
  # @return [Object] The value of the key in the hash that matches the label.
  def remove_repo(label)
    @test.call('channel.software.removeRepo', sessionKey: @test.token, label: label)
  end

  # Verifies if a given channel is a child of the given parent channel.
  #
  # @param child [String] The channel you want to check if it's a child of the parent channel.
  # @param parent [String] The possible parent channel.
  #
  # @return [Boolean] Returns true if the channel is a child of the parent channel, false otherwise.
  def parent_channel?(child, parent)
    channel = @test.call('channel.software.getDetails', sessionKey: @test.token, channelLabel: child)
    channel['parent_channel_label'] == parent
  end

  # Gets the details of a channel with the given label.
  #
  # @param label [String] The label of the channel.
  #
  # @return [Object] The details of the channel.
  def get_details(label)
    @test.call('channel.software.getDetails', sessionKey: @test.token, channelLabel: label)
  end

  # Lists the child channels for a given parent channel.
  #
  # @param parent_channel [String] The channel you want to list the children of.
  #
  # @return [Array<String>] An array of child channel labels.
  def list_child_channels(parent_channel)
    channels = @test.call('channel.listSoftwareChannels', sessionKey: @test.token)
    channel_labels = channels.map { |channel| channel['label'] }
    channel_labels.select { |channel| parent_channel?(channel, parent_channel) }
  end

  # Lists all the repos that the user has access to.
  #
  # @return [Array<String>] An array of repository labels.
  def list_user_repos
    repos = @test.call('channel.software.listUserRepos', sessionKey: @test.token)
    repos.map { |key| key['label'] }
  end

  # Lists the names of channels the system with the given system ID is subscribed to.
  #
  # @param system_id [String] The ID of the system.
  #
  # @return [Array<String>] An array of channel names.
  def list_system_channels(system_id)
    channels = @test.call('channel.software.listSystemChannels', sessionKey: @test.token, sid: system_id)
    channels.map { |channel| channel['name'] }
  end
end

# channel.appstreams namespace
class NamespaceChannelAppstreams
  # Initializes a new instance of the NamespaceChannelAppstreams class.
  #
  # @param api_test [Object] The test object that is passed to the initialize method.
  def initialize(api_test)
    @test = api_test
  end

  # Check if channel is modular.
  #
  # @param label [String] The label of the channel.
  #
  # @return [Boolean] Returns true if the channel is modular, false otherwise
  def modular?(label)
    @test.call('channel.appstreams.isModular', sessionKey: @test.token, channelLabel: label)
  end

  # List modular channels in users organization.
  #
  # @return [Array<String>] An array of modular channel names.
  def list_modular_channels
    channels = @test.call('channel.appstreams.listModular', sessionKey: @test.token)
    channels.map { |channel| channel['name'] }
  end

  # List available module streams for a given channel.
  #
  # @param label [String] The label of the channel.
  #
  # @return [Array<Object>] An array of objects representing each stream details
  def list_module_streams(label)
    @test.call('channel.appstreams.listModuleStreams', sessionKey: @test.token, channelLabel: label)
  end
end
