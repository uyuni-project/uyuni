# Copyright (c) 2011-2021 SUSE LLC.
# Licensed under the terms of the MIT license.

require_relative 'xmlrpc_test'

# Channel API Namespace
class XMLRPCChannelTest < XMLRPCBaseTest
  # Create a custom repository
  def create_repo(label, url)
    @connection.call('channel.software.create_repo', @sid, label, 'yum', url)
  end

  # Associate a repository with a custom channel
  def associate_repo(channel_label, repo_label)
    @connection.call('channel.software.associate_repo', @sid, channel_label, repo_label)
  end

  # Create a custom software channel
  def create(label, name, summary, arch, parent)
    @connection.call('channel.software.create', @sid, label, name, summary, arch, parent)
  end

  # Delete a custom software channel
  def delete(label)
    @connection.call('channel.software.delete', @sid, label)
  end

  # Delete a repo
  def delete_repo(label)
    @connection.call('channel.software.remove_repo', @sid, label)
  end

  # Return the number of custom software channels
  def software_channels_count
    channels = @connection.call('channel.list_software_channels', @sid)
    channels.nil? ? 0 : channels.length
  end

  # Check if a certain software channel is listed
  def verify_channel(label)
    @connection.call('channel.list_software_channels', @sid)
               .map { |c| c['label'] }
               .include?(label)
  end

  # Check if a software channel is the parent of a given child channel
  def parent_channel?(child, parent)
    channel = @connection.call('channel.software.details', @sid, child)
    channel['parent_channel_label'] == parent
  end

  # Get channel details
  def channel_details(label)
    @connection.call('channel.software.details', @sid, label)
  end

  # Get the list of channels and print some info
  def list_software_channels
    channels = @connection.call('channel.list_software_channels', @sid)
    channels.map { |channel| channel['label'] }
  end

  # List child channels by parent channel label
  def list_child_channels(parent_channel)
    list_software_channels.select { |channel| parent_channel?(channel, parent_channel) }
  end
end
