# COPYRIGHT 2017 SUSE LLC

And(/^I remove pkg "([^"]*)" on this "(.*?)"$/) do |pkg, host|
  node = get_target(host)
  node.run("zypper -n rm #{pkg}")
end

Given(/^I enable repository "(.*?)" on this "(.*?)"$/) do |repo, host|
  node = get_target(host)
  node.run("zypper mr --enable #{repo}")
end

Then(/^I disable repository "(.*?)" on this "(.*?)"$/) do |repo, host|
  node = get_target(host)
  node.run("zypper mr --disable #{repo}")
end

Then(/^I install pkg "(.*?)" on this "(.*?)"$/) do |pkg, host|
  node = get_target(host)
  node.run("zypper in -y #{pkg}")
end

And(/^I wait until the package "(.*?)" has been cached on this "(.*?)"$/) do |pkg_name, host|
  node = get_target(host)
  Timeout.timeout(DEFAULT_TIMEOUT) do
    loop do
      _out, code = node.run("ls /var/cache/zypp/packages/susemanager:test-channel-x86_64/getPackage/#{pkg_name}.rpm", false)
      break if code.zero?
      sleep 1
    end
  end
end
