# Copyright (c) 2013-2021 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'tempfile'
require 'yaml'
require 'date'

# Timeout Lib module includes methods to handle timeouts
module TimeoutLib
  extend self

  # Repeatedly executes a block raising an exception in case it is not finished within timeout seconds
  # or retries attempts, whichever comes first.
  # Exception will optionally contain the specified message and the result from the last block execution, if any,
  # in case report_result is set to true
  #
  # Implementation works around https://bugs.ruby-lang.org/issues/15886
  def repeat_until_timeout(timeout: DEFAULT_TIMEOUT, retries: nil, message: nil, report_result: false)
    last_result = nil
    Timeout.timeout(timeout) do
      # HACK: Timeout.timeout might not raise Timeout::Error depending on the yielded code block
      # Pitfalls with this method have been long known according to the following articles:
      # https://rnubel.svbtle.com/ruby-timeouts
      # https://vaneyckt.io/posts/the_disaster_that_is_rubys_timeout_method
      # At the time of writing some of the problems described have been addressed.
      # However, at least https://bugs.ruby-lang.org/issues/15886 remains reproducible and code below
      # works around it by adding an additional check between loops
      start = Time.new
      attempts = 0
      while (Time.new - start <= timeout) && (retries.nil? || attempts < retries)
        last_result = yield
        attempts += 1
      end

      detail = format_detail(message, last_result, report_result)
      raise(Timeout::Error, "Giving up after #{attempts} attempts#{detail}") if attempts == retries

      raise(Timeout::Error, "Timeout after #{timeout} seconds (repeat_until_timeout)#{detail}")
    end
  rescue Timeout::Error
    raise(Timeout::Error, "Timeout after #{timeout} seconds (Timeout.timeout)#{format_detail(message, last_result, report_result)}")
  end
end
