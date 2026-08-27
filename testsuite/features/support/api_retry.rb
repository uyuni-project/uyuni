# Copyright (c) 2026 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'faraday'
require 'net/protocol'
require 'openssl'

# Retry policy shared by the XML-RPC and the HTTP API clients.
#
# Both clients only translate the application-level faults the server returns. A
# transport-level failure - a connection reset during the TLS handshake, a truncated
# response, a timeout - propagates to the caller instead, and aborts the scenario even
# when the next attempt would have succeeded. Parallel workers opening their first
# connection at the same moment hit this regularly.
module ApiRetry
  # Number of attempts, the first one included.
  MAX_ATTEMPTS = 3
  private_constant :MAX_ATTEMPTS

  # Delay in seconds before a retry, multiplied by the number of the attempt that failed.
  RETRY_DELAY = 2
  private_constant :RETRY_DELAY

  # Errors raised before the request reached the server. The call had no effect, so any
  # call can be retried.
  UNSENT_REQUEST_ERRORS = [Errno::ECONNREFUSED, Errno::EHOSTUNREACH, Net::OpenTimeout].freeze
  private_constant :UNSENT_REQUEST_ERRORS

  # Errors that can also be raised once the request is on the wire. The server may have
  # processed it already, so only calls that can be repeated are retried. Faraday raises
  # its own errors for both cases, and carries the original one to tell them apart.
  UNKNOWN_OUTCOME_ERRORS = [
    Errno::ECONNRESET,
    EOFError,
    Net::ReadTimeout,
    OpenSSL::SSL::SSLError,
    Faraday::ConnectionFailed,
    Faraday::SSLError,
    Faraday::TimeoutError
  ].freeze
  private_constant :UNKNOWN_OUTCOME_ERRORS

  # Every error a retry is considered for.
  TRANSPORT_ERRORS = (UNSENT_REQUEST_ERRORS + UNKNOWN_OUTCOME_ERRORS).freeze
  private_constant :TRANSPORT_ERRORS

  module_function

  # Returns whether an API call has no side effect on the server.
  # This is also what tells apart a GET from a POST in the HTTP client.
  #
  # @param name [String] The name of the API call.
  # @return [Boolean] Whether the call is read-only or not.
  def read_only?(name)
    short_name = name.split('.').last
    short_name.start_with?('list', 'get', 'is', 'find') ||
      name.start_with?('system.search.', 'packages.search.') ||
      %w[auth.logout errata.applicableToChannels].include?(name)
  end

  # Returns whether an API call can be sent a second time without changing the outcome.
  # Read-only calls qualify, and so does auth.login: an extra session is harmless, and
  # the caller closes the session it ends up with anyway.
  #
  # @param name [String] The name of the API call.
  # @return [Boolean] Whether the call can be repeated or not.
  def repeatable?(name)
    read_only?(name) || name == 'auth.login'
  end

  # Returns whether an error was raised before the request reached the server.
  # Faraday wraps the error the adapter raised rather than letting it through, so the
  # original one is what the answer depends on.
  #
  # @param error [StandardError] The error the call failed with.
  # @return [Boolean] Whether the request is known not to have been sent or not.
  def unsent_request?(error)
    cause = error.is_a?(Faraday::Error) ? error.wrapped_exception || error : error
    UNSENT_REQUEST_ERRORS.any? { |klass| cause.is_a?(klass) }
  end

  # Runs the given block, and retries it when it fails with a transport-level error.
  # A call that is not repeatable is only retried while the request is known not to have
  # reached the server, so that it can never be applied twice.
  #
  # @param name [String] The name of the API call, used to tell what is safe to retry.
  # @return [Object] The value returned by the block.
  def with_retries(name)
    attempt = 1
    begin
      yield
    rescue *TRANSPORT_ERRORS => e
      raise if attempt >= MAX_ATTEMPTS
      raise unless repeatable?(name) || unsent_request?(e)

      delay = attempt * RETRY_DELAY
      attempt += 1
      warn "API call '#{name}' failed with #{e.class}: #{e.message}. Retrying in #{delay} seconds (attempt #{attempt}/#{MAX_ATTEMPTS})."
      sleep delay
      retry
    end
  end
end
