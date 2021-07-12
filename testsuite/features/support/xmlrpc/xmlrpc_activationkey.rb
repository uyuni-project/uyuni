# Copyright (c) 2011-2021 SUSE LLC.
# Licensed under the terms of the MIT license.

require_relative 'xmlrpc_test'

# Activation Key API Namespace
class XMLRPCActivationKeyTest < XMLRPCBaseTest
  # Create an Activation key
  def create_key(id, descr, base_channel, limit)
    @connection.call('activationkey.create', @sid, id, descr, base_channel, limit.to_i, [], false)
  end

  # Delete an Activation Key by ID
  def delete_key(id)
    @connection.call('activationkey.delete', @sid, id)
    @keys = @connection.call('activationkey.list_activation_keys', @sid)
  end

  # List all the systems activated using an AK by ID
  def list_activated_systems(id)
    systems = @connection.call('activationkey.list_activated_systems', @sid, id)
    systems.nil? ? 0 : systems.length
  end

  # Get amount of Activation Keys
  def activation_keys_count
    @keys = @connection.call('activationkey.list_activation_keys', @sid)
    @keys.nil? ? 0 : @keys.length
  end

  # Check if an Activation Key exist by ID
  def verify_key(id)
    @connection.call('activationkey.list_activation_keys', @sid)
               .map { |key| key['key'] }
               .include?(id)
  end

  # Get amount of configuration channels by Activation Key
  def config_channels_count(id)
    channels = @connection.call('activationkey.list_config_channels', @sid, id)
    channels.nil? ? 0 : channels.length
  end

  # Add configuration channels to an Activation Key
  def add_config_channels(id, config_channels)
    @connection.call('activationkey.add_config_channels', @sid, id, config_channels, false)
  end

  # Add child channels to an Activation Key
  def add_child_channels(id, child_channels)
    @connection.call('activationkey.add_child_channels', @sid, id, child_channels)
  end

  # Set details of an Activation Key
  def set_details(id, description, base_channel_label, usage_limit, contact_method)
    details = {
      description: description,
      base_channel_label: base_channel_label,
      usage_limit: usage_limit,
      universal_default: false,
      contact_method: contact_method
    }
    @connection.call('activationkey.set_details', @sid, id, details).to_i == 1
  end

  # Get details of an Activation Key
  def details(id)
    @connection.call('activationkey.details', @sid, id)
  end
end
