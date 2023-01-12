# Copyright (c) 2022 SUSE LLC.
# Licensed under the terms of the MIT license.

# "system" namespace
class NamespaceSystem
  def initialize(api_test)
    @test = api_test
    @config = NamespaceSystemConfig.new(api_test)
    @custominfo = NamespaceSystemCustominfo.new(api_test)
    @provisioning = NamespaceSystemProvisioning.new(api_test)
    @scap = NamespaceSystemScap.new(api_test)
    @search = NamespaceSystemSearch.new(api_test)
  end

  attr_reader :config
  attr_reader :custominfo
  attr_reader :provisioning
  attr_reader :scap
  attr_reader :search

  # utility: retrieve server ID
  def retrieve_server_id(server)
    systems = list_systems
    raise 'Cannot list systems' if systems.nil?

    server_id = systems
                .select { |s| s['name'] == server }
                .map { |s| s['id'] }.first
    raise "Cannot find #{server}" if server_id.nil?

    server_id
  end

  ##
  # > This function will list all systems in the system
  def list_systems
    @test.call('system.listSystems', sessionKey: @test.token)
  end

  ##
  # It searches for a name in the system
  #
  # Args:
  #   name: The name of the system you want to search for.
  def search_by_name(name)
    @test.call('system.searchByName', sessionKey: @test.token, regexp: name)
  end

  ##
  # This function lists all the packages that can be installed on a server
  #
  # Args:
  #   server: The server ID of the server you want to list the packages for.
  def list_all_installable_packages(server)
    @test.call('system.listAllInstallablePackages', sessionKey: @test.token, sid: server)
  end

  ##
  # `list_latest_upgradable_packages` returns a list of packages that are upgradable on a given server
  #
  # Args:
  #   server: The server ID
  def list_latest_upgradable_packages(server)
    @test.call('system.listLatestUpgradablePackages', sessionKey: @test.token, sid: server)
  end

  ##
  # If a proxy is defined, use it, otherwise don't
  #
  # Args:
  #   host: The hostname of the system to bootstrap
  #   activation_key: The activation key to use for the system.
  #   salt_ssh: true/false
  def bootstrap_system(host, activation_key, salt_ssh)
    if $proxy.nil?
      @test.call('system.bootstrap', sessionKey: @test.token, host: host, sshPort: 22, sshUser: 'root', sshPassword: 'linux', activationKey: activation_key, saltSSH: salt_ssh)
    else
      proxy = @test.call('system.searchByName', sessionKey: @test.token, regexp: $proxy.full_hostname)
      proxy_id = proxy.map { |s| s['id'] }.first
      @test.call('system.bootstrap', sessionKey: @test.token, host: host, sshPort: 22, sshUser: 'root', sshPassword: 'linux', activationKey: activation_key, proxyId: proxy_id, saltSSH: salt_ssh)
    end
  end

  ##
  # `schedule_apply_highstate` schedules a highstate to be applied to a server at a given date
  #
  # Args:
  #   server: The server ID of the server you want to schedule the highstate on.
  #   date: The date and time you want the highstate to be applied.
  #   test: true or false
  def schedule_apply_highstate(server, date, test)
    @test.call('system.scheduleApplyHighstate', sessionKey: @test.token, sid: server, earliestOccurrence: date, test: test)
  end

  ##
  # This function schedules a package refresh on a server
  #
  # Args:
  #   server: The server ID of the server you want to schedule the package refresh on.
  #   date: The date and time you want the package to be refreshed.
  def schedule_package_refresh(server, date)
    @test.call('system.schedulePackageRefresh', sessionKey: @test.token, sid: server, earliestOccurrence: date)
  end

  ##
  # > Schedule a reboot for a server on a specific date
  #
  # Args:
  #   server: The server ID you want to reboot.
  #   date: The date and time you want the server to reboot.
  def schedule_reboot(server, date)
    @test.call('system.scheduleReboot', sessionKey: @test.token, sid: server, earliestOccurrence: date)
  end

  ##
  # This function schedules a script to run on a server at a specified date and time
  #
  # Args:
  ##
  # This function creates a system record in the Satellite 6 server
  #
  # Args:
  #   name: The name of the system record.
  #   kslabel: The kickstart label you want to use.
  #   koptions:
  #   comment: A comment about the system record.
  #   netdevices: This is a hash of the network devices that you want to use.  The key is the device name, and the value
  # is the IP address.  For example:
  #   server: The server ID of the server you want to run the script on.
  #   uid: The user ID of the user who will run the script.
  #   gid: The group name of the user that will run the script.
  #   timeout: The amount of time in seconds that the script is allowed to run before it is killed.
  #   script: The script to run.
  #   date: The date and time you want the script to run.  This is in the format of YYYY-MM-DD HH:MM:SS.  For example, to
  # run the script at 11:30pm on December 31st, 2013, you would use 2013-12-31 23
  def schedule_script_run(server, uid, gid, timeout, script, date)
    @test.call('system.scheduleScriptRun', sessionKey: @test.token, sid: server, username: uid, groupname: gid, timeout: timeout, script: script, earliestOccurrence: date)
  end

  def create_system_record(name, kslabel, koptions, comment, netdevices)
    @test.call('system.createSystemRecord', sessionKey: @test.token, systemName: name, ksLabel: kslabel, kOptions: koptions, comment: comment, netDevices: netdevices)
  end

  ##
  # This function creates a system profile with the given name and data
  #
  # Args:
  #   name: The name of the system profile.
  #   data: This is the data that will be used to create the system profile. It is a JSON object that contains the
  # following keys:
  def create_system_profile(name, data)
    @test.call('system.createSystemProfile', sessionKey: @test.token, systemName: name, data: data)
  end

  ##
  # > Returns a list of system profiles that have no systems assigned to them
  def list_empty_system_profiles
    @test.call('system.listEmptySystemProfiles', sessionKey: @test.token)
  end

  ##
  # This function will obtain a reactivation key for a server
  #
  # Args:
  #   server: The server ID of the server you want to reactivate.
  def obtain_reactivation_key(server)
    @test.call('system.obtainReactivationKey', sessionKey: @test.token, sid: server)
  end
end

# "system.config" namespace
class NamespaceSystemConfig
  ##
  # It initializes the api_test variable.
  #
  # Args:
  #   api_test: This is the test object that is passed to the initialize method.
  def initialize(api_test)
    @test = api_test
  end

  ##
  # Remove the specified channels from the specified servers
  #
  # Args:
  #   servers: An array of server IDs
  #   channels: An array of channel labels to remove from the server.
  def remove_channels(servers, channels)
    @test.call('system.config.removeChannels', sessionKey: @test.token, sids: servers, configChannelLabels: channels)
  end
end

# "system.custominfo" namespace
class NamespaceSystemCustominfo
  ##
  # It initializes the api_test variable.
  #
  # Args:
  #   api_test: This is the test object that is passed in from the test script.
  def initialize(api_test)
    @test = api_test
  end

  ##
  # This function creates a custom info key
  #
  # Args:
  #   value: The name of the custom field
  #   desc: The description of the key.
  def create_key(value, desc)
    @test.call('system.custominfo.createKey', sessionKey: @test.token, keyLabel: value, keyDescription: desc)
  end
end

# "system.provisioning" namespace
class NamespaceSystemProvisioning
  ##
  # This function initializes the powermanagement namespace
  #
  # Args:
  #   api_test: This is the object that is passed in from the test script. It contains the methods that are used to make
  # the API calls.
  def initialize(api_test)
    @test = api_test
    @powermanagement = NamespaceSystemProvisioningPowermanagement.new(api_test)
  end

  attr_reader :powermanagement
end

# "system.provisioning.powermanagement" namespace
class NamespaceSystemProvisioningPowermanagement
  ##
  # It initializes the api_test variable.
  #
  # Args:
  #   api_test: This is the test object that is passed to the initialize method.
  def initialize(api_test)
    @test = api_test
  end

  ##
  # > This function lists the power management types available for a given system
  def list_types
    @test.call('system.provisioning.powermanagement.listTypes', sessionKey: @test.token)
  end

  ##
  # This function will return the power management details of a server
  #
  # Args:
  #   server: The server ID
  def get_details(server)
    @test.call('system.provisioning.powermanagement.getDetails', sessionKey: @test.token, sid: server)
  end

  ##
  # This function will return the power status of a server
  #
  # Args:
  #   server: The server ID
  def get_status(server)
    @test.call('system.provisioning.powermanagement.getStatus', sessionKey: @test.token, sid: server)
  end

  ##
  # This function sets the power management details for a server
  #
  # Args:
  #   server: The server ID
  #   data: A hash of the data to be set.
  def set_details(server, data)
    @test.call('system.provisioning.powermanagement.setDetails', sessionKey: @test.token, sid: server, data: data)
  end

  ##
  # This function powers on a server
  #
  # Args:
  #   server: The server ID of the server you want to power on.
  def power_on(server)
    @test.call('system.provisioning.powermanagement.powerOn', sessionKey: @test.token, sid: server)
  end

  ##
  # This function will power off a server
  #
  # Args:
  #   server: The server ID of the server you want to power off.
  def power_off(server)
    @test.call('system.provisioning.powermanagement.powerOff', sessionKey: @test.token, sid: server)
  end

  ##
  # This function reboots a server
  #
  # Args:
  #   server: The server ID you want to reboot.
  def reboot(server)
    @test.call('system.provisioning.powermanagement.reboot', sessionKey: @test.token, sid: server)
  end
end

# "system.scap" namespace
class NamespaceSystemScap
  ##
  # It initializes the api_test variable.
  #
  # Args:
  #   api_test: This is the test object that is passed to the initialize method.
  def initialize(api_test)
    @test = api_test
  end

  ##
  # > List all XCCDF scans for a given server
  #
  # Args:
  #   server: The server ID of the server you want to list the XCCDF scans for.
  def list_xccdf_scans(server)
    @test.call('system.scap.listXccdfScans', sessionKey: @test.token, sid: server)
  end
end

# "system.search" namespace
class NamespaceSystemSearch
  ##
  # It initializes the api_test variable.
  #
  # Args:
  #   api_test: This is the test object that is passed to the initialize method.
  def initialize(api_test)
    @test = api_test
  end

  ##
  # This function takes a server name as an argument and returns the hostname of the server
  #
  # Args:
  #   server: The server name you want to search for.
  def hostname(server)
    @test.call('system.search.hostname', sessionKey: @test.token, searchTerm: server)
  end
end
