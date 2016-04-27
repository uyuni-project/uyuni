require_relative 'xmlrpctest'

class XMLRPCActivationKeyTest < XMLRPCBaseTest
  def createKey(id, descr, limit)
    @connection.call("activationkey.create", @sid, id, descr, "", limit.to_i, [], false)
  end

  def deleteKey(id)
    @connection.call("activationkey.delete", @sid, id)
    @keys = @connection.call("activationkey.listActivationKeys", @sid)
  end

  def listActivatedSystems(key)
    systems = @connection.call("activationkey.listActivatedSystems", @sid, key)
    return systems == nil ? 0 : systems.length
  end

  def getActivationKeysCount
    @keys = @connection.call("activationkey.listActivationKeys", @sid)
    return @keys == nil ? 0 : @keys.length
  end

  def verifyKey(kid)
    @connection.call("activationkey.listActivationKeys", @sid)
      .map {|key| key['key'] }
      .include?(kid)
  end

  def getConfigChannelsCount(key)
    channels = @connection.call("activationkey.listConfigChannels", @sid, key)
    return channels == nil ? 0 : channels.length
  end

  def addConfigChannel(key, name)
    @connection.call("activationkey.addConfigChannels", @sid, [key], [name], false)
  end

  def setDetails(key)
    details = {
      'description' => 'Test description of the key ' + key,
      #        'base_channel_label' => "", # <---- Insert here a valid channel
      'usage_limit' => 10,
      'universal_default' => false
    }
    @connection.call("activationkey.setDetails", @sid, key, details).to_i == 1
  end

  def getDetails(key)
    keyinfo = @connection.call("activationkey.getDetails", @sid, key)
    puts "      Key info for the key " + keyinfo['key']

    keyinfo.each_pair do |k,v|
      puts "        " + k.to_s + ": " + v.to_s
    end

    res = ('Test description of the key ' + key) == keyinfo['description']
    return res
  end
end
