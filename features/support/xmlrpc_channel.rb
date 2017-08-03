require_relative 'xmlrpctest'

class XMLRPCChannelTest < XMLRPCBaseTest
  def createRepo(label, url)
    @connection.call("channel.software.createRepo", @sid, label, 'yum', url)
  end

  def associateRepo(channelLabel, repoLabel)
    @connection.call("channel.software.associateRepo", @sid, channelLabel, repoLabel)
  end

  #
  # Create a custom software channel
  #
  def create(label, name, summary, arch, parent)
    @connection.call("channel.software.create", @sid, label, name, summary, arch, parent)
  end

  #
  # Delete a custom software channel
  #
  def delete(label)
    @connection.call("channel.software.delete", @sid, label)
  end

  #
  # Delete a repo
  #
  def deleteRepo(label)
    @connection.call("channel.software.removeRepo", @sid, label)
  end

  #
  # Return the number of custom software channels
  #
  def getSoftwareChannelsCount
    channels = @connection.call("channel.listSoftwareChannels", @sid)
    channels.nil? ? 0 : channels.length
  end

  #
  # Check if a certain software channel is listed
  #
  def verifyChannel(label)
    @connection.call("channel.listSoftwareChannels", @sid)
               .map { |c| c['label'] }
               .include?(label)
  end

  #
  # Check if a software channel is the parent of a given child channel
  #
  def isParentChannel(child, parent)
    channel = @connection.call("channel.software.getDetails", @sid, child)
    return true if channel['parent_channel_label'] == parent
    false
  end

  #
  # get channel details
  #
  def getChannelDetails(label)
    @connection.call("channel.software.getDetails", @sid, label)
  end

  #
  # Debug: Get the list of channels and print some info
  #
  def listSoftwareChannels
    channels = @connection.call("channel.listSoftwareChannels", @sid)
    for c in channels
      print "    Channel: " + "\n"
      for key in c.keys
        print "      " + key + ": " + c[key] + "\n"
      end
    end
  end
end
