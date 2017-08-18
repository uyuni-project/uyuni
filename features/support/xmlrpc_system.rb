require_relative 'xmlrpctest'

# system class
class XMLRPCSystemTest < XMLRPCBaseTest
  def listSystems
    @connection.call('system.listSystems', @sid)
  end

  def deleteSystem(id)
    @connection.call('system.deleteSystems', @sid, id)
  end

  # Get the list of latest installable packages for a given system.
  def listAllInstallablePackages(server)
    @connection.call('system.listAllInstallablePackages', @sid, server)
  end

  # Get the list of latest upgradable packages for a given system.
  def listLatestUpgradablePackages(server)
    @connection.call('system.listLatestUpgradablePackages', @sid, server)
  end

  # List the installed packages for a given system.
  def listPackages(server)
    @connection.call('system.listPackages', @sid, server)
  end

  # Go wild...
  # No need to write monstrous scenario for a little checks.
  # We just do it all at once instead.
  def getSysInfo(server)
    server_id = server['id']
    conn_path = @connection.call('system.getConnectionPath', @sid, server_id)
    puts conn_path
  end

  # Create a cobbler system record for a system that is not registered
  def createSystemRecord(sys_name, ks_label, ip, mac)
    netdev = { 'ip' => ip, 'mac' => mac, 'name' => 'eth0' }
    netdevs = [netdev]
    @connection.call('system.createSystemRecord', @sid, sys_name, ks_label, '', '', netdevs)
  end

  # Bootstrap a salt system
  #
  # == Parameters:
  # host::
  #   hostname or IP address of system to be bootstrapped.
  # activation_key::
  #   activation key to be assigned to the system
  # salt_ssh::
  #   true if the bootstrapped system should be a salt ssh minion,
  #   false if it should be regular salt minion
  #
  # == Returns:
  # 1 if the bootstrap procedure was successful.
  #
  # == Raises:
  # XMLRPC fault with faultCode = -1 and descriptive faultString
  # when there was an error during bootstrap.
  #
  def bootstrapSystem(host, activation_key, salt_ssh)
    @connection.call('system.bootstrap', @sid, host, 22, 'root', 'linux', activation_key, salt_ssh)
  end
end
