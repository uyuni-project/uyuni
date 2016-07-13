#!/usr/bin/ruby
# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.
require 'nokogiri'

def client_is_zypp?
   out, _local, _remote, _code = $client.test_and_store_results_together("test -x /usr/bin/zypper", "root", 600)
   return out
end

def client_refresh_metadata
  if client_is_zypp?
     _out, _local, _remote, code = $client.test_and_store_results_together("zypper --non-interactive ref -s", "root", 600)
     fail if code != 0
  else
     _out, _local, _remote, code = $client.test_and_store_results_together("yum clean all", "root", 600)
     fail if code != 0
     _out, _local, _remote, code = $client.test_and_store_results_together("yum makecache", "root", 600)
     fail if code != 0
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
  out, _local, _remote, _code = $client.test_and_store_results_together("grep \"system_id\" /etc/sysconfig", "root", 600)
  puts out
end
