# Copyright (c) 2013-2024 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'tempfile'
require 'yaml'
require 'nokogiri'
require 'timeout'
require_relative 'constants'
require_relative 'api_test'

# return current URL
def current_url
  driver.current_url
end

def count_table_items
  # count table items using the table counter component
  items_label_xpath = '//span[contains(text(), \'Items \')]'
  raise ScriptError, 'Error counting items' unless (items_label = find(:xpath, items_label_xpath).text)

  items_label.split('of ')[1].strip
end

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

def product_version
  product_raw, code = get_target('server').run('rpm -q patterns-uyuni_server', check_errors: false)
  m = product_raw.match(/patterns-uyuni_server-(.*)-.*/)
  return m[1] if code.zero? && !m.nil?

  product_raw, code = get_target('server').run('rpm -q patterns-suma_server', check_errors: false)
  m = product_raw.match(/patterns-suma_server-(.*)-.*/)
  return m[1] if code.zero? && !m.nil?

  raise NotImplementedError, 'Could not determine product version'
end

# returns the full product version, e.g. 4.3-released or uyuni-master
def product_version_full
  cmd = 'salt-call --local grains.get product_version | tail -n 1'
  out, code = get_target('server').run(cmd)
  return out.strip if code.zero? && !out.nil?
end

def use_salt_bundle
  # Use venv-salt-minion in Uyuni, or SUMA Head, 5.0, 4.2 and 4.3
  product == 'Uyuni' || %w[head 5.0 4.3 4.2].include?(product_version)
end

# WARN: It's working for /24 mask, but couldn't not work properly with others
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

    raise TimeoutError, "Timeout after #{timeout} seconds (repeat_until_timeout)#{detail}"
  end
rescue Timeout::Error => e
  $stdout.puts "Timeout after #{timeout} seconds (Timeout.timeout)#{format_detail(message, last_result, report_result)}"
  raise e unless dont_raise
rescue StandardError => e
  raise e unless dont_raise
end

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

def format_detail(message, last_result, report_result)
  formatted_message = "#{': ' unless message.nil?}#{message}"
  formatted_result = "#{', last result was: ' unless last_result.nil?}#{last_result}" if report_result
  "#{formatted_message}#{formatted_result}"
end

def refresh_page
  accept_prompt do
    execute_script 'window.location.reload()'
  end
rescue Capybara::ModalNotFound
  # ignored
end

def click_button_and_wait(locator = nil, **options)
  click_button(locator, options)
  begin
    warn 'Timeout: Waiting AJAX transition (click link)' unless has_no_css?('.senna-loading', wait: 20)
  rescue StandardError => e
    $stdout.puts e.message # Skip errors related to .senna-loading element
  end
end

def click_link_and_wait(locator = nil, **options)
  click_link(locator, options)
  begin
    warn 'Timeout: Waiting AJAX transition (click link)' unless has_no_css?('.senna-loading', wait: 20)
  rescue StandardError => e
    $stdout.puts e.message # Skip errors related to .senna-loading element
  end
end

def click_link_or_button_and_wait(locator = nil, **options)
  click_link_or_button(locator, options)
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

def get_client_type(name)
  if name.include? '_client'
    'traditional'
  else
    'salt'
  end
end

def find_and_wait_click(*args, **options, &optional_filter_block)
  element = find(*args, options, &optional_filter_block)
  element.extend(CapybaraNodeElementExtension)
end

def suse_host?(name)
  (name.include? 'sle') || (name.include? 'opensuse') || (name.include? 'ssh')
end

def slemicro_host?(name)
  (name.include? 'slemicro') || (name.include? 'micro')
end

def rh_host?(name)
  (name.include? 'rhlike') || (name.include? 'alma') || (name.include? 'centos') || (name.include? 'liberty') || (name.include? 'oracle') || (name.include? 'rocky')
end

def deb_host?(name)
  (name.include? 'deblike') || (name.include? 'debian') || (name.include? 'ubuntu')
end

def repository_exist?(repo)
  repo_list = $api_test.channel.software.list_user_repos
  repo_list.include? repo
end

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

def extract_logs_from_node(node)
  os_family = node.os_family
  node.run('zypper --non-interactive install tar') if os_family =~ /^opensuse/ && !$is_gh_validation
  node.run('journalctl > /var/log/messages', check_errors: false)
  node.run('venv-salt-call --local grains.items | tee -a /var/log/salt_grains', verbose: true, check_errors: false) unless $host_by_node[node] == 'server'
  node.run("tar cfvJP /tmp/#{node.full_hostname}-logs.tar.xz /var/log/ || [[ $? -eq 1 ]]")
  `mkdir logs` unless Dir.exist?('logs')
  code = file_extract(node, "/tmp/#{node.full_hostname}-logs.tar.xz", "logs/#{node.full_hostname}-logs.tar.xz")
  raise ScriptError, 'Download log archive failed' unless code.zero?
rescue RuntimeError => e
  $stdout.puts e.message
end

def reportdb_server_query(query)
  "echo \"#{query}\" | spacewalk-sql --reportdb --select-mode -"
end

def get_uptime_from_host(host)
  node = get_target(host)
  uptime, _return_code = node.run('cat /proc/uptime') # run code on node only once, to get uptime
  seconds = Float(uptime.split[0]) # return only the uptime in seconds, as a float
  minutes = (seconds / 60.0) # 60 seconds; the .0 forces a float division
  hours = (minutes / 60.0) # 60 minutes
  days = (hours / 24.0) # 24 hours
  { seconds: seconds, minutes: minutes, hours: hours, days: days }
end

def escape_regex(text)
  text.gsub(%r{([$.*\[/^])}) { |match| "\\#{match}" }
end

def get_system_id(node)
  $api_test.system.search_by_name(node.full_hostname).first['id']
end

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

# Extract the OS version and OS family
# We get these data decoding the values in '/etc/os-release'
def get_os_version(node)
  os_family_raw, code = node.run('grep "^ID=" /etc/os-release', check_errors: false)
  return nil, nil unless code.zero?

  os_family = os_family_raw.strip.split('=')[1]
  return nil, nil if os_family.nil?

  os_family.delete! '"'
  os_version_raw, code = node.run('grep "^VERSION_ID=" /etc/os-release', check_errors: false)
  return nil, nil unless code.zero?

  os_version = os_version_raw.strip.split('=')[1]
  return nil, nil if os_version.nil?

  os_version.delete! '"'
  # on SLES, we need to replace the dot with '-SP'
  os_version.gsub!('.', '-SP') if os_family =~ /^sles/
  $stdout.puts "Node: #{node.hostname}, OS Version: #{os_version}, Family: #{os_family}"
  [os_version, os_family]
end

def get_gpg_keys(node, target = get_target('server'))
  os_version, os_family = get_os_version(node)
  case os_family
  when /^sles/
    # HACK: SLE 15 uses SLE 12 GPG key
    os_version = 12 if os_version =~ /^15/
    # SLE12 GPG keys don't contain service pack strings
    os_version = os_version.split('-')[0] if os_version =~ /^12/
    gpg_keys, _code = target.run("cd /srv/www/htdocs/pub/ && ls -1 sle#{os_version}*", check_errors: false)
  when /^centos/
    gpg_keys, _code = target.run("cd /srv/www/htdocs/pub/ && ls -1 #{os_family}#{os_version}* res*", check_errors: false)
  else
    gpg_keys, _code = target.run("cd /srv/www/htdocs/pub/ && ls -1 #{os_family}*", check_errors: false)
  end
  gpg_keys.lines.map(&:strip)
end

# Retrieve the value defined in the current feature scope context
def get_context(key)
  return unless $context.key?($feature_scope)

  $context[$feature_scope][key]
end

# Define or replace a key-value in the current feature scope context
def add_context(key, value)
  $context[$feature_scope] = {} unless $context.key?($feature_scope)
  $context[$feature_scope].merge!({ key => value })
end

# This function gets the system name, as displayed in systems list
# * for the usual clients, it is the full hostname, e.g. suma-41-min-sle15.tf.local
# * for the PXE booted clients, it is derived from the branch name, the hardware type,
#   and a fingerprint, e.g. example.Intel-Genuine-None-d6df84cca6f478cdafe824e35bbb6e3b
def get_system_name(host)
  case host
  # The PXE boot minion and the terminals are not directly accessible on the network,
  # therefore they are not represented by a twopence node
  when 'pxeboot_minion'
    output, _code = get_target('server').run('salt-key')
    system_name =
      output.split.find do |word|
        word =~ /example.Intel-Genuine-None-/ || word =~ /example.pxeboot-/ || word =~ /example.Intel/ || word =~ /pxeboot-/
      end
    system_name = 'pxeboot.example.org' if system_name.nil?
  when 'sle12sp5_terminal'
    output, _code = get_target('server').run('salt-key')
    system_name =
      output.split.find do |word|
        word =~ /example.sle12sp5terminal-/
      end
    system_name = 'sle12sp5terminal.example.org' if system_name.nil?
  when 'sle15sp4_terminal'
    output, _code = get_target('server').run('salt-key')
    system_name =
      output.split.find do |word|
        word =~ /example.sle15sp4terminal-/
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

# Get MAC address of system
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

# This functions checks if the channel has been synced
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
    command = "test -s /var/cache/rhn/repodata/#{channel}/Release && test -s /var/cache/rhn/repodata/#{channel}/Packages"
    _result, new_code = get_target('server').run(command, verbose: false, check_errors: false)
    log 'Debian-like channel synced, if Release and Packages files exist' if new_code.zero?
    new_code.zero?
  end
end

# This function initializes the API client
def new_api_client
  ssl_verify = !$is_gh_validation
  if $debug_mode || product == 'SUSE Manager'
    ApiTestXmlrpc.new(get_target('server', refresh: true).full_hostname)
  else
    ApiTestHttp.new(get_target('server', refresh: true).full_hostname, ssl_verify)
  end
end

# Get a time in the future, adding the minutes passed as parameter
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
def token(secret, claims = {})
  payload = {}
  payload.merge!(claims)
  log secret
  JWT.encode payload, [secret].pack('H*').bytes.to_a.pack('c*'), 'HS256'
end

# Get the server secret
def server_secret
  rhnconf, _code = get_target('server').run('cat /etc/rhn/rhn.conf', check_errors: false)
  data = /server.secret_key\s*=\s*(\h+)$/.match(rhnconf)
  data[1].strip
end

# Get a Salt pillar value passing the key and the minion name
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
def wait_action_complete(actionid, timeout: DEFAULT_TIMEOUT)
  repeat_until_timeout(timeout: timeout, message: 'Action was not found among completed actions') do
    list = $api_test.schedule.list_completed_actions
    break if list.any? { |a| a['id'] == actionid }

    sleep 2
  end
end
