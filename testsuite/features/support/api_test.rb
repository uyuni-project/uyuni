# Copyright (c) 2011-2022 SUSE LLC.
# Licensed under the terms of the MIT license.

require_relative 'xmlrpc_client'
require_relative 'http_client'

# Abstract parent class
class APITestBase
  def initialize(host)
    @connection = nil
    @sid = nil
  end

  ## "auth" namespace

  # authenticate against the host
  def auth_login(user, password)
    @sid = @connection.call('auth.login', login: user, password: password)
  end

  # log out from API
  def auth_logout
    @connection.call('auth.logout', sessionKey: @sid)
  end


  ## "channel" namespace

  def channel_create_repo(label, url)
    @connection.call('channel.software.createRepo', sessionKey: @sid, label: label, type: 'yum', url: url)
  end

  def channel_associate_repo(channel_label, repo_label)
    @connection.call('channel.software.associateRepo', sessionKey: @sid, channelLabel: channel_label, repoLabel: repo_label)
  end

  def channel_create(label, name, summary, arch, parent)
    @connection.call('channel.software.create', sessionKey: @sid, label: label, name: name, summary: summary, archLabel: arch, parentLabel: parent)
  end

  def channel_delete(label)
    @connection.call('channel.software.delete', sessionKey: @sid, label: label)
  end

  def channel_delete_repo(label)
    @connection.call('channel.software.remove_repo', sessionKey: @sid, label: label)
  end

  def channel_get_software_channels_count
    channels = @connection.call('channel.listSoftwareChannels', sessionKey: @sid)
    channels.nil? ? 0 : channels.length
  end

  def channel_verify_channel(label)
    @connection.call('channel.listSoftwareChannels', sessionKey: @sid)
               .map { |c| c['label'] }
               .include?(label)
  end

  def channel_list_software_channels
    channels = @connection.call('channel.listSoftwareChannels', @sid)
    channels.map { |channel| channel['label'] }
  end

  def channel_is_parent_channel(child, parent)
    channel = @connection.call('channel.software.getDetails', @sid, label: child)
    channel['parent_channel_label'] == parent
  end

  def channel_get_channel_details(label)
    @connection.call('channel.software.getDetails', @sid, label: label)
  end

  def channel_list_child_channels(parent_channel)
    channel_list_software_channels.select { |channel| channel_is_parent_channel(channel, parent_channel) }
  end


  ## "system" namespace

  # utility: retrieve server ID
  def system_retrieve_server_id(server)
    systems = system_list_systems
    refute_nil(systems)
    server_id = systems
                .select { |s| s['name'] == server }
                .map { |s| s['id'] }.first
    refute_nil(server_id, "client #{server} is not yet registered?")
    server_id
  end

  def system_list_systems
    @connection.call('system.listSystems', sessionKey: @sid)
  end

  def system_search_by_name(name)
    @connection.call('system.searchByName', sessionKey: @sid, name: name)
  end

  def system_list_all_installable_packages(server)
    @connection.call('system.listAllInstallablePackages', sessionKey: @sid, serverId: server)
  end

  def system_list_latest_upgradable_packages(server)
    @connection.call('system.listLatestUpgradablePackages', sessionKey: @sid, serverId: server)
  end

  def system_bootstrap_system(host, activation_key, salt_ssh)
    if $proxy.nil?
      @connection.call('system.bootstrap', sessionKey: @sid, host: host, sshPort: 22, sshUser: 'root', sshPassword: 'linux', activationKey: activation_key, saltSsh: salt_ssh)
    else
      proxy = @connection.call('system.searchByName', sessionKey: @sid, name: $proxy.full_hostname)
      proxy_id = proxy.map { |s| s['id'] }.first
      @connection.call('system.bootstrap', sessionKey: @sid, host: host, sshPort: 22, sshUser: 'root', sshPassword: 'linux', activationKey: activation_key, proxyId: proxy_id, saltSsh: salt_ssh)
    end
  end

  def system_schedule_apply_highstate(server, date, test)
    @connection.call('system.scheduleApplyHighstate', sessionKey: @sid, serverId: server, earliestOccurrence: date, test: test)
  end

  def system_remove_channels(servers, channels)
    @connection.call('system.config.removeChannels', sessionKey: @sid, ids: servers, channels: channels)
  end

  def system_create_system_record(name, kslabel, koptions, comment, netdevices)
    @connection.call('system.createSystemRecord', sessionKey: @sid, serverId: name, ksLabel: kslabel, ksOptions: koptions, comment: comment, devices: netdevices)
  end

  def system_create_system_profile(name, data)
    @connection.call('system.createSystemProfile', sessionKey: @sid, systemName: name, data: data)
  end

  def system_list_empty_system_profiles
    @connection.call('system.listEmptySystemProfiles', sessionKey: @sid)
  end

  def system_obtain_reactivation_key(server)
    @connection.call('system.obtainReactivationKey', sessionKey: @sid, serverId: server)
  end


  ## "user" namespace
  
  def user_list_users
    @connection.call('user.listUsers', sessionKey: @sid)
  end

  def user_list_roles(user)
    @connection.call('user.listRoles', sessionKey: @sid, login: user)
  end

  def user_create(user, password, first, last, email)
    @connection.call('user.create', sessionKey: @sid, desiredLogin: user, desiredPassword: password, firstName: first, lastName: last, email: email)
  end

  def user_delete(user)
    @connection.call('user.delete', sessionKey: @sid, login: user)
  end

  def user_add_role(user, role)
    @connection.call('user.addRole', sessionKey: @sid, login: user, role: role)
  end

  def user_remove_role(user, role)
    @connection.call('user.removeRole', sessionKey: @sid, login: user, role: role)
  end

end

# Derived class for XML-RPC test
class APITestXMLRPC < APITestBase
  def initialize(host)
    @connection = XmlrpcClient.new(host)
  end
end

# Derived class for HTTP test
class APITestHTTP < APITestBase
  def initialize(host)
    @connection = HttpClient.new(host)
  end
end
