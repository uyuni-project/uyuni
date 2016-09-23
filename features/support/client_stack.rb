#!/usr/bin/ruby
# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.
require 'nokogiri'

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
    return "/var/cache/zypp/raw/spacewalk:#{channel}/repodata"
  else
    return "/var/cache/yum/#{channel}"
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
