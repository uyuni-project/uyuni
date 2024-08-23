# Copyright (c) 2010-2023 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'nokogiri'
require 'timeout'

# Returns the path with the repodata of the given channel
def client_raw_repodata_dir(channel)
  "/var/cache/zypp/raw/spacewalk:#{channel}/repodata"
  # it would be "/var/cache/yum/#{channel}" for Red Hat-like
end

# Returns the client's system ID
def client_system_id_to_i
  out, _code = get_target('client').run('grep "ID" /etc/sysconfig/rhn/systemid | tr -d -c 0-9')
  out.gsub(/\s+/, '')
end
