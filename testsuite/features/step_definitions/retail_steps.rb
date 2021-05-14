# Copyright 2021 SUSE LLC
# Licensed under the terms of the MIT license.

# rubocop:disable Style/GlobalVars
When(/^I prepare configuration for "([^"]*)" terminal deployment$/) do |environment|
  environment = environment.downcase.gsub(' ', '')
  path = "ext-tools/semi-xmlrpc-tester/assets/#{environment}"
  buildhosts = {
    sle15sp3: $sle15sp3_buildhost,
    sle12sp5: $sle12sp5_buildhost,
    sle11sp4: $sle11sp4_buildhost,
    sle11sp3: $sle11sp4_buildhost
  }
  config_items = {
    SERVER_URL: $server.full_hostname,
    BUILDHOST: buildhosts[environment].full_hostname,
    RBS: $proxy.full_hostname
  }
  config_items.each do |key, value|
    raise StandardError, "Cannot set #{key}!" unless system("sed -i s'/#{key}/#{value}/'g #{path}/config.json")
  end
end

When(/^I execute "([^"]*)" for "([^"]*)" via semi-xmlrpc-tester$/) do |scenario, environment|
  environment = environment.downcase.gsub(' ', '')
  path = 'ext-tools/semi-xmlrpc-tester'
  system("cd #{path}; ./semi-xmlrpc-tester #{environment} #{scenario} -y --silent") or raise StandardError, "Scenario '#{scenario}' provided by semi-xmlrpc-tester failed!"
end

When(/^I prepare kiwi profile for SLE11 SP4 buildhost$/) do
  # Git clone is not possible with SLE11 system, so we need to provide kiwi profiles via different way
  system('git clone https://github.com/SUSE/manager-build-profiles /root/manager-build-profiles') or raise StandardError, 'Cannot clone kiwi profiles!'
  system("scp -o StrictHostKeyChecking=no -r /root/manager-build-profiles/ root@#{$sle11sp4_buildhost.full_hostname}:/manager-build-profiles") or raise StandardError, 'Cannot push kiwi profiles to SLE11 SP4 buildhost!'
  system('rm -rf /root/manager-build-profiles')
end
# rubocop:enable Style/GlobalVars
