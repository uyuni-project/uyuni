# COPYRIGHT 2015 SUSE LLC
require 'json'

virtualhostmanager = XMLRPCVHMTest.new(ENV['TESTHOST'])
modules = []
vhms = []
params = {}
detail = {}

Given /^I am logged in via XML\-RPC\/virtualhostmanager as user "([^"]*)" and password "([^"]*)"$/ do |luser, password|
  virtualhostmanager.login(luser, password)
end

When /^I call virtualhostmanager.listAvailableVirtualHostGathererModules\(\)$/ do
  modules = virtualhostmanager.listAvailableVirtualHostGathererModules()
end

When /^I call virtualhostmanager.listVirtualHostManagers\(\)$/ do
  vhms = virtualhostmanager.listVirtualHostManagers()
end

When /^I call virtualhostmanager.getModuleParameters\(\) for "([^"]*)"$/ do |moduleName|
  params = virtualhostmanager.getModuleParameters(moduleName)
end

When /^I call virtualhostmanager.create\("([^"]*)", "([^"]*)"\) and params from "([^"]*)"$/ do |label, moduleName, paramFile|
  p = JSON.load(File.new(paramFile))
  r = virtualhostmanager.create(label, moduleName, p)
  fail if r != 1
end

When /^I call virtualhostmanager.delete\("([^"]*)"\)$/ do |label|
  r = virtualhostmanager.delete(label)
  fail if r != 1
end

When /^I call virtualhostmanager.getDetail\("([^"]*)"\)$/ do |label|
  detail = virtualhostmanager.getDetail(label)
end

Then /^I should get two modules$/ do
  assert(modules.length == 2, "Expect: 'number of modules' == 2, but found '#{modules.length}' modules" )
end

Then /^I should get ([0-9]+) returned$/ do |num|
  assert(vhms.length == num.to_i, "Expect: 'number of VHMs' == '#{num}', but found '#{vhms.length}' VHMs" )
end

Then /^I should get "([^"]*)"$/ do |key|
  assert(params.has_key?(key), "Expect parameter key '#{key}', but got only '#{params}'")
end

Then /^"([^"]*)" should be "([^"]*)"$/ do |key, value|
  assert(detail.has_key?(key), "Expect parameter key '#{key}', but got only '#{detail}'")
  assert(detail[key].to_s == value, "Expect value for #{key} should be '#{value}, but got '#{detail[key]}'")
end

Then /^configs "([^"]*)" should be "([^"]*)"$/ do |key, value|
  assert(detail['configs'].has_key?(key), "Expect parameter key '#{key}', but got only '#{detail['configs']}'")
  assert(detail['configs'][key].to_s == value, "Expect value for #{key} should be '#{value}, but got '#{detail['configs'][key]}'")
end

Then /^I logout from XML\-RPC\/virtualhostmanager$/ do
  virtualhostmanager.logout
end


