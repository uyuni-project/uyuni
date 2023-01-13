# Copyright (c) 2022 SUSE LLC.
# Licensed under the terms of the MIT license.

# "activationkey" namespace
class NamespaceActivationkey
  ##
  # It initializes the function.
  #
  # Args:
  #   api_test: This is the test object that is passed in from the test script.
  def initialize(api_test)
    @test = api_test
    @keys = nil
  end

  ##
  # It creates an activation key.
  #
  # Args:
  #   id: The name of the activation key
  #   descr: The description of the activation key
  #   base_channel: The channel you want to use as the base for the activation key.
  #   limit: The number of systems that can be registered to this activation key.
  def create(id, descr, base_channel, limit)
    @test.call('activationkey.create', sessionKey: @test.token, key: id, description: descr, baseChannelLabel: base_channel, usageLimit: limit, entitlements: [], universalDefault: false)
  end

  ##
  # Deletes an activation key.
  #
  # Args:
  #   id: The ID of the activation key you want to delete.
  def delete(id)
    @test.call('activationkey.delete', sessionKey: @test.token, key: id)
    @keys = @test.call('activationkey.listActivationKeys', sessionKey: @test.token)
  end

  ##
  # Returns the number of activation keys.
  def get_activation_keys_count
    @keys = @test.call('activationkey.listActivationKeys', sessionKey: @test.token)
    @keys.nil? ? 0 : @keys.length
  end

  ##
  # It returns the number of activated systems for a given user.
  #
  # Args:
  #   id: The id of the user
  def get_activated_systems_count(id)
    systems = @test.call('activationkey.listActivatedSystems', sessionKey: @test.token, key: id)
    systems.nil? ? 0 : systems.length
  end

  ##
  # It returns the number of channels in the configuration with the given id.
  #
  # Args:
  #   id: The id of the configuration.
  def get_config_channels_count(id)
    channels = @test.call('activationkey.listConfigChannels', sessionKey: @test.token, key: id)
    channels.nil? ? 0 : channels.length
  end

  ##
  # It returns the id of the user.
  #
  # Args:
  #   id: The id of the user to verify.
  def verify(id)
    @test.call('activationkey.listActivationKeys', sessionKey: @test.token)
         .map { |key| key['key'] }
         .include?(id)
  end

  ##
  # It adds config channels to a system.
  #
  # Args:
  #   id: The id of the system to add the config channels to.
  #   config_channels: A list of config channels to add to the system.
  def add_config_channels(id, config_channels)
    @test.call('activationkey.addConfigChannels', sessionKey: @test.token, keys: id, configChannelLabels: config_channels, addToTop: false)
  end

  ##
  # It adds child channels to a channel.
  #
  # Args:
  #   id: The id of the channel you want to add child channels to.
  #   child_channels: An array of channel ids that you want to add to the parent channel.
  def add_child_channels(id, child_channels)
    @test.call('activationkey.addChildChannels', sessionKey: @test.token, key: id, childChannelLabels: child_channels)
  end

  ##
  # It returns the details of the user with the given id.
  #
  # Args:
  #   id: The id of the user you want to get details for.
  def get_details(id)
    @test.call('activationkey.getDetails', sessionKey: @test.token, key: id)
  end

  ##
  # It sets the details of a channel.
  #
  # Args:
  #   id: The id of the subscription.
  #   description: A description of the subscription.
  #   base_channel_label: The base channel label of the channel you want to subscribe to.
  #   usage_limit: The number of times this key can be used.
  #   contact_method:
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
