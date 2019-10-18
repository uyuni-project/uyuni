# Copyright (c) 2013-2019 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'tempfile'
require 'yaml'

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

# extract terminals from a retail yaml configuration
def get_terminals_from_yaml(name)
  tree = YAML.load_file(name)
  tree['branches'].values[0]['terminals'].keys
end

def get_branch_prefix_from_yaml(name)
  tree = YAML.load_file(name)
  tree['branches'].values[0]['branch_prefix']
end

def get_server_domain_from_yaml(name)
  tree = YAML.load_file(name)
  tree['branches'].values[0]['server_domain']
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

def product
  _product_raw, code = $server.run('rpm -q patterns-uyuni_server', false)
  return 'Uyuni' if code.zero?
  _product_raw, code = $server.run('rpm -q patterns-suma_server', false)
  return 'SUSE Manager' if code.zero?
  raise 'Could not determine product'
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

# WARN: It's working for /24 mask, but couldn't not work properly with others
def get_reverse_net(net)
  a = net.split('.')
  a[2] + '.' + a[1] + '.' + a[0] + '.in-addr.arpa'
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

def click_button_and_wait(locator = nil, **options)
  page.click_button(locator, options)
  # TODO: Rid of sleep in those wrappers, sometimes .senna-loading still not loaded,
  #       so we don't wait for the ajax transition. We added this sleep because using:
  #       > has_css?('.senna-loading', wait: 0.3)
  #       raise the error:
  #       > stale element reference: element is not attached to the page document
  #       We couldn't bring a better solution for now
  sleep 0.5
  raise 'Timeout: Waiting AJAX transition (click button)' unless page.has_no_css?('.senna-loading')
end

def click_link_and_wait(locator = nil, **options)
  page.click_link(locator, options)
  sleep 0.5
  raise 'Timeout: Waiting AJAX transition (click link)' unless page.has_no_css?('.senna-loading')
end

def click_link_or_button_and_wait(locator = nil, **options)
  page.click_link_or_button(locator, options)
  sleep 0.5
  raise 'Timeout: Waiting AJAX transition (click link or button)' unless page.has_no_css?('.senna-loading')
end

# Capybara Node Element extension to override click method, clicking and then waiting for ajax transition
module CapybaraNodeElementExtension
  def click
    super
    sleep 0.5
    raise 'Timeout: Waiting AJAX transition (find::click)' unless has_no_css?('.senna-loading')
  end
end

def find_and_wait_click(*args, **options, &optional_filter_block)
  element = page.find(*args, options, &optional_filter_block)
  element.extend(CapybaraNodeElementExtension)
end
