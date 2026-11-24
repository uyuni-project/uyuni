# Copyright (c) 2026 SUSE LLC
# SPDX-License-Identifier: MIT

### Step definitions for Prometheus server and exporter integration testing (SUSE/spacewalk#16477)

# Serializes a list of host strings into a properly escaped PromQL regex string literal matching the complete host/address and optional port suffix.
def promql_host_regex_literal(host_patterns)
  escaped = host_patterns.map { |h| Regexp.escape(h) }.join('|')
  regex = "^(?:#{escaped})(?::\\d+)?$"
  regex.to_json
end

# Verifies that a specific target for a host is discovered and reported as 'up' by Prometheus.
Then(/^the Prometheus target "([^"]*)" on "([^"]*)" should be up on "([^"]*)"$/) do |job, target_host, prom_host|
  effective_prom = resolve_prometheus_host(prom_host)
  repeat_until_timeout(timeout: 120, message: "Prometheus target '#{job}' on #{target_host} did not become 'up' on #{effective_prom}") do
    target = prometheus_find_target(effective_prom, job, target_host)
    if target && target['health'] == 'up'
      log "Prometheus target '#{job}' for #{target_host} is UP (#{target['scrapeUrl']})"
      break true
    end
    sleep 5
  end
end

# Verifies that a specific target for a host is reported as 'down' or unreachable by Prometheus.
Then(/^the Prometheus target "([^"]*)" on "([^"]*)" should be down on "([^"]*)"$/) do |job, target_host, prom_host|
  effective_prom = resolve_prometheus_host(prom_host)
  repeat_until_timeout(timeout: 120, message: "Prometheus target '#{job}' on #{target_host} is not reported as 'down' on #{effective_prom}") do
    target = prometheus_find_target(effective_prom, job, target_host)
    if target && target['health'] == 'down'
      log "Prometheus target '#{job}' for #{target_host} is DOWN"
      break true
    end
    sleep 5
  end
end

# Verifies that a target is reported as 'up' by Prometheus without specifying a client host.
Then(/^the Prometheus target "([^"]*)" should be up on "([^"]*)"$/) do |job, prom_host|
  effective_prom = resolve_prometheus_host(prom_host)
  repeat_until_timeout(timeout: 120, message: "Prometheus target '#{job}' did not become 'up' on #{effective_prom}") do
    target = prometheus_find_target(effective_prom, job)
    if target && target['health'] == 'up'
      log "Prometheus target '#{job}' is UP (#{target['scrapeUrl']})"
      break true
    end
    sleep 5
  end
end

# Verifies that a metric query for a specific host returns a positive numeric value.
Then(/^the Prometheus metric "([^"]*)" for "([^"]*)" should have a positive value on "([^"]*)"$/) do |metric, target_host, prom_host|
  effective_prom = resolve_prometheus_host(prom_host)
  target_node = get_target(target_host)
  host_patterns = [target_node.full_hostname, target_node.public_ip, target_node.private_ip].compact.reject(&:empty?)
  selector_literal = promql_host_regex_literal(host_patterns)

  repeat_until_timeout(timeout: 120, message: "Metric '#{metric}' for #{target_host} has no positive value on #{effective_prom}") do
    query = "#{metric}{instance=~#{selector_literal}}"
    value = prometheus_query_value(effective_prom, query)

    if value&.positive?
      log "Prometheus metric '#{metric}' for #{target_host} = #{value}"
      break true
    end
    sleep 5
  end
end

# Verifies that a metric query for a specific host equals an expected numeric value.
Then(/^the Prometheus metric "([^"]*)" for "([^"]*)" should equal "([^"]*)" on "([^"]*)"$/) do |metric, target_host, expected_val, prom_host|
  effective_prom = resolve_prometheus_host(prom_host)
  target_node = get_target(target_host)
  host_patterns = [target_node.full_hostname, target_node.public_ip, target_node.private_ip].compact.reject(&:empty?)
  selector_literal = promql_host_regex_literal(host_patterns)
  expected = expected_val.to_f

  repeat_until_timeout(timeout: 120, message: "Metric '#{metric}' for #{target_host} did not equal #{expected_val} on #{effective_prom}") do
    query = "#{metric}{instance=~#{selector_literal}}"
    value = prometheus_query_value(effective_prom, query)

    if value && (value - expected).abs < 0.001
      log "Prometheus metric '#{metric}' for #{target_host} = #{value} (expected #{expected})"
      break true
    end
    sleep 5
  end
end

# Verifies that a general PromQL query returns non-empty results on Prometheus.
Then(/^the Prometheus query "([^"]*)" should return results on "([^"]*)"$/) do |query, prom_host|
  effective_prom = resolve_prometheus_host(prom_host)
  repeat_until_timeout(timeout: 120, message: "Prometheus query '#{query}' returned no results on #{effective_prom}") do
    results = prometheus_query(effective_prom, query)
    if results && !results.empty?
      log "Prometheus query '#{query}' returned #{results.length} result(s)"
      break true
    end
    sleep 5
  end
end

# Verifies that all expected server self-monitoring targets are UP in Prometheus.
Then(/^the server self-monitoring targets should be up on "([^"]*)"$/) do |prom_host|
  effective_prom = resolve_prometheus_host(prom_host)
  server_fqdn = get_target('server').full_hostname
  server_targets = %w[mgr-server postgres jmx-tomcat jmx-taskomatic]

  repeat_until_timeout(timeout: 150, message: "Server self-monitoring targets not all UP on #{effective_prom}") do
    all_up =
      server_targets.all? do |job|
        target = prometheus_find_target(effective_prom, job, 'server')
        is_up = target && target['health'] == 'up'
        log "Target '#{job}' for server #{server_fqdn}: #{target ? target['health'] : 'not found'}" unless is_up
        is_up
      end
    break true if all_up

    sleep 5
  end
  log "All server self-monitoring targets are UP on #{effective_prom}"
end

# Verifies that server self-monitoring targets are reported DOWN or absent in Prometheus.
Then(/^the server self-monitoring targets should be down on "([^"]*)"$/) do |prom_host|
  effective_prom = resolve_prometheus_host(prom_host)
  server_targets = %w[postgres jmx-tomcat jmx-taskomatic]

  repeat_until_timeout(timeout: 120, message: "Server self-monitoring targets not all reported DOWN on #{effective_prom}") do
    all_down =
      server_targets.all? do |job|
        target = prometheus_find_target(effective_prom, job, 'server')
        target && target['health'] == 'down'
      end
    break true if all_down

    sleep 5
  end
  log "Server self-monitoring targets are DOWN on #{effective_prom}"
end

# Verifies key server self-monitoring metrics in Prometheus.
Then(/^the server self-monitoring metrics should be available on "([^"]*)"$/) do |prom_host|
  effective_prom = resolve_prometheus_host(prom_host)
  server_node = get_target('server')
  server_patterns = [server_node.full_hostname, server_node.public_ip, server_node.private_ip].compact.reject(&:empty?)
  server_selector = promql_host_regex_literal(server_patterns)

  queries_to_check = [
    'uyuni_all_systems',
    "pg_stat_database_tup_fetched{instance=~#{server_selector}}",
    "java_lang_Memory_HeapMemoryUsage_used{instance=~#{server_selector}}"
  ]

  queries_to_check.each do |query|
    repeat_until_timeout(timeout: 90, message: "Server metric query '#{query}' not available on #{effective_prom}") do
      value = prometheus_query_value(effective_prom, query)
      if value && value >= 0
        log "Server metric '#{query}' = #{value} on #{effective_prom}"
        break true
      end
      sleep 5
    end
  end
end

# Verifies that an installed package originates from a Client Tools channel or repository,
# and not from a standard base distribution repository.
Then(/^the "([^"]*)" package should be installed from the client tools channel on "([^"]*)"$/) do |package, host|
  node = get_target(host)
  client_tools_pattern = /client[_-]?tools|manager[_-]?tools|tools[_-]?pool|tools[_-]?update|uyuni[_-]?client/i

  if deb_host?(host)
    output, code = node.run("apt-cache policy #{package}", check_errors: false)
    raise ScriptError, "apt-cache policy failed for #{package} on #{host}" unless code.zero?

    installed_version = output[/Installed:\s*(\S+)/, 1]
    raise ScriptError, "Package #{package} is not installed on #{host} (Installed: #{installed_version || 'unknown'})" if installed_version.nil? || installed_version == '(none)'

    version_table = output.split('Version table:').last || ''
    installed_section = version_table[/(\*\*\*\s+#{Regexp.escape(installed_version)}.*?)(?=\n\s{1,6}\d|\z)/m, 1] || ''
    is_client_tools = installed_section.match?(client_tools_pattern)
    log "Package #{package} installed version #{installed_version} policy section on #{host}:\n#{installed_section}"
    raise ScriptError, "Package #{package} on #{host} was not installed from client tools:\n#{installed_section}" unless is_client_tools
  elsif suse_host?(host)
    output, code = node.run("zypper --no-refresh search -i -s --match-exact #{package}", check_errors: false)
    raise ScriptError, "Package #{package} is not installed on #{host}" unless code.zero?

    is_client_tools = output.match?(client_tools_pattern)
    log "Package #{package} zypper search on #{host}:\n#{output}"
    raise ScriptError, "Package #{package} on #{host} was not installed from client tools:\n#{output}" unless is_client_tools
  else
    cmd = "if which dnf >/dev/null 2>&1; then dnf list installed #{package}; else yum list installed #{package}; fi"
    output, code = node.run(cmd, check_errors: false)
    raise ScriptError, "Package #{package} is not installed on #{host}" unless code.zero?

    is_client_tools = output.match?(/@.*#{client_tools_pattern}/i)
    log "Package #{package} package list on #{host}:\n#{output}"
    raise ScriptError, "Package #{package} on #{host} was not installed from client tools:\n#{output}" unless is_client_tools
  end
end
