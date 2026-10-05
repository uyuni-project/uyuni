# Copyright (c) 2026 SUSE LLC
# Licensed under the terms of the MIT license.

### This file contains the definitions for all steps concerning Hub operations.

require 'tempfile'

# Clicks the "Apply Changes" button on the Peripheral Sync Channels page, confirms the
# resulting "Confirm Channel Synchronization Changes" modal, and waits for the success toast.
# The button starts disabled until a checkbox change is registered by the React state.
def click_apply_channels_button
  find_button('Apply Changes', disabled: false, wait: DEFAULT_TIMEOUT).click
  step %(I click on "Confirm" in "Confirm Channel Synchronization Changes" modal)
  step %(I wait until I see "Channels synced correctly to peripheral!" text)
end

# Sets the checkbox at the given xpath to the expected checked state, retrying on "not
# attached to the DOM" errors caused by the peripheral channel table re-rendering on
# expand/filter. Compares against the checkbox's own current state first: if it already
# matches, does nothing. Otherwise clicks it and re-reads it to confirm it actually landed
# on the expected state -- the table's re-render sometimes shifts rows under a stale xpath,
# so a click can silently toggle a different channel's checkbox instead of this one, which a
# blind ".check" would never catch. Returns whether a change was made.
def set_channel_checkbox_state(xpath, expected_checked)
  attempts = 0
  begin
    checkbox = find(:xpath, xpath)
    return false if checkbox.checked? == expected_checked

    checkbox.click
    actual_checked = find(:xpath, xpath).checked?
    unless actual_checked == expected_checked
      raise "Checkbox at #{xpath} is #{actual_checked ? 'checked' : 'unchecked'} after " \
            "clicking, expected #{expected_checked ? 'checked' : 'unchecked'}"
    end

    true
  rescue Playwright::Error => e
    raise unless e.message.include?('not attached to the DOM')

    attempts += 1
    retry if attempts < 3
    raise
  end
end

# Fetches the PEM-encoded root CA certificate that actually signed the
# certificate a peripheral node is currently serving on port 443.
#
# This used to just try known cert file paths on disk (mgradm's default
# self-signed CA location, the container-internal uyuni CA) and return
# whichever was found first. That breaks for a hub-signed peripheral: mgradm
# always writes its own "LOCAL-RHN-ORG-TRUSTED-SSL-CERT" CA to
# /etc/pki/trust/anchors/#{fqdn}.crt on install regardless of any custom
# --ssl-server-cert/--ssl-ca-root flags, so the old first fallback matched and
# returned that CA -- not the hub's CA that actually signed the served
# certificate, causing the hub to trust the wrong CA at registration and later
# fail outbound XMLRPC calls with "x509: certificate signed by unknown
# authority". Instead, read the issuer hash off the certificate actually being
# served and return whichever candidate CA file matches it.
def peripheral_root_ca(node)
  fqdn = node.full_hostname
  issuer_hash, code = node.run(
    "echo | openssl s_client -connect localhost:443 -servername #{fqdn} 2>/dev/null | openssl x509 -noout -issuer_hash",
    check_errors: false,
    runs_in_container: false
  )
  return '' unless code.zero?

  issuer_hash = issuer_hash.strip
  candidate_cas = [
    "#{HUB_SSL_BUILD_DIR}/RHN-ORG-TRUSTED-SSL-CERT",
    "/etc/pki/trust/anchors/#{fqdn}.crt",
    '/etc/uyuni/ca.crt'
  ]
  candidate_cas.each do |ca_path|
    subject_hash, ca_code = node.run("openssl x509 -in #{ca_path} -noout -subject_hash 2>/dev/null",
                                     check_errors: false,
runs_in_container: false)
    next unless ca_code.zero? && subject_hash.strip == issuer_hash

    ca_content, = node.run("openssl x509 -in #{ca_path} -outform PEM", check_errors: false, runs_in_container: false)
    return ca_content.strip
  end
  ''
end

# Secondary server web UI authentication

Given(/^I am authorized for the "Admin" section on "([^"]*)"$/) do |host|
  peripheral_fqdn = get_target(host).full_hostname
  switch_to_server(host)
  user, password = Credentials.for(host)
  Credentials.login_as(user, password)
  url_base = "https://#{peripheral_fqdn}"
  visit("#{url_base}/rhn/YourRhn.do")
  next if has_xpath?('//a[@href=\'/rhn/Logout.do\']', wait: 0)

  raise StandardError, "Login page for #{host} is not correctly loaded (url: #{page.current_url})" unless has_field?('username')

  fill_in('username', with: user)
  fill_in('password', with: password)
  click_button_and_wait('Sign In', match: :first)
  raise StandardError, "Login on #{host} failed (url: #{page.current_url})" unless
    has_xpath?('//a[@href=\'/rhn/Logout.do\']', wait: Capybara.default_max_wait_time * 3)
end

# Hub XMLRPC API authentication and operations

# Returns the hub XMLRPC API connection opened by "I am connected to the hub XMLRPC API".
def hub_api
  get_context('hub_api') || raise(StandardError, 'Not connected; run "I am connected to the hub XMLRPC API" first')
end

# Returns the ID of a registered peripheral, as listed by hub.listServerIds.
def hub_server_id_of(host)
  api_client_for('server').system.retrieve_server_id(get_system_name(host))
end

Given(/^I am connected to the hub XMLRPC API$/) do
  api = NamespaceHub.new(get_target('server').full_hostname)
  response = api.login_with_autoconnect(*Credentials.for('server'))
  raise StandardError, 'Hub login failed' if response['SessionKey'].nil?

  add_context('hub_api', api)
end

When(/^I call hub\.listServerIds via XMLRPC$/) do
  hub_api.list_server_ids
end

Then(/^the hub server IDs list should not be empty$/) do
  raise StandardError, 'Hub server IDs list is empty' if hub_api.server_ids.empty?

  log "Hub server IDs: #{hub_api.server_ids}"
end

When(/^I call multicast\.system\.list_systems via XMLRPC$/) do
  add_context('hub_multicast_response', hub_api.multicast_system_list)
end

Then(/^multicast response should have successful responses$/) do
  response = get_context('hub_multicast_response')
  raise StandardError, 'No multicast response; run "I call multicast.system.list_systems via XMLRPC" first' if response.nil?
  raise StandardError, "No successful multicast responses: #{response}" if response.dig('Successful', 'Responses').to_a.empty?

  log "Multicast successful responses: #{response['Successful']['Responses'].length}"
end

Then(/^multicast response should contain systems from "([^"]*)"$/) do |host|
  successful = get_context('hub_multicast_response')&.fetch('Successful', nil) || {}
  server_id = hub_server_id_of(host)
  index = successful.fetch('ServerIds', []).index(server_id)
  raise StandardError, "#{host} (server ID #{server_id}) has no successful multicast response: #{successful}" if index.nil?
  raise StandardError, "Multicast response for #{host} contains no systems" if successful['Responses'][index].to_a.empty?
end

When(/^I logout from hub XMLRPC API$/) do
  hub_api.logout
end

When(/^I login to hub XMLRPC API with (standard|auth relay) mode$/) do |mode|
  method = mode == 'standard' ? 'hub.login' : 'hub.loginWithAuthRelayMode'
  session_key = NamespaceHub.new(get_target('server').full_hostname).login(method, *Credentials.for('server'))
  raise StandardError, "#{method} returned no session key" if session_key.to_s.empty?

  add_context("hub_#{mode}_session", session_key)
end

Then(/^the hub (standard|auth relay) session key should be non-empty$/) do |mode|
  session_key = get_context("hub_#{mode}_session")
  raise StandardError, "No #{mode} session key stored" if session_key.to_s.empty?

  log "Hub #{mode} session key: #{session_key[0..8]}..."
end

When(/^I call unicast\.system\.list_systems for "([^"]*)" via XMLRPC$/) do |host|
  server_id = hub_server_id_of(host)
  raise StandardError, "#{host} (server ID #{server_id}) is not in hub.listServerIds #{hub_api.server_ids}" unless hub_api.server_ids.include?(server_id)

  add_context('hub_unicast_response', hub_api.unicast_system_list(server_id))
end

Then(/^unicast response should contain systems$/) do
  response = get_context('hub_unicast_response')
  raise StandardError, "Unicast response contains no systems: #{response.inspect}" unless response.is_a?(Array) && !response.empty?

  log "Unicast returned #{response.length} system(s)"
end

When(/^I call system\.list_systems on hub's own XMLRPC endpoint$/) do
  add_context('hub_direct_systems', api_client_for('server').system.list_systems)
end

Then(/^hub's own system list should not be empty$/) do
  systems = get_context('hub_direct_systems')
  raise StandardError, 'Hub /rpc/api returned no system' if systems.to_a.empty?

  log "Hub /rpc/api returned #{systems.length} system(s)"
end

# Hub peripheral registration

# Opens a hub configuration page on the hub itself. Scenarios of the same feature share the browser
# session, so an earlier peripheral login would otherwise make this act on the peripheral.
def follow_hub_menu(entry)
  switch_to_server('server')
  step %(I follow the left menu "Admin > Hub Configuration > #{entry}")
end

# Returns a value stored with add_context, raising when it is missing or empty.
def stored_context!(key)
  value = get_context(key)
  raise StandardError, "Nothing stored for #{key}" if value.to_s.empty?

  value
end

# Selects the root CA mode of the "Add peripheral" form and fills in the certificate.
#
# @param mode [Symbol] :none, :paste or :upload
# @param ca_content [String, nil] The PEM certificate, for :paste and :upload
def fill_peripheral_root_ca(mode, ca_content)
  case mode
  when :none
    step %(I check radio button "Not needed")
  when :paste
    step %(I check radio button "Paste the data")
    find("textarea[name='rootCA_pastedData']").set(ca_content)
  when :upload
    step %(I check radio button "Upload a file")
    Tempfile.create(%w[hub_root_ca .pem]) do |file|
      file.write(ca_content)
      file.flush
      attach_file('rootCA_uploadedFile', file.path)
    end
  end
end

# Fills and submits the hub "Add peripheral" form.
#
# @param host [String] The peripheral host.
# @param credentials [Array<String>, nil] Administrator user and password; nil to register with a token.
# @param token [String, nil] The access token, when registering with a token.
# @param root_ca [Symbol] :auto (the CA the peripheral serves, if found), :none, :paste or :upload.
# @param ca_content [String, nil] The certificate for :paste and :upload.
def register_peripheral(host, credentials: nil, token: nil, root_ca: :auto, ca_content: nil)
  node = get_target(host)
  if root_ca == :auto
    ca_content = peripheral_root_ca(node)
    root_ca = ca_content.empty? ? :none : :paste
  end
  follow_hub_menu('Peripherals Configuration')
  step %(I click on "addPeripheral")
  step %(I enter "#{node.full_hostname}" as "serverFqdn")
  if token
    step %(I check radio button "Existing token")
    step %(I enter "#{token}" as "token")
  else
    step %(I check radio button "Administrator User/Password")
    step %(I enter "#{credentials.first}" as "username")
    step %(I enter "#{credentials.last}" as "password")
  end
  fill_peripheral_root_ca(root_ca, ca_content)
  step %(I click on "Register")
end

When(/^I add "([^"]*)" as peripheral using administrator credentials$/) do |host|
  register_peripheral(host, credentials: Credentials.for(host))
end

When(/^I attempt to register "([^"]*)" as peripheral with wrong password$/) do |host|
  register_peripheral(host, credentials: %w[root wrong_password_xyz_invalid])
end

When(/^I attempt to register "([^"]*)" as peripheral with username "([^"]*)" and password "([^"]*)"$/) do |host, username, password|
  register_peripheral(host, credentials: [username, password])
end

When(/^I add "([^"]*)" as peripheral using its access token$/) do |host|
  register_peripheral(host, token: stored_context!("#{host}_access_token"))
end

When(/^I add "([^"]*)" as peripheral using its (?:access|invalidated) token without root CA$/) do |host|
  register_peripheral(host, token: stored_context!("#{host}_access_token"), root_ca: :none)
end

When(/^I add "([^"]*)" as peripheral using its wrong-FQDN token$/) do |host|
  register_peripheral(host, token: stored_context!("#{host}_wrong_fqdn_token"), root_ca: :none)
end

When(/^I add "([^"]*)" as peripheral using its access token and (pasted root CA|uploaded CA file)$/) do |host, ca_source|
  register_peripheral(host,
                      token: stored_context!("#{host}_access_token"),
                      root_ca: ca_source == 'pasted root CA' ? :paste : :upload,
                      ca_content: stored_context!("#{host}_root_ca"))
end

Then(/^I should see "([^"]*)" in peripherals list$/) do |host|
  fqdn = get_target(host).full_hostname
  follow_hub_menu('Peripherals Configuration')
  raise StandardError, "#{fqdn} not found in peripherals list" unless page.has_content?(fqdn, wait: DEFAULT_TIMEOUT)
end

Then(/^I should see "([^"]*)" in the system list with "([^"]*)" system type$/) do |host, expected_type|
  fqdn = get_target(host).full_hostname
  xpath = "//tr[.//a[contains(., '#{fqdn}')]]"
  row = nil
  repeat_until_timeout(message: "#{fqdn} did not appear in the system list") do
    row = find(:xpath, xpath, wait: 2) if has_xpath?(xpath, wait: 2)
    break if row

    refresh_page
    sleep 5
  end
  system_type = row.find(:xpath, 'td[last()]').text.strip
  raise StandardError, "Expected system type '#{expected_type}' for #{fqdn}, got '#{system_type}'" unless system_type == expected_type
end

Then(/^I should see a registration failure error$/) do
  error_shown = page.has_selector?('.alert-danger, .notification-error', wait: DEFAULT_TIMEOUT) ||
                page.has_content?(/failed|error|unable/i, wait: 5)
  raise StandardError, 'No error message shown after failed registration attempt' unless error_shown
end

Then(/^I should not see "([^"]*)" in peripherals list$/) do |host|
  fqdn = get_target(host).full_hostname
  follow_hub_menu('Peripherals Configuration')
  raise StandardError, "#{fqdn} found in peripherals list after expected failed registration" if page.has_content?(fqdn, wait: 5)
end

When(/^I create a non-admin user "([^"]*)" with password "([^"]*)" on "([^"]*)"$/) do |username, password, host|
  api_client_for(host).user.create(username, password, username, username, 'testuser@example.com')
end

When(/^I delete non-admin user "([^"]*)" from "([^"]*)"$/) do |username, host|
  api_client_for(host).user.delete(username)
rescue StandardError => e
  log "Warning: could not delete user #{username} from #{host}: #{e.message}"
end

Then(/^I should see a duplicate peripheral registration error$/) do
  error_shown = page.has_selector?('.alert-danger, .notification-error', wait: DEFAULT_TIMEOUT) ||
                page.has_content?(/already registered|already exists|only have one hub/i, wait: 5)
  raise StandardError, 'No duplicate-registration error shown when re-registering already-registered peripheral' unless error_shown
end

Then(/^the Hub Details page on "([^"]*)" should show the hub FQDN$/) do |_host|
  hub_fqdn = get_target('server').full_hostname
  raise StandardError, "Hub FQDN #{hub_fqdn} not found on peripheral Hub Details page" unless page.has_content?(hub_fqdn, wait: DEFAULT_TIMEOUT)
end

# Issues an access token on a server for the given FQDN and returns it.
def issue_access_token(host, fqdn)
  using_server(host) do
    visit('/rhn/manager/admin/hub/access-tokens')
    step %(I click on "Add token")
    step %(I click on "Issue a new token")
    step %(I enter "#{fqdn}" as "fqdn")
    step %(I click on "Issue")
    token = find('#generated-token', wait: DEFAULT_TIMEOUT).value.strip
    raise StandardError, "Empty token returned from #{host}" if token.empty?

    token
  end
end

When(/^I issue a new access token for hub on "([^"]*)"$/) do |host|
  add_context("#{host}_access_token", issue_access_token(host, get_target('server').full_hostname))
end

When(/^I issue a new access token for wrong FQDN on "([^"]*)"$/) do |host|
  add_context("#{host}_wrong_fqdn_token", issue_access_token(host, 'wrong.fqdn.example.com'))
end

When(/^I invalidate the token I just issued on "([^"]*)"$/) do |host|
  hub_fqdn = get_target('server').full_hostname
  using_server(host) do
    visit('/rhn/manager/admin/hub/access-tokens')
    row_xpath = "//tr[contains(., '#{hub_fqdn}') and contains(., 'Issued')]"
    find(:xpath, "#{row_xpath}//button[@aria-label='Invalidate']", wait: DEFAULT_TIMEOUT).click
    # Unlike the hub's own Access Tokens page, this peripheral-side page applies the action
    # immediately, with no "Confirm access token modification" dialog.
    raise StandardError, "Token for #{hub_fqdn} on #{host} did not become invalid" unless
      has_xpath?("#{row_xpath}//button[@aria-label='Validate']", wait: DEFAULT_TIMEOUT)
  end
end

Then(/^I should see a token rejection error$/) do
  error_shown = page.has_selector?('.alert-danger, .notification-error', wait: DEFAULT_TIMEOUT) ||
                page.has_content?(/invalid|rejected|failed/i, wait: 5)
  raise StandardError, 'No token rejection error shown' unless error_shown
end

When(/^I fetch root CA certificate from "([^"]*)"$/) do |host|
  ca_content = peripheral_root_ca(get_target(host))
  raise StandardError, "Could not read root CA certificate from #{host}" if ca_content.empty?

  add_context("#{host}_root_ca", ca_content)
end

# Access token lifecycle (A-05)

When(/^I invalidate the access token for "([^"]*)" on hub$/) do |host|
  fqdn = get_target(host).full_hostname
  follow_hub_menu('Access Tokens')
  row_xpath = "//tr[contains(., '#{fqdn}') and contains(., 'Consumed')]"
  find(:xpath, "#{row_xpath}//button[@aria-label='Invalidate']", wait: DEFAULT_TIMEOUT).click
  step %(I click on "Invalidate" in "Confirm access token modification" modal)
end

Then(/^the access token for "([^"]*)" should be listed as "([^"]*)"$/) do |host, state|
  fqdn = get_target(host).full_hostname
  row_xpath = "//tr[contains(., '#{fqdn}') and contains(., 'Consumed')]"
  case state
  when 'Invalid'
    raise StandardError, "Token for #{host} is not showing as invalid" unless
      page.has_xpath?("#{row_xpath}//button[@aria-label='Validate']", wait: DEFAULT_TIMEOUT)
  when 'Valid'
    raise StandardError, "Token for #{host} is not showing as valid" unless
      page.has_xpath?("#{row_xpath}//button[@aria-label='Invalidate']", wait: DEFAULT_TIMEOUT)
  else
    xpath = "#{row_xpath}//td[contains(., '#{state}')]"
    raise StandardError, "Token state '#{state}' not found for #{host}" unless page.has_xpath?(xpath, wait: DEFAULT_TIMEOUT)
  end
end

# Hub channel synchronization (A-06)

# A-06 mirror credential regeneration

When(/^I regenerate mirror credentials for peripheral "([^"]*)"$/) do |host|
  fqdn = get_target(host).full_hostname
  follow_hub_menu('Peripherals Configuration')
  step %(I follow "#{fqdn}")
  step %(I click on "Regenerate Credentials")
  step %(I click on "Confirm")
  raise StandardError, 'Credentials regeneration did not confirm' unless
    page.has_content?(/regenerated/i, wait: DEFAULT_TIMEOUT)
end

When(/^I configure hub to sync channel "([^"]*)" to "([^"]*)"$/) do |channel, host|
  fqdn = get_target(host).full_hostname
  follow_hub_menu('Peripherals Configuration')
  step %(I follow "#{fqdn}")
  step %(I follow "Edit channels")
  find('input.table-input-search').set(channel)
  # Channels with no architecture-specific children (e.g. a freshly created single-arch
  # custom channel) render the expand icon but keep it invisible -- nothing to expand.
  expand_icon_xpath = '//tr[contains(@class, "parent-row")]//i[contains(@class, "expand-icon")]'
  find(:xpath, expand_icon_xpath).click if has_xpath?(expand_icon_xpath, wait: 1)
  changed = set_channel_checkbox_state("//tr[contains(., '#{channel}')]//input[@type='checkbox']", true)
  click_apply_channels_button if changed
end

When(/^I configure hub to sync all "([^"]*)" channels to "([^"]*)"$/) do |search_term, host|
  fqdn = get_target(host).full_hostname
  follow_hub_menu('Peripherals Configuration')
  step %(I follow "#{fqdn}")
  step %(I follow "Edit channels")
  find('input.table-input-search').set(search_term)
  find(:xpath, '//tr[contains(@class, "parent-row")]//i[contains(@class, "expand-icon")]', wait: DEFAULT_TIMEOUT).click
  channel_names = all(:xpath, '//tbody/tr', wait: DEFAULT_TIMEOUT, minimum: 2).map { |row| row.find(:xpath, 'td[3]').text }
  changed =
    channel_names.reduce(false) do |any_changed, channel_name|
      checkbox_xpath = "//tbody//tr[td[3][normalize-space(.)='#{channel_name}']]//input[@type='checkbox']"
      set_channel_checkbox_state(checkbox_xpath, true) || any_changed
    end
  click_apply_channels_button if changed
end

When(/^I select target organization "([^"]*)" for channel "([^"]*)" on "([^"]*)"$/) do |org, channel, _host|
  # The org select's data-testid is keyed by the channel's internal numeric ID
  # (e.g. "org-select-125"), which isn't known ahead of time -- scope the lookup
  # to the table row matching the channel name instead.
  row_xpath = "//tr[contains(., '#{channel}')]"
  find(:xpath, "#{row_xpath}//div[contains(@class, 'org-select') and contains(@class, '__control')]").click
  find(:xpath, "//div[contains(@class, 'org-select') and contains(@class, '__option') and contains(., '#{org}')]", match: :first).click
end

Then(/^channel sync from peripheral "([^"]*)" should fail with a repository access error$/) do |host|
  node = get_target(host)
  start_time = get_context("#{host}_taskomatic_check_start_time") || ''
  repeat_until_timeout(message: "RepoMDError not found in taskomatic log on #{host} since #{start_time}") do
    break if recent_taskomatic_repomd_error?(node, start_time)

    sleep 5
  end
end

Then(/^channel sync from peripheral "([^"]*)" should succeed$/) do |host|
  node = get_target(host)
  start_time = get_context("#{host}_taskomatic_check_start_time") || ''
  sleep 15
  raise StandardError, "RepoMDError present in taskomatic log on #{host} since #{start_time} after token reactivation" if
    recent_taskomatic_repomd_error?(node, start_time)
end

When(/^I initiate channel sync from peripheral "([^"]*)"$/) do |host|
  node = get_target(host)
  start_time, _code = node.run("date '+%F %T'", check_errors: false, exec_option: '--')
  add_context("#{host}_taskomatic_check_start_time", start_time.strip)
  using_server(host) do
    visit('/rhn/manager/admin/hub/hub-details')
    step %(I should see a "Hub Details" text)
    step %(I should see "server" hostname)
    step %(I click on "Sync Channels")
    step %(I click on "Schedule" in "Confirm channels synchronization" modal)
  end
end

Then(/^channel "([^"]*)" should exist on "([^"]*)"$/) do |channel, host|
  raise StandardError, "Channel #{channel} not found on #{host}" unless api_client_for(host).channel.channel_verified?(channel)
end

Then(/^channel "([^"]*)" on "([^"]*)" should have "([^"]*)" packages?$/) do |channel, host, pkg_count|
  node = get_target(host)
  user, password = Credentials.for(host)
  output, _code = node.run("spacecmd -u #{user} -p #{password} -- softwarechannel_listallpackages #{channel} | wc -l")
  actual_count = output.strip.to_i
  expected_count = pkg_count.to_i
  raise StandardError, "Expected #{expected_count} packages, found #{actual_count}" unless actual_count >= expected_count
end

When(/^I remove synced channels from "([^"]*)"$/) do |host|
  fqdn = get_target(host).full_hostname
  follow_hub_menu('Peripherals Configuration')
  step %(I follow "#{fqdn}")
  step %(I follow "Edit channels")
  checked_boxes = all('input[type="checkbox"]:checked')
  checked_boxes.each(&:uncheck)
  click_apply_channels_button unless checked_boxes.empty?
end

# Confirms hub-side peripheral deregistration, tolerating the "Error deregistering server"
# cleanup-timeout modal (BUG-039): the hub's cleanup call to the peripheral can time out, in
# which case the UI offers a "Deregister without cleanup" fallback that removes the
# registration anyway. Without this, the step would sit waiting the full DEFAULT_TIMEOUT for
# a success text that never appears once the timeout modal has taken over.
def confirm_hub_deregistration(fqdn)
  step %(I click on "Deregister" in "Confirm deregistration" modal)
  success_text = "#{fqdn} has been successfully deregistered"
  cleanup_timeout_text = 'Cleanup timed out. Please check if the machine is reachable.'
  raise StandardError, "Neither deregistration success nor the cleanup-timeout modal appeared for #{fqdn}" unless
    check_text?(success_text, text2: cleanup_timeout_text, timeout: DEFAULT_TIMEOUT)

  return unless has_content?(cleanup_timeout_text, wait: 0)

  log "WARN: deregistering #{fqdn} hit the cleanup-timeout modal (BUG-039) - falling back to 'Deregister without cleanup'"
  step %(I click on "Deregister without cleanup" in "Error deregistering server" modal)
  step %(I wait until I see "#{success_text}" text)
end

When(/^I unregister "([^"]*)" from hub$/) do |host|
  fqdn = get_target(host).full_hostname
  follow_hub_menu('Peripherals Configuration')
  row_xpath = "//tr[.//a[contains(., '#{fqdn}')]]"
  find(:xpath, "#{row_xpath}//button[contains(., 'Deregister')]", wait: DEFAULT_TIMEOUT).click
  confirm_hub_deregistration(fqdn)
  refresh_page
end

When(/^I unregister "([^"]*)" from hub if registered$/) do |host|
  fqdn = get_target(host).full_hostname
  follow_hub_menu('Peripherals Configuration')
  row_xpath = "//tr[.//a[contains(., '#{fqdn}')]]"
  next unless page.has_xpath?("#{row_xpath}//button[contains(., 'Deregister')]", wait: 5)

  find(:xpath, "#{row_xpath}//button[contains(., 'Deregister')]").click
  confirm_hub_deregistration(fqdn)
  refresh_page
end

# Hub deployment depth checks (A-01)

Then(/^the hub\.conf on "([^"]*)" should contain the required configuration keys$/) do |host|
  node = get_target(host)
  conf, _code = node.run('podman exec uyuni-hub-xmlrpc-0 cat /etc/hub/hub.conf',
                         check_errors: false,
                         runs_in_container: false)
  %w[HUB_API_URL HUB_CONNECT_TIMEOUT HUB_REQUEST_TIMEOUT HUB_CONNECT_USING_SSL].each do |key|
    raise StandardError, "hub.conf missing key: #{key}" unless conf.include?(key)
  end
  log 'hub.conf contains all required keys'
end

# Hub service verification (A-01)

When(/^I wait until hub\.conf exists in the hub xmlrpc container on "([^"]*)"$/) do |host|
  node = get_target(host)
  repeat_until_timeout(message: 'hub.conf not found inside uyuni-hub-xmlrpc-0 container') do
    _out, code = node.run('podman exec uyuni-hub-xmlrpc-0 test -f /etc/hub/hub.conf',
                          check_errors: false,
                          runs_in_container: false)
    break if code.zero?

    sleep 5
  end
end

Then(/^the Hub XMLRPC API should be running on "([^"]*)"$/) do |host|
  node = get_target(host)
  result, _code = node.run('curl -k -s -o /dev/null -w "%{http_code}" https://localhost/hub/rpc/api', check_errors: false)
  raise StandardError, "Hub XMLRPC API returned unexpected status '#{result.strip}', expected 405" unless result.strip == '405'
end

# Channel sync waiting

When(/^I wait at most (\d+) seconds until channel "([^"]*)" has been synced on "([^"]*)"$/) do |timeout, channel, host|
  repeat_until_timeout(timeout: timeout.to_i, message: "Channel #{channel} not synced on #{host}") do
    break if api_client_for(host).channel.channel_verified?(channel)

    sleep 10
  end
end

# ISSv2 prerequisite checks (A-09)

Given(/^"inter-server-sync" is installed on both hub and "([^"]*)"$/) do |host|
  peripheral_node = get_target(host)
  _out, hub_code = get_target('server').run('rpm -q inter-server-sync', check_errors: false)
  _out2, prh_code = peripheral_node.run('rpm -q inter-server-sync', check_errors: false)
  skip_this_scenario if hub_code.nonzero? || prh_code.nonzero?
end

Given(/^hub and "([^"]*)" have the same MLM version$/) do |host|
  peripheral_node = get_target(host)
  hub_ver, _c1 = get_target('server').run("rpm -q --qf '%{VERSION}' spacewalk-base-minimal 2>/dev/null || echo unknown")
  prh_ver, _c2 = peripheral_node.run("rpm -q --qf '%{VERSION}' spacewalk-base-minimal 2>/dev/null || echo unknown")
  skip_this_scenario if hub_ver.strip != prh_ver.strip
end

# A-09 org parity check

Given(/^the default organization name on hub and "([^"]*)" match$/) do |host|
  peripheral_node = get_target(host)
  hub_user, hub_password = Credentials.for('server')
  hub_org, hub_code = get_target('server').run(
    "spacecmd -u #{hub_user} -p #{hub_password} org_list 2>/dev/null | head -1",
    check_errors: false
  )
  prh_user, prh_password = Credentials.for(host)
  prh_org, prh_code = peripheral_node.run(
    "spacecmd -u #{prh_user} -p #{prh_password} org_list 2>/dev/null | head -1",
    check_errors: false
  )
  if hub_code.nonzero? || prh_code.nonzero? || hub_org.strip != prh_org.strip
    log "Org name mismatch or spacecmd error — hub='#{hub_org.strip}' prh='#{prh_org.strip}'; skipping ISS v2 import"
    skip_this_scenario
  end
end

# ISSv2 export/transfer/import

When(/^I export channel "([^"]*)" with ISS v2 to "([^"]*)" on hub$/) do |channel, path|
  hub_node = get_target('server')
  # inter-server-sync refuses to export into a non-empty directory, so clear out any
  # leftovers from a previous run of this scenario before exporting.
  hub_node.run("rm -rf #{path}", verbose: true)
  hub_node.run("inter-server-sync export --channels=#{channel} --outputDir=#{path}", verbose: true)
  add_context('iss_export_path', path)
  add_context('iss_export_channel', channel)
end

When(/^I transfer ISS v2 export from hub to "([^"]*)"$/) do |host|
  peripheral_node = get_target(host)
  export_path = get_context('iss_export_path')
  raise StandardError, 'No ISS export path stored' if export_path.nil?

  hub_node = get_target('server')
  archive_path = "/tmp/iss-export-#{host}.tar.gz"

  # Hub and peripheral servers have no passwordless SSH between each other, so route
  # the export directory through the controller instead of rsync-ing node to node.
  hub_node.run("tar czf #{archive_path} -C #{export_path} .", verbose: true)

  success = file_extract(hub_node, archive_path, archive_path)
  raise StandardError, 'Failed to extract ISS v2 export archive from hub' unless success

  success = file_inject(peripheral_node, archive_path, archive_path)
  raise StandardError, 'Failed to inject ISS v2 export archive into peripheral' unless success

  peripheral_node.run("mkdir -p #{export_path}", verbose: true)
  peripheral_node.run("tar xzf #{archive_path} -C #{export_path}", verbose: true)
end

When(/^I import ISS v2 data from "([^"]*)" on "([^"]*)"$/) do |path, host|
  get_target(host).run("echo admin | inter-server-sync import --importDir=#{path}", verbose: true)
end

# Hub reporting (C-01)

# Peripheral admin sessions default to 25 items per page (the "core" phase only raises this
# to 100 for the primary server's admin/testing users), so a schedule/task link further down
# an alphabetical list can land on a page Capybara never sees. Reuse on any host/scenario that
# hits a paginated admin list on a peripheral server.
When(/^I set the admin page size to "([^"]*)" on "([^"]*)"$/) do |size, host|
  using_server(host) do
    step %(I follow the left menu "Home > My Preferences")
    step %(I select "#{size}" from "pagesize")
    step %(I click on "Save Preferences")
    raise StandardError, "Failed to save page size preference on #{host}" unless page.has_content?('Preferences modified', wait: DEFAULT_TIMEOUT)
  end
end

When(/^I schedule the reporting update task on "([^"]*)"$/) do |host|
  # "hub" is an alias of "server": both run the hub aggregation task in the current session.
  if ENV_VAR_BY_HOST[host] == 'SERVER'
    switch_to_server('server')
    steps %(
      When I follow the left menu "Admin > Task Schedules"
      And I follow "update-reporting-hub-default"
      And I follow "mgr-update-reporting-hub-bunch"
      And I click on "Single Run Schedule"
      Then I should see a "bunch was scheduled" text
      And I wait until the table contains "FINISHED" or "SKIPPED" followed by "FINISHED" in its first rows
    )
  else
    using_server(host) { step %(I schedule a task to update ReportDB) }
  end
end

Then(/^the hub reportdb should contain one row per peripheral$/) do
  count = reportdb_value('SELECT COUNT(DISTINCT mgm_id) FROM system;').to_i
  raise StandardError, "Expected at least 2 distinct mgm_id rows in hub reportdb, got #{count}" unless count >= 2
end

Then(/^the hub reportdb "([^"]*)" table should have a recent synced_date$/) do |table|
  recent = reportdb_value("SELECT MAX(synced_date) > NOW() - INTERVAL '1 hour' FROM #{table};")
  raise StandardError, "synced_date in #{table} is not recent" unless recent == 't'
end

When(/^I create organization "([^"]*)" on "([^"]*)"$/) do |org_name, host|
  api = api_client_for(host)
  org_admin = { adminLogin: 'test_org_admin', adminPassword: 'TestPass123!', prefix: 'Mr.', firstName: 'Test', lastName: 'Admin', email: 'test_org_admin@example.com' }
  api.call('org.create', sessionKey: api.token, orgName: org_name, **org_admin, usePamAuth: false)
end

# Peripheral-side activation key and minion bootstrap

When(/^I create an activation key "([^"]*)" on "([^"]*)" with channel "([^"]*)"$/) do |key_label, host, channel|
  api_client_for(host).activationkey.create(key_label, key_label, channel, 100)
  add_context("#{host}_activation_key", key_label)
end

# Fills and submits the bootstrapping form of the current server for a host.
def bootstrap_from_ui(host, activation_key = nil)
  visit('/rhn/systems/bootstrapping')
  step %(I enter the hostname of "#{host}" as "hostname")
  step %(I enter "22" as "port")
  step %(I enter "root" as "user")
  step %(I enter "linux" as "password")
  step %(I select "#{activation_key}" from "activationKeys") if activation_key
  step %(I click on "Bootstrap")
  raise StandardError, "Bootstrap of #{host} did not initiate" unless page.has_content?('Bootstrap process initiated.', wait: DEFAULT_TIMEOUT)
end

When(/^I bootstrap "([^"]*)" to peripheral "([^"]*)" using activation key "([^"]*)"$/) do |minion_host, peripheral_host, key_label|
  using_server(peripheral_host) { bootstrap_from_ui(minion_host, key_label) }
end

When(/^I bootstrap "([^"]*)" as a Salt minion of hub$/) do |host|
  using_server('server') { bootstrap_from_ui(host) }
end

# Returns the systems registered on a server whose name is the system name of a host.
def systems_registered_as(host, on:)
  system_name = get_system_name(host)
  api_client_for(on).system.search_by_name(Regexp.escape(system_name)).select { |system| system['name'] == system_name }
end

Then(/^I should see "([^"]*)" registered on "([^"]*)"$/) do |minion_host, server_host|
  raise StandardError, "#{minion_host} not found on #{server_host}" if systems_registered_as(minion_host, on: server_host).empty?
end

Then(/^I should not see "([^"]*)" registered on hub$/) do |minion_host|
  raise StandardError, "#{minion_host} unexpectedly found on hub" unless systems_registered_as(minion_host, on: 'server').empty?
end

Then(/^I should see "([^"]*)" in hub system list as "(Salt Minion|Foreign)" type$/) do |host, expected_type|
  entitlement = { 'Salt Minion' => 'salt_entitled', 'Foreign' => 'foreign_entitled' }[expected_type]
  api = api_client_for('server')
  system = nil
  repeat_until_timeout(message: "#{host} not in hub system list") do
    system = systems_registered_as(host, on: 'server').first
    break if system

    sleep 10
  end
  details = api.call('system.getDetails', sessionKey: api.token, sid: system['id'])
  raise StandardError, "#{host} is #{details['base_entitlement']}, expected #{entitlement}" unless details['base_entitlement'] == entitlement
end

Then(/^there should be exactly one entry for "([^"]*)" in hub system list$/) do |host|
  count = systems_registered_as(host, on: 'server').length
  raise StandardError, "Expected 1 entry for #{host} in hub system list, found #{count}" unless count == 1
end

# Peripheral-side deregistration

When(/^I deregister from hub on "([^"]*)"$/) do |host|
  using_server(host) do
    visit('/rhn/manager/admin/hub/hub-details')
    step %(I click on "Deregister")
    step %(I click on "Confirm")
  end
end

Then(/^the Hub Details page on "([^"]*)" should be empty$/) do |host|
  using_server(host) do
    visit('/rhn/manager/admin/hub/hub-details')
    raise StandardError, 'Hub Details page still shows hub FQDN after deregistration' if
      page.has_content?(get_target('server').full_hostname, wait: 5)
  end
end

Then(/^I should not see "([^"]*)" in peripherals list on hub$/) do |host|
  fqdn = get_target(host).full_hostname
  using_server('server') do
    visit('/rhn/manager/admin/hub/peripherals')
    raise StandardError, "#{fqdn} still appears in hub peripherals list after peripheral-side deregistration" if
      page.has_content?(fqdn, wait: 10)
  end
end

# Client operations through a peripheral

When(/^I apply erratum "([^"]*)" on "([^"]*)" via "([^"]*)" peripheral API$/) do |errata_name, minion_host, peripheral_host|
  api = api_client_for(peripheral_host)
  system_id = api.system.retrieve_server_id(get_system_name(minion_host))
  errata_ids = api.system.get_system_errata(system_id).select { |e| e['advisory_name'] == errata_name }.map { |e| e['id'] }
  raise StandardError, "Errata #{errata_name} not relevant for #{minion_host} on #{peripheral_host}" if errata_ids.empty?

  action_ids = api.call('system.scheduleApplyErrata', sessionKey: api.token, sid: system_id, errataIds: errata_ids)
  action_ids.each { |action_id| wait_action_complete(action_id, mgr_server: peripheral_host) }
end

When(/^I run a remote command "([^"]*)" on "([^"]*)" via "([^"]*)"$/) do |cmd, minion_host, peripheral_host|
  api = api_client_for(peripheral_host)
  system_id = api.system.retrieve_server_id(get_system_name(minion_host))
  action_id = api.system.schedule_script_run(system_id, 'root', 'root', 30, "#!/bin/sh\n#{cmd}\n", api.date_now)
  add_context('remote_command_action', [action_id, peripheral_host])
end

Then(/^the remote command should complete$/) do
  action_id, peripheral_host = get_context('remote_command_action')
  raise StandardError, 'No remote command scheduled; run "I run a remote command ..." first' if action_id.nil?

  wait_action_complete(action_id, mgr_server: peripheral_host)
end

Then(/^the package "([^"]*)" installed on "([^"]*)" should have the same header digest as on hub$/) do |package, minion_host|
  query = "rpm -q --queryformat '%{NAME}-%{VERSION}-%{RELEASE}.%{ARCH} %{SHA256HEADER}' #{package}"
  minion_out, = get_target(minion_host).run(query)
  nevra, minion_digest = minion_out.split
  hub_out, = get_target('server').run("rpm -qp --queryformat '%{SHA256HEADER}' $(find /var/spacewalk/packages -name '#{nevra}.rpm' | head -1)")
  raise StandardError, "#{nevra} header digest on #{minion_host} (#{minion_digest}) differs from hub (#{hub_out.strip})" unless hub_out.strip == minion_digest
end

# Hub outage resilience

Then(/^I should see a channel sync failure error on "([^"]*)"$/) do |host|
  using_server(host) do
    visit('/rhn/manager/admin/hub/hub-details')
    step %(I click on "Sync Channels")
    step %(I click on "Confirm")
    error_shown = page.has_selector?('.alert-danger, .notification-error', wait: 120) ||
                  page.has_content?(/failed|error|unreachable/i, wait: 5)
    raise StandardError, 'Expected sync failure error not shown when hub is down' unless error_shown
  end
end

# Never leave the hub stopped when an outage scenario fails: the restart scenario would not run.
After('@hub_outage') do |scenario|
  next unless scenario.failed?

  hub_fqdn = get_target('server').full_hostname
  _out, _err, code = ssh_command('systemctl is-active --quiet uyuni-server', hub_fqdn)
  unless code.zero?
    log 'After hook: hub services were left stopped, restarting them'
    ssh_command('mgradm start', hub_fqdn)
  end
end

### Hub-signed SSL certificate provisioning and peripheral installation.
###
### Replaces the sumaform hub_peripheral_certs.sls / hub_peripheral.sls mechanism:
### the hub generates the peripheral's server certificate with rhn-ssl-tool, the
### testsuite transfers CA + cert + key over SSH (the private key never touches
### the hub's public /pub directory), and the peripheral server is then installed
### with mgradm using the hub-signed certificates.

# Directory used on both the hub (inside the container) and the peripheral (host)
# to store the SSL build artifacts, mirroring the rhn-ssl-tool default.
HUB_SSL_BUILD_DIR = '/root/ssl-build'.freeze

# Returns the paths of the hub-signed CA, server certificate and server key
# as laid out on the peripheral host for the given FQDN.
def hub_signed_cert_paths(fqdn)
  {
    ca: "#{HUB_SSL_BUILD_DIR}/RHN-ORG-TRUSTED-SSL-CERT",
    cert: "#{HUB_SSL_BUILD_DIR}/peripheral-#{fqdn}-server.crt",
    key: "#{HUB_SSL_BUILD_DIR}/peripheral-#{fqdn}-server.key"
  }
end

# A "podman ps" hit only proves *a* server is running on the peripheral, not that
# it is serving the hub-signed certificate this scenario expects. A server
# installed before this scenario runs (e.g. sumaform not honoring
# skip_server_install) used to skip installation here silently, leaving a
# self-signed peripheral cert in place - the mismatch only surfaced much later,
# and confusingly, as an "x509: certificate signed by unknown authority" error on
# an unrelated hub XMLRPC unicast/multicast call. Fail fast here instead.
def verify_hub_signed_cert_is_served!(peripheral, node, fqdn, paths)
  hub_ca_subject, = node.run("openssl x509 -in #{paths[:ca]} -noout -subject", runs_in_container: false, check_errors: false)
  served_issuer, = node.run(
    "echo | openssl s_client -connect localhost:443 -servername #{fqdn} 2>/dev/null | openssl x509 -noout -issuer",
    runs_in_container: false,
    check_errors: false
  )
  hub_ca_dn = hub_ca_subject.strip.sub(/^subject=/, '')
  served_dn = served_issuer.strip.sub(/^issuer=/, '')
  unless hub_ca_dn == served_dn
    raise StandardError,
          "Server container already running on #{peripheral} but serving a certificate issued by " \
          "'#{served_dn}', not the hub-signed CA ('#{hub_ca_dn}'). The peripheral must not have a server " \
          'pre-installed before this scenario runs - uninstall the existing server and rerun.'
  end

  log "Server container already running on #{peripheral} and already serving the hub-signed certificate, skipping installation"
end

When(/^I generate hub-signed SSL certificates for "([^"]*)" on "([^"]*)"$/) do |peripheral, hub|
  hub_node = get_target(hub)
  fqdn = get_target(peripheral).full_hostname
  machine_name = fqdn.split('.').first
  _out, code = hub_node.run(
    "find #{HUB_SSL_BUILD_DIR} -mindepth 2 -maxdepth 2 -name server.crt -path '*/#{machine_name}/server.crt' | grep -q .",
    check_errors: false
  )
  if code.zero?
    log "Hub-signed certificate for #{fqdn} already present on #{hub}, skipping generation"
  else
    hub_node.run(
      "rhn-ssl-tool --gen-server --dir=#{HUB_SSL_BUILD_DIR} --set-hostname=#{fqdn} " \
      "--set-cname=#{fqdn} --set-cname=db --set-cname=reportdb --password=spacewalk"
    )
  end
end

When(/^I copy the hub-signed SSL certificates for "([^"]*)" from "([^"]*)"$/) do |peripheral, hub|
  hub_node = get_target(hub)
  peripheral_node = get_target(peripheral)
  fqdn = peripheral_node.full_hostname
  machine_name = fqdn.split('.').first
  cert_dir, = hub_node.run(
    "find #{HUB_SSL_BUILD_DIR} -mindepth 2 -maxdepth 2 -name server.crt -path '*/#{machine_name}/server.crt' -exec dirname {} \\; | head -1"
  )
  cert_dir = cert_dir.strip
  raise StandardError, "No certificates for #{fqdn} found on #{hub} - generate them first" if cert_dir.empty?

  paths = hub_signed_cert_paths(fqdn)
  transfers = {
    "#{HUB_SSL_BUILD_DIR}/RHN-ORG-TRUSTED-SSL-CERT" => paths[:ca],
    "#{cert_dir}/server.crt" => paths[:cert],
    "#{cert_dir}/server.key" => paths[:key]
  }

  # The peripheral server is not installed yet, so everything must land on the
  # host (runs_in_container: false, plain scp instead of node.inject which would
  # attempt an mgrctl cp into a non-existent container).
  peripheral_node.run("mkdir -p #{HUB_SSL_BUILD_DIR}", runs_in_container: false)
  transfers.each do |hub_path, peripheral_path|
    # Prefix with machine_name to avoid /tmp collisions when prh1 and prh2 run in parallel.
    controller_tmp = "/tmp/#{machine_name}-#{File.basename(peripheral_path)}"
    raise StandardError, "Failed to extract #{hub_path} from #{hub}" unless file_extract(hub_node, hub_path, controller_tmp)

    success = get_target('localhost').scp_upload(controller_tmp, peripheral_path, host: peripheral_node.full_hostname)
    FileUtils.rm_f(controller_tmp)
    raise StandardError, "Failed to copy #{peripheral_path} to #{peripheral}" unless success
  end
  peripheral_node.run("chmod 600 #{paths[:key]}", runs_in_container: false)
end

When(/^I trust the hub CA certificate on "([^"]*)"$/) do |peripheral|
  node = get_target(peripheral)
  node.run(
    "cp #{HUB_SSL_BUILD_DIR}/RHN-ORG-TRUSTED-SSL-CERT /etc/pki/trust/anchors/RHN-ORG-TRUSTED-SSL-CERT.pem && " \
    'update-ca-certificates',
    runs_in_container: false
  )
end

When(/^I install the peripheral server on "([^"]*)" using the hub-signed certificates$/) do |peripheral|
  node = get_target(peripheral)
  fqdn = node.full_hostname
  paths = hub_signed_cert_paths(fqdn)
  _out, code = node.run('podman ps | grep -q uyuni-server', runs_in_container: false, check_errors: false)
  if code.zero?
    verify_hub_signed_cert_is_served!(peripheral, node, fqdn, paths)
  else
    node.run(
      'mgradm install podman --logLevel=debug --config /root/mgradm.yaml ' \
      "--ssl-ca-root #{paths[:ca]} " \
      "--ssl-server-cert #{paths[:cert]} " \
      "--ssl-server-key #{paths[:key]} " \
      "--ssl-db-ca-root #{paths[:ca]} " \
      "--ssl-db-cert #{paths[:cert]} " \
      "--ssl-db-key #{paths[:key]} " \
      "#{fqdn}",
      runs_in_container: false,
      timeout: 1800,
      verbose: true
    )
  end
end

When(/^I wait until the server on "([^"]*)" is ready$/) do |host|
  node = get_target(host)
  repeat_until_timeout(timeout: 600, message: "Web UI on #{host} did not come up") do
    out, code = node.run(
      "curl -ks -o /dev/null -w '%{http_code}' https://localhost/docs/en/release-notes/release-notes-server.html",
      runs_in_container: false,
      check_errors: false
    )
    break if code.zero? && out.strip == '200'

    sleep 5
  end
end

### Peripheral installation with its own default (mgradm-generated) self-signed
### certificate - used for the cross-CA registration tests (A-04), which need
### the peripheral's CA to differ from the hub's CA to prove root-CA
### pasting/upload during token-based registration actually works. Unlike the
### hub-signed flow above, no --ssl-* flags are passed, so mgradm generates its
### own certificate as it would for a standalone install.

When(/^I install the peripheral server on "([^"]*)" with its own self-signed certificate$/) do |peripheral|
  node = get_target(peripheral)
  fqdn = node.full_hostname
  _out, code = node.run('podman ps | grep -q uyuni-server', runs_in_container: false, check_errors: false)
  if code.zero?
    log "Server container already running on #{peripheral}, skipping installation"
  else
    node.run(
      "mgradm install podman --logLevel=debug --config /root/mgradm.yaml #{fqdn}",
      runs_in_container: false,
      timeout: 1800,
      verbose: true
    )
  end
end

### Post-install configuration of a peripheral installed by the testsuite itself.
###
### With sumaform's `skip_server_install: true`, the peripheral host is prepared
### (podman, mgradm binary, /root/mgradm.yaml) but none of sumaform's post-install
### configuration (rhn.sls / testsuite.sls) is applied, since it never ran `mgradm
### install`. Once the testsuite installs the server (see above), it must also
### apply the subset of that configuration the hub secondary features rely on.

RHN_CONF_PATH = '/etc/rhn/rhn.conf'.freeze

# Subset of rhn.sls / testsuite.sls settings the hub secondary features rely on.
RHN_CONF_TESTSUITE_SETTINGS = {
  'java.max_changelog_entries' => '3',
  'java.salt_presence_ping_timeout' => '6',
  'server.susemanager.forward_registration' => '0',
  'server.satellite.reposync_download_threads' => '2',
  'java.salt_content_staging_window' => '0.033',
  'java.salt_content_staging_advance' => '0.05',
  'java.kiwi_os_image_building_enabled' => 'true'
}.freeze

When(/^I apply the testsuite configuration on the peripheral server "([^"]*)"$/) do |peripheral|
  node = get_target(peripheral)

  # rhn.conf: idempotent key/value upsert, one sed/append per key rather than a blind append,
  # so re-running this step (or running it against an already-configured server) is a no-op.
  changed = false
  RHN_CONF_TESTSUITE_SETTINGS.each do |key, value|
    # get_variable_from_conf_file returns nil (not "") when the key is absent - String#strip!
    # returns nil when there is nothing to strip, which is the case for empty sed output.
    current = get_variable_from_conf_file(peripheral, RHN_CONF_PATH, key)
    next if current == value

    if current.nil?
      node.run("echo '#{key} = #{value}' >> #{RHN_CONF_PATH}")
    else
      node.run("sed -i 's/^#{key} = .*/#{key} = #{value}/' #{RHN_CONF_PATH}")
    end
    changed = true
  end
  node.run('systemctl restart tomcat taskomatic') if changed

  # mgr-sync autologin, mirroring the ~/.mgr-sync convention already used against "server"
  # elsewhere in the testsuite (see command_steps.rb).
  node.run("echo -e 'mgrsync.user = admin\nmgrsync.password = admin\n' > /root/.mgr-sync")

  # First user: `mgradm install` creates the admin/admin user from /root/mgradm.yaml - the
  # existing "I am authorized for the "Admin" section on ..." step already logs into server2
  # with admin/admin (see above), confirming this holds. No satpasswd step needed.

  # salt-events service (testsuite.sls): no hub feature reads Salt event history on the
  # peripheral (grepped step_definitions/ and features/secondary/srv_hub_*.feature for
  # get_event_history/event_history/salt-events - no hits), so it is intentionally not
  # installed here.

  # GPG key import, cobbler permissive mode, minima test repos, salt-bundle pillar: the hub
  # channel sync features clone and sync channels FROM the hub's own repositories to the
  # peripheral over ISSv3 (see srv_hub_channel_synchronization.feature) - the peripheral never
  # syncs its own repos - so none of these sumaform testsuite.sls steps are needed here.
end
