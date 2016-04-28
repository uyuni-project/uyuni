#!/usr/bin/ruby
# Copyright (c) 2010-2011 Novell, Inc.
# Licensed under the terms of the MIT license.
require 'nokogiri'

def client_is_zypp?
  File.stat("/usr/bin/zypper").executable?
end

def client_refresh_metadata
  if client_is_zypp?
    `zypper --non-interactive ref -s`
    fail unless $?.success?
  else
    `yum clean all`
   fail unless $?.success?
   `yum makecache`
   fail unless $?.success?
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
  xml = Nokogiri::XML(File.read('/etc/sysconfig/rhn/systemid'))
  xml.xpath('/params/param/value/struct/member[name="system_id"]/value').text
end

def client_system_id_to_i
  client_system_id.gsub(/ID-/, '').to_i
end
