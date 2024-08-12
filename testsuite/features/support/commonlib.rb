# Copyright (c) 2013-2024 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'tempfile'
require 'yaml'
require 'nokogiri'
require 'timeout'
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
  items_label_xpath = '//span[contains(text(), \'Items \')]'
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

# Retrieves the full product version using the 'salt-call' command.
#
# @return [String, nil] The full product version if the command execution was successful and
#   the output is not empty, otherwise nil.
def product_version_full
  cmd = 'salt-call --local grains.get product_version | tail -n 1'
  out, code = get_target('server').run(cmd)
  out.strip if code.zero? && !out.nil?
end

# Determines whether to use the Salt bundle based on the product and product version.
#
# @return [Boolean] true if the product is 'Uyuni' or the product version is 'head', '5.0', '4.3', or '4.2'
# - false otherwise
def use_salt_bundle
  # Use venv-salt-minion in Uyuni, or SUMA Head, 5.0, 4.2 and 4.3
  product == 'Uyuni' || %w[head 5.0 4.3 4.2].include?(product_version)
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
      return true if has_text?(text1, wait: 4)
      return true if !text2.nil? && has_text?(text2, wait: 4)

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
def suse_host?(name)
  (name.include? 'sle') || (name.include? 'opensuse') || (name.include? 'ssh')
end

# Determines if the given host name is a SLE/SL Micro host.
#
# @param name [String] The host name to check.
# @return [Boolean] Returns true if the system is a SLE/SL Micro one
def slemicro_host?(name)
  node = get_target(name)
  os_family = node.os_family
  (name.include? 'slemicro') || (name.include? 'micro') || os_family.include?('sle-micro') || os_family.include?('suse-microos')
end

# Determines if the given host name is a openSUSE Leap Micro host.
#
# @param name [String] The host name to check.
# @return [Boolean] Returns true if the system is a openSUSE Leap Micro one.
def leapmicro_host?(name)
  node = get_target(name)
  os_family = node.os_family
  os_family.include?('opensuse-leap-micro')
end

# Determines if the given host name is a transactional system
# At the moment only SLE/SL Micro and openSUSE Leap Micro
#
# @param name [String] The host name to check.
# @return [Boolean] Returns true if the system is a transactional system
def transactional_system?(name)
  slemicro_host?(name) || leapmicro_host?(name)
end

# Determines if a given host name belongs to a Red Hat-like distribution.
#
# @param name [String] the host name to check
# @return [Boolean] true if the host name belongs to a Red Hat-like distribution, false otherwise
def rh_host?(name)
  (name.include? 'rhlike') || (name.include? 'alma') || (name.include? 'centos') || (name.include? 'liberty') || (name.include? 'oracle') || (name.include? 'rocky')
end

# Determines if the given host name is a Debian-based host.
#
# @param name [String] The host name to check.
# @return [Boolean] Returns true if the host name is Debian-based, false otherwise.
def deb_host?(name)
  (name.include? 'deblike') || (name.include? 'debian') || (name.include? 'ubuntu')
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
  repo_name.sub!(%r{http://download.suse.de/ibs/SUSE:/Maintenance:/}, '')
  repo_name.sub!(%r{http://download.suse.de/download/ibs/SUSE:/Maintenance:/}, '')
  repo_name.sub!(%r{http://download.suse.de/download/ibs/SUSE:/}, '')
  repo_name.sub!(%r{http://.*compute.internal/SUSE:/}, '')
  repo_name.sub!(%r{http://.*compute.internal/SUSE:/Maintenance:/}, '')
  repo_name.gsub!('/', '_')
  repo_name.gsub!(':', '_')
  repo_name[0...64] # HACK: Due to the 64 characters size limit of a repository label
end

#
# Extracts logs from a given node.
#
# @param [Node] node - The node from which to extract the logs.
# @param [Host] - The host from which to extract the logs.
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
    code = file_extract(node, "/tmp/#{node.full_hostname}-logs.tar.xz", "logs/#{node.full_hostname}-logs.tar.xz")
    raise ScriptError, 'Download log archive failed' unless code.zero?
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
  # TODO: Remove this retrying code when this issue https://github.com/SUSE/spacewalk/issues/24084 is fixed:
  result = []
  repeat_until_timeout(message: "The API can't see the system id for '#{node.full_hostname}'", timeout: 10) do
    result = $api_test.system.search_by_name(node.full_hostname)
    break if result.any?

    sleep 1
  end
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
  os_version, os_family = get_os_version(node)
  os_version_str = os_version.to_s
  case os_family
  when /^sles/
    # HACK: SLE 15 uses SLE 12 GPG key
    if os_version_str.start_with?('15')
      os_version = 12
    elsif os_version_str.start_with?('12')
      # SLE12 GPG keys don't contain service pack strings
      os_version = os_version_str.slice(0, 2)
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
  when 'sle12sp5_terminal'
    output, _code = get_target('server').run('salt-key')
    system_name =
      output.split.find do |word|
        word.match?(/example.sle12sp5terminal-/)
      end
    system_name = 'sle12sp5terminal.example.org' if system_name.nil?
  when 'sle15sp4_terminal'
    output, _code = get_target('server').run('salt-key')
    system_name =
      output.split.find do |word|
        word.match?(/example.sle15sp4terminal-/)
      end
    system_name = 'sle15sp4terminal.example.org' if system_name.nil?
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

# Determines whether a channel is synchronized on the server.
#
# @param channel [String] The name of the channel to check.
# @return [Boolean] Returns true if the channel is synchronized, false otherwise.
def channel_is_synced(channel)
  # solv is the last file to be written when the server synchronizes a channel, therefore we wait until it exist
  result, code = get_target('server').run("dumpsolv /var/cache/rhn/repodata/#{channel}/solv", verbose: false, check_errors: false)
  if code.zero? && !result.include?('repo size: 0')
    # We want to check if no .new files exists. On a re-sync, the old files stay, the new one have this suffix until it's ready.
    _result, new_code = get_target('server').run("dumpsolv /var/cache/rhn/repodata/#{channel}/solv.new", verbose: false, check_errors: false)
    log 'Channel synced, no .new files exist and number of solvables is bigger than 0' unless new_code.zero?
    !new_code.zero?
  elsif result.include?('repo size: 0')
    if EMPTY_CHANNELS.include?(channel)
      true
    else
      _result, code = get_target('server').run("zcat /var/cache/rhn/repodata/#{channel}/*primary.xml.gz | grep 'packages=\"0\"'", verbose: false, check_errors: false)
      log "/var/cache/rhn/repodata/#{channel}/*primary.xml.gz contains 0 packages" if code.zero?
      false
    end
  else
    # If the solv file doesn't exist, we check if we are under a Debian-like repository
    command = "test -s /var/cache/rhn/repodata/#{channel}/Release && test -e /var/cache/rhn/repodata/#{channel}/Packages"
    _result, new_code = get_target('server').run(command, verbose: false, check_errors: false)
    log 'Debian-like channel synced, if Release and Packages files exist' if new_code.zero?
    new_code.zero?
  end
end

# This function initializes the API client
#
# The API client is determined based on the `$debug_mode` and `product` variables.
# If `$debug_mode` is true or the `product` is 'SUSE Manager', an `ApiTestXmlrpc` client is created.
# Otherwise, an `ApiTestHttp` client is created with the `ssl_verify` parameter set to the negation of `$is_gh_validation`.
#
# @return [ApiTestXmlrpc, ApiTestHttp] The created API client.
def new_api_client
  ssl_verify = !$is_gh_validation
  if product == 'SUSE Manager'
    ApiTestXmlrpc.new(get_target('server', refresh: true).full_hostname)
  else
    ApiTestHttp.new(get_target('server', refresh: true).full_hostname, ssl_verify)
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
  filters.each do |filter|
    channels.delete_if { |channel| channel.include? filter }
  end
  channels
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
