# Copyright (c) 2010-2021 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'nokogiri'
require 'timeout'
require_relative 'lavanda'

# Client stack module interacts with the default client
module ClientStack
  extend LavandaBasic
  extend self

  # Returns the path for the repodata of a channel
  def client_raw_repodata_dir(channel)
    "/var/cache/zypp/raw/spacewalk:#{channel}/repodata"
    # it would be "/var/cache/yum/#{channel}" for CentOS
  end

  # Returns the system id of the default client
  def client_system_id_to_i(client)
    out, _code = client.run('grep "ID" /etc/sysconfig/rhn/systemid | tr -d -c 0-9')
    out.gsub(/\s+/, '')
  end

  # Check during a defined time if the host doesn't response
  def check_shutdown(host, time_out)
    cmd = "ping -c1 #{host}"
    repeat_until_timeout(timeout: time_out, message: "machine didn't reboot") do
      _out = `#{cmd}`
      puts("machine: #{host} went down"); break if $CHILD_STATUS.exitstatus.nonzero?
    end
  end

  # Check during a defined time if the host response
  def check_restart(host, time_out)
    cmd = "ping -c1 #{host}"
    repeat_until_timeout(timeout: time_out, message: "machine didn't come up") do
      _out = `#{cmd}`
      puts("machine: #{host} network is up"); break if $CHILD_STATUS.exitstatus.zero?
    end
    sleep 1
    repeat_until_timeout(timeout: time_out, message: "machine didn't come up") do
      _out, code = node.run('ls', check_errors: false, timeout: 10)
      if code.zero?
        puts "machine: #{host} ssh is up"
        break
      end
    end
  end

  # Extract the OS version
  # We get these data decoding the values in '/etc/os-release'
  def get_os_version(node)
    os_family_raw, code = node.run('grep "^ID=" /etc/os-release', check_errors: false)
    return nil, nil unless code.zero?

    os_family = os_family_raw.strip.split('=')[1]
    return nil, nil if os_family.nil?

    os_family.delete! '"'
    os_version_raw, code = node.run('grep "^VERSION_ID=" /etc/os-release', check_errors: false)
    return nil, nil unless code.zero?

    os_version = os_version_raw.strip.split('=')[1]
    return nil, nil if os_version.nil?

    os_version.delete! '"'
    # on SLES, we need to replace the dot with '-SP'
    os_version.gsub!(/\./, '-SP') if os_family =~ /^sles/
    puts "Node: #{node.hostname}, OS Version: #{os_version}, Family: #{os_family}"
    [os_version, os_family]
  end

  # Extract the GPG keys of the target
  def get_gpg_keys(node, target)
    os_version, os_family = get_os_version(node)
    case os_family
    when /^sles/
      # HACK: SLE 15 uses SLE 12 GPG key
      os_version = 12 if os_version =~ /^15/
      # SLE11 and SLE12 gpg keys don't contain service pack strings
      os_version = os_version.split('-')[0] if os_version =~ /^1[12]/
      gpg_keys, _code = target.run("cd /srv/www/htdocs/pub/ && ls -1 sle#{os_version}*", check_errors: false)
    when /^centos/
      gpg_keys, _code = target.run("cd /srv/www/htdocs/pub/ && ls -1 #{os_family}#{os_version}* res*", check_errors: false)
    else
      gpg_keys, _code = target.run("cd /srv/www/htdocs/pub/ && ls -1 #{os_family}*", check_errors: false)
    end
    gpg_keys.lines.map(&:strip)
  end

  # Returns if the node is SLE 11 family
  def sle11family?(node)
    _out, code = node.run('pidof systemd', check_errors: false)
    code.nonzero?
  end

  # Wait that an action is completed
  def wait_action_complete(actionid, timeout: DEFAULT_TIMEOUT)
    host = $server.full_hostname
    @client_api = XMLRPC::Client.new2("http://#{host}/rpc/api")
    @sid = @client_api.call('auth.login', 'admin', 'admin')
    repeat_until_timeout(timeout: timeout, message: 'Action was not found among completed actions') do
      list = @client_api.call('schedule.list_completed_actions', @sid)
      break if list.any? { |a| a['id'] == actionid }

      sleep(2)
    end
  end
end
