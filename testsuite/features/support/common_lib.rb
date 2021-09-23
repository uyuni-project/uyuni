# Copyright (c) 2013-2021 SUSE LLC.
# Licensed under the terms of the MIT license.

require 'tempfile'
require 'yaml'
require 'jwt'
require 'date'
require_relative 'lavanda'
require_relative 'client_stack'

# Common Lib module includes a bunch of useful methods of our step definitions
module CommonLib
  extend LavandaBasic
  extend ClientStack
  extend self

  # return current URL
  def current_url
    $capybara_driver.current_url
  end

  # generate temporary file on the controller
  def generate_temp_file(name, content)
    Tempfile.open(name) do |file|
      file.write(content)
      return file.path
    end
  end

  # If we for example
  #  - start a reposync in reposync/srv_sync_channels.feature
  #  - then kill it in reposync/srv_wait_for_reposync.feature
  #  - then restart it later on in init_clients/sle_minion.feature
  # then the channel will be in an inconsistent state.
  #
  # This function computes a list of reposyncs to avoid killing, because they might be involved in bootstrapping.
  #
  # This is a safety net only, the best thing to do is to not start the reposync at all.
  def compute_list_to_leave_running
    # keep the repos needed for the auto-installation tests
    do_not_kill = $long_tests_enabled ? CHANNEL_TO_SYNCH_BY_OS_VERSION[:default] : []
    [$minion, $build_host, $sshminion].each do |node|
      next unless node

      os_version, os_family = get_os_version(node)
      next unless os_family == 'sles'
      raise(StandardError, "Can't build list of reposyncs to leave running") unless %w[12-SP4 12-SP5 15-SP2 15-SP3].include?(os_version)

      do_not_kill += CHANNEL_TO_SYNCH_BY_OS_VERSION[os_version.to_sym]
    end
    do_not_kill += CHANNEL_TO_SYNCH_BY_OS_VERSION[MIGRATE_SSH_MINION_FROM.to_sym]
    do_not_kill += CHANNEL_TO_SYNCH_BY_OS_VERSION[MIGRATE_SSH_MINION_TO.to_sym]
    do_not_kill.uniq
  end

  # Returns the amount of Items
  def count_table_items
    # count table items using the table counter component
    items_label_xpath = "//span[contains(text(), 'Items ')]"
    raise unless (items_label = find(:xpath, items_label_xpath).text)

    items_label.split('of ')[1]
  end

  # Returns the product tested, checking the pattern installed
  def product
    _product_raw, code = $server.run('rpm -q patterns-uyuni_server', check_errors: false)
    return 'Uyuni' if code.zero?

    _product_raw, code = $server.run('rpm -q patterns-suma_server', check_errors: false)
    return 'SUSE Manager' if code.zero?

    raise(ArgumentError, 'Could not determine product')
  end

  # create salt pillar file in the default pillar_roots location
  def inject_salt_pillar_file(source, file)
    dest = "/srv/pillar/#{file}"
    return_code = file_inject($server, source, dest)
    raise(ScriptError, 'File injection failed') unless return_code.zero?

    # make file readeable by salt
    $server.run("chgrp salt #{dest}")
    return_code
  end

  # WARN: It's working for /24 mask, but couldn't not work properly with others
  def get_reverse_net(net)
    a = net.split('.')
    "#{a[2]}.#{a[1]}.#{a[0]}.in-addr.arpa"
  end

  # Get interface slot by host name
  def get_interface_slot(host)
    case host
    when /^sle/, /^ssh/, /^ceos/, /^debian/, 'server', 'proxy', 'build_host'
      'eth0'
    when /^ubuntu/
      'ens3'
    when 'kvm_server', 'xen_server'
      'br0'
    else
      raise(ScriptError, "Unknown net interface for #{host}")
    end
  end

  # Retrieve Server ID
  def retrieve_server_id(server)
    sysrpc = XMLRPCSystemTest.new(ENV['SERVER'])
    sysrpc.login('admin', 'admin')
    systems = sysrpc.list_systems
    refute_nil(systems)
    server_id = systems
                .select { |s| s['name'] == server }
                .map { |s| s['id'] }
                .first
    refute_nil(server_id, "client #{server} is not yet registered?")
    server_id
  end

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

  # Returns a formatted message
  def format_detail(message, last_result, report_result)
    formatted_message = "#{': ' unless message.nil?}#{message}"
    formatted_result = "#{', last result was: ' unless last_result.nil?}#{last_result}" if report_result
    "#{formatted_message}#{formatted_result}"
  end

  # Discover the client type based on its name
  def get_client_type(name)
    if name.include?('_client')
      'traditional'
    else
      'salt'
    end
  end

  # Check if the repository is defined in the server
  def repository_exist?(repo)
    repo_xmlrpc = XMLRPCRepositoryTest.new(ENV['SERVER'])
    repo_xmlrpc.login('admin', 'admin')
    repo_list = repo_xmlrpc.repo_list
    repo_list.include?(repo)
  end

  # Generate a repository name for custom repositories
  def generate_repository_name(repo_url)
    repo_name = repo_url.strip
    repo_name.delete_prefix!('http://download.suse.de/ibs/SUSE:/Maintenance:/')
    repo_name.delete_prefix!('http://minima-mirror-qam.mgr.prv.suse.net/ibs/SUSE:/Maintenance:/')
    repo_name.gsub!('/', '_')
    # HACK: Due to the 64 characters size limit of a repository label
    repo_name[0...64]
  end

  # Extract logs from the specified node
  def extract_logs_from_node(node)
    _os_version, os_family = get_os_version(node)
    if os_family =~ /^opensuse/
      node.run('zypper mr --enable os_pool_repo os_update_repo') unless $build_validation
      node.run('zypper --non-interactive install tar')
      node.run('zypper mr --disable os_pool_repo os_update_repo') unless $build_validation
    end
    # Some clients might not support systemd
    node.run('journalctl > /var/log/messages', check_errors: false)
    node.run("tar cfvJP /tmp/#{node.full_hostname}-logs.tar.xz /var/log/ || [[ $? -eq 1 ]]")
    `mkdir logs` unless Dir.exist?('logs')
    code = file_extract(node, "/tmp/#{node.full_hostname}-logs.tar.xz", "logs/#{node.full_hostname}-logs.tar.xz")
    raise(ScriptError, 'Download log archive failed') unless code.zero?
  end

  # Generate a Token
  # Valid claims:
  #   - org
  #   - onlyChannels
  def token(secret, claims = {})
    payload = {}
    payload.merge!(claims)
    puts(secret)
    JWT.encode(payload, [secret].pack('H*').bytes.to_a.pack('c*'), 'HS256')
  end

  # Returns the server secret
  def server_secret
    rhnconf, _code = $server.run('cat /etc/rhn/rhn.conf', check_errors: false)
    data = /server.secret_key\s*=\s*(\h+)$/.match(rhnconf)
    data[1].strip
  end

  # Based on https://github.com/akarzim/capybara-bootstrap-datepicker
  # (MIT license)

  # Pick selected days
  def days_find(picker_days, day)
    day_xpath = <<-EOS
   //*[contains(concat(" ", normalize-space(@class), " "), " day ")
    and not (contains(concat(" ", normalize-space(@class), " "), " new "))
    and not(contains(concat(" ", normalize-space(@class), " "), " old "))
    and normalize-space(text())="#{day}"]
    EOS
    picker_days.find(:xpath, day_xpath).click
  end

  # Get a time X minutes in the future
  def get_future_time(minutes_to_add)
    now = Time.new
    future_time = now + 60 * minutes_to_add.to_i
    future_time.strftime('%l:%M %P').to_s.strip
  end

  # retrieve build host id, needed for scheduleImageBuild call
  def retrieve_build_host_id
    sysrpc = XMLRPCSystemTest.new(ENV['SERVER'])
    sysrpc.login('admin', 'admin')
    systems = sysrpc.list_systems
    refute_nil(systems)
    build_host_id = systems
                    .select { |s| s['name'] == $build_host.full_hostname }
                    .map { |s| s['id'] }
                    .first
    refute_nil(build_host_id, "Build host #{$build_host.full_hostname} is not yet registered?")
    build_host_id
  end

  # Retrieve pillar data from a minion
  def pillar_get(key, minion)
    system_name = get_system_name(minion)
    if minion == 'sle_minion'
      cmd = 'salt'
      extra_cmd = ''
    elsif %w[ssh_minion ceos_minion ubuntu_minion].include?(minion)
      cmd = 'salt-ssh'
      extra_cmd = '-i --roster-file=/tmp/roster_tests -w -W 2>/dev/null'
      $server.run("printf '#{system_name}:\n host: #{system_name}\n user: root\n  passwd: linux\n' > /tmp/roster_tests")
    else
      raise(ArgumentError, 'Invalid target')
    end
    $server.run("#{cmd} '#{system_name}' pillar.get '#{key}' #{extra_cmd}")
  end

  # have more infos about the errors
  def debug_server_on_realtime_failure
    puts('=> /var/log/rhn/rhn_web_ui.log')
    out, _code = $server.run(
      'tail -n20 /var/log/rhn/rhn_web_ui.log ' \
      "| awk -v limit=\"$(date --date='5 minutes ago' '+%Y-%m-%d %H:%M:%S')\" ' $0 > limit'"
    )
    out.each_line do |line|
      puts(line.to_s)
    end
    puts
    puts('=> /var/log/rhn/rhn_web_api.log')
    out, _code = $server.run(
      'tail -n20 /var/log/rhn/rhn_web_api.log' \
      "| awk -v limit=\"$(date --date='5 minutes ago' '+%Y-%m-%d %H:%M:%S')\" ' $0 > limit'"
    )
    out.each_line do |line|
      puts(line.to_s)
    end
    puts
  end
end
