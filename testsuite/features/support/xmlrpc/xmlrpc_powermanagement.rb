# Copyright (c) 2020-2021 SUSE LLC.
# Licensed under the terms of the MIT license.

require_relative 'xmlrpc_test'

# Power Management API Namespace
class XMLRPCPowerManagementTest < XMLRPCBaseTest
  # list power management types
  def list_types
    @connection.call('system.provisioning.powermanagement.list_types', @sid)
  end

  # get power management configuration details
  def details(server)
    @connection.call('system.provisioning.powermanagement.details', @sid, server)
  end

  # get power status
  def status(server)
    @connection.call('system.provisioning.powermanagement.status', @sid, server)
  end

  # set power management configuration details
  def set_details(server, data)
    @connection.call('system.provisioning.powermanagement.set_details', @sid, server, data)
  end

  # power on
  def power_on(server)
    @connection.call('system.provisioning.powermanagement.power_on', @sid, server)
  end

  # power off
  def power_off(server)
    @connection.call('system.provisioning.powermanagement.power_off', @sid, server)
  end

  # reboot
  def reboot(server)
    @connection.call('system.provisioning.powermanagement.reboot', @sid, server)
  end
end
