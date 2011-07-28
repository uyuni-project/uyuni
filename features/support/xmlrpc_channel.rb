File.expand_path(__FILE__)           # For Ruby 1.9.2+
$LOAD_PATH << File.dirname(__FILE__) # For Ruby 1.8

require 'xmlrpctest'

class XMLRPCChannelTest < XMLRPCBaseTest

  #
  # Create a custom software channel
  #
  def create(label, name, summary, arch, parent)
    ret = nil
    begin
      ret = @connection.call("channel.software.create", @sid, label, name, summary, arch, parent)
    rescue Exception => ex
      puts "Something went wrong: " + ex
    end
    return ret
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
      return false
    end
    return ret
  end

  #
  # Return the number of custom software channels
  #
  def getSoftwareChannelsCount()
    channels = @connection.call("channel.listSoftwareChannels", @sid)
    return channels == nil ? 0 : channels.length
  end
 
  #
  # Check if a certain software channel is in the list
  #
  def verifyChannel(label)
    channels = @connection.call("channel.listSoftwareChannels", @sid)
    for c in channels
      if label == c['label']:
          return true
      end
    end
    return false
  end

  #
  # Get the list of software channels and print some info
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

