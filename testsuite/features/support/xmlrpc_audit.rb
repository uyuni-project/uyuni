# Copyright (c) 2013-2022 SUSE LLC.
# Licensed under the terms of the MIT license.

require_relative 'api_test'

# APIAuditTest class
class APIAuditTest < APITestBase
  def populate_cveserver_channels
    @connection.call('audit.populate_cveserver_channels', @sid)
  end

  def list_systems_by_patch_status(cve_identifier)
    @connection.call('audit.list_systems_by_patch_status', @sid, cve_identifier)
  end
end
