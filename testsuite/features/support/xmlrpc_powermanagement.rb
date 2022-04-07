# Copyright (c) 2020-2022 SUSE LLC.
# Licensed under the terms of the MIT license.

require_relative 'api_test'

# APIPowermanagementTest class
class APIPowermanagementTest < APITestBase
  #
  # list power management types
  #
  def list_types
    @connection.call('system.provisioning.powermanagement.list_types', @sid)
  end

  #
  # get power management configuration details
  #
  def get_details(server)
    @connection.call('system.provisioning.powermanagement.get_details', @sid, server)
  end

  #
  # get power status
  #
  def get_status(server)
    @connection.call('system.provisioning.powermanagement.get_status', @sid, server)
  end

  #
  # set power management configuration details
  #
  def set_details(server, data)
    @connection.call('system.provisioning.powermanagement.set_details', @sid, server, data)
  end

  #
  # power on
  #
  def power_on(server)
    @connection.call('system.provisioning.powermanagement.power_on', @sid, server)
  end

  #
  # power off
  #
  def power_off(server)
    @connection.call('system.provisioning.powermanagement.power_off', @sid, server)
  end

  #
  # reboot
  #
  def reboot(server)
    @connection.call('system.provisioning.powermanagement.reboot', @sid, server)
  end
end
