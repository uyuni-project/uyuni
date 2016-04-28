require_relative 'xmlrpctest'

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
  def createSystemRecord(sysName, ksLabel, ip, mac)
    netdev = { 'ip' => ip, 'mac' => mac, 'name' => 'eth0' }
    netdevs = [netdev]
    @connection.call('system.createSystemRecord', @sid, sysName, ksLabel, '', '', netdevs)
  end
end
