require_relative 'xmlrpctest'

# system class
class XMLRPCSystemTest < XMLRPCBaseTest
  def list_systems
    @connection.call('system.list_systems', @sid)
  end

  def search_by_name(name)
    @connection.call('system.search_by_name', @sid, name)
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

  # Schedule apply highstate
  def schedule_apply_highstate(server, date, test)
    @connection.call('system.schedule_apply_highstate', @sid, server, date, test)
  end

  # Unsubscribe configuration channels from server
  def remove_channels(servers, channels)
    @connection.call('system.config.remove_channels', @sid, servers, channels)
  end

  def create_system_record(name, kslabel, koptions, comment, netdevices)
    @connection.call('system.createSystemRecord', @sid, name, kslabel, koptions, comment, netdevices)
  end

  # Create an empty system profile based on the given data
  # == Parameters:
  # name::
  #    system profile name
  # data::
  #    map containing 'hwAddress' or 'hostname' key
  # == Returns:
  # The id of created system
  # == Raises:
  # An XMLRPC fault with a descriptive faultString if a matching profile
  # already exists or if the given data is not sufficient for an empty
  # profile creation.
  #
  def create_system_profile(name, data)
    @connection.call('system.createSystemProfile', @sid, name, data)
  end

  # List empty system profiles
  #
  # == Returns:
  # The list of empty system profiles
  #
  def list_empty_system_profiles
    @connection.call('system.listEmptySystemProfiles', @sid)
  end
end
