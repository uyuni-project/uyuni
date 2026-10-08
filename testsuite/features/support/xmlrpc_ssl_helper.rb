# Copyright (c) 2026 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'openssl'
require 'xmlrpc/client'

# Builds XMLRPC::Client instances with optional SSL certificate verification,
# shared by callers that talk to hosts with self-signed certificates (Hub and
# its peripherals) so the verify_mode-disabling logic lives in one place.
module XmlrpcSslHelper
  # Builds an XMLRPC client for the given URL.
  #
  # @param url [String] The XMLRPC endpoint URL.
  # @param ssl_verify [Boolean] Whether to verify SSL certificates (default is true).
  # @return [XMLRPC::Client] The configured XMLRPC client.
  def self.build_client(url, ssl_verify: true)
    client = XMLRPC::Client.new2(url, nil, DEFAULT_TIMEOUT)
    client.instance_variable_get(:@http).verify_mode = OpenSSL::SSL::VERIFY_NONE unless ssl_verify
    client
  end
end
