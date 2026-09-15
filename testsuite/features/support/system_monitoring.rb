# Copyright (c) 2024 SUSE LLC.
# SPDX-License-Identifier: MIT

require 'time'

# This method should return the last bootstrap duration for the given host
# @param host [String] the hostname
# @return [Float] the duration in seconds
def last_bootstrap_duration(host)
  system_name = get_system_name(host)
  duration = nil
  lines, _code = get_target('server').run('tail -n100 /var/log/rhn/rhn_web_api.log')
  lines.each_line do |line|
    if line.include?(system_name) && line.include?('systems.bootstrap')
      match = line.match(/TIME: (\d+\.\d+) seconds/)
      duration = match[1].to_f if match
    end
  end
  raise ScriptError, "Boostrap duration not found for #{host}" if duration.nil?

  duration
end

# This method should return the last onboarding duration for the given host
# @param host [String] the hostname
# @return [Float] the duration in seconds
def last_onboarding_duration(host)
  begin
    node = get_target(host)
    system_id = get_system_id(node)
    events = $api_test.system.get_event_history(system_id, 0, 10)
    onboarding_events = events.select { |event| event['summary'].include? 'certs, channels, packages' }
    last_event_id = onboarding_events.last['id']
    event_details = $api_test.system.get_event_details(system_id, last_event_id)
    # Convert XMLRPC::DateTime to Ruby's Time if necessary
    completed_time = event_details['completed'].is_a?(XMLRPC::DateTime) ? event_details['completed'].to_time : Time.parse(event_details['completed'])
    picked_up_time = event_details['picked_up'].is_a?(XMLRPC::DateTime) ? event_details['picked_up'].to_time : Time.parse(event_details['picked_up'])
    completed_time - picked_up_time
  rescue StandardError => e
    raise ScriptError, "Error extracting onboarding duration for #{host}.\n #{e.full_message}"
  end
end

# This method should return the synchronization duration for the given product
# @param os_product_version [String] the product name
# @return [Integer] the duration in seconds
def product_synchronization_duration(os_product_version)
  channels_to_evaluate = CHANNEL_TO_SYNC_BY_OS_PRODUCT_VERSION.dig(product, os_product_version).clone
  $stdout.puts("Product: #{product}\n#{CHANNEL_TO_SYNC_BY_OS_PRODUCT_VERSION}\n#{CHANNEL_TO_SYNC_BY_OS_PRODUCT_VERSION.dig(product, os_product_version)}") if channels_to_evaluate.empty?
  channels_to_evaluate = filter_channels(channels_to_evaluate, ['beta']) unless $beta_enabled
  $stdout.puts("Channels to evaluate:\n#{channels_to_evaluate}")
  raise ScriptError, "Synchronization error, channels for #{os_product_version} in #{product} not found" if channels_to_evaluate.nil?

  get_target('server').extract('/var/log/rhn/reposync.log', '/tmp/reposync.log')
  raise ScriptError, 'The file with repository synchronization logs doesn\'t exist or is empty' if !File.exist?('/tmp/reposync.log') || File.empty?('/tmp/reposync.log')

  duration = 0
  channel_to_evaluate = false
  matches = 0
  channel_name = ''
  log_content = File.readlines('/tmp/reposync.log')
  log_content.each do |line|
    if line.include?('Channel: ')
      channel_name = line.split('Channel: ')[1].strip
      channel_to_evaluate = channels_to_evaluate.include?(channel_name)
    end
    next unless line.include?('Total time: ') && channel_to_evaluate

    match = line.match(/Total time: (\d+):(\d+):(\d+)/)
    hours, minutes, seconds = match.captures.map(&:to_i)
    total_seconds = (hours * 3600) + (minutes * 60) + seconds
    $stdout.puts("Channel #{channel_name} synchronization duration: #{total_seconds} seconds")
    duration += total_seconds
    matches += 1
    channel_to_evaluate = false
  end
  if matches < channels_to_evaluate.size
    $stdout.puts("Error extracting the synchronization duration of #{os_product_version}")
    $stdout.puts("Content of reposync.log:\n#{log_content.join}")
  end
  duration
end

# This method should return the synchronization duration for the given channel
# @param channel [String] the channel name
# @return [Integer] the duration in seconds
def channel_synchronization_duration(channel)
  channel_found = false
  get_target('server').extract('/var/log/rhn/reposync.log', '/tmp/reposync.log')
  raise ScriptError, 'The file with repository synchronization logs doesn\'t exist or is empty' if !File.exist?('/tmp/reposync.log') || File.empty?('/tmp/reposync.log')

  duration = 0
  matches = 0
  File.foreach('/tmp/reposync.log') do |line|
    if line.include?('Channel: ')
      channel_name = line.split('Channel: ')[1].strip
      if channel_name == channel
        channel_found = true
        duration = 0
        matches += 1
      end
    end
    if line.include?('Total time: ') && channel_found
      match = line.match(/Total time: (\d+):(\d+):(\d+)/)
      hours, minutes, seconds = match.captures.map(&:to_i)
      total_seconds = (hours * 3600) + (minutes * 60) + seconds
      duration = total_seconds
      channel_found = false
    end
  end
  $stdout.puts "Channel #{channel} was found #{matches} times in the logs, we return the last synchronization time." if matches > 1
  raise ScriptError, "Error extracting the synchronization duration of #{channel}" if matches.zero?

  duration
end
