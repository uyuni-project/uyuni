# Copyright (c) 2026 SUSE LLC.
# Licensed under the terms of the MIT license.

# Admin namespace
class NamespaceAdmin
  # Initializes a new instance of the NamespaceAdmin class.
  #
  # @param api_test [Object] The test object passed to the initialize method.
  def initialize(api_test)
    @gpg = NamespaceAdminGpg.new(api_test)
  end

  attr_reader :gpg
end

# Admin.gpg namespace
class NamespaceAdminGpg
  # Initializes a new instance of the NamespaceAdminGpg class.
  #
  # @param api_test [Object] The test object passed to the initialize method.
  def initialize(api_test)
    @test = api_test
  end

  # Uploads a GPG key to the customer keyring.
  #
  # @param key [String] The armored GPG public key.
  # @return [Object] The result of the API call.
  def upload_key(key)
    @test.call('admin.gpg.uploadGpgKey', sessionKey: @test.token, gpgKey: key)
  end

  # Lists the GPG keys in the customer keyring.
  #
  # @return [Object] The result of the API call.
  def list_keys
    @test.call('admin.gpg.listGpgKeys', sessionKey: @test.token)
  end

  # Removes a GPG key from the customer keyring.
  #
  # @param fingerprint [String] The fingerprint of the GPG key.
  # @return [Object] The result of the API call.
  def remove_key(fingerprint)
    @test.call('admin.gpg.removeGpgKey', sessionKey: @test.token, fingerprint: fingerprint)
  end
end
