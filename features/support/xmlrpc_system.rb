File.expand_path(__FILE__)           # For Ruby 1.9.2+
$LOAD_PATH << File.dirname(__FILE__) # For Ruby 1.8

require 'xmlrpctest'

class XMLRPCSystemTest < XMLRPCBaseTest
  def listSystems
    @connection.call('system.listSystems', @sid) || []
  end

  # Get the list of latest installable packages for a given system.
  def listAllInstallablePackages(server)
    @connection.call('system.listAllInstallablePackages', @sid, server) || []
  end

  # Get the list of latest upgradable packages for a given system.
  def listLatestUpgradablePackages(server)
    @connection.call('system.listLatestUpgradablePackages', @sid, server) || []
  end

  # List the installed packages for a given system.
  def listPackages(server)
    @connection.call('system.listPackages', @sid, server) || []
  end

  # Go wild...
  # No need to write monstrous scenario for a little checks.
  # We just do it all at once instead.
  def getSysInfo(server)
    serverId = server['id']
    connPath = @connection.call('system.getConnectionPath', @sid, serverId)
    puts connPath
  end

  # Create a cobbler system record for a system that is not registered
  def createSystemRecord(sysName, ksLabel, ip, mac)
    netdev = { 'ip' => ip, 'mac' => mac, 'name' => 'eth0' }
    netdevs = [netdev]
    @connection.call('system.createSystemRecord', @sid, sysName, ksLabel, '', '', netdevs)
  end
end
