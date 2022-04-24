# Copyright (c) 2022 SUSE LLC.
# Licensed under the terms of the MIT license.

# "activationkey" namespace
class NamespaceActivationkey
  def initialize(api_test)
    @test = api_test
    @keys = nil
  end

  def create(id, descr, base_channel, limit)
    @test.call('activationkey.create', sessionKey: @test.token, key: id, description: descr, baseChannelLabel: base_channel, usageLimit: limit, entitlements: [], universalDefault: false)
  end

  def delete(id)
    @test.call('activationkey.delete', sessionKey: @test.token, key: id)
    @keys = @test.call('activationkey.listActivationKeys', sessionKey: @test.token)
  end

  def get_activation_keys_count
    @keys = @test.call('activationkey.listActivationKeys', sessionKey: @test.token)
    @keys.nil? ? 0 : @keys.length
  end

  def get_activated_systems_count(id)
    systems = @test.call('activationkey.listActivatedSystems', sessionKey: @test.token, key: id)
    systems.nil? ? 0 : systems.length
  end

  def get_config_channels_count(id)
    channels = @test.call('activationkey.listConfigChannels', sessionKey: @test.token, key: id)
    channels.nil? ? 0 : channels.length
  end

  def verify(id)
    @test.call('activationkey.listActivationKeys', sessionKey: @test.token)
         .map { |key| key['key'] }
         .include?(id)
  end

  def add_config_channels(id, config_channels)
    @test.call('activationkey.addConfigChannels', sessionKey: @test.token, keys: id, configChannels: config_channels, addToTop: false)
  end

  def add_child_channels(id, child_channels)
    @test.call('activationkey.addChildChannels', sessionKey: @test.token, key: id, childChannels: child_channels)
  end

  def get_details(id)
    @test.call('activationkey.getDetails', sessionKey: @test.token, key: id)
  end

  def set_details(id, description, base_channel_label, usage_limit, contact_method)
    details = {
      description: description,
      base_channel_label: base_channel_label,
      usage_limit: usage_limit,
      universal_default: false,
      contact_method: contact_method
    }
    @test.call('activationkey.setDetails', sessionKey: @test.token, key: id, details: details).to_i == 1
  end
end
