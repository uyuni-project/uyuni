# Copyright (c) 2011-2017 SUSE LLC.
# Licensed under the terms of the MIT license.

require_relative 'xmlrpctest'

# channel class
class XMLRPCChannelTest < XMLRPCBaseTest
  def create_repo(label, url)
    @connection.call('channel.software.create_repo', @sid, label, 'yum', url)
  end

  def associate_repo(channel_label, repo_label)
    @connection.call('channel.software.associate_repo', @sid, channel_label, repo_label)
  end

  #
  # Create a custom software channel
  #
  def create(label, name, summary, arch, parent)
    @connection.call('channel.software.create', @sid, label, name, summary, arch, parent)
  end

  #
  # Delete a custom software channel
  #
  def delete(label)
    @connection.call('channel.software.delete', @sid, label)
  end

  #
  # Delete a repo
  #
  def delete_repo(label)
    @connection.call('channel.software.remove_repo', @sid, label)
  end

  #
  # Return the number of custom software channels
  #
  def get_software_channels_count
    channels = @connection.call('channel.list_software_channels', @sid)
    channels.nil? ? 0 : channels.length
  end

  #
  # Check if a certain software channel is listed
  #
  def verify_channel(label)
    @connection.call('channel.list_software_channels', @sid)
               .map { |c| c['label'] }
               .include?(label)
  end

  #
  # Check if a software channel is the parent of a given child channel
  #
  def is_parent_channel(child, parent)
    channel = @connection.call('channel.software.get_details', @sid, child)
    return true if channel['parent_channel_label'] == parent
    false
  end

  #
  # get channel details
  #
  def get_channel_details(label)
    @connection.call('channel.software.get_details', @sid, label)
  end

  #
  # Debug: Get the list of channels and print some info
  #
  def list_software_channels
    channels = @connection.call('channel.list_software_channels', @sid)
    channels.each do |c|
      print '    Channel: ' + "\n"
      c.keys.each do |key|
        print '      ' + key + ': ' + c[key] + "\n"
      end
    end
  end
end
