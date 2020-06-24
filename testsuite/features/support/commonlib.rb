# Copyright (c) 2013-2020 SUSE LLC.
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

# extract various data from Retail yaml configuration
def read_terminals_from_yaml
  name = File.dirname(__FILE__) + '/../upload_files/massive-import-terminals.yml'
  tree = YAML.load_file(name)
  tree['branches'].values[0]['terminals'].keys
end

def read_branch_prefix_from_yaml
  name = File.dirname(__FILE__) + '/../upload_files/massive-import-terminals.yml'
  tree = YAML.load_file(name)
  tree['branches'].values[0]['branch_prefix']
end

def read_server_domain_from_yaml
  name = File.dirname(__FILE__) + '/../upload_files/massive-import-terminals.yml'
  tree = YAML.load_file(name)
  tree['branches'].values[0]['server_domain']
end

# determine image for PXE boot tests
def compute_image_filename
  case ENV['PXEBOOT_IMAGE']
  when nil
    'Kiwi/POS_Image-JeOS6_head'
  when 'sles15sp2'
    'Kiwi/POS_Image-JeOS7_head'
  else
    'Kiwi/POS_Image-JeOS6_head'
  end
end

def compute_image_name
  case ENV['PXEBOOT_IMAGE']
  when nil
    'POS_Image_JeOS6_head'
  when 'sles15sp2'
    'POS_Image_JeOS7_head'
  else
    'POS_Image_JeOS6_head'
  end
end

# compute list of reposyncs to avoid killing because they might be involved in bootstrapping
# this is a safety net only, the best thing to do is to not start the reposync at all
def compute_list_to_leave_running
  do_not_kill = []
  [$minion, $build_host, $sshminion].each do |node|
    next if node.nil?
    os_version, os_family = get_os_version(node)
    if os_family == 'sles' && os_version == '12SP4'
      do_not_kill += ['sles12-sp4-pool-x86_64', 'sle-manager-tools12-pool-x86_64-sp4', 'sle-module-containers12-pool-x86_64-sp4',
                      'sles12-sp4-updates-x86_64', 'sle-manager-tools12-updates-x86_64-sp4', 'sle-module-containers12-updates-x86_64-sp4']
    elsif os_family == 'sles' && os_version == '15SP1'
      do_not_kill += ['sle-product-sles15-sp1-pool-x86_64', 'sle-manager-tools15-pool-x86_64-sp1', 'sle-module-containers15-sp1-pool-x86_64',
                      'sle-product-sles15-sp1-updates-x86_64', 'sle-manager-tools15-updates-x86_64-sp1', 'sle-module-containers15-sp1-updates-x86_64']
    end
  end
  do_not_kill
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

# create salt pillar file in the default pillar_roots location
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
  click_button(locator, options)
  begin
    raise 'Timeout: Waiting AJAX transition (click link)' unless has_no_css?('.senna-loading', wait: 5)
  rescue StandardError, Capybara::ExpectationNotMet => e
    puts e.message # Skip errors related to .senna-loading element
  end
end

def click_link_and_wait(locator = nil, **options)
  click_link(locator, options)
  begin
    raise 'Timeout: Waiting AJAX transition (click link)' unless has_no_css?('.senna-loading', wait: 5)
  rescue StandardError, Capybara::ExpectationNotMet => e
    puts e.message # Skip errors related to .senna-loading element
  end
end

def click_link_or_button_and_wait(locator = nil, **options)
  click_link_or_button(locator, options)
  begin
    raise 'Timeout: Waiting AJAX transition (click link)' unless has_no_css?('.senna-loading', wait: 5)
  rescue StandardError, Capybara::ExpectationNotMet => e
    puts e.message # Skip errors related to .senna-loading element
  end
end

# Capybara Node Element extension to override click method, clicking and then waiting for ajax transition
module CapybaraNodeElementExtension
  def click
    super
    begin
      raise 'Timeout: Waiting AJAX transition (click link)' unless has_no_css?('.senna-loading', wait: 5)
    rescue StandardError, Capybara::ExpectationNotMet => e
      puts e.message # Skip errors related to .senna-loading element
    end
  end
end

def find_and_wait_click(*args, **options, &optional_filter_block)
  element = find(*args, options, &optional_filter_block)
  element.extend(CapybaraNodeElementExtension)
end

def get_client_type(name)
  if name.include? '_client'
    'traditional'
  else
    'salt'
  end
end
