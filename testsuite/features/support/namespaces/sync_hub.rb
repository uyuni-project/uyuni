# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

# sync.hub namespace: channel synchronization from the hub to its peripherals
class NamespaceSyncHub
  # Initializes a new instance of the NamespaceSyncHub class.
  #
  # @param api_test [Object] The test object that is passed to the initialize method.
  def initialize(api_test)
    @test = api_test
  end

  # Adds channels to the list that the hub synchronizes to a peripheral.
  #
  # @param peripheral_fqdn [String] The FQDN of the peripheral.
  # @param channel_labels [Array<String>] The labels of the channels to add.
  # @return [Integer] 1 on success.
  def add_channels_to_sync(peripheral_fqdn, channel_labels)
    @test.call('sync.hub.addPeripheralChannelsToSync', sessionKey: @test.token, fqdn: peripheral_fqdn, channelLabels: channel_labels)
  end

  # Lists the channel labels that the hub synchronizes to a peripheral.
  #
  # @param peripheral_fqdn [String] The FQDN of the peripheral.
  # @return [Array<String>] The channel labels configured for synchronization.
  def list_channels_to_sync(peripheral_fqdn)
    @test.call('sync.hub.listPeripheralChannelsToSync', sessionKey: @test.token, fqdn: peripheral_fqdn)
  end

  # Schedules a channel synchronization from the hub to a peripheral.
  #
  # @param peripheral_fqdn [String] The FQDN of the peripheral.
  # @return [Integer] 1 when the synchronization is scheduled.
  def sync_peripheral_channels(peripheral_fqdn)
    @test.call('sync.hub.syncPeripheralChannels', sessionKey: @test.token, fqdn: peripheral_fqdn)
  end
end
