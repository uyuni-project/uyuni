# Copyright (c) 2014-2021 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'base64'
require_relative 'xmlrpc_test'

# Action Chain API Namespace
class XMLRPCActionChain < XMLRPCBaseTest
  # List chains
  def list_chains
    @connection.call('actionchain.list_chains', @sid).map { |x| x['label'] }
  end

  # Create a chain using a label
  def create_chain(label)
    @connection.call('actionchain.create_chain', @sid, label)
  end

  # Delete a chain using a label
  def delete_chain(label)
    @connection.call('actionchain.delete_chain', @sid, label)
  end

  # Remove an action using a label chain plus an action id
  def remove_action(label, aid)
    @connection.call('actionchain.remove_action', @sid, label, aid)
  end

  # Rename a chain label
  def rename_chain(old_label, new_label)
    @connection.call('actionchain.rename_chain', @sid, old_label, new_label)
  end

  # Add a script to run in the chain, providing the system where it runs
  def add_script_run(system, script, label)
    @connection.call('actionchain.add_script_run', @sid, system, label, 'root', 'root', 300, Base64.strict_encode64(script))
  end

  # List action chains by chain label
  def list_chain_actions(label)
    @connection.call('actionchain.list_chain_actions', @sid, label)
  end

  # Add a system reboot by system and chain label
  def add_system_reboot(system, label)
    @connection.call('actionchain.add_system_reboot', @sid, system, label)
  end

  # Add a package install into a system, providing a list of packages and the chain label
  def add_package_install(system, packages, label)
    @connection.call('actionchain.add_package_install', @sid, system, packages, label)
  end

  # Add a package upgrade into a system, providing a list of packages and the chain label
  def add_package_upgrade(system, packages, label)
    @connection.call('actionchain.add_package_upgrade', @sid, system, packages, label)
  end

  # Add a package verify into a system, providing a list of packages and the chain label
  def add_package_verify(system, packages, label)
    @connection.call('actionchain.add_package_verify', @sid, system, packages, label)
  end

  # Add a package removal into a system, providing a list of packages and the chain label
  def add_package_removal(system, packages, label)
    @connection.call('actionchain.add_package_removal', @sid, system, packages, label)
  end

  # Schedule a chain
  def schedule_chain(label, iso8601)
    @connection.call('actionchain.schedule_chain', @sid, label, iso8601)
  end
end
