# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

### Step definitions for Grafana formula setup and hub reporting dashboard verification (C-02..C-06).

require 'json'

GRAFANA_PG_TYPES = %w[grafana-postgresql-datasource postgres].freeze
HUB_DASHBOARD_KEYWORDS = ['Fleet Overview', 'Hub Overview', 'Reports'].freeze

# Returns parsed JSON from a Grafana API GET request executed via SSH on the given node.
def grafana_api_get(node, host, path)
  user, pass = Credentials.for(host)
  output, code = node.run(
    "curl -s -u '#{user}:#{pass}' http://localhost:3000#{path}",
    check_errors: false
  )
  raise StandardError, "Grafana API GET #{path} returned exit #{code}" unless code.zero?
  raise StandardError, "Grafana API GET #{path} returned empty body" if output.strip.empty?

  JSON.parse(output)
rescue JSON::ParserError => e
  raise StandardError, "Grafana API #{path} returned non-JSON: #{output.strip[0..200]}. Error: #{e.message}"
end

# Polls Grafana until at least one hub reporting dashboard appears; returns the full dashboard list.
def grafana_dashboard_search(node, host)
  result = nil
  repeat_until_timeout(timeout: 120, message: 'Hub reporting dashboards not yet provisioned in Grafana') do
    results = grafana_api_get(node, host, '/api/search?query=&type=dash-db')
    if results.any? { |d| d['folderTitle']&.include?('Reporting') || d['title']&.include?('SUSE Multi-Linux') }
      result = results
      break
    end
    sleep 10
  end
  result
end

# -- Grafana API verification steps --

Then(/^the Grafana API health endpoint should report database ok on "([^"]*)"$/) do |host|
  node = get_target(host)
  repeat_until_timeout(timeout: 120, message: "Grafana API health endpoint not ready on #{host}") do
    health = grafana_api_get(node, host, '/api/health')
    break if health['database'] == 'ok'

    sleep 10
  end
end

Then(/^the Grafana Report DB datasource should target the hub reportdb on "([^"]*)"$/) do |host|
  node = get_target(host)
  datasources = nil
  repeat_until_timeout(timeout: 120, message: "Report DB datasource not provisioned on #{host}") do
    datasources = grafana_api_get(node, host, '/api/datasources')
    break if datasources.any? { |ds| GRAFANA_PG_TYPES.include?(ds['type']) }

    sleep 10
  end
  pg_ds = datasources.select { |ds| GRAFANA_PG_TYPES.include?(ds['type']) }
  report_db_ds =
    pg_ds.find do |ds|
      (ds.dig('jsonData', 'database') || ds['database'] || '').include?('reportdb')
    end
  unless report_db_ds
    raise StandardError,
          "No Report DB datasource found; postgresql datasources: #{pg_ds.map { |d| d['name'] }.join(', ')}"
  end
  hub_fqdn = get_target('server').full_hostname
  raise StandardError, "Report DB datasource #{report_db_ds['name']} targets #{report_db_ds['url']}, not the hub #{hub_fqdn}" unless report_db_ds['url'].to_s.include?(hub_fqdn)

  add_context('grafana_reportdb_ds_uid', report_db_ds['uid'] || report_db_ds['id'].to_s)
  log "Report DB datasource confirmed: #{report_db_ds['name']}"
end

Then(/^there should be exactly one Grafana Report DB datasource on "([^"]*)"$/) do |host|
  node = get_target(host)
  datasources = grafana_api_get(node, host, '/api/datasources')
  pg_datasources =
    datasources.select do |ds|
      GRAFANA_PG_TYPES.include?(ds['type']) &&
        (ds.dig('jsonData', 'database') || ds['database'] || '').include?('reportdb')
    end
  count = pg_datasources.length
  raise StandardError, "Expected exactly 1 Report DB datasource, found #{count}" unless count == 1

  log 'Idempotency check passed: exactly 1 Report DB datasource provisioned'
end

# -- Dashboard provisioning verification --

Then(/^the Grafana Reporting folder should contain the "([^"]*)" dashboard on "([^"]*)"$/) do |title, host|
  dashboards = grafana_dashboard_search(get_target(host), host)
  found = dashboards.find { |d| d['title']&.include?(title) && d['folderTitle']&.include?('Reporting') }
  raise StandardError, "Dashboard '#{title}' not found in the Reporting folder; dashboards: #{dashboards.map { |d| "#{d['folderTitle']}/#{d['title']}" }.join(', ')}" unless found

  log "Dashboard found: #{found['title']} (uid=#{found['uid']})"
end

Then(/^each hub reporting dashboard should be provisioned on "([^"]*)"$/) do |host|
  node = get_target(host)
  dashboards = grafana_dashboard_search(node, host)
  hub_dashboards =
    dashboards.select do |d|
      HUB_DASHBOARD_KEYWORDS.any? { |kw| d['title']&.include?(kw) }
    end
  raise StandardError, 'No hub reporting dashboards found' if hub_dashboards.empty?

  hub_dashboards.each do |dashboard|
    detail = grafana_api_get(node, host, "/api/dashboards/uid/#{dashboard['uid']}")
    raise StandardError, "Dashboard #{dashboard['title']} has no JSON body" unless detail['dashboard']

    log "Dashboard #{dashboard['title']} is provisioned"
  end
end

Then(/^the hub overview dashboard should declare the Report DB datasource on "([^"]*)"$/) do |host|
  node = get_target(host)
  hub_dash = grafana_dashboard_search(node, host).find { |d| d['title']&.include?('Hub Overview') }
  raise StandardError, 'Hub Overview dashboard not found in Grafana' unless hub_dash

  detail = grafana_api_get(node, host, "/api/dashboards/uid/#{hub_dash['uid']}")
  dash_json = detail['dashboard'].to_s
  has_reportdb = dash_json.include?('reportdb') || dash_json.include?('Report DB') || dash_json.include?('PostgreSQL')
  raise StandardError, 'Hub Overview dashboard JSON does not reference Report DB datasource' unless has_reportdb

  log 'Hub Overview dashboard references Report DB datasource'
end

# -- Hub reportdb data checks --

Then(/^the hub reportdb system count should be positive$/) do
  count = reportdb_value('SELECT count(*) FROM system;')
  raise StandardError, "Could not read system count from hub reportdb (got: '#{count}')" unless count.to_i.positive?

  log "Hub reportdb system count = #{count}"
end

Then(/^the hub reportdb channel count should be positive$/) do
  count = reportdb_value('SELECT count(*) FROM channel;')
  raise StandardError, "Hub reportdb channel count is empty or zero (got: '#{count}')" unless count.to_i.positive?

  log "Hub reportdb channel count = #{count}"
end

Then(/^the hub reportdb should contain systems from at least one peripheral$/) do
  count = reportdb_value('SELECT count(DISTINCT mgm_id) FROM system WHERE mgm_id != 1;').to_i
  raise StandardError, "Expected at least 1 peripheral in reportdb, found #{count}" unless count >= 1

  log "#{count} peripheral(s) have distinct mgm_id entries in reportdb system table"
end

Then(/^the hub reportdb should contain systems managed by the hub and by peripherals$/) do
  hub_rows = reportdb_value('SELECT count(*) FROM system WHERE mgm_id = 1;').to_i
  peripheral_rows = reportdb_value('SELECT count(*) FROM system WHERE mgm_id != 1;').to_i
  raise StandardError, "Expected hub-managed entries (mgm_id=1) > 0, found #{hub_rows}" unless hub_rows >= 1
  raise StandardError, "Expected peripheral-managed entries (mgm_id!=1) > 0, found #{peripheral_rows}" unless peripheral_rows >= 1

  log "Hub-managed rows=#{hub_rows}, peripheral-managed rows=#{peripheral_rows}"
end

Then(/^the hub reportdb latest actions should include a recent action for "([^"]*)"$/) do |host|
  system_id = api_client_for('server').system.retrieve_server_id(get_system_name(host))
  count = reportdb_value("SELECT count(*) FROM systemaction WHERE system_id = #{system_id} AND completion_time > NOW() - INTERVAL '2 hours';").to_i
  raise StandardError, "No action completed in the last 2 hours for #{host} in the hub reportdb" if count.zero?

  log "#{count} recent action(s) for #{host} in the hub reportdb"
end

Then(/^the hub reportdb user accounts table should include the admin user$/) do
  user, = Credentials.for('server')
  count = reportdb_value("SELECT count(*) FROM account WHERE login = '#{user.gsub("'", "''")}';").to_i
  raise StandardError, "Admin user '#{user}' not found in reportdb account table" unless count >= 1

  log "Admin user '#{user}' confirmed in reportdb account table"
end
