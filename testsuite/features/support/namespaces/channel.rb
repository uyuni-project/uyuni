# Copyright (c) 2022 SUSE LLC.
# Licensed under the terms of the MIT license.

# "channel" namespace
class NamespaceChannel
  def initialize(api_test)
    @test = api_test
    @software = NamespaceChannelSoftware.new(api_test)
  end

  attr_reader :software

  def get_software_channels_count
    channels = @test.call('channel.listSoftwareChannels', sessionKey: @test.token)
    channels.nil? ? 0 : channels.length
  end

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

# "channel.software" namespace
class NamespaceChannelSoftware
  def initialize(api_test)
    @test = api_test
  end

  def create(label, name, summary, arch, parent)
    @test.call('channel.software.create', sessionKey: @test.token, label: label, name: name, summary: summary, archLabel: arch, parentLabel: parent)
  end

  def delete(label)
    @test.call('channel.software.delete', sessionKey: @test.token, channelLabel: label)
  end

  def create_repo(label, url)
    @test.call('channel.software.createRepo', sessionKey: @test.token, label: label, type: 'yum', url: url)
  end

  def associate_repo(channel_label, repo_label)
    @test.call('channel.software.associateRepo', sessionKey: @test.token, channelLabel: channel_label, repoLabel: repo_label)
  end

  def remove_repo(label)
    @test.call('channel.software.removeRepo', sessionKey: @test.token, label: label)
  end

  def parent_channel?(child, parent)
    channel = @test.call('channel.software.getDetails', sessionKey: @test.token, channelLabel: child)
    channel['parent_channel_label'] == parent
  end

  def get_details(label)
    @test.call('channel.software.getDetails', sessionKey: @test.token, channelLabel: label)
  end

  def list_child_channels(parent_channel)
    channels = @test.call('channel.listSoftwareChannels', sessionKey: @test.token)
    channel_labels = channels.map { |channel| channel['label'] }
    channel_labels.select { |channel| parent_channel?(channel, parent_channel) }
  end

  def list_user_repos
    repos = @test.call('channel.software.listUserRepos', sessionKey: @test.token)
    repos.map { |key| key['label'] }
  end
end
