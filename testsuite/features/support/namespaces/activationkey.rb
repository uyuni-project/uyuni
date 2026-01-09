# Copyright (c) 2022-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

# Represents a namespace for activation key operations.
class NamespaceActivationkey
  # Initializes the NamespaceActivationkey object.
  #
  # @param api_test [Object] The test object passed in from the test script.
  def initialize(api_test)
    @test = api_test
    @keys = nil
  end

  # Creates an activation key.
  #
  # @param id [String] The name of the activation key.
  # @param descr [String] The description of the activation key.
  # @param base_channel [String] The channel to use as the base for the activation key.
  # @param limit [Integer] The number of systems that can be registered to this activation key.
  def create(id, descr, base_channel, limit)
    @test.call('activationkey.create', sessionKey: @test.token, key: id, description: descr, baseChannelLabel: base_channel, usageLimit: limit, entitlements: [], universalDefault: false)
  end

  # Deletes an activation key.
  #
  # @param id [String] The ID of the activation key to delete.
  def delete(id)
    @test.call('activationkey.delete', sessionKey: @test.token, key: id)
    @keys = @test.call('activationkey.listActivationKeys', sessionKey: @test.token)
  end

  # Returns the number of activation keys.
  #
  # @return [Integer] The number of activation keys.
  def get_activation_keys_count
    @keys = @test.call('activationkey.listActivationKeys', sessionKey: @test.token)
    @keys.nil? ? 0 : @keys.length
  end

  # Returns the number of activated systems for a given user.
  #
  # @param id [String] The ID of the user.
  # @return [Integer] The number of activated systems.
  def get_activated_systems_count(id)
    systems = @test.call('activationkey.listActivatedSystems', sessionKey: @test.token, key: id)
    systems.nil? ? 0 : systems.length
  end

  # Returns the number of channels in the configuration with the given ID.
  #
  # @param id [String] The ID of the configuration.
  # @return [Integer] The number of channels in the configuration.
  def get_config_channels_count(id)
    channels = @test.call('activationkey.listConfigChannels', sessionKey: @test.token, key: id)
    channels.nil? ? 0 : channels.length
  end

  # Checks if the ID of the user is valid and active.
  #
  # @param id [String] The ID of the user to verify.
  # @return [Boolean] True if the user ID is valid and active, false otherwise.
  def verified?(id)
    @test.call('activationkey.listActivationKeys', sessionKey: @test.token)
         .map { |key| key['key'] }
         .include?(id)
  end

  # Adds configuration channels to a system.
  #
  # @param id [String] The ID of the system to add the config channels to.
  # @param config_channels [Array<String>] The list of config channels to add to the system.
  def add_config_channels(id, config_channels)
    @test.call('activationkey.addConfigChannels', sessionKey: @test.token, keys: id, configChannelLabels: config_channels, addToTop: false)
  end

  # Adds child channels to a channel.
  #
  # @param id [String] The ID of the channel to add child channels to.
  # @param child_channels [Array<String>] The array of channel IDs to add to the parent channel.
  def add_child_channels(id, child_channels)
    @test.call('activationkey.addChildChannels', sessionKey: @test.token, key: id, childChannelLabels: child_channels)
  end

  # Returns the details of the user with the given ID.
  #
  # @param id [String] The ID of the user to get details for.
  # @return [Hash] The details of the user.
  def get_details(id)
    @test.call('activationkey.getDetails', sessionKey: @test.token, key: id)
  end

  # Sets the details of a channel and returns whether the operation was successful.
  #
  # @param id [String] The ID of the channel.
  # @param description [String] The description of the channel.
  # @param base_channel_label [String] The label of the base channel to subscribe to.
  # @param usage_limit [Integer] The number of times this key can be used.
  # @param contact_method [String] The contact method to use when onboarding a system using this activation key.
  #   Valid values are:
  #   - "default"
  #   - "ssh-push"
  #   - "ssh-push-tunnel"
  # @return [Boolean] True if the details were set successfully, false otherwise.
  def details_set?(id, description, base_channel_label, usage_limit, contact_method)
    details = { description: description, base_channel_label: base_channel_label, usage_limit: usage_limit, universal_default: false, contact_method: contact_method }
    @test.call('activationkey.setDetails', sessionKey: @test.token, key: id, details: details).to_i == 1
  end

  # Sets the entitlements of an activation key.
  #
  # @param id [String] The ID of the activation key.
  # @param entitlements [Array<String>] The array of entitlements to enable on this activation key.
  #   Valid values are:
  #   - "container_build_host"
  #   - "monitoring_entitled"
  #   - "osimage_build_host"
  #   - "virtualization_host"
  #   - "ansible_control_node"
  def set_entitlement(id, entitlements)
    @test.call('activationkey.addEntitlements', sessionKey: @test.token, key: id, entitlements: entitlements)
  end
end
