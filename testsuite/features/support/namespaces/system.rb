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

  def list_systems
    @test.call('system.listSystems', sessionKey: @test.token)
  end

  def search_by_name(name)
    @test.call('system.searchByName', sessionKey: @test.token, regexp: name)
  end

  def list_all_installable_packages(server)
    @test.call('system.listAllInstallablePackages', sessionKey: @test.token, sid: server)
  end

  def list_latest_upgradable_packages(server)
    @test.call('system.listLatestUpgradablePackages', sessionKey: @test.token, sid: server)
  end

  def bootstrap_system(host, activation_key, salt_ssh)
    if $proxy.nil?
      @test.call('system.bootstrap', sessionKey: @test.token, host: host, sshPort: 22, sshUser: 'root', sshPassword: 'linux', activationKey: activation_key, saltSSH: salt_ssh)
    else
      proxy = @test.call('system.searchByName', sessionKey: @test.token, regexp: $proxy.full_hostname)
      proxy_id = proxy.map { |s| s['id'] }.first
      @test.call('system.bootstrap', sessionKey: @test.token, host: host, sshPort: 22, sshUser: 'root', sshPassword: 'linux', activationKey: activation_key, proxyId: proxy_id, saltSSH: salt_ssh)
    end
  end

  def schedule_apply_highstate(server, date, test)
    @test.call('system.scheduleApplyHighstate', sessionKey: @test.token, sid: server, earliestOccurrence: date, test: test)
  end

  def schedule_package_refresh(server, date)
    @test.call('system.schedulePackageRefresh', sessionKey: @test.token, sid: server, earliestOccurrence: date)
  end

  def schedule_reboot(server, date)
    @test.call('system.scheduleReboot', sessionKey: @test.token, sid: server, earliestOccurrence: date)
  end

  def schedule_script_run(server, uid, gid, timeout, script, date)
    @test.call('system.scheduleScriptRun', sessionKey: @test.token, sid: server, username: uid, groupname: gid, timeout: timeout, script: script, earliestOccurrence: date)
  end

  def create_system_record(name, kslabel, koptions, comment, netdevices)
    @test.call('system.createSystemRecord', sessionKey: @test.token, systemName: name, ksLabel: kslabel, kOptions: koptions, comment: comment, netDevices: netdevices)
  end

  def create_system_profile(name, data)
    @test.call('system.createSystemProfile', sessionKey: @test.token, systemName: name, data: data)
  end

  def list_empty_system_profiles
    @test.call('system.listEmptySystemProfiles', sessionKey: @test.token)
  end

  def obtain_reactivation_key(server)
    @test.call('system.obtainReactivationKey', sessionKey: @test.token, sid: server)
  end
end

# "system.config" namespace
class NamespaceSystemConfig
  def initialize(api_test)
    @test = api_test
  end

  def remove_channels(servers, channels)
    @test.call('system.config.removeChannels', sessionKey: @test.token, sids: servers, configChannelLabels: channels)
  end
end

# "system.custominfo" namespace
class NamespaceSystemCustominfo
  def initialize(api_test)
    @test = api_test
  end

  def create_key(value, desc)
    @test.call('system.custominfo.createKey', sessionKey: @test.token, keyLabel: value, keyDescription: desc)
  end
end

# "system.provisioning" namespace
class NamespaceSystemProvisioning
  def initialize(api_test)
    @test = api_test
    @powermanagement = NamespaceSystemProvisioningPowermanagement.new(api_test)
  end

  attr_reader :powermanagement
end

# "system.provisioning.powermanagement" namespace
class NamespaceSystemProvisioningPowermanagement
  def initialize(api_test)
    @test = api_test
  end

  def list_types
    @test.call('system.provisioning.powermanagement.listTypes', sessionKey: @test.token)
  end

  def get_details(server)
    @test.call('system.provisioning.powermanagement.getDetails', sessionKey: @test.token, sid: server)
  end

  def get_status(server)
    @test.call('system.provisioning.powermanagement.getStatus', sessionKey: @test.token, sid: server)
  end

  def set_details(server, data)
    @test.call('system.provisioning.powermanagement.setDetails', sessionKey: @test.token, sid: server, data: data)
  end

  def power_on(server)
    @test.call('system.provisioning.powermanagement.powerOn', sessionKey: @test.token, sid: server)
  end

  def power_off(server)
    @test.call('system.provisioning.powermanagement.powerOff', sessionKey: @test.token, sid: server)
  end

  def reboot(server)
    @test.call('system.provisioning.powermanagement.reboot', sessionKey: @test.token, sid: server)
  end
end

# "system.scap" namespace
class NamespaceSystemScap
  def initialize(api_test)
    @test = api_test
  end

  def list_xccdf_scans(server)
    @test.call('system.scap.listXccdfScans', sessionKey: @test.token, sid: server)
  end
end

# "system.search" namespace
class NamespaceSystemSearch
  def initialize(api_test)
    @test = api_test
  end

  def hostname(server)
    @test.call('system.search.hostname', sessionKey: @test.token, searchTerm: server)
  end
end
