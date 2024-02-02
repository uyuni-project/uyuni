# Copyright (c) 2022-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

# Channel namespace
class NamespaceChannel
  ##
  # Initializes the NamespaceChannelSoftware class.
  #
  # Args:
  #   api_test: This is the test object that is passed in from the test script.
  def initialize(api_test)
    @test = api_test
    @software = NamespaceChannelSoftware.new(api_test)
  end

  attr_reader :software

  ##
  # It returns the number of software channels in the system.
  def get_software_channels_count
    channels = @test.call('channel.listSoftwareChannels', sessionKey: @test.token)
    channels.nil? ? 0 : channels.length
  end

  ##
  # Checks if a channel is valid, based on its label.
  #
  # Args:
  #   label: The label of the channel you want to verify.
  def verify_channel(label)
    @test.call('channel.listSoftwareChannels', sessionKey: @test.token)
         .map { |c| c['label'] }
         .include?(label)
  end

  def list_software_channels
    channels = @test.call('channel.listSoftwareChannels', sessionKey: @test.token)
    channels.map { |channel| channel['label'] }
  end
end

# Software Channel namespace
class NamespaceChannelSoftware
  ##
  # It initializes the class.
  #
  # Args:
  #   api_test: This is the name of the test. It's used to create the test's directory.
  def initialize(api_test)
    @test = api_test
  end

  ##
  # Creates a new repository.
  #
  # Args:
  #   label: The label of the repository.
  #   name: The name of the repository.
  #   summary: A short description of the repository.
  #   arch: The architecture of the packages in the repo.
  #   parent: The parent of the new repository. This is a Repository object.
  def create(label, name, summary, arch, parent)
    @test.call('channel.software.create', sessionKey: @test.token, label: label, name: name, summary: summary, archLabel: arch, parentLabel: parent)
  end

  ##
  # Deletes the repository with the given label.
  #
  # Args:
  #   label: The label of the repository to delete.
  def delete(label)
    @test.call('channel.software.delete', sessionKey: @test.token, channelLabel: label)
  end

  ##
  # Creates a new repository, with a given label and URL.
  #
  # Args:
  #   label: The name of the repository.
  #   url: The URL of the repository.
  def create_repo(label, url)
    @test.call('channel.software.createRepo', sessionKey: @test.token, label: label, type: 'yum', url: url)
  end

  ##
  # Associates a repository with a channel.
  #
  # Args:
  #   channel_label: The label of the channel you want to associate the repo with.
  #   repo_label: The label of the repository you want to associate with the channel.
  def associate_repo(channel_label, repo_label)
    @test.call('channel.software.associateRepo', sessionKey: @test.token, channelLabel: channel_label, repoLabel: repo_label)
  end

  ##
  # Removes a repository from the list of repositories to be processed.
  #
  # Args:
  #   label: The label of the repo you want to remove.
  ##
  # It returns the value of the key in the hash that matches the label.
  #
  # Args:
  #   label: The label of the item you want to get details for.
  def remove_repo(label)
    @test.call('channel.software.removeRepo', sessionKey: @test.token, label: label)
  end

  ##
  # Verifies if a given channel is a child of the given parent channel.
  #
  # Args:
  #   child: The channel you want to check if it's a child of the parent channel.
  #   parent: The possible parent channel.
  def parent_channel?(child, parent)
    channel = @test.call('channel.software.getDetails', sessionKey: @test.token, channelLabel: child)
    channel['parent_channel_label'] == parent
  end

  def get_details(label)
    @test.call('channel.software.getDetails', sessionKey: @test.token, channelLabel: label)
  end

  ##
  # Lists the child channels for a given parent channel.
  #
  # Args:
  #   parent_channel: The channel you want to list the children of.
  def list_child_channels(parent_channel)
    channels = @test.call('channel.listSoftwareChannels', sessionKey: @test.token)
    channel_labels = channels.map { |channel| channel['label'] }
    channel_labels.select { |channel| parent_channel?(channel, parent_channel) }
  end

  ##
  # Lists all the repos that the user has access to.
  def list_user_repos
    repos = @test.call('channel.software.listUserRepos', sessionKey: @test.token)
    repos.map { |key| key['label'] }
  end

  ##
  # Lists the name of channels the system with the given system ID is subscribed to
  #
  # Args:
  #   system_id: The ID of the system.
  def list_system_channels(system_id)
    channels = @test.call('channel.software.listSystemChannels', sessionKey: @test.token, sid: system_id)
    channels.map { |channel| channel['name'] }
  end
end
