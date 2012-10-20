File.expand_path(__FILE__)           # For Ruby 1.9.2+
$LOAD_PATH << File.dirname(__FILE__) # For Ruby 1.8

require 'xmlrpctest'

class XMLRPCChannelTest < XMLRPCBaseTest

  #
  # Create a repo
  #
  def createRepo(label, url)
    ret = nil
    begin
       ret = @connection.call("channel.software.createRepo", @sid, label, 'yum', url)
    rescue Exception => ex
      puts "Something went wrong: " + ex
    ensure
      return ret
    end
  end

  #
  # associateRepo
  #
  def  associateRepo(channelLabel, repoLabel)
    ret = nil
    begin
      ret = @connection.call("channel.software.associateRepo", @sid, channelLabel, repoLabel)
    rescue Exception => ex
      puts "Something went wrong: " + ex
    ensure
      return ret
    end
  end

  #
  # Create a custom software channel
  #
  def create(label, name, summary, arch, parent)
    ret = nil
    begin
      ret = @connection.call("channel.software.create", @sid, label, name, summary, arch, parent)
    rescue Exception => ex
      puts "Something went wrong: " + ex
    ensure
      return ret
    end
  end

  #
  # Delete a custom software channel
  #
  def delete(label)
    ret = nil
    begin
      ret = @connection.call("channel.software.delete", @sid, label)
    rescue Exception => ex
      puts "Something went wrong: " + ex
    ensure
      return ret
    end
  end

  #
  # Delete a repo
  #
  def deleteRepo(label)
    ret = nil
    begin
      ret = @connection.call("channel.software.removeRepo", @sid, label)
    rescue Exception => ex
      puts "Something went wrong: " + ex
    ensure
      return ret
    end
  end

  #
  # Return the number of custom software channels
  #
  def getSoftwareChannelsCount()
    channels = @connection.call("channel.listSoftwareChannels", @sid)
    return channels == nil ? 0 : channels.length
  end
 
  #
  # Check if a certain software channel is listed
  #
  def verifyChannel(label)
    channels = @connection.call("channel.listSoftwareChannels", @sid)
    for c in channels
      if label == c['label']
          return true
      end
    end
    return false
  end

  #
  # Check if a software channel is the parent of a given child channel
  #
  def isParentChannel(child, parent)
    ret = false
    begin
      channel = @connection.call("channel.software.getDetails", @sid, child)
      if channel['parent_channel_label'] == parent
        ret = true
      end
    rescue Exception => ex
      puts "Something went wrong: " + ex
    ensure
      return ret
    end
  end

  #
  # get channel details
  #
  def getChannelDetails(label)
    ret = nil
    begin
      ret = @connection.call("channel.software.getDetails", @sid, label)
    rescue Exception => ex
      puts "Something went wrong: " + ex
    ensure
      return ret
    end
  end

  #
  # Debug: Get the list of channels and print some info
  #
  def listSoftwareChannels()
    channels = @connection.call("channel.listSoftwareChannels", @sid)
    for c in channels
      print "    Channel: " + "\n"
      for key in c.keys
        print "      " + key + ": " + c[key] + "\n"
      end
    end
  end
end

