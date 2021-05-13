# Copyright (c) 2010-2021 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'nokogiri'
require 'timeout'

def client_raw_repodata_dir(channel)
  "/var/cache/zypp/raw/spacewalk:#{channel}/repodata"
  # it would be "/var/cache/yum/#{channel}" for CentOS
end

def client_system_id_to_i
  out, _code = $client.run('grep "ID" /etc/sysconfig/rhn/systemid | tr -d -c 0-9')
  out.gsub(/\s+/, '')
end

def check_shutdown(host, time_out)
  cmd = "ping -c1 #{host}"
  repeat_until_timeout(timeout: time_out, message: "machine didn't reboot") do
    _out = `#{cmd}`
    if $CHILD_STATUS.exitstatus.nonzero?
      puts "machine: #{host} went down"
      break
    end
    sleep 1
  end
end

def check_restart(host, node, time_out)
  cmd = "ping -c1 #{host}"
  repeat_until_timeout(timeout: time_out, message: "machine didn't come up") do
    _out = `#{cmd}`
    if $CHILD_STATUS.exitstatus.zero?
      puts "machine: #{host} network is up"
      break
    end
    sleep 1
  end
  repeat_until_timeout(timeout: time_out, message: "machine didn't come up") do
    _out, code = node.run('ls', false, 10)
    if code.zero?
      puts "machine: #{host} ssh is up"
      break
    end
    sleep 1
  end
end

# Extract the OS version and OS family
# We get these data decoding the values in '/etc/os-release'
# rubocop:disable Metrics/AbcSize
def get_os_version(node)
  os_family_raw, code = node.run('grep "^ID=" /etc/os-release', false)
  if code.zero?
    os_family = os_family_raw.strip.split('=')[1]
    return nil, nil if os_family.nil?
    os_family.delete! '"'
    os_version_raw, _code = node.run('grep "^VERSION_ID=" /etc/os-release')
    os_version = os_version_raw.strip.split('=')[1]
    return nil, nil if os_version.nil?
    os_version.delete! '"'
    # on SLES, we need to replace the dot with '-SP'
    os_version.gsub!(/\./, '-SP') if os_family =~ /^sles/
  else
    # The only node that we handle which doesn't support 'os-release' file is Centos 6
    _os_family_raw, code = node.run('test -f /etc/centos-release', false)
    return nil, nil unless code.zero?
    os_version = '6'
    os_family = 'centos'
  end
  puts "Node: #{node.hostname}, OS Version: #{os_version}, Family: #{os_family}"
  [os_version, os_family]
end

def get_gpg_keys(node, target = $server)
  os_version, os_family = get_os_version(node)
  if os_family =~ /^sles/
    # HACK: SLE 15 uses SLE 12 GPG key
    os_version = 12 if os_version =~ /^15/
    # SLE11 and SLE12 gpg keys don't contain service pack strings
    os_version = os_version.split('-')[0] if os_version =~ /^1[12]/
    gpg_keys, _code = target.run("cd /srv/www/htdocs/pub/ && ls -1 sle#{os_version}*", false)
  elsif os_family =~ /^centos/
    gpg_keys, _code = target.run("cd /srv/www/htdocs/pub/ && ls -1 #{os_family}#{os_version}* res*", false)
  else
    gpg_keys, _code = target.run("cd /srv/www/htdocs/pub/ && ls -1 #{os_family}*", false)
  end
  gpg_keys.lines.map(&:strip)
end
# rubocop:enable Metrics/AbcSize

def sle11family?(node)
  _out, code = node.run('pidof systemd', false)
  code.nonzero?
end
