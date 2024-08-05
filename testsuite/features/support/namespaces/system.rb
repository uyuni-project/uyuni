# Copyright (c) 2022-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

# Represents a namespace for system-related operations.
class NamespaceSystem
  # Initializes a new instance of the NamespaceSystem class.
  #
  # @param api_test [Object] The API test object.
  def initialize(api_test)
    @test = api_test
    @config = NamespaceSystemConfig.new(api_test)
    @custominfo = NamespaceSystemCustominfo.new(api_test)
    @provisioning = NamespaceSystemProvisioning.new(api_test)
    @scap = NamespaceSystemScap.new(api_test)
    @search = NamespaceSystemSearch.new(api_test)
  end

  attr_reader :config, :custominfo, :provisioning, :scap, :search

  # Retrieves the server ID for a given server name.
  #
  # @param server [String] The name of the server.
  # @return [String] The server ID.
  # @raise [StandardError] If the systems cannot be listed or the server cannot be found.
  def retrieve_server_id(server)
    systems = list_systems
    raise 'Cannot list systems' if systems.nil?

    server_id = systems
                .select { |s| s['name'] == server }
                .map { |s| s['id'] }
                .first
    raise StandardError, "Cannot find #{server}" if server_id.nil?

    server_id
  end

  # Lists all systems in the server.
  #
  # @return [Array<Hash>] An array of system objects.
  def list_systems
    @test.call('system.listSystems', sessionKey: @test.token)
  end

  # Searches for a system based on its name.
  #
  # @param name [String] The name of the system to search for.
  # @return [Array<Hash>] An array of system objects matching the search criteria.
  def search_by_name(name)
    @test.call('system.searchByName', sessionKey: @test.token, regexp: name)
  end

  # Lists all the packages that can be installed on a server.
  #
  # @param server [String] The server ID.
  # @return [Array<Hash>] An array of package objects.
  def list_all_installable_packages(server)
    @test.call('system.listAllInstallablePackages', sessionKey: @test.token, sid: server)
  end

  # Lists the packages that are upgradable on a given server.
  #
  # @param server [String] The server ID.
  # @return [Array<Hash>] An array of upgradable package objects.
  def list_latest_upgradable_packages(server)
    @test.call('system.listLatestUpgradablePackages', sessionKey: @test.token, sid: server)
  end

  # Bootstraps a system.
  #
  # @param host [String] The hostname of the system to bootstrap.
  # @param activation_key [String] The activation key to use for the system.
  # @param salt_ssh [Boolean] Determines if the system is SSH managed or not.
  def bootstrap_system(host, activation_key, salt_ssh)
    if get_target('proxy').nil?
      @test.call('system.bootstrap', sessionKey: @test.token, host: host, sshPort: 22, sshUser: 'root', sshPassword: 'linux', activationKey: activation_key, saltSSH: salt_ssh)
    else
      proxy = @test.call('system.searchByName', sessionKey: @test.token, regexp: get_target('proxy').full_hostname)
      proxy_id = proxy.map { |s| s['id'] }.first
      @test.call('system.bootstrap', sessionKey: @test.token, host: host, sshPort: 22, sshUser: 'root', sshPassword: 'linux', activationKey: activation_key, proxyId: proxy_id, saltSSH: salt_ssh)
    end
  end

  # Schedules a highstate to be applied to a server at a given date.
  #
  # @param server [String] The server ID.
  # @param date [String] The date and time to apply the highstate.
  # @param test [Boolean] Determines if the highstate is run on test mode or not.
  def schedule_apply_highstate(server, date, test)
    @test.call('system.scheduleApplyHighstate', sessionKey: @test.token, sid: server, earliestOccurrence: date, test: test)
  end

  # Schedules a package refresh on a server.
  #
  # @param server [String] The server ID.
  # @param date [String] The date and time to refresh the package.
  def schedule_package_refresh(server, date)
    @test.call('system.schedulePackageRefresh', sessionKey: @test.token, sid: server, earliestOccurrence: date)
  end

  # Schedules a reboot for a server on a specific date.
  #
  # @param server [String] The server ID.
  # @param date [String] The date and time to reboot the server.
  def schedule_reboot(server, date)
    @test.call('system.scheduleReboot', sessionKey: @test.token, sid: server, earliestOccurrence: date)
  end

  # Schedules a script to run on a server at a specified date and time.
  #
  # @param server [String] The server ID.
  # @param uid [String] The user ID of the user who will run the script.
  # @param gid [String] The group name of the user that will run the script.
  # @param timeout [Integer] The amount of time in seconds that the script is allowed to run before it is killed.
  # @param script [String] The script to run.
  # @param date [String] The date and time to run the script.
  def schedule_script_run(server, uid, gid, timeout, script, date)
    @test.call('system.scheduleScriptRun', sessionKey: @test.token, sid: server, username: uid, groupname: gid, timeout: timeout, script: script, earliestOccurrence: date)
  end

  # Creates a Cobbler system record for a system that is not registered on the SUMA server.
  #
  # @param name [String] The name of the system record.
  # @param kslabel [String] The kickstart label to use.
  # @param koptions [String] The kickstart options.
  # @param comment [String] A comment about the system record.
  # @param netdevices [Hash] A hash of network devices and their IP addresses.
  def create_system_record(name, kslabel, koptions, comment, netdevices)
    @test.call('system.createSystemRecord', sessionKey: @test.token, systemName: name, ksLabel: kslabel, kOptions: koptions, comment: comment, netDevices: netdevices)
  end

  # Creates a Cobbler system record with the specified kickstart label.
  #
  # @param sid [String] The system ID.
  # @param kslabel [String] The kickstart label to use.
  def create_system_record_with_sid(sid, kslabel)
    @test.call('system.createSystemRecord', sessionKey: @test.token, sid: sid, ksLabel: kslabel)
  end

  # Creates a system profile with the given name and data.
  #
  # @param name [String] The name of the system profile.
  # @param data [String] The data used to create the system profile.
  def create_system_profile(name, data)
    @test.call('system.createSystemProfile', sessionKey: @test.token, systemName: name, data: data)
  end

  # Lists system profiles that have no systems assigned to them.
  #
  # @return [Array<Hash>] An array of system profile objects.
  def list_empty_system_profiles
    @test.call('system.listEmptySystemProfiles', sessionKey: @test.token)
  end

  # Gets the reactivation key of a server.
  #
  # @param server [String] The server ID.
  # @return [String] The reactivation key.
  def obtain_reactivation_key(server)
    @test.call('system.obtainReactivationKey', sessionKey: @test.token, sid: server)
  end

  # Sets a list of kickstart variables in the Cobbler system record for the specified server.
  #
  # @param server [String] The server ID.
  # @param variables [Array<Hash>] A list of system kickstart variables to set.
  def set_variables(server, variables)
    @test.call('system.setVariables', sessionKey: @test.token, sid: server, netboot: true, variables: variables)
  end

  # Returns a list of all errata that are relevant to the system with the given SID.
  #
  # @param system_id [String] The ID of the system.
  # @return [Array<Hash>] An array of errata objects.
  def get_system_errata(system_id)
    @test.call('system.getRelevantErrata', sessionKey: @test.token, sid: system_id)
  end

  # Returns a list of all errata that are relevant to the systems with the given SIDs.
  #
  # @param system_ids [Array<String>] The IDs of the systems.
  # @return [Array<Hash>] An array of errata objects.
  def get_systems_errata(system_ids)
    @test.call('system.getRelevantErrata', sessionKey: @test.token, sids: system_ids)
  end
end

# System Configuration namespace
# This class represents the configuration for the namespace system.
class NamespaceSystemConfig
  # Initializes a new instance of the NamespaceSystemConfig class.
  #
  # @param api_test [Object] The test object that is passed to the initialize method.
  def initialize(api_test)
    @test = api_test
  end

  # Removes the specified channels from the specified servers.
  #
  # @param servers [Array<Integer>] An array of server IDs.
  # @param channels [Array<String>] An array of channel labels to remove from the server.
  def remove_channels(servers, channels)
    @test.call('system.config.removeChannels', sessionKey: @test.token, sids: servers, configChannelLabels: channels)
  end
end

# This class represents a namespace for system custom information.
# It provides methods to create custom info keys.
class NamespaceSystemCustominfo
  # Initializes a new instance of the NamespaceSystemCustominfo class.
  #
  # @param api_test [Object] The test object passed in from the test script.
  def initialize(api_test)
    @test = api_test
  end

  # Creates a custom info key.
  #
  # @param value [String] The name of the custom field.
  # @param desc [String] The description of the key.
  def create_key(value, desc)
    @test.call('system.custominfo.createKey', sessionKey: @test.token, keyLabel: value, keyDescription: desc)
  end
end

# System Provisioning namespace
class NamespaceSystemProvisioning
  # Initializes the Power Management namespace.
  #
  #
  # @param api_test [Object] This is the object that is passed in from the test script.
  #   It contains the methods that are used to make the API calls.
  #
  # @return [NamespaceSystemProvisioning] An instance of the NamespaceSystemProvisioning class.
  def initialize(api_test)
    @test = api_test
    @powermanagement = NamespaceSystemProvisioningPowermanagement.new(api_test)
  end

  attr_reader :powermanagement
end

# System Provisioning Power Management namespace
class NamespaceSystemProvisioningPowermanagement
  # Initializes the NamespaceSystemProvisioningPowermanagement class.
  #
  # @param api_test [Object] The test object that is passed to the initialize method.
  def initialize(api_test)
    @test = api_test
  end

  # Lists the power management types available for a given system.
  def list_types
    @test.call('system.provisioning.powermanagement.listTypes', sessionKey: @test.token)
  end

  # Returns the power management details of a server.
  #
  # @param server [String] The server ID.
  def get_details(server)
    @test.call('system.provisioning.powermanagement.getDetails', sessionKey: @test.token, sid: server)
  end

  # Returns the power status of a server.
  #
  # @param server [String] The server ID.
  def get_status(server)
    @test.call('system.provisioning.powermanagement.getStatus', sessionKey: @test.token, sid: server)
  end

  # Sets the power management details for a server.
  #
  # @param server [String] The server ID.
  # @param data [Hash] A hash of the data to be set.
  def set_details(server, data)
    @test.call('system.provisioning.powermanagement.setDetails', sessionKey: @test.token, sid: server, data: data)
  end

  # Powers on a server.
  #
  # @param server [String] The server ID of the server you want to power on.
  def power_on(server)
    @test.call('system.provisioning.powermanagement.powerOn', sessionKey: @test.token, sid: server)
  end

  # Powers off a server.
  #
  # @param server [String] The server ID of the server you want to power off.
  def power_off(server)
    @test.call('system.provisioning.powermanagement.powerOff', sessionKey: @test.token, sid: server)
  end

  # Reboots a server.
  #
  # @param server [String] The server ID you want to reboot.
  def reboot(server)
    @test.call('system.provisioning.powermanagement.reboot', sessionKey: @test.token, sid: server)
  end
end

# This class represents a namespace for system SCAP related operations.
class NamespaceSystemScap
  # Initializes a new instance of the NamespaceSystemScap class.
  #
  # @param api_test [Object] The test object that is passed to the initialize method.
  def initialize(api_test)
    @test = api_test
  end

  # Lists all XCCDF scans for a given server.
  #
  # @param server [String] The server ID of the server you want to list the XCCDF scans for.
  def list_xccdf_scans(server)
    @test.call('system.scap.listXccdfScans', sessionKey: @test.token, sid: server)
  end
end

# The NamespaceSystemSearch class provides methods for searching system information.
class NamespaceSystemSearch
  # Initializes a new instance of the NamespaceSystemSearch class.
  #
  # @param api_test [Object] The test object that is passed to the initialize method.
  def initialize(api_test)
    @test = api_test
  end

  # Takes a server name as an argument and returns the hostname of the server.
  #
  # @param server [String] The server name you want to search for.
  # @return [String] The hostname of the server.
  def hostname(server)
    @test.call('system.search.hostname', sessionKey: @test.token, searchTerm: server)
  end
end
