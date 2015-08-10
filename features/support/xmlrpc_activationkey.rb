File.expand_path(__FILE__)           # For Ruby 1.9.2+
$LOAD_PATH << File.dirname(__FILE__) # For Ruby 1.8

require 'xmlrpctest'


class XMLRPCActivationKeyTest < XMLRPCBaseTest
  def createKey(id, descr, limit)
    key = nil
    begin
      key = @connection.call("activationkey.create", @sid, id, descr, "", limit.to_i, [], false)
    rescue Exception => ex
      puts "Something went wrong: " + ex
    end

    return key
  end


  def deleteKey(id)
    begin
      @connection.call("activationkey.delete", @sid, id)
      @keys = @connection.call("activationkey.listActivationKeys", @sid)
    rescue Exception => ex
      puts "Something went wrong during key deletion: " + ex
      return false
    end

    return true
  end


  def listActivatedSystems(key)
    systems = @connection.call("activationkey.listActivatedSystems", @sid, key)
    return systems == nil ? 0 : systems.length
  end


  def getActivationKeysCount()
    @keys = @connection.call("activationkey.listActivationKeys", @sid)
    return @keys == nil ? 0 : @keys.length
  end


  def verifyKey(kid)
    keys = @connection.call("activationkey.listActivationKeys", @sid)
    for key in keys
      if kid == key['key']
          return true
      end
    end
    return false
  end


  def getConfigChannelsCount(key)
    channels = @connection.call("activationkey.listConfigChannels", @sid, key)
    return channels == nil ? 0 : channels.length
  end


  def addConfigChannel(key, name)
    res = 0
    begin
      res = @connection.call("activationkey.addConfigChannels", @sid, [key], [name], false)
    rescue Exception => ex
      puts "Cannot add config channel: " + ex
    end

    return res
  end


  def setDetails(key)
    res = false
    begin
      details = {
        'description' => 'Test description of the key ' + key,
#        'base_channel_label' => "", # <---- Insert here a valid channel
        'usage_limit' => 10,
        'universal_default' => false
      }
      res = @connection.call("activationkey.setDetails", @sid, key, details).to_i == 1
    rescue Exception => ex
      puts "Cannot set details to the key: " + ex
    end

    return res
  end


  def getDetails(key)
    res = false
    begin
      keyinfo = @connection.call("activationkey.getDetails", @sid, key)
      puts "      Key info for the key " + keyinfo['key']

      keyinfo.each_pair do |k,v|
        puts "        " + k.to_s + ": " + v.to_s
      end

      res = ('Test description of the key ' + key) == keyinfo['description']
    rescue Exception => ex
    end

    return res
  end
end
