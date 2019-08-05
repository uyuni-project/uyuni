# Copyright (c) 2013-2019 Novell, Inc.
# Licensed under the terms of the MIT license.

require 'tempfile'

# return current URL
def current_url
  driver.current_url
end

# generate temporary file on the controller
def generate_temp_file(name, content)
  Tempfile.open(name) do |file|
    file.write(content)
    return file.path
  end
end

# get registration URL
# the URL depends on whether we use a proxy or not
def registration_url
  if $proxy.nil?
    "https://#{$server.ip}/XMLRPC"
  else
    "https://#{$proxy.ip}/XMLRPC"
  end
end

def count_table_items
  # count table items using the table counter component
  items_label_xpath = "//span[contains(text(), 'Items ')]"
  raise unless (items_label = find(:xpath, items_label_xpath).text)
  items_label.split('of ')[1]
end

# This function creates salt pillar file in the default pillar_roots location
def inject_salt_pillar_file(source, file)
  dest = '/srv/pillar/' + file
  return_code = file_inject($server, source, dest)
  raise 'File injection failed' unless return_code.zero?
  # make file readeable by salt
  $server.run("chgrp salt #{dest}")
  return_code
end

# Repeatedly executes a block raising an exception in case it is not finished within timeout seconds
# or retries attempts, whichever comes first.
# Exception will optionally contain the specified message and the result from the last block execution, if any, in case
# report_result is set to true
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
    raise "Giving up after #{attempts} attempts#{detail}" if attempts == retries
    raise "Timeout after #{timeout} seconds (repeat_until_timeout)#{detail}"
  end
rescue Timeout::Error
  raise "Timeout after #{timeout} seconds (Timeout.timeout)#{format_detail(message, last_result, report_result)}"
end

def format_detail(message, last_result, report_result)
  formatted_message = "#{': ' unless message.nil?}#{message}"
  formatted_result = "#{', last result was: ' unless last_result.nil?}#{last_result}" if report_result
  "#{formatted_message}#{formatted_result}"
end
