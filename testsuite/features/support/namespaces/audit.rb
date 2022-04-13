# Copyright (c) 2022 SUSE LLC.
# Licensed under the terms of the MIT license.

# "audit" namespace
class NamespaceAudit
  def initialize(api_test)
    @test = api_test
  end

  def list_systems_by_patch_status(cve_identifier)
    @test.call('audit.listSystemsByPatchStatus', sessionKey: @test.token, cveIdentifier: cve_identifier)
  end
end
