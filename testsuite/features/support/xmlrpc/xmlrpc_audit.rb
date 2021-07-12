# Copyright (c) 2013-2021 SUSE LLC.
# Licensed under the terms of the MIT license.

require_relative 'xmlrpc_test'

# Audit API Namespace
class XMLRPCCVEAuditTest < XMLRPCBaseTest
  # Populate CVE server channels
  def populate_cveserver_channels
    @connection.call('audit.populate_cveserver_channels', @sid)
  end

  # List systems by patch status
  def list_systems_by_patch_status(cve_identifier)
    @connection.call('audit.list_systems_by_patch_status', @sid, cve_identifier)
  end
end
