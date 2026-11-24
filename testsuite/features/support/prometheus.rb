# Copyright (c) 2026 SUSE LLC
# SPDX-License-Identifier: MIT

### Helper functions for Prometheus REST API interactions (SUSE/spacewalk#16477)

require 'cgi'
require 'json'
require 'uri'

# Resolves the effective host running the Prometheus server.
# If a specific host is provided, it is used; otherwise it detects whether
# monitoring_server or sle_minion is available in the current environment.
def resolve_prometheus_host(host = nil)
  return host if host && !%w[prometheus_server active_prometheus_server].include?(host)

  if ENV['MONITORING_SERVER'] && !ENV['MONITORING_SERVER'].empty?
    'monitoring_server'
  else
    'sle_minion'
  end
end

# Executes a GET request against the Prometheus HTTP API on the given host.
# Supports optional basic auth (defaults to admin:admin, matching the formula configuration).
def prometheus_api_get(host, path, user: 'admin', pass: 'admin')
  effective_host = resolve_prometheus_host(host)
  node = get_target(effective_host)
  auth_param = user && pass ? "-u '#{user}:#{pass}'" : ''
  output, code = node.run(
    "curl -s -k #{auth_param} 'http://localhost:9090#{path}'",
    check_errors: false
  )

  # Fallback: if 401 Unauthorized or auth failed, try without auth
  if code != 0 || output.include?('Unauthorized')
    output, code = node.run(
      "curl -s -k 'http://localhost:9090#{path}'",
      check_errors: false
    )
  end

  raise StandardError, "Prometheus API GET #{path} returned exit code #{code}" unless code.zero?
  raise StandardError, "Prometheus API GET #{path} returned empty body" if output.strip.empty?

  JSON.parse(output)
rescue JSON::ParserError => e
  raise StandardError, "Prometheus API #{path} returned non-JSON: #{output.strip[0..200]}. Error: #{e.message}"
end

# Retrieves all active scrape targets from Prometheus.
def prometheus_targets(host)
  effective_host = resolve_prometheus_host(host)
  data = prometheus_api_get(effective_host, '/api/v1/targets')
  data.dig('data', 'activeTargets') || []
end

# Checks whether a Prometheus target matches the address or scrape URL of a given node.
def target_matches_host?(target, target_node)
  return true if target_node.nil?

  known_hosts = [target_node.full_hostname, target_node.public_ip, target_node.private_ip].compact.reject(&:empty?)
  host_regex = /\A(?:#{known_hosts.map { |h| Regexp.escape(h) }.join('|')})\z/

  instance = target.dig('labels', 'instance') || ''
  instance_host = instance.sub(/:\d+\z/, '')
  return true if instance_host.match?(host_regex)

  scrape_url = target['scrapeUrl'] || ''
  begin
    uri = URI.parse(scrape_url)
    if uri.query
      query_params = CGI.parse(uri.query)
      targets_in_query = query_params['target']
      if targets_in_query && !targets_in_query.empty?
        return targets_in_query.any? do |t_param|
          t_host = t_param.sub(/:\d+\z/, '')
          t_host.match?(host_regex)
        end
      end
    end

    return true if uri.host&.match?(host_regex)
  rescue URI::InvalidURIError
    # Fallback to instance match if scrapeUrl cannot be parsed
  end

  false
end

# Finds a specific target by job/scrapePool and target node name or address.
def prometheus_find_target(host, job_or_pool, target_host = nil)
  effective_host = resolve_prometheus_host(host)
  targets = prometheus_targets(effective_host)
  target_node = target_host ? get_target(target_host) : nil

  targets.find do |t|
    scrape_pool = t['scrapePool'] || ''
    job_label = t.dig('labels', 'job') || ''
    job_match = scrape_pool == job_or_pool ||
                job_label == job_or_pool ||
                scrape_pool.split('/').last == job_or_pool
    next false unless job_match

    target_matches_host?(t, target_node)
  end
end

# Executes an instant PromQL query on the given host.
# Returns the results array: data.result
def prometheus_query(host, query)
  effective_host = resolve_prometheus_host(host)
  encoded_query = CGI.escape(query)
  response = prometheus_api_get(effective_host, "/api/v1/query?query=#{encoded_query}")
  raise StandardError, "Prometheus query error: #{response['error']}" if response['status'] == 'error'

  response.dig('data', 'result') || []
end

# Executes a PromQL query expecting a scalar or vector with at least one value.
# Returns the first numeric value found, or nil if no result.
def prometheus_query_value(host, query)
  results = prometheus_query(host, query)
  return if results.empty?

  # PromQL scalar queries return data.result as [timestamp, "value"]
  return results[1].to_f if results.length == 2 && !results.first.is_a?(Hash)

  first_result = results.first
  return unless first_result.is_a?(Hash) && first_result['value'].is_a?(Array)

  first_result['value'][1].to_f
end
