# Copyright (c) 2011-2017 SUSE LLC.
# Licensed under the terms of the MIT license.

require_relative 'xmlrpctest'

# actkey xmlrpc test
class XMLRPCActivationKeyTest < XMLRPCBaseTest
  def create_key(id, descr, limit)
    @connection.call('activationkey.create', @sid, id, descr, '', limit.to_i, [], false)
  end

  def delete_key(id)
    @connection.call('activationkey.delete', @sid, id)
    @keys = @connection.call('activationkey.list_activation_keys', @sid)
  end

  def list_activated_systems(key)
    systems = @connection.call('activationkey.list_activated_systems', @sid, key)
    systems.nil? ? 0 : systems.length
  end

  def get_activation_keys_count
    @keys = @connection.call('activationkey.list_activation_keys', @sid)
    @keys.nil? ? 0 : @keys.length
  end

  def verify_key(kid)
    @connection.call('activationkey.list_activation_keys', @sid)
               .map { |key| key['key'] }
               .include?(kid)
  end

  def get_config_channels_count(key)
    channels = @connection.call('activationkey.list_config_channels', @sid, key)
    channels.nil? ? 0 : channels.length
  end

  def add_config_channel(key, name)
    @connection.call('activationkey.add_config_channels', @sid, [key], [name], false)
  end

  def set_details(key)
    details = {
      'description' => 'Test description of the key ' + key,
      #        'base_channel_label' => "", # <---- Insert here a valid channel
      'usage_limit' => 10,
      'universal_default' => false
    }
    @connection.call('activationkey.set_details', @sid, key, details).to_i == 1
  end

  def get_details(key)
    keyinfo = @connection.call('activationkey.get_details', @sid, key)
    puts '      Key info for the key ' + keyinfo['key']

    keyinfo.each_pair do |k, v|
      puts '        ' + k.to_s + ': ' + v.to_s
    end

    res = ('Test description of the key ' + key) == keyinfo['description']
    res
  end
end
