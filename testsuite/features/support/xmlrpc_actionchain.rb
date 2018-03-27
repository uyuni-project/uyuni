require 'base64'
require_relative 'xmlrpctest'

# actionchain class
class XMLRPCActionChain < XMLRPCBaseTest
  def list_chains
    @connection.call('actionchain.list_chains', @sid).map { |x| x['label'] }
  end

  def create_chain(label)
    @connection.call('actionchain.create_chain', @sid, label)
  end

  def delete_chain(label)
    @connection.call('actionchain.delete_chain', @sid, label)
  end

  def remove_action(label, aid)
    @connection.call('actionchain.remove_action', @sid, label, aid)
  end

  def rename_chain(old_label, new_label)
    @connection.call('actionchain.rename_chain', @sid, old_label, new_label)
  end

  def add_script_run(system, script, label)
    @connection.call('actionchain.add_script_run',
                     @sid, system, label, 'root', 'root', 300,
                     Base64.strict_encode64(script))
  end

  def list_chain_actions(label)
    @connection.call('actionchain.list_chain_actions', @sid, label)
  end

  def add_system_reboot(system, label)
    @connection.call('actionchain.add_system_reboot', @sid, system, label)
  end

  def add_package_install(system, packages, label)
    @connection.call('actionchain.add_package_install', @sid, system, packages, label)
  end

  def add_package_upgrade(system, packages, label)
    @connection.call('actionchain.add_package_upgrade', @sid, system, packages, label)
  end

  def add_package_verify(system, packages, label)
    @connection.call('actionchain.add_package_verify', @sid, system, packages, label)
  end

  def add_package_removal(system, packages, label)
    @connection.call('actionchain.add_package_removal', @sid, system, packages, label)
  end

  def schedule_chain(label, iso8601)
    @connection.call('actionchain.schedule_chain', @sid, label, iso8601)
  end
end
