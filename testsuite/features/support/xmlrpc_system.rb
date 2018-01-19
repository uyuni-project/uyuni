require_relative 'xmlrpctest'

# system class
class XMLRPCSystemTest < XMLRPCBaseTest
  def list_systems
    @connection.call('system.list_systems', @sid)
  end

  # Get the list of latest installable packages for a given system.
  def list_all_installable_packages(server)
    @connection.call('system.list_all_installable_packages', @sid, server)
  end

  # Get the list of latest upgradable packages for a given system.
  def list_latest_upgradable_packages(server)
    @connection.call('system.list_latest_upgradable_packages', @sid, server)
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
  def bootstrap_system(host, activation_key, salt_ssh)
    if $proxy.nil?
      @connection.call('system.bootstrap', @sid, host, 22, 'root', 'linux', activation_key, salt_ssh)
    else
      proxy = @connection.call('system.search_by_name', @sid, $proxy.ip)
      proxy_id = proxy.map { |s| s['id'] }.first
      @connection.call('system.bootstrap', @sid, host, 22, 'root', 'linux', activation_key, proxy_id, salt_ssh)
    end
  end
end
