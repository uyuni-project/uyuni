# Copyright (c) 2013-2025 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'tempfile'
require 'yaml'
require 'nokogiri'
require 'timeout'
require 'rubygems'
require_relative 'constants'
require_relative 'api_test'

# Returns the current URL of the driver.
#
# @return [String] The current URL.
def current_url
  driver.current_url
end

# Counts the number of items in a table.
#
# @return [String] The number of items in the table.
# @raise [ScriptError] If there is an error counting the items.
def count_table_items
  items_label_xpath = '//button[contains(text(), \'Items \')]'
  raise ScriptError, 'Error counting items' unless (items_label = find(:xpath, items_label_xpath).text)

  items_label.split('of ')[1].strip
end

# Determines the product type (Uyuni or SUSE Manager) based on installed patterns, raises error if undetermined.
#
# @return [String] The product name.
def product
  return $product unless $product.nil?

  _product_raw, code = get_target('server').run('rpm -q patterns-uyuni_server', check_errors: false)
  if code.zero?
    $product = 'Uyuni'
    return 'Uyuni'
  end
  _product_raw, code = get_target('server').run('rpm -q patterns-suma_server', check_errors: false)
  if code.zero?
    $product = 'SUSE Manager'
    return 'SUSE Manager'
  end
  raise NotImplementedError, 'Could not determine product'
end

# Returns the version of the product
#
# @return [String] The version number of the product being tested.
def product_version
  product_raw, code = get_target('server').run('rpm -q patterns-uyuni_server', check_errors: false)
  m = product_raw.match(/patterns-uyuni_server-(.*)-.*/)
  return m[1] if code.zero? && !m.nil?

  product_raw, code = get_target('server').run('rpm -q patterns-suma_server', check_errors: false)
  m = product_raw.match(/patterns-suma_server-(.*)-.*/)
  return m[1] if code.zero? && !m.nil?

  raise NotImplementedError, 'Could not determine product version'
end

# Retrieves the full product version using the 'venv-salt-call' command.
#
# @return [String, nil] The full product version if the command execution was successful and
#   the output is not empty, otherwise nil.
def product_version_full
  cmd = 'venv-salt-call --local grains.get product_version | tail -n 1'
  out, code = get_target('server').run(cmd)
  out.strip if code.zero? && !out.nil?
end

# WARN: It's working for /24 mask, but couldn't not work properly with others
# Returns the reverse DNS lookup address for a given network address.
#
# @param net [String] The network address in the format "x.x.x.x".
# @return [String] The reverse DNS lookup address in the format "x.x.x.in-addr.arpa".
def get_reverse_net(net)
  a = net.split('.')
  "#{a[2]}.#{a[1]}.#{a[0]}.in-addr.arpa"
end

# Repeatedly executes a block raising an exception in case it is not finished within timeout seconds
# or retries attempts, whichever comes first.
# Exception will optionally contain the specified message and the result from the last block execution, if any, in case
# report_result is set to true
#
# Implementation works around https://bugs.ruby-lang.org/issues/15886
def repeat_until_timeout(timeout: DEFAULT_TIMEOUT, retries: nil, message: nil, report_result: false, dont_raise: false)
  begin
    last_result = nil
    # When we run the code coverage tracking tool together with our server, its performance decreases.
    timeout *= 2 if $code_coverage_mode
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
      raise ScriptError, "Giving up after #{attempts} attempts#{detail}" if attempts == retries

      raise Timeout::Error, "Timeout after #{timeout} seconds (repeat_until_timeout)#{detail}"
    end
  rescue Timeout::Error => e
    $stdout.puts "Timeout after #{timeout} seconds (Timeout.timeout)#{format_detail(message, last_result, report_result)}"
    raise e unless dont_raise
  rescue StandardError => e
    raise e unless dont_raise
  end
end

#
# Checks if the specified text is visible on the page and catches a request timeout popup if it appears.
#
# @param text1 [String] The first text to check for visibility.
# @param text2 [String, nil] The second text to check for visibility (optional).
# @param timeout [Integer] The maximum time to wait for the text to become visible (default: Capybara.default_max_wait_time).
# @return [Boolean] Returns true if the text is visible or the request timeout popup is caught, false otherwise.
def check_text_and_catch_request_timeout_popup?(text1, text2: nil, timeout: Capybara.default_max_wait_time)
  return has_text?(text1, wait: timeout) || (!text2.nil? && has_text?(text2, wait: timeout)) unless $catch_timeout_message

  start_time = Time.now
  repeat_until_timeout(message: "'#{text1}' still not visible", timeout: DEFAULT_TIMEOUT) do
    while Time.now - start_time <= timeout
      begin
        return true if has_text?(text1, wait: 4)
        return true if !text2.nil? && has_text?(text2, wait: 4)
      rescue Selenium::WebDriver::Error::UnknownError, Selenium::WebDriver::Error::StaleElementReferenceError => e
        warn "Selenium::WebDriver::Error caught: #{e.message}"
        next
      end
      next unless has_text?('Request has timed out', wait: 0)

      log 'Request timeout found, performing reload'
      click_button('reload the page')
      start_time = Time.now
      raise "Request timeout message still present after #{Capybara.default_max_wait_time} seconds." unless has_no_text?('Request has timed out')

    end
    return false
  end
end

# Formats the detail message with optional last result and report result.
#
# @param message [String, nil] The detail message to be formatted.
# @param last_result [String, nil] The last result to be included in the formatted message.
# @param report_result [Boolean] Whether to include the last result in the formatted message.
# @return [String] The formatted detail message.
def format_detail(message, last_result, report_result)
  formatted_message = "#{': ' unless message.nil?}#{message}"
  formatted_result = "#{', last result was: ' unless last_result.nil?}#{last_result}" if report_result
  "#{formatted_message}#{formatted_result}"
end

# This Ruby function refreshes the current page and handles any modal not found errors.
def refresh_page
  begin
    accept_prompt do
      execute_script 'window.location.reload()'
    end
  rescue Capybara::ModalNotFound
    # ignored
  end
end

#
# Clicks a button and waits for any AJAX transition to complete.
#
# @param locator [String] (optional) The locator for the button element.
# @param options [Hash] (optional) Additional options for the click_button method.
def click_button_and_wait(locator = nil, **options)
  click_button(locator, **options)
  begin
    warn 'Timeout: Waiting AJAX transition (click link)' unless has_no_css?('.senna-loading', wait: 20)
  rescue StandardError => e
    $stdout.puts e.message # Skip errors related to .senna-loading element
  end
end

#
# Clicks on a link and waits for any AJAX transition to complete.
#
# @param locator [String, nil] The locator for the link to click.
# @param options [Hash] Additional options for the click action.
def click_link_and_wait(locator = nil, **options)
  click_link(locator, **options)
  begin
    warn 'Timeout: Waiting AJAX transition (click link)' unless has_no_css?('.senna-loading', wait: 20)
  rescue StandardError => e
    $stdout.puts e.message # Skip errors related to .senna-loading element
  end
end

#
# Clicks on a link or button and waits for any AJAX transition to complete.
#
# @param locator [String] (optional) The locator for the link or button to click.
# @param options [Hash] (optional) Additional options for the click operation.
def click_link_or_button_and_wait(locator = nil, **options)
  click_link_or_button(locator, **options)
  begin
    warn 'Timeout: Waiting AJAX transition (click link)' unless has_no_css?('.senna-loading', wait: 20)
  rescue StandardError => e
    $stdout.puts e.message # Skip errors related to .senna-loading element
  end
end

# Capybara Node Element extension to override click method, clicking and then waiting for ajax transition
module CapybaraNodeElementExtension
  def click
    super
    begin
      warn 'Timeout: Waiting AJAX transition (click link)' unless has_no_css?('.senna-loading', wait: 20)
    rescue StandardError => e
      $stdout.puts e.message # Skip errors related to .senna-loading element
    end
  end
end

# Determines the type of client based on the given name.
#
# @param name [String] the name of the client
# @return [String] the type of client ('traditional' or 'salt')
def get_client_type(name)
  if name.include? '_client'
    'traditional'
  else
    'salt'
  end
end

#
# Finds an element using the given arguments and options, waits for it to be visible,
# and then clicks on it. Returns the element after extending it with the
# CapybaraNodeElementExtension module.
#
# @param args [Array] The arguments to be passed to the `find` method.
# @param options [Hash] The options to be passed to the `find` method.
# @param optional_filter_block [Block] An optional filter block to be passed to the `find` method.
# @return [Capybara::Node::Element] The element after extending it with the CapybaraNodeElementExtension module.
def find_and_wait_click(*args, **options, &optional_filter_block)
  element = find(*args, **options, &optional_filter_block)
  element.extend(CapybaraNodeElementExtension)
end

# Determines if a host is a SUSE host based on its name.
#
# @param name [String] The name of the host.
# @return [Boolean] Returns true if the host is a SUSE host, false otherwise.
def suse_host?(name, runs_in_container: true)
  node = get_target(name)
  os_family = runs_in_container ? node.os_family : node.local_os_family
  %w[sles opensuse opensuse-tumbleweed opensuse-leap sle-micro suse-microos opensuse-leap-micro].include? os_family
end

# Determines if the given host name is a SLE/SL Micro host.
#
# @param name [String] The host name to check.
# @return [Boolean] Returns true if the system is a SLE/SL Micro one
def slemicro_host?(name, runs_in_container: true)
  node = get_target(name)
  os_family = runs_in_container ? node.os_family : node.local_os_family
  os_family.include?('sle-micro') || os_family.include?('suse-microos') || os_family.include?('sl-micro')
end

# Determines if the given host name is a openSUSE Leap Micro host.
#
# @param name [String] The host name to check.
# @return [Boolean] Returns true if the system is a openSUSE Leap Micro one.
def leapmicro_host?(name, runs_in_container: true)
  node = get_target(name)
  os_family = runs_in_container ? node.os_family : node.local_os_family
  os_family.include?('opensuse-leap-micro')
end

# Determines if the given host name is a transactional system
# At the moment only SLE/SL Micro and openSUSE Leap Micro
#
# @param name [String] The host name to check.
# @return [Boolean] Returns true if the system is a transactional system
def transactional_system?(name, runs_in_container: true)
  slemicro_host?(name, runs_in_container: runs_in_container) || leapmicro_host?(name, runs_in_container: runs_in_container)
end

# Checks if the 'proxy' host is a transactional system
#
# @return [Boolean] Returns true if the proxy is transactional
def suse_proxy_transactional?
  ENV.key?(ENV_VAR_BY_HOST['proxy']) && transactional_system?('proxy', runs_in_container: false)
end

# Checks if the 'proxy' host is a is non-transactional
#
# @return [Boolean] Returns true if the proxy is is non-transactional
def suse_proxy_non_transactional?
  ENV.key?(ENV_VAR_BY_HOST['proxy']) && !transactional_system?('proxy', runs_in_container: false)
end

# Determines if a given host name belongs to a Red Hat-like distribution.
#
# @param name [String] the host name to check
# @return [Boolean] true if the host name belongs to a Red Hat-like distribution, false otherwise
def rh_host?(name)
  os_family = get_target(name).os_family
  %w[alma almalinux amzn centos liberty ol oracle rocky redhat rhel].include? os_family
end

# Determines if the given host name is a Debian-based host.
#
# @param name [String] The host name to check.
# @return [Boolean] Returns true if the host name is Debian-based, false otherwise.
def deb_host?(name)
  os_family = get_target(name).os_family
  %w[debian ubuntu].include? os_family
end

# Checks if a repository exists.
#
# @param repo [String] The name of the repository to check.
# @return [Boolean] Returns true if the repository exists, false otherwise.
def repository_exist?(repo)
  repo_list = $api_test.channel.software.list_user_repos
  repo_list.include? repo
end

# Generates a repository name based on the provided repo URL.
#
# @param repo_url [String] The URL of the repository.
# @return [String] The generated repository name.
def generate_repository_name(repo_url)
  repo_name = repo_url.strip
  repo_name.sub!(%r{http://(download.suse.de|download.opensuse.org|minima-mirror-ci-bv.mgr.*|.*compute.internal)/ibs/SUSE:/Maintenance:/}, '')
  repo_name.sub!(%r{http://(download.suse.de|download.opensuse.org|minima-mirror-ci-bv.mgr.*|.*compute.internal)/download/ibs/SUSE:/Maintenance:/}, '')
  repo_name.sub!(%r{http://(download.suse.de|download.opensuse.org|minima-mirror-ci-bv.mgr.*|.*compute.internal)/download/ibs/SUSE:/}, '')
  repo_name.sub!(%r{http://(download.suse.de|download.opensuse.org|minima-mirror-ci-bv.mgr.*|.*compute.internal)/repositories/systemsmanagement:/}, '')
  repo_name.sub!(%r{http://(download.suse.de|download.opensuse.org|minima-mirror-ci-bv.mgr.*|.*compute.internal)/SUSE:/}, '')
  repo_name.sub!(%r{http://(download.suse.de|download.opensuse.org|minima-mirror-ci-bv.mgr.*|.*compute.internal)/ibs/Devel:/Galaxy:/Manager:/}, '')
  repo_name.sub!(%r{http://(download.suse.de|download.opensuse.org|minima-mirror-ci-bv.mgr.*|.*compute.internal)/SUSE:/Maintenance:/}, '')
  repo_name.sub!(%r{http://(download.suse.de|download.opensuse.org|minima-mirror-ci-bv.mgr.*|.*compute.internal)/ibs/SUSE:/SLE-15:/Update:/Products:/MultiLinuxManagerTools/images/repo/}, '')
  repo_name.sub!(%r{http://(download.suse.de|download.opensuse.org|minima-mirror-ci-bv.mgr.*|.*compute.internal)/ibs/SUSE:/}, '')
  repo_name.gsub!('/', '_')
  repo_name.gsub!(':', '_')
  repo_name[0...64] # HACK: Due to the 64 characters size limit of a repository label
end

#
# Extracts logs from a given node.
#
# @param [Node] node - The node from which to extract the logs.
# @param [Host] host - The host from which to extract the logs.
# @raise [ScriptError] if the download of the log archive fails.
def extract_logs_from_node(node, host)
  begin
    os_family = node.os_family
    if os_family.match?(/^opensuse/) && !$is_gh_validation && !transactional_system?(host)
      node.run('zypper --non-interactive install tar')
    elsif transactional_system?(host)
      node.run('transactional-update --continue -n pkg install tar')
    end
    node.run('journalctl > /var/log/messages', check_errors: false)
    node.run('venv-salt-call --local grains.items | tee -a /var/log/salt_grains', verbose: true, check_errors: false) unless $host_by_node[node] == 'server'
    node.run("tar cfvJP /tmp/#{node.full_hostname}-logs.tar.xz /var/log/ || [[ $? -eq 1 ]]")
    `mkdir logs` unless Dir.exist?('logs')
    success = file_extract(node, "/tmp/#{node.full_hostname}-logs.tar.xz", "logs/#{node.full_hostname}-logs.tar.xz")
    raise ScriptError, 'Download log archive failed' unless success
  rescue Errno::ECONNRESET
    $stdout.puts "⚠️ WARN: Skipping log extraction for node #{host} due to connection reset."
  rescue RuntimeError => e
    $stdout.puts e.message
  end
end

# Executes a SQL query on the server.
#
# @param query [String] The SQL query to execute.
# @return [String] The result from the SQL query.
def reportdb_server_query(query)
  "echo \"#{query}\" | spacewalk-sql --reportdb --select-mode -"
end

# Retrieves the uptime of a given host.
#
# @param [String] host The hostname or IP address of the target host.
# @return [Hash] A hash containing the uptime in seconds, minutes, hours, and days.
def get_uptime_from_host(host)
  node = get_target(host)
  uptime, _return_code = node.run('cat /proc/uptime') # run code on node only once, to get uptime
  seconds = Float(uptime.split[0]) # return only the uptime in seconds, as a float
  minutes = (seconds / 60.0) # 60 seconds; the .0 forces a float division
  hours = (minutes / 60.0) # 60 minutes
  days = (hours / 24.0) # 24 hours
  { seconds: seconds, minutes: minutes, hours: hours, days: days }
end

# Escapes special characters in a given text to be treated as literal characters in a regular expression.
#
# @param text [String] The text to escape.
# @return [String] The escaped text.
def escape_regex(text)
  text.gsub(%r{([$.*\[/^])}) { |match| "\\#{match}" }
end

# Returns the system ID of the given node.
#
# @param [Node] node The node object representing the system.
# @return [String] The system ID.
def get_system_id(node)
  result = $api_test.system.search_by_name(node.full_hostname)
  raise "No system found for hostname: #{node.full_hostname}" unless result.any?

  result.first['id']
end

# Checks if a host has shut down within a specified timeout period.
#
# @param host [String] The hostname or IP address of the host to check.
# @param time_out [Integer] The timeout period in seconds.
def check_shutdown(host, time_out)
  cmd = "ping -c1 #{host}"
  repeat_until_timeout(timeout: time_out, message: 'machine didn\'t reboot') do
    _out = `#{cmd}`
    if $CHILD_STATUS.exitstatus.nonzero?
      $stdout.puts "machine: #{host} went down"
      break
    else
      sleep 1
    end
  end
end

#
# Checks if a machine has restarted successfully by pinging the host and checking SSH connectivity.
#
# @param host [String] The IP address or hostname of the machine to check.
# @param node [Object] The node object representing the machine.
# @param time_out [Integer] The maximum time in seconds to wait for the machine to come up.
def check_restart(host, node, time_out)
  cmd = "ping -c1 #{host}"
  repeat_until_timeout(timeout: time_out, message: 'machine didn\'t come up') do
    _out = `#{cmd}`
    if $CHILD_STATUS.exitstatus.zero?
      $stdout.puts "machine: #{host} network is up"
      break
    else
      sleep 1
    end
  end
  repeat_until_timeout(timeout: time_out, message: 'machine didn\'t come up') do
    _out, code = node.run('ls', check_errors: false, timeout: 10)
    if code.zero?
      $stdout.puts "machine: #{host} ssh is up"
      break
    else
      sleep 1
    end
  end
end

#
# Retrieves the GPG keys for a given node and target.
#
# @param node [String] The node for which to retrieve the GPG keys.
# @param target [String] The target server to execute the command on.
#   Defaults to the server obtained from the `get_target` method.
# @return [Array<String>] An array of GPG keys.
def get_gpg_keys(node, target = get_target('server'))
  os_version = node.os_version
  os_family = node.os_family
  case os_family
  when /^sles/
    # HACK: SLE 15 uses SLE 12 GPG key
    if os_version.start_with?('15')
      os_version = 12
    elsif os_version.start_with?('12')
      # SLE12 GPG keys don't contain service pack strings
      os_version = os_version.slice(0, 2)
    end
    gpg_keys, _code = target.run("cd /srv/www/htdocs/pub/ && ls -1 sle#{os_version}*", check_errors: false)
  when /^centos/
    gpg_keys, _code = target.run("cd /srv/www/htdocs/pub/ && ls -1 #{os_family}#{os_version}* res*", check_errors: false)
  else
    gpg_keys, _code = target.run("cd /srv/www/htdocs/pub/ && ls -1 #{os_family}*", check_errors: false)
  end
  gpg_keys.lines.map(&:strip)
end

# Retrieves the value associated within the current feature scope context.
#
# @param key [Symbol] The key to retrieve the value for.
# @return [Object, nil] The value associated with the key, or nil if the key is not found.
def get_context(key)
  return unless $context.key?($feature_scope)

  $context[$feature_scope][key]
end

# Define or replace a key-value in the current feature scope context
#
# @param key [Object] The key to be added to the context hash.
# @param value [Object] The value to be associated with the key in the context hash.
def add_context(key, value)
  $context[$feature_scope] = {} unless $context.key?($feature_scope)
  $context[$feature_scope].merge!({ key => value })
end

# This function gets the system name, as displayed in systems list
# * for the usual clients, it is the full hostname, e.g. suma-41-min-sle15.tf.local
# * for the PXE booted clients, it is derived from the branch name, the hardware type,
#   and a fingerprint, e.g. example.Intel-Genuine-None-d6df84cca6f478cdafe824e35bbb6e3b
# TODO: don't hardcode anymore the names in the private network once we have them in .bashrc
#
# @param host [String] The name of the host.
# @return [String] The system name.
def get_system_name(host)
  case host
  # The PXE boot minion and the terminals are not directly accessible on the network,
  # therefore they are not represented by a RemoteNode
  when 'pxeboot_minion'
    output, _code = get_target('server').run('salt-key')
    system_name =
      output.split.find do |word|
        word.match?(/example.Intel-Genuine-None-/) || word.match?(/example.pxeboot-/) || word.match?(/example.Intel/) || word.match?(/pxeboot-/)
      end
    system_name = 'pxeboot.example.org' if system_name.nil?
  when 'sle15sp6_terminal'
    output, _code = get_target('server').run('salt-key')
    system_name =
      output.split.find do |word|
        word.match?(/example.sle15sp6terminal-/)
      end
    system_name = 'sle15sp6terminal.example.org' if system_name.nil?
  when 'sle15sp7_terminal'
    output, _code = get_target('server').run('salt-key')
    system_name =
      output.split.find do |word|
        word.match?(/example.sle15sp7terminal-/)
      end
    system_name = 'sle15sp7terminal.example.org' if system_name.nil?
  else
    begin
      node = get_target(host)
      system_name = node.full_hostname
    rescue NotImplementedError => e
      # If the node for that host is not defined, just return the host parameter as system_name
      warn e.message
      system_name = host
    end
  end
  system_name
end

# Retrieves the MAC address of a given host.
#
# @param host [String] The name of the host.
# @return [String] The MAC address of the host.
def get_mac_address(host)
  if host == 'pxeboot_minion'
    mac = ENV.fetch('PXEBOOT_MAC', nil)
  else
    node = get_target(host)
    output, _code = node.run('ip link show dev eth1')
    mac = output.split("\n")[1].split[1]
  end
  mac
end

# This function returns the net prefix, caching it
def net_prefix
  $net_prefix = $private_net.sub(%r{\.0+/24$}, '.') if $net_prefix.nil? && !$private_net.nil?
  $net_prefix
end

# This function updates the server certificate on the controller node by
# - deleting the old one from the nss database
# - importing the new one from the server
def update_controller_ca
  server_ip = get_target('server').public_ip
  server_name = get_target('server').full_hostname

  puts `certutil -d sql:/root/.pki/nssdb -t TC -n "susemanager" -D;
  rm /etc/pki/trust/anchors/*;
  curl http://#{server_ip}/pub/RHN-ORG-TRUSTED-SSL-CERT -o /etc/pki/trust/anchors/#{server_name}.cert &&
  update-ca-certificates &&
  certutil -d sql:/root/.pki/nssdb -A -t TC -n "susemanager" -i  /etc/pki/trust/anchors/#{server_name}.cert`
end

# This method returns the timeout, in seconds, for syncing the given channel
#
# @param channel [String] the channel to check
# @return [Integer] number of seconds representing the timeout
def channel_timeout(channel)
  if channel.include?('custom_channel') || channel.include?('ptf')
    $stdout.puts "#{channel} is a custom or PTF channel - timeout set to 10 minutes"
    return 600
  elsif TIMEOUT_BY_CHANNEL_NAME[channel].nil?
    $stdout.puts "Unknown timeout for channel #{channel}, assuming one minute"
    return 60
  end

  timeout = TIMEOUT_BY_CHANNEL_NAME[channel]
  timeout *= 2 if $code_coverage_mode
  timeout
end

# @param channel_label [String] the label of the channel to check
# @return [Boolean] true if the synchronization is completed, false otherwise
def channel_sync_completed?(channel_label)
  channel_details = $api_test.channel.software.get_details(channel_label)
  # 'C' for new created, 'S' for syncing and 'R' for ready
  channel_details['sync_status'] == 'R'
end

# Verifies that a list of channels has downloaded all delivered packages,
# blocking until completion or until the global timeout budget is exhausted.
#
# This method handles the lifecycle of channel synchronization by:
# 1 Initializing shared context variables (idempotent).
# 2 Calculating a cumulative timeout: Sum of (channel_timeouts) + 900s flat margin.
# 3 Polling the system until packages are downloaded or the timeout expires.
# 4 Updating a global 'channels_timeout' budget used by the step solving packages dependencies for each channel
#
# @param channels [String, Array<String>] A single channel name or an array of channel names.
# @param label [String] A descriptive name (e.g., parent channel name) for logging.
# @param margin [Integer] The time buffer in seconds to add to the timeout (e.g., 900 for a standard, 0 for custom/PTF).
#
# @return [void]
def wait_for_channels(channels, label, margin: 900)
  channels = Array(channels).clone
  # --- Context Initialization ---
  add_context('channels_timeout', 0) if get_context('channels_timeout').nil?
  add_context('channels_to_wait_solv_file', []) if get_context('channels_to_wait_solv_file').nil?
  add_context('channels_failed_downloading', []) if get_context('channels_failed_downloading').nil?

  # Register these channels for the later step solving packages dependencies for each channel
  add_context('channels_to_wait_solv_file', get_context('channels_to_wait_solv_file') + channels)
  # --- Timeout Calculation ---
  total_channel_timeouts = channels.reduce(0) { |acc, elem| acc + channel_timeout(elem) }
  timeout = total_channel_timeouts + margin
  time_spent = 0
  checking_rate = 10

  # --- Execution Loop ---
  begin
    repeat_until_timeout(timeout: timeout, message: "Sync failed for #{label}") do
      # Remove channels from the local tracking list as they complete
      channels.reject! { |c| channel_packages_are_downloaded?(c) }
      break if channels.empty?

      if ((time_spent += checking_rate) % 60).zero?
        log "#{time_spent / 60}m / #{timeout / 60}m waiting for #{label} synchronization"
      end
      sleep checking_rate
    end
  rescue StandardError => e
    log "Failed channels for #{label}: #{channels}. #{e.message}"
    # Cleanup: Remove failed channels from the solving queue
    add_context('channels_to_wait_solv_file', get_context('channels_to_wait_solv_file') - channels)
    add_context('channels_failed_downloading', get_context('channels_failed_downloading') + channels)
    # Credit the remaining time budget to the global channels timeout
    add_context('channels_timeout', get_context('channels_timeout') + (timeout - time_spent))
    raise unless $build_validation
  else
    # Success: Add the "saved" time from this run to the global channels timeout
    add_context('channels_timeout', get_context('channels_timeout') + (timeout - time_spent))
  end
end

# This method checks if the channel with the given label has been fully synced
#
# @param channel_name [String] the label of the channel to check
# @return [Boolean] true if the synchronization is completed, false otherwise
def channel_packages_are_downloaded?(channel_name)
  if channel_name.include?('custom_channel')
    client = channel_name.delete_prefix('custom_channel_')
    return true if $custom_repositories[client].nil?
  end
  log_tmp_file = '/tmp/reposync.log'
  get_target('server').extract('/var/log/rhn/reposync.log', log_tmp_file)
  unless File.exist?(log_tmp_file) && !File.empty?(log_tmp_file)
    log "DEBUG: Log file #{log_tmp_file} is missing or empty."
    return false
  end
  log_content = File.readlines(log_tmp_file)
  target_index = log_content.rindex { |line| line.include?("Channel: #{channel_name}") }
  if target_index.nil?
    log "DEBUG: Channel '#{channel_name}' not found in reposync.log"
    return false
  end
  log "DEBUG: Found channel '#{channel_name}' at line #{target_index + 1}. Checking for completion..."
  (target_index...log_content.length).each do |i|
    line = log_content[i]
    if line.include?('Channel: ') && !line.include?(channel_name)
      log "DEBUG: Found a different channel header before completion for #{channel_name} at line #{i + 1}."
      break
    end

    next unless line.include?('Sync of channel completed.')

    log "DEBUG: Found 'Sync of channel completed.' for #{channel_name} at line #{i + 1}."
    log "SUCCESS: #{channel_name} is fully synchronized."
    return true
  end
  log "DEBUG: Sync for #{channel_name} still in progress (no completion message found)."
  false
end

# Determines whether a channel is synchronized on the server.
#
# @param channel [String] The name of the channel to check.
# @return [Boolean] Returns true if the channel is synchronized, false otherwise.
def channel_is_synced?(channel)
  sync_status = false
  repo_path = "/var/cache/rhn/repodata/#{channel}"
  server = get_target('server')
  # Using a temporary dump file to avoid timeout with huge dumpsolv output
  tmp_file = "/tmp/#{channel}_solv_dump"

  _, new_file_check_code = server.run("test -f #{repo_path}/solv.new", check_errors: false)
  if new_file_check_code.zero?
    log "INFO: Found #{repo_path}/solv.new - metadata regeneration still in progress."
    return false
  end
  deb_code = nil

  # Try RPM-based solv check
  _, rpm_code = server.run("dumpsolv #{repo_path}/solv > #{tmp_file}", verbose: false, check_errors: false)
  # Try Debian-based check
  _, deb_code = server.run("test -s #{repo_path}/Release && test -e #{repo_path}/Packages", verbose: false, check_errors: false) unless rpm_code.zero?
  if rpm_code.zero?
    size_check, = server.run("grep 'repo size:' #{tmp_file}", verbose: false, check_errors: false)
    if size_check.include?('repo size: 0')
      if EMPTY_CHANNELS.include?(channel)
        log "INFO: Channel #{channel} is verified empty as expected."
      else
        # Confirm package count in XML for channels not explicitly marked as empty
        primary_result, = server.run("zcat #{repo_path}/*primary.xml.gz", verbose: false, check_errors: false)
        log "WARN: #{channel} metadata exists but contains 0 packages." if primary_result.include?('packages="0")')
      end
    else
      log "SUCCESS: Channel #{channel} initialized. No '.new' files and repo size > 0."
    end
    sync_status = true
    server.run("rm #{tmp_file}", verbose: false, check_errors: false)
  elsif deb_code&.zero?
    log "SUCCESS: Debian-like channel #{channel} initialized (Release/Packages exist)."
    sync_status = true
  else
    sync_status = false
  end

  # Log duration if synchronization is confirmed
  if sync_status
    begin
      duration = channel_synchronization_duration(channel)
      log "INFO: Channel #{channel} synchronization took #{duration} seconds."
    rescue ScriptError => e
      log "ERROR: Failed to retrieve sync duration for #{channel}: #{e.message}"
      # We don't necessarily set sync_status to false here if the files actually exist
    end
  end

  sync_status
end

# This function initializes the API client
#
# The API client is determined based on the `$debug_mode` and `product` variables.
# If `$debug_mode` is true or the `product` is 'SUSE Manager', an `ApiTestXmlrpc` client is created.
# Otherwise, an `ApiTestHttp` client is created with the `ssl_verify` parameter set to the negation of `$is_gh_validation`.
#
# @return [ApiTestXmlrpc, ApiTestHttp] The created API client.
def new_api_client
  hostname = get_target('server').full_hostname
  ssl_verify = !$is_gh_validation

  case $api_protocol
  when 'xmlrpc'
    ApiTestXmlrpc.new(hostname)
  when 'http'
    # We use API_PROTOCOL env. variable only for debugging purposes from our local machine, so we can skip the SSL verification
    ApiTestHttp.new(hostname, false)
  else
    if product == 'SUSE Manager'
      ApiTestXmlrpc.new(hostname)
    else
      ApiTestHttp.new(hostname, ssl_verify)
    end
  end
end

# Get a time in the future, adding the minutes passed as parameter
#
# @param minutes_to_add [Integer] The number of minutes to add to the current time.
# @return [String] The future time in the format "HH:MM".
# @raise [TypeError] If minutes_to_add is not an Integer.
def get_future_time(minutes_to_add)
  raise TypeError, 'minutes_to_add should be an Integer' unless minutes_to_add.is_a?(Integer)

  now = Time.new
  future_time = now + (60 * minutes_to_add)
  future_time.strftime('%H:%M').to_s.strip
end

# Get a token for the given secret and claims
# Valid claims:
#   - org
#   - onlyChannels
#
# @param secret [String] The secret key used to generate the token.
# @param claims [Hash] (optional) Additional claims to include in the token.
def token(secret, claims = {})
  payload = {}
  payload.merge!(claims)
  log secret
  JWT.encode payload, [secret].pack('H*').bytes.to_a.pack('c*'), 'HS256'
end

# Get the server secret key
#
# This method reads the contents of the rhn.conf file located at /etc/rhn/rhn.conf
# and extracts the server secret key using a regular expression. The server secret key
# is then returned as a string.
#
# @return [String] The server secret key.
def server_secret
  rhnconf, _code = get_target('server').run('cat /etc/rhn/rhn.conf', check_errors: false)
  data = /server.secret_key\s*=\s*(\h+)$/.match(rhnconf)
  data[1].strip
end

# Get a Salt pillar value passing the key and the minion name
#
# @param key [String] The key of the pillar to retrieve.
# @param minion [String] The name of the minion.
# @return [String] The value of the specified pillar key.
# @raise [RuntimeError] If an invalid minion target is provided.
def pillar_get(key, minion)
  system_name = get_system_name(minion)
  if minion == 'sle_minion'
    cmd = 'salt'
  elsif %w[ssh_minion rhlike_minion deblike_minion].include?(minion)
    cmd = 'mgr-salt-ssh'
  else
    raise 'Invalid target'
  end
  get_target('server').run("#{cmd} #{system_name} pillar.get #{key}")
end

# Get a Salt pillar value from the master via Salt runner
#
# @param key [String] The key of the pillar to retrieve.
# @return [String] The value of the specified pillar key.
def salt_master_pillar_get(key)
  output, _code = get_target('server').run('salt-run --out=yaml salt.cmd pillar.items')
  pillars = YAML.load(output)
  pillars.key?(key) ? pillars[key] : ''
end

# Wait for an action to be completed, passing the action id and a timeout
#
# @param actionid [String] The ID of the action to wait for.
# @param timeout [Integer] The maximum time to wait for the action to complete, in seconds. Defaults to `DEFAULT_TIMEOUT`.
def wait_action_complete(actionid, timeout: DEFAULT_TIMEOUT)
  repeat_until_timeout(timeout: timeout, message: 'Action was not found among completed actions') do
    list = $api_test.schedule.list_completed_actions
    break if list.any? { |a| a['id'] == actionid }

    sleep 2
  end
end

# This function deletes channels matching one of the filters
#
# @param channels [Array<String>] The list of channels to filter.
# @param filters [Array<String>] The list of filters to apply.
def filter_channels(channels, filters = [])
  if channels.nil? || channels.empty?
    puts 'Warning: No channels to filter'
  else
    filtered_channels = channels.clone
    filters.each do |filter|
      filtered_channels.delete_if { |channel| channel.include? filter }
    end
  end
  filtered_channels
end

# Mutex for processes accessing the API of the server via admin user
def api_lock?
  File.open('server_api_call.lock', File::CREAT) do |file|
    return !file.flock(File::LOCK_EX)
  end
end

# Unlock the Mutex for processes accessing the API of the server via admin user
def api_unlock
  File.open('server_api_call.lock') do |file|
    file.flock(File::LOCK_UN)
  end
end

# Function to get the highest event ID (latest event)
#
# @param host String The hostname of the system from requested
def get_last_event(host)
  node = get_target(host)
  system_id = get_system_id(node)
  $api_test.system.get_event_history(system_id, 0, 1)[0]
end

# Function to trigger the upgrade command
#
# @param hostname String The hostname of the system from requested
# @param package String The package name where it will trigger an upgrade
def trigger_upgrade(hostname, package)
  get_target('server').run("spacecmd -u admin -p admin system_upgradepackage #{hostname} #{package} -y", check_errors: true)
end

# Function to select the latest package from a list based on version and release
#
# @param packages [Array<String>] A list of package strings in the format 'name-version-release'
# @return [String] The package string with the highest version and release
def latest_package(packages)
  packages.max_by do |package|
    # Match something like 'bison-3.8.2-3.oe2403sp1'
    if package =~ /^(.+)-(\d+(?:\.\d+)*?)-(.+)$/
      version = Regexp.last_match(2)          # => "3.8.2"
      release = Regexp.last_match(3)          # => "3.oe2403sp1"

      begin
        # extract numeric components like ["3", "2403", "1"]
        numeric_parts = release.scan(/\d+/)
        cleaned_release = numeric_parts.join('.')
        [
          Gem::Version.new(version),
          Gem::Version.new(cleaned_release)
        ]
      rescue ArgumentError => e
        puts "WARNING: Failed to parse version in package '#{package}': #{e.message}"
        [Gem::Version.new('0.0.0'), Gem::Version.new('0')]
      end
    else
      [Gem::Version.new('0.0.0'), Gem::Version.new('0')]
    end
  end
end
