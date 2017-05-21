#!/usr/bin/ruby
# Copyright (c) 2010-2017 SUSE-LINUX
# Licensed under the terms of the MIT license.
require 'nokogiri'
require 'timeout'
def client_is_zypp?
  _out, _local, _remote, _code = $client.test_and_store_results_together("test -x /usr/bin/zypper", "root", 600)
end

def client_refresh_metadata
  if client_is_zypp?
    $client.run("zypper --non-interactive ref -s", true, 500, 'root')
  else
    $client.run("yum clean all", true, 600, 'root')
    $client.run("yum makecache", true, 600, 'root')
  end
end

def client_raw_repodata_dir(channel)
  if client_is_zypp?
    "/var/cache/zypp/raw/spacewalk:#{channel}/repodata"
  else
    "/var/cache/yum/#{channel}"
  end
end

def client_system_id
  out, _local, _remote, _code = $client.test_and_store_results_together("grep \"system_id\" /etc/sysconfig/rhn/systemid", "root", 600)
   puts out
end

def client_system_id_to_i
  out, _local, _remote, _code = $client.test_and_store_results_together("grep \"ID\" /etc/sysconfig/rhn/systemid | tr -d -c 0-9", "root", 600)
  out.gsub(/\s+/, "")
end
## functions for reboot tests

def checkShutdown(host, time_out)
  cmd = "ping -c1 #{host}"
  Timeout.timeout(time_out) do
    loop do
      _out = `#{cmd}`
      if $?.exitstatus.nonzero?
        puts "machine: #{host} went down"
        break
      end
      sleep 1
    end
  end
rescue Timeout::Error
  raise "Machine didn't reboot!"
end

def checkRestart(host, node, time_out)
  cmd = "ping -c1 #{host}"
  Timeout.timeout(time_out) do
    loop do
      _out = `#{cmd}`
      if $?.exitstatus.zero?
        puts "machine: #{host} network is up"
        break
      end
      sleep 1
    end
    loop do
      _out, code = node.run("ls", false, 10)
      if code.zero?
        puts "machine: #{host} ssh is up"
	break
      end
      sleep 1
    end
  end
rescue Timeout::Error
  raise "ERR: Machine didn't Went-up!"
end
