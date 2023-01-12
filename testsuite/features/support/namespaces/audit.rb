# Copyright (c) 2022 SUSE LLC.
# Licensed under the terms of the MIT license.

# "audit" namespace
class NamespaceAudit
  ##
  # It initializes the api_test variable.
  #
  # Args:
  #   api_test: This is the test object that is passed to the initialize method.
  def initialize(api_test)
    @test = api_test
  end

  ##
  # `list_systems_by_patch_status` returns a list of systems that are affected by a given CVE
  #
  # Args:
  #   cve_identifier: The CVE identifier for the vulnerability you want to check.
  def list_systems_by_patch_status(cve_identifier)
    @test.call('audit.listSystemsByPatchStatus', sessionKey: @test.token, cveIdentifier: cve_identifier)
  end
end
