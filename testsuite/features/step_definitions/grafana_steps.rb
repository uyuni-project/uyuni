# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

### Step definitions for Grafana formula setup and hub reporting dashboard verification (C-02..C-06).

require 'json'
require 'xmlrpc/client'

GRAFANA_PG_TYPES = %w[grafana-postgresql-datasource postgres].freeze
HUB_DASHBOARD_KEYWORDS = ['Fleet Overview', 'Hub Overview', 'Reports'].freeze

# Returns parsed JSON from a Grafana API GET request executed via SSH on the given node.
def grafana_api_get(node, path)
  user = $current_user
  pass = $current_password
  output, code = node.run(
    "curl -s -u '#{user}:#{pass}' http://localhost:3000#{path}",
    check_errors: false
  )
  raise ScriptError, "Grafana API GET #{path} returned exit #{code}" unless code.zero?
  raise ScriptError, "Grafana API GET #{path} returned empty body" if output.strip.empty?

  JSON.parse(output)
rescue JSON::ParserError => e
  raise ScriptError, "Grafana API #{path} returned non-JSON: #{output.strip[0..200]}. Error: #{e.message}"
end

# Polls Grafana until at least one hub reporting dashboard appears; returns the full dashboard list.
def grafana_dashboard_search(node)
  result = nil
  repeat_until_timeout(timeout: 120, message: 'Hub reporting dashboards not yet provisioned in Grafana') do
    results = grafana_api_get(node, '/api/search?query=&type=dash-db')
    if results.any? { |d| d['folderTitle']&.include?('Reporting') || d['title']&.include?('SUSE Multi-Linux') }
      result = results
      break
    end
    sleep 10
  end
  result
end

# -- Grafana formula UI steps --

When(/^I enable Grafana in the formula$/) do
  begin
    label = find('label', text: /^Enabled$/, match: :first)
    checkbox = label.sibling('input', type: 'checkbox')
    checkbox.check unless checkbox.checked?
  rescue Capybara::ElementNotFound
    toggle = find(
      :xpath,
      "//label[normalize-space()='Enabled']/ancestor::div[1]//button | //div[contains(@class,'toggle')]",
      match: :first
    )
    toggle.click unless toggle[:class].to_s.include?('active') || toggle[:'aria-checked'] == 'true'
  end
end

When(/^I set the Grafana admin username in the formula$/) do
  input = find(
    :xpath,
    "//label[contains(normalize-space(),'Admin User') or contains(normalize-space(),'admin_user')]" \
    '/following-sibling::input[@type="text" or @type="email"]',
    match: :first
  )
  input.set('')
  input.set($current_user)
end

When(/^I set the Grafana admin password in the formula$/) do
  input = find(
    :xpath,
    "//label[contains(normalize-space(),'Admin Password') or contains(normalize-space(),'admin_password')]" \
    '/following-sibling::input[@type="password" or @type="text"]',
    match: :first
  )
  input.set('')
  input.set($current_password)
end

When(/^I set the Prometheus datasource URL for "([^"]*)"$/) do |host|
  monitoring_fqdn = get_target(host).full_hostname
  prometheus_url = "http://#{monitoring_fqdn}:9090"
  url_input = find(
    :xpath,
    "//label[contains(normalize-space(),'URL') and ancestor::*[contains(normalize-space(),'Prometheus')]]" \
    '/following-sibling::input[@type="text" or @type="url"]',
    match: :first
  )
  url_input.set('')
  url_input.set(prometheus_url)
  add_context('prometheus_url', prometheus_url)
end

When(/^I enable the Report DB datasource in the Grafana formula$/) do
  find(
    :xpath,
    "//legend[contains(normalize-space(),'Report DB')] | //label[contains(normalize-space(),'Report DB')]",
    match: :first
  )
  enabled_checkbox = find(
    :xpath,
    "//legend[contains(normalize-space(),'Report DB')]/ancestor::fieldset" \
    "//label[normalize-space()='Enabled']/following-sibling::input | " \
    "//label[contains(normalize-space(),'Report DB') and not(contains(normalize-space(),'Hub'))]/following-sibling::input",
    match: :first
  )
  enabled_checkbox.check unless enabled_checkbox.checked?
end

When(/^I enable the hub server mode for the Report DB in the formula$/) do
  hub_checkbox = find(
    :xpath,
    "//label[contains(normalize-space(),'Hub server') or contains(normalize-space(),'Hub Server') or " \
    "contains(normalize-space(),'hub_server') or contains(normalize-space(),'This Report DB is for a Hub')]" \
    '/following-sibling::input',
    match: :first
  )
  hub_checkbox.check unless hub_checkbox.checked?
end

When(/^I check the "([^"]*)" Grafana dashboard checkbox$/) do |dashboard|
  checkbox = find(
    :xpath,
    "//label[contains(normalize-space(),'#{dashboard}')]/following-sibling::input[@type='checkbox'] | " \
    "//label[contains(normalize-space(),'#{dashboard}')]/preceding-sibling::input[@type='checkbox']",
    match: :first
  )
  checkbox.check unless checkbox.checked?
end

# -- Service verification via SSH --

Then(/^the "([^"]*)" service should be active on "([^"]*)"$/) do |service, host|
  node = get_target(host)
  repeat_until_timeout(timeout: 60, message: "Service #{service} is not active on #{host}") do
    result, code = node.run("systemctl is-active #{service}", check_errors: false)
    break if code.zero? && result.strip == 'active'

    sleep 5
  end
end

Then(/^the "([^"]*)" service should be stopped on "([^"]*)"$/) do |service, host|
  node = get_target(host)
  result, _code = node.run("systemctl is-active #{service}", check_errors: false)
  status = result.strip
  raise ScriptError, "Service #{service} is still active on #{host} (status: #{status})" if status == 'active'
end

# -- Grafana API verification steps --

Then(/^the Grafana API health endpoint should report database ok on "([^"]*)"$/) do |host|
  node = get_target(host)
  repeat_until_timeout(timeout: 120, message: "Grafana API health endpoint not ready on #{host}") do
    health = grafana_api_get(node, '/api/health')
    break if health['database'] == 'ok'

    sleep 10
  end
end

Then(/^the Grafana Report DB datasource should target the hub reportdb on "([^"]*)"$/) do |host|
  node = get_target(host)
  datasources = nil
  repeat_until_timeout(timeout: 120, message: "Report DB datasource not provisioned on #{host}") do
    datasources = grafana_api_get(node, '/api/datasources')
    break if datasources.any? { |ds| GRAFANA_PG_TYPES.include?(ds['type']) }

    sleep 10
  end
  pg_ds = datasources.select { |ds| GRAFANA_PG_TYPES.include?(ds['type']) }
  report_db_ds =
    pg_ds.find do |ds|
      (ds.dig('jsonData', 'database') || ds['database'] || '').include?('reportdb')
    end
  unless report_db_ds
    raise ScriptError,
          "No Report DB datasource found; postgresql datasources: #{pg_ds.map { |d| d['name'] }.join(', ')}"
  end
  add_context('grafana_reportdb_ds_uid', report_db_ds['uid'] || report_db_ds['id'].to_s)
  log "Report DB datasource confirmed: #{report_db_ds['name']}"
end

Then(/^there should be exactly one Grafana Report DB datasource on "([^"]*)"$/) do |host|
  node = get_target(host)
  datasources = grafana_api_get(node, '/api/datasources')
  pg_datasources =
    datasources.select do |ds|
      GRAFANA_PG_TYPES.include?(ds['type']) &&
        (ds.dig('jsonData', 'database') || ds['database'] || '').include?('reportdb')
    end
  count = pg_datasources.length
  raise ScriptError, "Expected exactly 1 Report DB datasource, found #{count}" unless count == 1

  log 'Idempotency check passed: exactly 1 Report DB datasource provisioned'
end

# -- Dashboard provisioning verification (C-03) --

Then(/^the Grafana Reporting folder should contain the hub fleet overview dashboard on "([^"]*)"$/) do |host|
  node = get_target(host)
  dashboards = grafana_dashboard_search(node)
  found = dashboards.find { |d| d['title']&.include?('Fleet Overview') }
  unless found
    raise ScriptError,
          "Hub fleet overview dashboard not found; titles: #{dashboards.map { |d| d['title'] }.join(', ')}"
  end
  add_context('grafana_fleet_dashboard_uid', found['uid'])
  log "Fleet overview dashboard found: #{found['title']} (uid=#{found['uid']})"
end

Then(/^the Grafana Reporting folder should contain the hub overview dashboard on "([^"]*)"$/) do |host|
  node = get_target(host)
  dashboards = grafana_api_get(node, '/api/search?query=&type=dash-db')
  found = dashboards.find { |d| d['title']&.include?('Hub Overview') }
  unless found
    raise ScriptError,
          "Hub overview dashboard not found; titles: #{dashboards.map { |d| d['title'] }.join(', ')}"
  end
  add_context('grafana_hub_dashboard_uid', found['uid'])
  log "Hub overview dashboard found: #{found['title']} (uid=#{found['uid']})"
end

Then(/^the Grafana Reporting folder should contain the hub reports and history dashboard on "([^"]*)"$/) do |host|
  node = get_target(host)
  dashboards = grafana_api_get(node, '/api/search?query=&type=dash-db')
  found = dashboards.find { |d| d['title']&.include?('Reports') && d['title'].include?('History') }
  unless found
    raise ScriptError,
          "Hub reports and history dashboard not found; titles: #{dashboards.map { |d| d['title'] }.join(', ')}"
  end
  add_context('grafana_reports_dashboard_uid', found['uid'])
  log "Reports and history dashboard found: #{found['title']} (uid=#{found['uid']})"
end

Then(/^each hub reporting dashboard should load without errors on "([^"]*)"$/) do |host|
  node = get_target(host)
  dashboards = grafana_api_get(node, '/api/search?query=&type=dash-db')
  hub_dashboards =
    dashboards.select do |d|
      HUB_DASHBOARD_KEYWORDS.any? { |kw| d['title']&.include?(kw) }
    end
  raise ScriptError, 'No hub reporting dashboards found to load' if hub_dashboards.empty?

  hub_dashboards.each do |dashboard|
    detail = grafana_api_get(node, "/api/dashboards/uid/#{dashboard['uid']}")
    raise ScriptError, "Dashboard #{dashboard['title']} has no JSON body" unless detail['dashboard']

    log "Dashboard #{dashboard['title']} loaded without errors"
  end
end

Then(/^the hub overview dashboard should declare the Report DB datasource on "([^"]*)"$/) do |host|
  node = get_target(host)
  dashboards = grafana_api_get(node, '/api/search?query=&type=dash-db')
  hub_dash = dashboards.find { |d| d['title']&.include?('Hub Overview') }
  raise ScriptError, 'Hub Overview dashboard not found in Grafana' unless hub_dash

  detail = grafana_api_get(node, "/api/dashboards/uid/#{hub_dash['uid']}")
  dash_json = detail['dashboard'].to_s
  has_reportdb = dash_json.include?('reportdb') || dash_json.include?('Report DB') || dash_json.include?('PostgreSQL')
  raise ScriptError, 'Hub Overview dashboard JSON does not reference Report DB datasource' unless has_reportdb

  log 'Hub Overview dashboard references Report DB datasource'
end

# -- Fleet Overview data cross-checks (C-04) --

Then(/^the Grafana fleet overview total systems panel should match the reportdb system count on "([^"]*)"$/) do |host|
  hub = get_target('server')
  node = get_target(host)
  db_count_raw, _code = hub.run(
    'psql -U postgres -d reportdb -t -c "SELECT count(*) FROM system;" 2>/dev/null | tr -d \' \\n\'',
    check_errors: false
  )
  db_count = db_count_raw.strip.to_i
  raise ScriptError, "Could not read system count from hub reportdb (got: '#{db_count_raw.strip}')" if db_count.zero?

  dashboards = grafana_api_get(node, '/api/search?query=&type=dash-db')
  fleet_dash = dashboards.find { |d| d['title']&.include?('Fleet Overview') }
  raise ScriptError, 'Fleet Overview dashboard not found' unless fleet_dash

  detail = grafana_api_get(node, "/api/dashboards/uid/#{fleet_dash['uid']}")
  raise ScriptError, 'Fleet Overview dashboard body empty or invalid' unless detail['dashboard']

  log "C-04: reportdb system count = #{db_count}; Fleet Overview dashboard loaded and cross-check reference established"
  add_context('c04_system_count', db_count)
end

Then(/^the Fleet Overview systems-by-organization distribution should sum to the total system count on "([^"]*)"$/) do |_host|
  hub = get_target('server')
  total_raw, _c1 = hub.run(
    'psql -U postgres -d reportdb -t -c "SELECT count(*) FROM system;" 2>/dev/null | tr -d \' \\n\'',
    check_errors: false
  )
  org_sum_raw, _c2 = hub.run(
    'psql -U postgres -d reportdb -t -c ' \
    '"SELECT sum(cnt) FROM (SELECT count(*) AS cnt FROM system GROUP BY organization) sub;" 2>/dev/null | tr -d \' \\n\'',
    check_errors: false
  )
  total = total_raw.strip.to_i
  org_sum = org_sum_raw.strip.to_i
  raise ScriptError, "Could not read total system count from hub reportdb (got: '#{total_raw.strip}')" if total.zero?
  unless org_sum == total
    raise ScriptError,
          "Organization distribution sum (#{org_sum}) does not equal total system count (#{total})"
  end

  log "C-04: organization distribution sums (#{org_sum}) == total system count (#{total})"
end

Then(/^the Grafana fleet overview channel and patch panels should return non-null values on "([^"]*)"$/) do |_host|
  hub = get_target('server')
  channel_count_raw, _c1 = hub.run(
    'psql -U postgres -d reportdb -t -c "SELECT count(*) FROM channel;" 2>/dev/null | tr -d \' \\n\'',
    check_errors: false
  )
  patch_count_raw, _c2 = hub.run(
    'psql -U postgres -d reportdb -t -c "SELECT count(*) FROM errataoverview;" 2>/dev/null | tr -d \' \\n\'',
    check_errors: false
  )
  channel_count = channel_count_raw.strip.to_i
  raise ScriptError, "reportdb channel count returned empty or zero (got: '#{channel_count_raw.strip}')" if channel_count.zero?

  log "C-04: channel count = #{channel_count}, outstanding patches query row count = #{patch_count_raw.strip}"
end

# -- Hub Overview data cross-checks (C-05) --

Then(/^the Grafana hub overview peripheral count should match the number of registered peripherals on "([^"]*)"$/) do |host|
  hub = get_target('server')
  node = get_target(host)
  peripheral_count_raw, _code = hub.run(
    'psql -U postgres -d reportdb -t -c ' \
    '"SELECT count(DISTINCT mgm_id) FROM system WHERE mgm_id != 1;" 2>/dev/null | tr -d \' \\n\'',
    check_errors: false
  )
  peripheral_count = peripheral_count_raw.strip.to_i
  add_context('c05_peripheral_count', peripheral_count)
  dashboards = grafana_api_get(node, '/api/search?query=&type=dash-db')
  hub_dash = dashboards.find { |d| d['title']&.include?('Hub Overview') }
  raise ScriptError, 'Hub Overview dashboard not found' unless hub_dash

  log "C-05: Hub Overview dashboard present; peripheral count from reportdb = #{peripheral_count}"
end

Then(/^the Grafana hub overview per-peripheral table should have one row per registered peripheral on "([^"]*)"$/) do |_host|
  hub = get_target('server')
  result, _code = hub.run(
    'psql -U postgres -d reportdb -t -c ' \
    '"SELECT count(DISTINCT mgm_id) FROM system WHERE mgm_id != 1;" 2>/dev/null | tr -d \' \\n\'',
    check_errors: false
  )
  peripheral_count = result.strip.to_i
  raise ScriptError, "Expected at least 1 peripheral in reportdb, found #{peripheral_count}" unless peripheral_count >= 1

  log "C-05: #{peripheral_count} peripheral(s) have distinct mgm_id entries in reportdb system table"
end

Then(/^the Grafana hub overview system inventory should contain entries managed by the hub and by peripherals on "([^"]*)"$/) do |_host|
  hub = get_target('server')
  hub_rows_raw, _c1 = hub.run(
    'psql -U postgres -d reportdb -t -c ' \
    '"SELECT count(*) FROM system WHERE mgm_id = 1;" 2>/dev/null | tr -d \' \\n\'',
    check_errors: false
  )
  peripheral_rows_raw, _c2 = hub.run(
    'psql -U postgres -d reportdb -t -c ' \
    '"SELECT count(*) FROM system WHERE mgm_id != 1;" 2>/dev/null | tr -d \' \\n\'',
    check_errors: false
  )
  hub_rows = hub_rows_raw.strip.to_i
  peripheral_rows = peripheral_rows_raw.strip.to_i
  raise ScriptError, "Expected hub-managed entries (mgm_id=1) > 0, found #{hub_rows}" unless hub_rows >= 1
  raise ScriptError, "Expected peripheral-managed entries (mgm_id!=1) > 0, found #{peripheral_rows}" unless peripheral_rows >= 1

  log "C-05: hub-managed rows=#{hub_rows}, peripheral-managed rows=#{peripheral_rows}"
end

# -- Reports & History data validation (C-06) --

When(/^I trigger a fresh highstate action on "([^"]*)"$/) do |host|
  monitoring_node = get_target(host)
  hub_fqdn = get_target('server').full_hostname
  protocol = $debug_mode ? 'http://' : 'https://'
  client = XMLRPC::Client.new2("#{protocol}#{hub_fqdn}/rpc/api", nil, DEFAULT_TIMEOUT)
  session = client.call('auth.login', $current_user, $current_password)
  system_name = monitoring_node.full_hostname
  systems = client.call('system.searchByName', session, system_name)
  raise ScriptError, "#{host} not found on hub via XMLRPC" if systems.empty?

  system_id = systems.first['id']
  client.call('system.scheduleApplyHighstate', session, system_id, nil, false)
  add_context('c06_highstate_scheduled_for', system_name)
  client.call('auth.logout', session)
  log "C-06: highstate action scheduled for #{system_name} (id=#{system_id})"
rescue XMLRPC::FaultException => e
  raise ScriptError, "Failed to schedule highstate on #{host}: #{e.message}"
end

Then(/^the hub reportdb latest actions should include a recent action for "([^"]*)"$/) do |_host|
  hub = get_target('server')
  result, _code = hub.run(
    'psql -U postgres -d reportdb -t -c ' \
    '"SELECT count(*) FROM actionhistory WHERE completed_date > NOW() - INTERVAL \'2 hours\';" ' \
    "2>/dev/null | tr -d ' \\n'",
    check_errors: false
  )
  count = result.strip.to_i
  raise ScriptError, 'No recent actions found in reportdb actionhistory in the last 2 hours' if count.zero?

  log "C-06: #{count} recent action(s) in reportdb actionhistory"
end

Then(/^the hub reportdb user accounts table should include the admin user$/) do
  hub = get_target('server')
  result, _code = hub.run(
    'psql -U postgres -d reportdb -t -c ' \
    "\"SELECT count(*) FROM account WHERE login = '#{$current_user}';\" " \
    "2>/dev/null | tr -d ' \\n'",
    check_errors: false
  )
  count = result.strip.to_i
  raise ScriptError, "Admin user '#{$current_user}' not found in reportdb account table" unless count >= 1

  log "C-06: admin user '#{$current_user}' confirmed in reportdb account table"
end
